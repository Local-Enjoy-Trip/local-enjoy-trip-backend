# Add @Service Annotations to Service Classes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `@Service` annotation and its import to all specified service classes in the `com.ssafy.enjoytrip.service` package.

**Architecture:** Standard Spring Boot service layer annotation.

**Tech Stack:** Java, Spring Boot, Lombok.

---

### Task 1: Update AttractionService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\AttractionService.java`

- [ ] **Step 1: Add import and annotation**

```java
package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.repository.AttractionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttractionService {
```

### Task 2: Update BoardService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\BoardService.java`

- [ ] **Step 1: Add import and annotation**

```java
package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
```

### Task 3: Update EvChargerService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\EvChargerService.java`

- [ ] **Step 1: Add import and annotation**

### Task 4: Update HotplaceService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\HotplaceService.java`

- [ ] **Step 1: Add import and annotation**

### Task 5: Update MemberService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\MemberService.java`

- [ ] **Step 1: Add import and annotation**

### Task 6: Update NewsService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\NewsService.java`

- [ ] **Step 1: Add import and annotation**

### Task 7: Update NoticeService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\NoticeService.java`

- [ ] **Step 1: Add import and annotation**

### Task 8: Update PlanService.java

**Files:**
- Modify: `C:\Users\SSAFY\IdeaProjects\ssafy-enjoytrip\backend\core\src\main\java\com\ssafy\enjoytrip\service\PlanService.java`

- [ ] **Step 1: Add import and annotation**

---
**Verification:**
- Run `./gradlew build` in `core` to ensure no compilation errors.
