# API Documentation Update Design

## Goal
Update the API documentation in `docs/api` using generated snippets from `backend/web/build/generated-snippets`.

## Strategy: Scripted Automation
Use a Node.js script to automate the transformation and replacement of snippets across 11+ documentation files.

## Technical Details
### 1. Transformation Logic
- Read `.adoc` files: `http-request.adoc`, `http-response.adoc`, `curl-request.adoc`.
- Convert AsciiDoc source blocks to Markdown code blocks using regex.
  - Pattern: `\[source,([^,\]]+)(?:,.*?)?\]\n----\n([\s\S]*?)\n----`
  - Replacement: ` ```$1\n$2\n``` `
- Ensure consistent line endings and indentation.

### 2. File Integration
- Target files: `docs/api/<folder>.md`.
- For each snippet folder:
  - Check if target `.md` file exists.
  - If it exists:
    - Replace the first `_Snippet not generated yet._` with converted `http-request.adoc`.
    - Replace the second `_Snippet not generated yet._` with converted `http-response.adoc`.
    - Append `### Curl Request` and converted `curl-request.adoc` to the end.
  - If it does NOT exist:
    - Create the file with the title `# <FolderName> API`.
    - Add sections for HTTP Request, HTTP Response, and Curl Request.

### 3. Verification
- Log each file updated/created.
- Verify that the resulting Markdown is valid and formatted correctly.

## Scope
Snippet folders:
- attractions
- boards
- chargers
- health
- hotplaces
- members
- news
- notices
- plans
- route-optimize
- route-split-by-day
