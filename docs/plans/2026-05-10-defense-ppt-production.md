# LearnCourse Defense PPT Production Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `executing-plans` to implement this plan task-by-task. It will decide whether each batch should run in parallel or serial subagent mode and will pass only task-local context to each subagent. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a 15-slide editable PPTX defense deck for the LearnCourse graduation project based on the approved narrative and visual system.

**Architecture:** Produce the deck inside a thread-scoped presentation workspace using artifact-tool presentation JSX. Extract thesis diagrams from the source `.docx`, lock the slide claim spine and design tokens, build slide modules in batches, render previews after each batch, then export and verify the final PPTX.

**Tech Stack:** Node.js, `@oai/artifact-tool`, presentation JSX, PowerShell, bundled Python, extracted thesis assets

---

### Task 1: Prepare the deck workspace and thesis assets

**Files:**
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/assets/docx-media/`
- Source: `C:/Users/28071/Desktop/简历/计科2202_李强_22412030220_基于课程知识图谱的学习平台设计与实现1_图表规范化_修改稿_表格修正 - 副本.docx`

- [ ] **Step 1: Create the standard presentation workspace directories**

Run:

```powershell
$workspace = 'C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck'
New-Item -ItemType Directory -Force -Path $workspace, "$workspace\slides", "$workspace\preview", "$workspace\layout", "$workspace\assets", "$workspace\qa", "$workspace\output" | Out-Null
```

Expected: all workspace directories exist with no error.

- [ ] **Step 2: Extract embedded thesis media from the source `.docx`**

Run:

```powershell
@'
import zipfile, os, shutil
from pathlib import Path
src = Path(r"C:/Users/28071/Desktop/简历/计科2202_李强_22412030220_基于课程知识图谱的学习平台设计与实现1_图表规范化_修改稿_表格修正 - 副本.docx")
out_dir = Path(r"C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/assets/docx-media")
out_dir.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(src) as zf:
    media = [n for n in zf.namelist() if n.startswith("word/media/")]
    for name in media:
        target = out_dir / Path(name).name
        with zf.open(name) as fsrc, open(target, "wb") as fdst:
            shutil.copyfileobj(fsrc, fdst)
print(f"extracted={len(media)}")
'@ | C:\Users\28071\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe -
```

Expected: `extracted=23` or another positive count is printed and media files appear in `assets/docx-media`.

- [ ] **Step 3: Generate a contact sheet for manual visual inspection**

Run:

```powershell
@'
from PIL import Image, ImageOps, ImageDraw, ImageFont
from pathlib import Path
src = Path(r"C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/assets/docx-media")
out = src / "contact-sheet.png"
files = [p for p in sorted(src.glob("image*.*")) if p.is_file()]
thumb_w, thumb_h = 280, 180
cols = 3
rows = (len(files) + cols - 1) // cols
sheet = Image.new("RGB", (cols * thumb_w, rows * (thumb_h + 28)), (245, 245, 245))
draw = ImageDraw.Draw(sheet)
font = ImageFont.load_default()
for i, p in enumerate(files):
    with Image.open(p) as im:
        thumb = ImageOps.contain(im.convert("RGB"), (thumb_w - 20, thumb_h - 20))
        x = (i % cols) * thumb_w
        y = (i // cols) * (thumb_h + 28)
        draw.rectangle([x + 8, y + 8, x + thumb_w - 8, y + thumb_h - 8], outline=(150, 150, 150), width=1)
        tx = x + (thumb_w - thumb.size[0]) // 2
        ty = y + (thumb_h - thumb.size[1]) // 2
        sheet.paste(thumb, (tx, ty))
        draw.text((x + 10, y + thumb_h + 4), p.name, fill=(0, 0, 0), font=font)
sheet.save(out)
print(out)
'@ | C:\Users\28071\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe -
```

Expected: `contact-sheet.png` is written under `assets/docx-media`.

### Task 2: Lock the source story and proof objects

**Files:**
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/profile-plan.txt`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/source-notes.txt`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/claim-spine.txt`
- Reference: `docs/specs/2026-05-10-defense-ppt-design.md`

- [ ] **Step 1: Write the engineering-platform profile plan**

Include:

```text
task mode: create
primary deck-profile: engineering-platform
secondary profile gates: strategy-style defense narrative, thesis-source fidelity
required proof objects: architecture map, teaching-resource ER diagram, AI/RAG entity diagram, module capability slide, algorithm slide, conclusion slide
source requirements: thesis headings, extracted docx diagrams, codebase-confirmed entities
```

Expected: `profile-plan.txt` captures mode, profile, proof objects, and source constraints.

- [ ] **Step 2: Write source notes with exact thesis-derived claims**

Include:

```text
- pain points: knowledge fragmentation, unclear learning path, hallucination risk
- innovation points: workflow transparency, graph-progress coupling, SSE + Agent + RAG
- architecture technologies: Vue 3, Spring Boot 3.x, MongoDB, ECharts, LLM, Embedding
- key entities: User, Course, Chapter, Progress, Agent, KbCollection, KbDocument, KbChunk
- algorithm topics: hybrid retrieval, graph analysis, SSE streaming
```

Expected: `source-notes.txt` becomes the single factual reference for slide copy.

- [ ] **Step 3: Write the 15-slide claim spine**

Include one block per slide with:

```text
slide 01 | claim: LearnCourse is a graph-driven intelligent learning platform | proof: title + identity
slide 02 | claim: existing online learning tools fail on fragmentation, path planning, and trusted QA | proof: three pain cards
slide 03 | claim: the project differentiates itself through graph-AI coupling | proof: three innovation modules
slide 04 | claim: the system closes the loop from learning to knowledge accumulation | proof: end-to-end workflow
slide 05 | claim: a layered architecture enables intelligent teaching services | proof: four-layer architecture map
slide 06 | claim: Progress is the core relation between learner and teaching resource | proof: ER diagram
slide 07 | claim: RAG adds document-grounded evidence to AI answers | proof: entity map + retrieval chain
slide 08 | claim: the platform supports continuous learning and progress persistence | proof: module card
slide 09 | claim: the knowledge graph turns courses into navigable structure | proof: graph-centered module card
slide 10 | claim: chapter-aware SSE chat improves response quality and experience | proof: chat interaction slide
slide 11 | claim: private KB enables professional answers grounded in uploaded material | proof: dual-lane RAG flow
slide 12 | claim: hybrid retrieval, graph algorithms, and SSE are the three core technical engines | proof: three-column algorithm panel
slide 13 | claim: core modules have been implemented into a coherent product experience | proof: 2x2 product showcase
slide 14 | claim: the project proves feasibility while exposing clear next-step optimizations | proof: summary + limitations + outlook
slide 15 | claim: the project is ready for defense wrap-up | proof: minimal Q&A page
```

Expected: `claim-spine.txt` lists all slides with a claim and proof object.

### Task 3: Build the shared deck foundation

**Files:**
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/shared.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-01.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-02.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-03.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-04.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-05.mjs`

- [ ] **Step 1: Create shared design tokens and helpers**

Implement `shared.mjs` with:

```js
export const COLORS = {
  bg: "#07111f",
  panel: "#0d1b2a",
  grid: "#17324d",
  line: "#4fd1ff",
  text: "#f5fbff",
  muted: "#8ea7bd",
  accent: "#8bf3ff",
};
```

Also include helpers for slide background, kicker, title, footer, card shell, and connection line drawing.

- [ ] **Step 2: Implement slides 01-05 using the approved narrative**

Use:

```text
slide-01: cover
slide-02: pain-point triad
slide-03: goals and innovations
slide-04: closed-loop workflow
slide-05: layered architecture
```

Expected: five slide modules exist and each exports one slide function.

- [ ] **Step 3: Render slides 01-05 individually for fast QA**

Run once per slide pattern:

```powershell
C:\Users\28071\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe "C:\Users\28071\.codex\plugins\cache\openai-primary-runtime\presentations\26.430.10722\skills\presentations\scripts\render_artifact_slide.mjs" --workspace "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck" --slide-module "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\slides\slide-01.mjs" --output "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\preview\slide-01.png" --layout "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\layout\slide-01.layout.json"
```

Expected: each slide writes one preview PNG and one layout JSON file.

### Task 4: Build the middle proof slides

**Files:**
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-06.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-07.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-08.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-09.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-10.mjs`

- [ ] **Step 1: Implement the two entity-relationship proof slides**

Use:

```text
slide-06: User / Course / Chapter / Progress relationship
slide-07: User / Agent / KbCollection / KbDocument / KbChunk / RAG relationship
```

Expected: both slides render cleanly with legible connectors and labels.

- [ ] **Step 2: Implement three module capability slides**

Use:

```text
slide-08: course and progress management
slide-09: interactive knowledge graph
slide-10: AI assistant and chapter Q&A
```

Expected: each slide combines a strong central proof object with compact side notes.

- [ ] **Step 3: Render and review slides 06-10**

Run the same `render_artifact_slide.mjs` command for slides `06` through `10`.

Expected: `preview/slide-06.png` through `preview/slide-10.png` are generated without script errors.

### Task 5: Build the closing proof slides

**Files:**
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-11.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-12.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-13.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-14.mjs`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/slides/slide-15.mjs`

- [ ] **Step 1: Implement the KB, algorithm, and showcase slides**

Use:

```text
slide-11: private KB + hybrid RAG pipeline
slide-12: three-column algorithm engine
slide-13: 2x2 system implementation showcase
```

Expected: each slide is visually distinct but still uses the locked design system.

- [ ] **Step 2: Implement the wrap-up slides**

Use:

```text
slide-14: summary, limitations, outlook
slide-15: Q&A closing page
```

Expected: slide-14 is information-dense but readable; slide-15 is minimal and calm.

- [ ] **Step 3: Render and review slides 11-15**

Run the same `render_artifact_slide.mjs` command for slides `11` through `15`.

Expected: `preview/slide-11.png` through `preview/slide-15.png` are generated without script errors.

### Task 6: Export the full deck and perform final QA

**Files:**
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/output/learncourse-defense-deck.pptx`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/preview/contact-sheet.png`
- Create: `C:/Users/28071/AppData/Local/Temp/codex-presentations/019e081b-0194-7a11-8858-8320f509e94e/learncourse-defense-deck/output/artifact-build-manifest.json`

- [ ] **Step 1: Build the full PPTX deck**

Run:

```powershell
C:\Users\28071\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe "C:\Users\28071\.codex\plugins\cache\openai-primary-runtime\presentations\26.430.10722\skills\presentations\scripts\build_artifact_deck.mjs" --workspace "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck" --slides-dir "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\slides" --out "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\output\learncourse-defense-deck.pptx" --preview-dir "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\preview" --layout-dir "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\layout" --contact-sheet "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck\preview\contact-sheet.png" --slide-count 15
```

Expected: the script prints a JSON manifest with `slideCount: 15` and writes the final PPTX.

- [ ] **Step 2: Inspect the contact sheet and full-size previews**

Check:

```text
- title hierarchy is consistent
- line and connector colors are readable on dark backgrounds
- no slide looks like a pasted thesis screenshot without redesign
- the deck rhythm alternates overview, diagram, module, and closing slides
```

Expected: no slide has obvious text overflow, collisions, or washed-out diagrams.

- [ ] **Step 3: Keep the final deliverable and clean scratch artifacts only after QA passes**

Run:

```powershell
C:\Users\28071\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe "C:\Users\28071\.codex\plugins\cache\openai-primary-runtime\presentations\26.430.10722\skills\presentations\scripts\cleanup_presentation_workspace.mjs" --workspace "C:\Users\28071\AppData\Local\Temp\codex-presentations\019e081b-0194-7a11-8858-8320f509e94e\learncourse-defense-deck" --preserve-output
```

Expected: the final PPTX remains in `output/` and temporary critique artifacts are removed.
