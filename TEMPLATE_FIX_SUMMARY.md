# Template Fix Summary

## Problem
The application showed a Thymeleaf template parsing error when accessing `/dashboard`:

```
org.thymeleaf.exceptions.TemplateInputException: An error happened during template parsing
...
Fragment 'layout/main :: main' specifies synthetic (unnamed) parameters, but the resolved fragment does not match a fragment signature
```

## Root Cause
All template files in `src/main/resources/templates/` were referencing a non-existent fragment:
- `dashboard/index.html`
- `courses/list.html`
- `courses/detail.html`
- `chapters/view.html`
- `knowledge-graph/view.html`

They were using:
```html
th:replace="layout/main :: main (~{::title}, ~{::main-content})"
```

But `layout/main.html` only defines a fragment named `content`, not `main`.

## Solution
Changed all 5 template files from:
```html
th:replace="layout/main :: main (~{::title}, ~{::main-content})"
```

To:
```html
th:replace="layout/main"
```

This allows the `main-content` fragment from child templates to correctly replace the `content` fragment in the parent layout.

## Files Modified
1. `src/main/resources/templates/dashboard/index.html:5`
2. `src/main/resources/templates/courses/list.html:5`
3. `src/main/resources/templates/courses/detail.html:5`
4. `src/main/resources/templates/chapters/view.html:5`
5. `src/main/resources/templates/knowledge-graph/view.html:5`

## Verification
✓ Application starts successfully (Tomcat on port 8084)
✓ No more Thymeleaf parsing errors in logs
✓ HTTP 200 response on dashboard endpoint when authenticated
✓ All 5 template files now use correct reference

## Status
**FIXED** - Template parsing errors resolved. Application runs without errors.
