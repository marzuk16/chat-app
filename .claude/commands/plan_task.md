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
6. Derive a short slug from the feature/fix name (e.g. `add-user-profile`).
7. Create and switch to the feature branch:
   ```bash
   git fetch origin staging
   git checkout -b feature/issue-$ARGUMENTS-<derived-slug> origin/staging
   ```
8. Commit the plan file as the first commit on the branch:
   ```bash
   git add docs/plan/task_plan_$ARGUMENTS.md
   git commit -m "chore: add implementation plan for issue #$ARGUMENTS"
   ```
9. Push the branch:
   ```bash
   git push -u origin feature/issue-$ARGUMENTS-<derived-slug>
   ```
10. Open the PR against `staging`:
    - Determine the **Service** label from the plan (one of: `common`, `config`, `gateway`, `auth`, `user`, `chat`, `media`, `notification`, `presence`, `admin`, `infrastructure`).
    - Determine the **Type** label from the issue (one of: `Feature`, `Task`, `🐞 Bug`).
    - Read `.github/PULL_REQUEST_TEMPLATE.md`, fill in the placeholders based on the plan, and set `Closes: #$ARGUMENTS`. Include a link to `docs/plan/task_plan_$ARGUMENTS.md` in the PR body.
    ```bash
    gh pr create \
      --base staging \
      --title "<concise title> (#$ARGUMENTS)" \
      --label "🚧 Work In Progress" \
      --label "<service>" \
      --label "<Feature|Task|🐞 Bug>" \
      --body "$(cat <<'EOF'
    <populated PR template content>
    EOF
    )"
    ```
11. Add the PR to the **chat-app** GitHub Project and set its fields:

    ```bash
    # Add PR to project and capture the item ID
    ITEM_ID=$(gh project item-add 2 --owner marzuk16 --url "<pr-url>" --format json --jq '.id')
    ```

    - **Priority** — read the `priority:` label from the issue and map it to the correct option ID, then set it:
      | Issue label       | Option ID  |
      |-------------------|------------|
      | priority: critical | `79628723` |
      | priority: high     | `0a877460` |
      | priority: medium   | `da944a9c` |
      | priority: low      | `0be8e596` |
      ```bash
      gh project item-edit --project-id PVT_kwHOAgBHKs4BTtVL --id "$ITEM_ID" \
        --field-id PVTSSF_lAHOAgBHKs4BTtVLzhA6c1Y \
        --single-select-option-id <mapped-option-id>
      ```

    - **Size** — infer from the plan scope (number of layers, services, and test surface):
      | Scope                                      | Size |
      |--------------------------------------------|------|
      | Single file / trivial change               | XS (`6c6483d2`) |
      | 1–2 layers, one service                    | S (`f784b110`)  |
      | Full layer stack, one service              | M (`7515a9f1`)  |
      | Multiple services or shared lib changes    | L (`817d0097`)  |
      | Cross-cutting, many services               | XL (`db339eb2`) |
      ```bash
      gh project item-edit --project-id PVT_kwHOAgBHKs4BTtVL --id "$ITEM_ID" \
        --field-id PVTSSF_lAHOAgBHKs4BTtVLzhA6c1c \
        --single-select-option-id <inferred-option-id>
      ```

    - **Start date** — set to today's date:
      ```bash
      gh project item-edit --project-id PVT_kwHOAgBHKs4BTtVL --id "$ITEM_ID" \
        --field-id PVTF_lAHOAgBHKs4BTtVLzhA6c1k \
        --date "$(date +%Y-%m-%d)"
      ```

12. Return the PR URL to the user.
