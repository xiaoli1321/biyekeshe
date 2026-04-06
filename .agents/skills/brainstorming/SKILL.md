---
name: brainstorming
description: "在进行任何创意工作（创建功能、构建组件、添加功能或修改行为）之前，您必须使用此技能。在实施前探索用户意图、要求和设计。 (You MUST use this before any creative work - creating features, building components, adding functionality, or modifying behavior. Explores user intent, requirements and design before implementation.)"
---

# 头脑风暴 (Brainstorming Ideas Into Designs)

> [!NOTE]
> **中文摘要**：核心原则是“先设计，后编码”。通过苏格拉底式的持续对话，深入理解用户意图、约束条件和成功标准。严禁在设计文档未获批准前开始任何实质性的编码工作。

Help turn ideas into fully formed designs and specs through natural collaborative dialogue.

Start by understanding the current project context, then ask questions one at a time to refine the idea. Once you understand what you're building, present the design and get user approval.

<HARD-GATE>
Do NOT invoke any implementation skill, write any code, scaffold any project, or take any implementation action until you have presented a design and the user has approved it. This applies to EVERY project regardless of perceived simplicity.
</HARD-GATE>

## Anti-Pattern: "This Is Too Simple To Need A Design"

Every project goes through this process. A todo list, a single-function utility, a config change — all of them. "Simple" projects are where unexamined assumptions cause the most wasted work. The design can be short (a few sentences for truly simple projects), but you MUST present it and get approval.

## Autonomous Mode

**Detecting Explicit Authorization:**
At any point during the brainstorming process, if the user gives explicit authorization to proceed without further approval checks, enter **autonomous mode** for all remaining steps.

Authorization signals include:
- "don't ask me anymore" / "stop asking" / "no more questions"
- "you decide" / "your call" / "you choose" / "your decision"
- "proceed autonomously" / "full authority" / "full autonomy"
- "skip the approval gates" / "skip approvals"

**When autonomous mode is activated:**
- **Step 4 (Approaches):** Present your recommendation and reasoning, but proceed without waiting for user selection
- **Step 5 (Design):** Present the entire design at once without asking for approval after each section
- **Step 8 (Spec Review):** Skip the user review gate and proceed directly to writing-plans

## Checklist

You MUST create a task for each of these items and complete them in order:

1. **Explore project context** — check files, docs, recent commits
2. **Offer visual companion** — this is its own message, not combined with a clarifying question. 
3. **Ask clarifying questions** — one at a time, understand purpose/constraints/success criteria
4. **Propose 2-3 approaches** — with trade-offs and your recommendation
5. **Present design** — in sections scaled to their complexity, get user approval after each section
6. **Write design doc** — save to `docs/specs/YYYY-MM-DD-<topic>-design.md` and commit
7. **Spec self-review** — quick inline check for placeholders, contradictions, ambiguity, scope 
8. **User reviews written spec** — ask user to review the spec file before proceeding
9. **Transition to implementation** — invoke writing-plans skill to create implementation plan

## After the Design

**Documentation:**

- Write the validated design (spec) to `docs/specs/YYYY-MM-DD-<topic>-design.md`
- Commit the design document to git

**Spec Self-Review:**
After writing the spec document, look at it with fresh eyes:

1. **Placeholder scan:** Any "TBD", "TODO", incomplete sections, or vague requirements? Fix them.
2. **Internal consistency:** Do any sections contradict each other? Does the architecture match the feature descriptions?
3. **Scope check:** Is this focused enough for a single implementation plan, or does it need decomposition?
4. **Ambiguity check:** Could any requirement be interpreted two different ways? If so, pick one and make it explicit.

**User Review Gate:**
(Skip this if in autonomous mode)
After the spec review loop passes, ask the user to review the written spec before proceeding:

> "设计文档已撰写并提交至 `<path>`。在我们开始编写实施计划之前，请查看它并告诉我是否需要进行任何更改。"

**Implementation:**

- Invoke the `writing-plans` skill to create a detailed implementation plan.
