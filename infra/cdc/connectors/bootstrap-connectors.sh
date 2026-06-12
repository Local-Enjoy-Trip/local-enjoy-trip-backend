#!/usr/bin/env bash
set -euo pipefail

: "${CONNECT_URL:=http://kafka-connect:8083}"
: "${CONNECT_BOOTSTRAP_TIMEOUT_SECONDS:=300}"

CONNECTOR_DIR="${CONNECTOR_DIR:-/cdc/connectors}"
CONNECTORS=(
  "${CONNECTOR_DIR}/debezium-attraction-favorites-source.properties"
  "${CONNECTOR_DIR}/debezium-notification-outbox-source.properties"
  "${CONNECTOR_DIR}/clickhouse-attraction-favorites-sink.properties"
)

deadline=$((SECONDS + CONNECT_BOOTSTRAP_TIMEOUT_SECONDS))

echo "[kafka-connect-bootstrap] waiting for Kafka Connect at ${CONNECT_URL}"
while true; do
  if python3 - "$CONNECT_URL" <<'PYCONNECT' >/dev/null 2>&1
import sys, urllib.request
urllib.request.urlopen(sys.argv[1] + '/connectors', timeout=5).read()
PYCONNECT
  then
    break
  fi

  if (( SECONDS >= deadline )); then
    echo "[kafka-connect-bootstrap] Kafka Connect did not become healthy before timeout" >&2
    exit 1
  fi
  sleep 3
done

for props in "${CONNECTORS[@]}"; do
  echo "[kafka-connect-bootstrap] reconciling ${props}"
  python3 - "$CONNECT_URL" "$props" <<'PYCONNECTOR'
import json
import os
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

connect_url = sys.argv[1].rstrip('/')
props_path = Path(sys.argv[2])
ENV_PATTERN = re.compile(r"\$\{([A-Za-z_][A-Za-z0-9_]*)(?::-([^}]*))?}")


def substitute_env(value):
    def replace(match):
        name, default = match.group(1), match.group(2)
        if name in os.environ:
            return os.environ[name]
        if default is not None:
            return default
        raise SystemExit(f"Missing environment variable {name} required by {props_path}")

    return ENV_PATTERN.sub(replace, value)

config = {}
for raw_line in props_path.read_text().splitlines():
    line = raw_line.strip()
    if not line or line.startswith('#'):
        continue
    if '=' not in line:
        raise SystemExit(f"Invalid properties line in {props_path}: {raw_line}")
    key, value = line.split('=', 1)
    config[key.strip()] = substitute_env(value.strip())

name = config.pop('name', None)
if not name:
    raise SystemExit(f"Connector properties must include name=: {props_path}")

body = json.dumps(config).encode('utf-8')
request = urllib.request.Request(
    f"{connect_url}/connectors/{name}/config",
    data=body,
    method='PUT',
    headers={'Content-Type': 'application/json'},
)
try:
    with urllib.request.urlopen(request, timeout=30) as response:
        print(response.read().decode('utf-8'))
except urllib.error.HTTPError as exc:
    print(exc.read().decode('utf-8'), file=sys.stderr)
    raise

import time

status_url = f"{connect_url}/connectors/{name}/status"
last_error = None
for _ in range(30):
    status_request = urllib.request.Request(status_url)
    try:
        with urllib.request.urlopen(status_request, timeout=30) as response:
            status = json.loads(response.read().decode('utf-8'))
            print(json.dumps(status, indent=2, sort_keys=True))
            connector_state = status.get('connector', {}).get('state')
            task_states = [task.get('state') for task in status.get('tasks', [])]
            if connector_state == 'FAILED' or any(state == 'FAILED' for state in task_states):
                raise SystemExit(f"Connector {name} failed: {json.dumps(status, sort_keys=True)}")
            if connector_state == 'RUNNING' and task_states and all(state == 'RUNNING' for state in task_states):
                break
    except urllib.error.HTTPError as exc:
        last_error = exc.read().decode('utf-8')
        if exc.code != 404:
            print(last_error, file=sys.stderr)
            raise
    time.sleep(2)
else:
    raise SystemExit(f"Connector {name} did not reach RUNNING after registration. Last error: {last_error}")
PYCONNECTOR
done

echo "[kafka-connect-bootstrap] connector reconciliation completed"
