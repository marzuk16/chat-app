---
model: claude-opus-4-6
---

# Plan Task

Fetch GitHub issue **#$ARGUMENTS** and produce an implementation plan.

1. Run `gh issue view $ARGUMENTS` to read the full issue body, labels, and comments.
2. Present a step-by-step design covering:
   - **What** the feature/fix is and which service(s) it touches.
   - **Layers involved**: Controller → Service → Repository → (Utils / JPA Specification if needed).
   - **Reusable components**: identify anything that belongs in `lib/components` rather than a single service.
   - **SOLID alignment**: call out which principles apply (SRP for service split, OCP for extension points, etc.).
   - **KISS check**: flag any unnecessary complexity and propose the simplest shape that satisfies the requirement.
3. Propose unit tests (pure logic, mocked dependencies) and integration tests (Testcontainers where infra is needed).
4. **STOP. Ask the user to confirm or revise the plan before writing any code.**
5. Once the user approves the final plan, save it to `docs/plan/task_plan_$ARGUMENTS.md`. The file should capture the agreed design: what the feature/fix is, which services and layers are involved, any reusable components, and the proposed tests. This file serves as the handoff to `/implement_task`.
