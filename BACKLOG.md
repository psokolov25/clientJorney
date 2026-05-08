# Product Backlog (MVP Iterations)

## Iteration 1 (in progress)
- [x] Typed runtime start-session API baseline.
- [x] Multi-module Maven skeleton.
- [x] Admin API for Scenario CRUD (`/api/admin/scenarios`).
- [x] In-memory repository implementation to decouple controllers from persistence details.
- [x] Core service layer for scenario management.
- [x] Tests for Admin API endpoints.

## Iteration 2
- [x] ScenarioGraph model + storage SPI.
- [x] `/api/admin/scenarios/{id}/graph` endpoints.
- [x] RouteValidationService with baseline graph checks.
- [x] Publish/archive/clone-version commands.

## Iteration 3
- [ ] File storage implementation (`client-journey-storage-file`).
- [ ] Atomic write + backup-on-write support.
- [ ] Corrupted JSON handling strategy.

## Iteration 4
- [ ] H2 storage module + Flyway migrations.
- [ ] Postgres storage module + JSONB mappings.
- [ ] Storage selection through Micronaut `@Requires`.

## Iteration 5
- [ ] VisitCreation orchestrator and attempt logging.
- [ ] VisitManager typed client (ENTRYPOINT_WITH_PARAMETERS mode first).
- [ ] Dry-run and mock visit clients.

## Iteration 6
- [ ] Embedded frontend build module and static asset copy into app resources.
- [ ] Admin UI skeleton + graph editor skeleton.
- [ ] Widget skeleton served from `/widget/**`.
