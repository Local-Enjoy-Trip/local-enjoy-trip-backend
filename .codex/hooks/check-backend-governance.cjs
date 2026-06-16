#!/usr/bin/env node

const childProcess = require("child_process");
const fs = require("fs");
const path = require("path");

const GOVERNANCE_DOCS = [
  "backend/CONSTITUTION.md",
  "backend/RULES.md",
];

function readStdin() {
  try {
    return fs.readFileSync(0, "utf8");
  } catch {
    return "";
  }
}

function parsePayload(text) {
  if (!text.trim()) {
    return {};
  }

  try {
    return JSON.parse(text);
  } catch {
    return {};
  }
}

function findGitRoot(startDir) {
  let current = path.resolve(startDir);

  while (true) {
    if (fs.existsSync(path.join(current, ".git"))) {
      return current;
    }

    const parent = path.dirname(current);
    if (parent === current) {
      return "";
    }
    current = parent;
  }
}

function repoRoot(payload) {
  if (typeof payload.cwd === "string" && payload.cwd.trim()) {
    return findGitRoot(payload.cwd) || path.resolve(payload.cwd);
  }

  return findGitRoot(process.cwd()) || path.resolve(__dirname, "..", "..");
}

function git(root, args) {
  try {
    return childProcess.execFileSync("git", args, {
      cwd: root,
      encoding: "utf8",
      stdio: ["ignore", "pipe", "ignore"],
    });
  } catch {
    return "";
  }
}

function changedPaths(root) {
  const status = git(root, ["status", "--porcelain"]);

  return status
    .split(/\r?\n/)
    .map((line) => line.trimEnd())
    .filter(Boolean)
    .map((line) => line.slice(3).split(" -> ").pop())
    .filter(Boolean);
}

function fileExists(root, relativePath) {
  return fs.existsSync(path.join(root, relativePath));
}

function readFile(root, relativePath) {
  try {
    return fs.readFileSync(path.join(root, relativePath), "utf8");
  } catch {
    return "";
  }
}

function listFiles(root, relativeDir) {
  try {
    return childProcess.execFileSync("find", [relativeDir, "-type", "f"], {
      cwd: root,
      encoding: "utf8",
      stdio: ["ignore", "pipe", "ignore"],
    })
      .split(/\r?\n/)
      .filter(Boolean);
  } catch {
    return [];
  }
}

function failIf(condition, failures, message) {
  if (condition) {
    failures.push(message);
  }
}

function validateGovernanceDocs(root, failures) {
  for (const doc of GOVERNANCE_DOCS) {
    failIf(!fileExists(root, doc), failures, `${doc} must exist for backend governance checks.`);
  }
}

function modifiedJavaFiles(root, paths) {
  return paths
    .filter((file) => file.startsWith("backend/"))
    .filter((file) => file.endsWith(".java"))
    .filter((file) => fileExists(root, file));
}

function hasForbiddenFullyQualifiedName(content) {
  return content
    .split(/\r?\n/)
    .filter((line) => !line.trimStart().startsWith("import "))
    .some((line) => /\bjava\.(util|time)\.[A-Z][A-Za-z0-9_]*/.test(line));
}

function validateChangedFiles(root, paths, failures, warnings) {
  const backendPaths = paths.filter((file) => file.startsWith("backend/"));

  if (backendPaths.length === 0) {
    return;
  }

  const forbiddenAppSources = backendPaths.filter((file) => (
    file.startsWith("backend/app/src/main/")
      || file.startsWith("backend/app/src/test/")
  ));
  failIf(
    forbiddenAppSources.length > 0,
    failures,
    `backend/app must stay source-free; changed forbidden path(s): ${forbiddenAppSources.join(", ")}`,
  );

  const lingeringAppSources = [
    ...listFiles(root, "backend/app/src/main"),
    ...listFiles(root, "backend/app/src/test"),
  ];
  if (lingeringAppSources.length > 0) {
    warnings.push(`Existing backend/app source-free violation is present: ${lingeringAppSources.join(", ")}`);
  }

  for (const file of modifiedJavaFiles(root, backendPaths)) {
    const content = readFile(root, file);

    if (file.startsWith("backend/app/web/src/main/") || file.startsWith("backend/app/worker/src/main/")) {
      failIf(
        /\bcom\.ssafy\.enjoytrip\.storage\./.test(content),
        failures,
        `${file} references storage implementation packages from an executable module.`,
      );
    }

    if (file.startsWith("backend/app/web/src/main/")) {
      failIf(
        /@KafkaListener\b|@Scheduled\b/.test(content),
        failures,
        `${file} appears to contain worker-only Kafka/Scheduled logic inside app:web.`,
      );
    }

    if (file.startsWith("backend/app/worker/src/main/")) {
      failIf(
        /@(?:Rest)?Controller\b|RestController\b|ApiResponse<|org\.springframework\.web\.bind\.annotation\./.test(content),
        failures,
        `${file} appears to contain web/controller contract code inside app:worker.`,
      );
    }

    if (file.includes("/controller/") || /Controller\.java$/.test(file)) {
      failIf(
        /@RequestParam\s+Map\b|@RequestBody\s+Map\b|ApiResponse\s*<\s*Map\b|Map\.of\s*\(/.test(content),
        failures,
        `${file} may expose a Map-based public controller contract.`,
      );
    }

    failIf(/\bJdbcTemplate\b/.test(content), failures, `${file} uses JdbcTemplate; use JPA or jOOQ.`);
    if (/\bnativeQuery\s*=\s*true\b/.test(content)) {
      warnings.push(`${file} declares nativeQuery=true; confirm this is not a native mutation and prefer jOOQ for complex SQL.`);
    }
    failIf(/\bSystem\.(getenv|getProperty)\s*\(/.test(content), failures, `${file} reads env/system properties directly.`);
    failIf(hasForbiddenFullyQualifiedName(content), failures, `${file} contains java.util/java.time fully qualified names outside imports.`);
  }

  const moduleHints = [
    ["backend/app/web/", "backend/app/web/AGENTS.md"],
    ["backend/app/worker/", "backend/app/worker/AGENTS.md"],
    ["backend/app/", "backend/app/AGENTS.md"],
    ["backend/core/", "backend/core/AGENTS.md"],
    ["backend/storage/", "backend/storage/AGENTS.md"],
    ["backend/external/", "backend/external/AGENTS.md"],
    ["backend/batch/", "backend/batch/AGENTS.md"],
  ]
    .filter(([prefix]) => backendPaths.some((file) => file.startsWith(prefix)))
    .map(([, agent]) => agent);

  if (moduleHints.length > 0) {
    warnings.push(`Confirm the nearest module guidance was applied: ${[...new Set(moduleHints)].join(", ")}`);
  }

  warnings.push("Before final response, explicitly check backend/CONSTITUTION.md and backend/RULES.md against the changed files.");
  warnings.push("If public API behavior changed, verify actual JSON over HTTP or state why runtime API validation was not applicable.");
}

function renderReport(paths, failures, warnings) {
  const lines = [
    "[backend-governance Stop hook]",
    `Changed paths inspected: ${paths.length}`,
  ];

  if (failures.length === 0) {
    lines.push("Mechanical checks: PASS");
  } else {
    lines.push("Mechanical checks: FAIL");
    for (const failure of failures) {
      lines.push(`- ${failure}`);
    }
  }

  if (warnings.length > 0) {
    lines.push("Manual checks before final response:");
    for (const warning of warnings) {
      lines.push(`- ${warning}`);
    }
  }

  return `${lines.join("\n")}\n`;
}

function main() {
  const payload = parsePayload(readStdin());
  const eventName = payload.hook_event_name || payload.hookEventName || payload.event;

  if (eventName && eventName !== "Stop") {
    return;
  }

  const root = repoRoot(payload);
  const paths = changedPaths(root);
  const failures = [];
  const warnings = [];

  validateGovernanceDocs(root, failures);
  validateChangedFiles(root, paths, failures, warnings);

  process.stdout.write(renderReport(paths, failures, warnings));

  if (failures.length > 0) {
    process.exitCode = 1;
  }
}

main();
