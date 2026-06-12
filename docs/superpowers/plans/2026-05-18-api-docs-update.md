# API Documentation Update Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Automate the update of API documentation markdown files using generated Spring Rest Docs snippets.

**Architecture:** A standalone Node.js script that reads AsciiDoc snippets, converts them to Markdown, and integrates them into existing or new documentation files.

**Tech Stack:** Node.js (File System, Path modules).

---

### Task 1: Create Automation Script

**Files:**
- Create: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\scripts\update_api_docs.js`

- [ ] **Step 1: Write the script content**

```javascript
const fs = require('fs');
const path = require('path');

const SNIPPETS_DIR = 'C:\\Users\\SSAFY\\IdeaProjects\\ssafy-enjoytrip\\backend\\app\\web\\build\\generated-snippets';
const DOCS_DIR = 'C:\\Users\\SSAFY\\IdeaProjects\\ssafy-enjoytrip\\docs\\api';

const FOLDERS = [
  'attractions', 'boards', 'chargers', 'health', 'hotplaces',
  'members', 'news', 'notices', 'plans', 'route-optimize', 'route-split-by-day'
];

function convertAdocToMd(content) {
  // Regex to match AsciiDoc source blocks
  // Pattern: [source,lang,options="nowrap"]\n----\ncontent\n----
  // Robust pattern to handle variations
  const regex = /\[source,([^,\]\s\n]+)(?:,[^\]\n]*)?\]\s*\n----\s*\n([\s\S]*?)\n----/g;
  return content.replace(regex, (match, lang, code) => {
    return '```' + lang + '\n' + code.trim() + '\n```';
  });
}

function processFolder(folder) {
  const snippetPath = path.join(SNIPPETS_DIR, folder);
  const mdPath = path.join(DOCS_DIR, `${folder}.md`);

  if (!fs.existsSync(snippetPath)) {
    console.warn(`Snippet folder not found: ${snippetPath}`);
    return;
  }

  const httpRequestFile = path.join(snippetPath, 'http-request.adoc');
  const httpResponseFile = path.join(snippetPath, 'http-response.adoc');
  const curlRequestFile = path.join(snippetPath, 'curl-request.adoc');

  const httpRequest = fs.existsSync(httpRequestFile) 
    ? convertAdocToMd(fs.readFileSync(httpRequestFile, 'utf8')) 
    : '_Snippet not generated yet._';
    
  const httpResponse = fs.existsSync(httpResponseFile) 
    ? convertAdocToMd(fs.readFileSync(httpResponseFile, 'utf8')) 
    : '_Snippet not generated yet._';
    
  const curlRequest = fs.existsSync(curlRequestFile) 
    ? convertAdocToMd(fs.readFileSync(curlRequestFile, 'utf8')) 
    : '';

  let mdContent = '';
  if (fs.existsSync(mdPath)) {
    mdContent = fs.readFileSync(mdPath, 'utf8');
    // Replace placeholders
    mdContent = mdContent.replace('### HTTP Request\n\n_Snippet not generated yet._', `### HTTP Request\n\n${httpRequest}`);
    mdContent = mdContent.replace('### HTTP Response\n\n_Snippet not generated yet._', `### HTTP Response\n\n${httpResponse}`);
    
    // Check if Curl section already exists, if not, append it
    if (!mdContent.includes('### Curl Request')) {
      mdContent += `\n### Curl Request\n\n${curlRequest}\n`;
    } else {
       // Replace existing curl content if any (optional, but good for idempotency)
       const curlMarker = '### Curl Request\n\n';
       const index = mdContent.indexOf(curlMarker);
       if (index !== -1) {
         mdContent = mdContent.substring(0, index + curlMarker.length) + curlRequest + '\n';
       }
    }
  } else {
    // Create new file
    const title = folder.charAt(0).toUpperCase() + folder.slice(1).replace(/-/g, ' ');
    mdContent = `# ${title} API\n\n### HTTP Request\n\n${httpRequest}\n\n### HTTP Response\n\n${httpResponse}\n\n### Curl Request\n\n${curlRequest}\n`;
  }

  fs.writeFileSync(mdPath, mdContent, 'utf8');
  console.log(`Updated: ${mdPath}`);
}

FOLDERS.forEach(processFolder);
```

- [ ] **Step 2: Save the script**
Save the code block above to `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\scripts\update_api_docs.js`.

### Task 2: Execute Automation Script

- [ ] **Step 1: Run the script using Node.js**

Run: `node C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\scripts\update_api_docs.js`
Expected: Output showing updated files for each folder.

### Task 3: Verify Results

- [ ] **Step 1: Read one updated file (e.g., members.md) to verify conversion**

Run: `cat C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\docs\api\members.md`
Expected: Markdown code blocks instead of AsciiDoc source blocks, and actual content instead of placeholders.

- [ ] **Step 2: Check for any newly created files**

Run: `ls C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\docs\api`
Expected: All folders in the scope should have a corresponding `.md` file.

- [ ] **Step 3: Cleanup script**

Run: `rm C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\scripts\update_api_docs.js`
Expected: Script file removed.
