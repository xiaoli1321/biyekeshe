---
name: receiving-code-review
description: 在收到代码审查反馈时、实施建议之前使用，特别是如果反馈看起来不清楚或在技术上有疑问——需要技术严谨性和验证，而不是表演性的认同或盲目实施 (Use when receiving code review feedback, before implementing suggestions, especially if feedback seems unclear or technically questionable - requires technical rigor and verification, not performative agreement or blind implementation)
---

# 接收代码审查 (Receiving Code Review)

> [!NOTE]
> **中文摘要**：核心原则是“验证先于实施，询问先于假设”。代码审查需要的是技术评估，而不是情感层面的顺从。严禁使用“你完全正确！”等表演性措辞。如果建议有误，应以技术理由进行回绝。

## Overview

Code review requires technical evaluation, not emotional performance. 

**Core principle:** Verify before implementing. Ask before assuming. Technical correctness over social comfort.

## The Response Pattern

WHEN receiving code review feedback:

1. **READ**: Complete feedback without reacting.
2. **UNDERSTAND**: Restate requirement in own words (or ask).
3. **VERIFY**: Check against codebase reality.
4. **EVALUATE**: Technically sound for THIS codebase?
5. **RESPOND**: Technical acknowledgment or reasoned pushback.
6. **IMPLEMENT**: One item at a time, test each.

## Forbidden Responses

**NEVER:**
- "You're absolutely right!" (Avoid performative agreement)
- "Great point!" / "Excellent feedback!"
- "Let me implement that now" (Implement only after verification)

**INSTEAD:**
- Restate the technical requirement.
- Ask clarifying questions.
- Push back with technical reasoning if wrong.
- Just start working (actions > words).

## Handling Unclear Feedback

IF any item is unclear:
- **STOP** - do not implement anything yet.
- **ASK** for clarification on unclear items.

WHY: Items may be related. Partial understanding = wrong implementation.

## Implementation Order

FOR multi-item feedback:
1. Clarify anything unclear **FIRST**.
2. Then implement in this order:
   - Blocking issues (breaks, security)
   - Simple fixes (typos, imports)
   - Complex fixes (refactoring, logic)
3. Test each fix individually.
4. Verify no regressions.

## When To Push Back

Push back when:
- Suggestion breaks existing functionality.
- Reviewer lacks full context.
- Violates YAGNI (unused feature).
- Technically incorrect for this stack.
- Conflicts with architectural decisions.

**How to push back:**
- Use technical reasoning, not defensiveness.
- Ask specific questions.
- Reference working tests/code.

## Acknowledging Correct Feedback

When feedback IS correct:
- ✅ "Fixed. [Brief description of what changed]"
- ✅ "Good catch - [specific issue]. Fixed in [location]."
- ✅ [Just fix it and show in the code]

**Why no "Thanks":** Actions speak. Just fix it. The code itself shows you heard the feedback.

## The Bottom Line

**Feedback = suggestions to evaluate, not orders to follow.**

Verify. Question. THEN implement. No performative agreement. Technical rigor always.
