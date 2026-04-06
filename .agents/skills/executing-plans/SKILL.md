---
name: executing-plans
description: 在拥有编写好的实施计划并希望通过带有审核点的子代理会话来执行时使用。 (Use when you have a written implementation plan to execute in a separate session with review checkpoints)
---

# 执行计划 (Executing Plans)

> [!NOTE]
> **中文摘要**：核心原则是“上下文保护”。主代理负责协调和决策，所有的代码编写和实现工作必须委托给子代理。主代理绝不亲自写代码，甚至是一个一行的修复，也要分派给子代理。

## Overview

Load plan, review critically, execute all tasks with subagents, report when complete.

**Announce at start:** "I'm using the executing-plans skill to implement this plan."

**CRITICAL PRINCIPLE: Context Preservation**

Your context is precious and limited. Reserve it for coordination, planning, and critical decision-making. **ALL code writing, editing, and implementation work MUST be delegated to subagents**, regardless of task size or complexity.

- **Single task?** Dispatch one subagent
- **Multiple independent tasks?** Dispatch parallel subagents
- **Sequential dependent tasks?** Dispatch serial subagents
- **Never write code yourself** - even "quick fixes" consume context that should be preserved for orchestration

This skill assumes subagent support is available (via `browser_subagent`). It chooses between parallel and serial subagent dispatch automatically, based on task independence and shared context boundaries.

## The Process

### Step 1: Load and Review Plan
1. Read plan file
2. Review critically - identify any questions or concerns about the plan
3. If concerns: Raise them with your human partner before starting
4. If no concerns: Proceed to Task 2

### Step 2: Execute Tasks

**ALL tasks MUST be executed by subagents.** Your role is orchestration, not implementation.

For each task or batch:
1. **Assess task dependencies:**
   - Single independent task? → Dispatch one browser subagent
   - Multiple independent tasks? → Dispatch parallel browser subagents (one per task)
   - Sequential dependent tasks? → Dispatch serial browser subagents (each gets only upstream context it needs)

2. **Dispatch subagents with minimal context:**
   - Give each subagent only the task-local slice: task text, allowed files, acceptance criteria, and any directly required upstream summary

3. **Track progress:**
   - Mark task or batch as `[/]` in `task.md` before dispatch
   - Subagents follow plan steps exactly
   - Subagents run verifications as specified
   - Mark as `[x]` after subagent returns

**Never execute tasks yourself** - even if it seems faster, preserving your context for coordination is more valuable.

### Step 3: Complete Development

After all tasks complete and verified:
- Announce: "I'm using the finishing-a-development-branch skill to complete this work."
- **REQUIRED SUB-SKILL:** Use `finishing-a-development-branch`

## Anti-Patterns: What NOT to Do

**❌ NEVER do these - they violate the context preservation principle:**

1. **"It's just a small change, I'll do it myself"**
   - NO. Even one-line changes consume context. Dispatch a subagent.

2. **"I already have the file open, let me edit it quickly"**
   - NO. Reading files for review is fine. Editing them is not. Use a subagent.

3. **"There's only one task, subagents seem like overkill"**
   - NO. Single tasks still go to subagents. Your context is for coordination.

4. **"I'll just fix this test failure inline"**
   - NO. Debugging and fixing code is implementation work. Use a subagent.

**✅ Correct approach:**
- Read files to understand and coordinate
- Review subagent output
- Make architectural decisions
- Dispatch all implementation to subagents
- Preserve your context for the big picture

## Integration

**Required workflow skills:**
- **using-git-worktrees** - REQUIRED: Set up isolated workspace before starting
- **writing-plans** - Creates the plan this skill executes
- **finishing-a-development-branch** - Complete development after all tasks
