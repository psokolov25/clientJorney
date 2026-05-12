# Product Backlog

Текущий рабочий бэклог и проектные чеклисты перенесены в каталог `project-management/`:

- `project-management/backlog.md` — основной бэклог (RU)
- `project-management/checklist.md` — архитектурный и функциональный чеклист (RU)
- `project-management/backlog-coverage.md` — аудит фактического покрытия бэклога (RU)

## Технический статус текущей кодовой базы

### Done
- [x] Typed runtime start-session API baseline.
- [x] Multi-module Maven skeleton.
- [x] Admin API for Scenario CRUD (`/api/admin/scenarios`).
- [x] ScenarioGraph endpoints and baseline validation.
- [x] File storage module with atomic write, backup-on-write and corrupted JSON handling.

### Planned next
- [x] H2 storage module + baseline schema bootstrap.
- [x] PostgreSQL storage module + baseline mappings/indexes.
- [x] Storage selection via Micronaut `@Requires`.
- [ ] Visit creation orchestration and clients.
- [ ] Embedded frontend build and static asset delivery.
