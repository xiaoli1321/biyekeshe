---
name: dispatching-parallel-agents
description: 在面对 2 个以上可以独立进行的任务（没有共享状态或顺序依赖关系）时使用。 (Use when facing 2+ independent tasks that can be worked on without shared state or sequential dependencies)
---

# 分派并行代理 (Dispatching Parallel Agents)

> [!NOTE]
> **中文摘要**：核心原则是“一个独立的问题域分派一个子代理”。并行调查多个不相关的失败（如不同测试文件、不同子系统）或并行实现独立的功能块，从而将开发速度从串行提升到并行。

## Overview

You delegate tasks to specialized agents with isolated context via `browser_subagent`. By precisely crafting their instructions and context, you ensure they stay focused and succeed at their task. They should never inherit your session's context or history — you construct exactly what they need. This also preserves your own context for coordination work.

**Core principle:** Dispatch one agent per independent problem domain. Let them work concurrently.

## When to Use

**Use when:**
- 3+ test files failing with different root causes
- Multiple subsystems broken independently
- Each problem can be understood without context from others
- No shared state between investigations

**Don't use when:**
- Failures are related (fix one might fix others)
- Need to understand full system state
- Agents would interfere with each other (editing same lines of same files)

## The Pattern

### 1. Identify Independent Domains

Group failures by what's broken:
- Domain A: Frontend component UI
- Domain B: Backend API validation
- Domain C: Database migration script

### 2. Create Focused Agent Tasks

Each agent gets:
- **Specific scope:** One file or subsystem
- **Clear goal:** Fix X or Implement Y
- **Constraints:** Don't change code outside of scope
- **Expected output:** Summary of findings and changes

### 3. Dispatch in Parallel

Invoke multiple `browser_subagent` calls (if system allows parallel execution) or queue them.

### 4. Review and Integrate

When agents return:
- Read each summary
- Verify fixes don't conflict
- Run full test suite
- Integrate all changes

## Agent Prompt Structure

Good agent prompts are:
1. **Focused** - One clear problem domain
2. **Self-contained** - All context needed to understand the problem
3. **Specific about output** - What should the agent return?

Example:
```markdown
Fix the 3 failing tests in src/api/user.test.ts:

1. "should reject invalid email"
2. "should return 404 for missing user"

Your task:
1. Read the test file
2. Identify root cause
3. Fix the bug
4. Verify by running the test

Do NOT refactor other parts of the system.
Return: Summary of root cause and what you fixed.
```

## Key Benefits

1. **Parallelization** - Multiple investigations/implementations happen simultaneously
2. **Focus** - Each agent has narrow scope
3. **Speed** - N problems solved in time of 1
