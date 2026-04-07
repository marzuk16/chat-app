---
model: claude-sonnet-4-6
---

# Implement Task

Start by reading `docs/plan/task_plan_$ARGUMENTS.md` to load the agreed design. If the file does not exist, stop and tell the user to run `/plan_task $ARGUMENTS` first.

### Steps

1. Derive a short slug from the feature/fix name in the plan (e.g. `add-user-profile`).
2. Set up the branch:
   ```bash
   git fetch origin staging
   git checkout -b feature/issue-$ARGUMENTS-<derived-slug> origin/staging
   ```
3. Implement each layer in order (only what the plan calls for), following these standards:
   - Entity + Repository
   - Service
   - Controller
   - Utils / JPA Specification if needed

   **Standards:**
   - **Dependency Injection** — constructor injection only; no field injection.
   - **Lombok** — use `@RequiredArgsConstructor`, `@Builder`, `@Data` / `@Value` where appropriate.
   - **MapStruct** — map between entity ↔ DTO; never expose entities from controllers.
   - **SpringDoc** — annotate controllers with `@Operation` / `@Tag`.
   - **Comments** — only on genuinely complex logic; no obvious or redundant comments.
4. Write tests (unit + integration) as each layer is completed.
5. Commit at each meaningful milestone. No co-author trailer. Message format: `feat|fix|refactor|test: <what and why> (#$ARGUMENTS)`
6. Verify the build passes:
   ```bash
   mvn clean package -pl <affected-module> -am
   ```
7. Open the PR.

### PR

Read `.github/PULL_REQUEST_TEMPLATE.md`, fill in the placeholders based on the implementation, and set `Closes: #$ARGUMENTS`. Include a link to `docs/plan/task_plan_$ARGUMENTS.md` in the PR body. Pass the populated content as the `--body` to `gh pr create`.

Apply `--label` flags for:
- **Service**: match the service the issue touches to one of: `common`, `config`, `gateway`, `auth`, `user`, `chat`, `media`, `notification`, `presence`, `admin`, `infrastructure`
- **Type**: match the issue type to one of: `Feature`, `Task`, `🐞 Bug`
- **Status**: always add `👋  Waiting For Review`

```bash
gh pr create \
  --base staging \
  --title "<concise title> (#$ARGUMENTS)" \
  --label "<service>" \
  --label "<Feature|Task|🐞 Bug>" \
  --label "👋  Waiting For Review"
```

Push the branch and return the PR URL to the user.
