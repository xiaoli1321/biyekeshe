---
name: using-git-worktrees
description: 在开始需要与当前工作空间隔离的功能工作时，或在执行实施计划之前使用——创建具有智能目录选择和安全性验证的隔离 git 工作树 (Use when starting feature work that needs isolation from current workspace or before executing implementation plans - creates isolated git worktrees with smart directory selection and safety verification)
---

# 使用 Git 工作树 (Using Git Worktrees)

> [!NOTE]
> **中文摘要**：Git 工作树允许在同一个仓库中创建多个隔离的工作空间，从而实现并行开发不同分支。核心流程包括：1. 系统化目录选择（优先使用项目内的 `.worktrees/`）；2. 安全性验证（确保目录已被 `.gitignore` 忽略）；3. 自动环境搭建与基准测试验证。

## Overview

Git worktrees create isolated workspaces sharing the same repository, allowing work on multiple branches simultaneously without switching.

**Core principle:** Systematic directory selection + safety verification = reliable isolation.

**Announce at start:** "I'm using the using-git-worktrees skill to set up an isolated workspace."

## Directory Selection Process

Follow this priority order:

### 1. Check Existing Directories

```bash
# Check in priority order
ls -d .worktrees 2>/dev/null     # Preferred (hidden)
ls -d worktrees 2>/dev/null      # Alternative
```

**If found:** Use that directory. If both exist, `.worktrees` wins.

### 2. Check Project Docs (CLAUDE.md / ARCH_OVERVIEW.md)

**If preference specified:** Use it without asking.

### 3. Ask User

If no directory exists and no preference found:

```
No worktree directory found. Where should I create worktrees?

1. .worktrees/ (project-local, hidden)
2. ../worktrees/<project-name>/ (sibling to project)

Which would you prefer?
```

## Safety Verification

### For Project-Local Directories (.worktrees or worktrees)

**MUST verify directory is ignored before creating worktree:**

```bash
git check-ignore -q .worktrees 2>/dev/null || git check-ignore -q worktrees 2>/dev/null
```

**If NOT ignored:**

1. Add appropriate line to `.gitignore`.
2. Commit the change.
3. Proceed with worktree creation.

**Why critical:** Prevents accidentally committing worktree contents to repository.

## Creation Steps

### 1. Create Worktree

```bash
# Example
git worktree add <path>/<branch-name> -b <branch-name>
cd <path>/<branch-name>
```

### 2. Run Project Setup

Auto-detect and run appropriate setup (e.g., `npm install`, `mvn clean install`).

### 3. Verify Clean Baseline

Run tests to ensure worktree starts clean. If tests fail, report and ask whether to proceed.

### 4. Report Location

```
Worktree ready at <full-path>
Tests passing (<N> tests, 0 failures)
Ready to implement <feature-name>
```

## Common Mistakes

### Skipping ignore verification
- **Problem:** Worktree contents get tracked, pollute git status.
- **Fix:** Always check `.gitignore`.

### Proceeding with failing tests
- **Problem:** Can't distinguish new bugs from pre-existing issues.
- **Fix:** Verify clean test baseline before starting work.

## Integration

**Pairs with:**
- **finishing-a-development-branch** - REQUIRED for cleanup after work complete.
