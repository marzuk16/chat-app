---
model: claude-sonnet-4-6
---

# Implement Task

### Steps

1. Find and check out the existing feature branch for this issue:
   ```bash
   git fetch origin
   BRANCH=$(git branch -r | grep "origin/feature/issue-$ARGUMENTS-" | head -1 | xargs | sed 's|origin/||')
   git checkout $BRANCH
   ```
   If no matching branch is found, stop and tell the user to run `/plan_task $ARGUMENTS` first.

2. Read `docs/plan/task_plan_$ARGUMENTS.md` to load the agreed design. If the file does not exist, stop and tell the user to run `/plan_task $ARGUMENTS` first.

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

7. Push the branch to update the existing PR:
   ```bash
   git push
   ```

8. Update PR status — remove the WIP label and mark it ready for review:
   ```bash
   gh pr edit --remove-label "🚧 Work In Progress" --add-label "👋  Waiting For Review"
   ```

9. Return the PR URL to the user.
