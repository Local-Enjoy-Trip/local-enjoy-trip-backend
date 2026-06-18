# Storage Module Coding Style & Rules

## Operating Principles

- **Persistence Layer**: The `storage` namespace keeps `storage/db-core`, which handles database schema mapping and persistence infrastructure.
- **DB Core Only**: `storage/db-core` owns JPA entities, Spring Data repositories, Flyway migrations, jOOQ codegen, and database configuration only.
- **Technology Stack**: Uses Spring Data JPA for straightforward CRUD operations and simple queries. Uses jOOQ for complex read queries requiring dynamic predicates, joins, or composable filtering. **JdbcTemplate is strictly forbidden; any query that cannot be handled by JPA must be implemented using jOOQ.**

## Coding Style

- **Entity Definitions**: Use `@Entity` for database mapping. Map table structures explicitly using `@Table` and `@Column`. For auditing timestamps (e.g., `createdAt`, `updatedAt`), use Spring Data JPA Auditing through the storage `BaseEntity` instead of entity-local `@PrePersist`/`@PreUpdate` callbacks.
- **JPA Repositories**: Spring Data repositories stay in `storage/db-core` and return storage entities directly; do not wrap them in core-domain repository ports or custom storage repository/model layers.
- **Lombok**: Ensure entities use `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- **Data Transformation**: Do not add separate mapping layers for core-domain ports. Storage entities must not expose
  `toModel`/`toDomain` methods that return core-api domain models. In the monolithic target, core-api services that need
  a domain object should instantiate it directly from the storage entity at the service call path.
- **JOOQ Usage**: Use JOOQ's type-safe DSL for native SQL. Avoid raw string manipulation for SQL queries.

## Verification

- **Transaction Boundaries**: Core-api service/helper write/delete methods should declare `@Transactional` when needed.
- **Native Mutations**: Native queries for update/delete operations must be implemented using jOOQ. JPA `@Query(nativeQuery = true)` is prohibited.
