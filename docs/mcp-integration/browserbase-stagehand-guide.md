# Browserbase/Stagehand Guide (Single-Session Workflow)

Version: 1.0
Last Updated: 2025-09-09
Purpose: Define a robust, single-session workflow for Browserbase/Stagehand and document best practices to avoid mixed-session errors.

---

## Why Single-Session?

Stagehand has two families of tools:
- Single-session: `browserbase_session_create`, `browserbase_stagehand_[navigate|observe|extract|get_url]`, `browserbase_session_close`
- Multi-session: `multi_browserbase_stagehand_session_create`, `multi_browserbase_stagehand_[navigate_session|observe_session|extract_session|get_url_session]`, `multi_browserbase_stagehand_session_close`

Mixing the two families in one flow can produce empty results or confusing failures. For most documentation extraction tasks, a single session is sufficient and easier to reason about.

---

## Single-Session Playbook

1) Create session
- Use `browserbase_session_create` once per run or when you need a new clean state.

2) Navigate
- Use `browserbase_stagehand_navigate` with a trustworthy URL (e.g., official Android docs).
- If navigation fails, retry once; otherwise validate the URL.

3) Extract text
- Prefer a specific and narrow instruction when calling `browserbase_stagehand_extract`:
  - Example: "Extract h1–h3 headings, the API requirements section (permissions, threading), and any code blocks on this page."
- If the result looks incomplete, refine the instruction to explicitly mention the sections you want.

4) Close session
- Always close the session using `browserbase_session_close` to free resources.

---

## Instruction Patterns (Good vs Better)

- Good: "Extract content about AudioRecord"
- Better: "Extract: (1) required permissions, (2) threading model guidance, (3) buffer sizing recommendations, (4) read modes (blocking vs non-blocking), (5) overflow/underrun handling, (6) supported audio sources, and (7) notes for foreground/background service usage."

---

## Troubleshooting

- Empty extraction
  - Ensure you used only single-session calls (no `multi_..._session` calls in this flow).
  - Re-run navigate to confirm load, then extract with more explicit instructions.
- Partial content
  - Target specific sections by heading names (e.g., "Permissions", "Usage notes", "Threading").
- Slow or stuck pages
  - Try a simpler page first (e.g., API reference), then move to higher-latency guides.

---

## Quality Checklist

- [ ] Single session created and closed
- [ ] Extract instruction names specific sections
- [ ] Result is complete (headings and key constraints present)
- [ ] Links or references captured if important
