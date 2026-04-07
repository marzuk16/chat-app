# Pick Task

Fetch GitHub issue **#$ARGUMENTS** and implement it end-to-end.

## Phase 1 — Analysis (present to user, wait for approval)

1. Run `gh issue view $ARGUMENTS` to read the full issue body, labels, and comments.
2. Present a step-by-step design covering:
   - **What** the feature/fix is and which service(s) it touches.
   - **Layers involved**: Controller → Service → Repository → (Utils / JPA Specification if needed).
   - **Reusable components**: identify anything that belongs in `lib/components` rather than a single service.
   - **SOLID alignment**: call out which principles apply (SRP for service split, OCP for extension points, etc.).
   - **KISS check**: flag any unnecessary complexity and propose the simplest shape that satisfies the requirement.
3. Propose unit tests (pure logic, mocked dependencies) and integration tests (Testcontainers where infra is needed).
4. **STOP. Ask the user to confirm or revise the plan before writing any code.**

## Phase 2 — Implementation (only after explicit user approval)

Follow the approved plan and produce production-grade code.

### Git setup
```bash
git fetch origin staging
git checkout -b feature/issue-$ARGUMENTS-<short-slug> origin/staging
```

### Code standards
- **Layered structure** — Controller, Service, Repository, Utils, JPA Specification (only what is actually needed).
- **Dependency Injection** — constructor injection only; no field injection.
- **Lombok** — use `@RequiredArgsConstructor`, `@Builder`, `@Data` / `@Value` where appropriate.
- **MapStruct** — map between entity ↔ DTO; never expose entities from controllers.
- **SpringDoc** — annotate controllers with `@Operation` / `@Tag`.
- **Comments** — only on genuinely complex logic; no obvious or redundant comments.
- **No co-author trailer** in commits.

### Commit discipline
- One logical commit per meaningful milestone (e.g. entity+repo, service, controller, tests).
- Message format: `feat|fix|refactor|test: <what and why> (#$ARGUMENTS)`

### Tests
- Unit tests in `src/test/java` alongside production code.
- Integration tests using Testcontainers for DB / Kafka / MinIO interactions.

### PR
```bash
gh pr create \
  --base staging \
  --title "<concise title> (#$ARGUMENTS)" \
  --body "$(cat <<'EOF'
## Summary
- <bullet 1>
- <bullet 2>

## Changes
- <layer-by-layer summary>

## Test plan
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual smoke-test steps

Closes #$ARGUMENTS
EOF
)"
```

Push the branch and return the PR URL to the user.
