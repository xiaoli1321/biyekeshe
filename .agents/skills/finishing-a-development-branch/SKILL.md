---
name: finishing-a-development-branch
description: 在实现完成、所有测试通过并且需要决定如何整合工作时使用——通过提供结构化的合并、PR 或清理选项来指导开发工作的完成 (Use when implementation is complete, all tests pass, and you need to decide how to integrate the work - guides completion of development work by presenting structured options for merge, PR, or cleanup)
---

# 完成开发分支 (Finishing a Development Branch)

> [!NOTE]
> **中文摘要**：在开发完成后，指导如何整合成果。流程包括：1. 强制运行全量测试；2. 确定基准分支（通常是 `main`）；3. 提供四项结构化选项：本地合并、提交并创建 PR、保持现状、放弃工作。严禁在测试失败的情况下合并。

## Overview

Guide completion of development work by presenting clear options and handling chosen workflow.

**Core principle:** Verify tests → Present options → Execute choice → Clean up.

**Announce at start:** "I'm using the finishing-a-development-branch skill to complete this work."

## The Process

### Step 1: Verify Tests

**Before presenting options, verify tests pass:**

```bash
# Example
mvn test / npm test
```

**If tests fail:** STOP. Do not proceed to Step 2 until tests pass.

### Step 2: Determine Base Branch

Identify the branch this feature split from (usually `main` or `master`).

### Step 3: Present Options

Present exactly these 4 options:

```
Implementation complete. What would you like to do?

1. Merge back to <base-branch> locally
2. Push and create a Pull Request
3. Keep the branch as-is (I'll handle it later)
4. Discard this work

Which option?
```

### Step 4: Execute Choice

#### Option 1: Merge Locally
- Checkout base branch.
- Pull latest.
- Merge feature branch.
- Verify tests.
- Delete feature branch.

#### Option 2: Push and Create PR
- Push branch to remote.
- Create PR (using `gh pr create` or providing a link).

#### Option 3: Keep As-Is
- Preserve worktree and branch.

#### Option 4: Discard
- **REQUIRES typed "discard" confirmation**.
- Delete branch and worktree.

### Step 5: Cleanup Worktree

For Options 1, 2, and 4, remove the worktree if it was used.

## Red Flags

**Never:**
- Proceed with failing tests.
- Merge without verifying tests on the merged result.
- Delete work without explicit "discard" confirmation.
- Force-push without explicit request.

## Integration

**Called by:**
- **executing-plans** - After all tasks are completed.
- **using-git-worktrees** - Cleans up the workspace.
