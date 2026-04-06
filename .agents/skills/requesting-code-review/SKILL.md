---
name: requesting-code-review
description: 在完成任务、实现重大功能或合并之前使用，以验证工作是否符合要求。 (Use when completing tasks, implementing major features, or before merging to verify work meets requirements)
---

# 请求代码审查 (Requesting Code Review)

> [!NOTE]
> **中文摘要**：核心原则是“提早审查，经常审查”。在完成执行计划中的每个任务或批处理后、完成重大功能后以及合并到主分支前，都必须进行代码审查。

## Overview

Catch issues before they cascade by requesting review from a peer or a specialized reviewer subagent. This keeps the work product focused and ensures it meets the design goals.

**Core principle:** Review early, review often.

## When to Request Review

**Mandatory:**
- After each task or batch in `executing-plans`
- After completing a major feature
- Before merging to `main` or the primary branch

**Optional but valuable:**
- When stuck (fresh perspective)
- Before refactoring (baseline check)
- After fixing a complex bug

## How to Request

**1. Prepare the context:**
Identify what was implemented and what the original requirements were.

**2. Request review:**
Provide the following information:
- **What was implemented**: A summary of your changes.
- **Requirements**: The original plan or spec.
- **Git diff**: The range of commits or a description of changes.

**3. Act on feedback:**
- **Critical issues**: Fix immediately.
- **Important issues**: Fix before proceeding.
- **Minor issues**: Note for later or fix if quick.
- **Disagreement**: Push back with technical reasoning if the reviewer is incorrect.

## Integration with Workflows

**Executing Plans:**
- Review after each batch to ensure quality before continuing.

**Ad-Hoc Development:**
- Review before merging to ensure no regressions.
