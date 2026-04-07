# Finish Branch

Switch to staging, sync with remote, and delete the current local branch.

```bash
git checkout staging && git pull origin staging && git branch -d <current-branch>
```
