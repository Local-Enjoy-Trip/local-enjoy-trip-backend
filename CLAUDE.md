# CLAUDE.md

이 파일은 Claude Code가 이 프로젝트에서 작업할 때 반드시 따라야 하는 지침이다.

## 1. 규칙 문서 계층

작업 시작 전 반드시 다음 순서로 읽는다.

1. **`CONSTITUTION.md`** — 절대 규칙. 항상 우선한다. 예외 없이 따른다.
2. **`RULES.md`** — 운영 규칙과 현장 템플릿. `CONSTITUTION.md`를 구체화하며 완화할 수 없다.
3. **`PRECEDENTS.md`** — 사례 축적소. 동일 유형 사례가 3회 반복되면 `RULES.md`로 승격한다.

## 2. 모듈 접근 시 AGENTS.md 읽기

특정 모듈 코드에 접근하거나 수정할 때는, **해당 모듈의 `AGENTS.md`를 먼저 읽고** 그 지침을 따른다.

모듈별 AGENTS.md 위치:
- 루트: `AGENTS.md`
- `core/AGENTS.md`
- `core/core-api/AGENTS.md`
- `storage/AGENTS.md`
- `storage/db-core/AGENTS.md`
- `external/AGENTS.md`
- `batch/AGENTS.md`

## 3. 코드 검증 기준

코드를 작성하거나 리뷰할 때는 **`docs/code-review.md`** 를 참고해 검증한다.

## 4. PRECEDENTS.md 관리

- 반복적으로 발생하는 패턴, 판단, 예외 사례는 `PRECEDENTS.md`에 기록한다.
- **동일 유형 사례가 3회 이상 등장하면 `RULES.md`로 승격**하고 `PRECEDENTS.md`의 해당 항목에 승격 사실을 표시한다.
