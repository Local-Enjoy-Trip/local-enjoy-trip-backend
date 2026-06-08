# Storage Module Coding Style & Rules

## Operating Principles

- **Persistence Layer**: The `storage` module handles database access, schema mapping, and persistence infrastructure.
- **Adapter Pattern**: It implements repository interfaces defined in the `core` module, acting as persistence adapters.
- **Technology Stack**: Uses Spring Data JPA for straightforward CRUD operations and simple queries. Uses jOOQ for complex read queries requiring dynamic predicates, joins, or composable filtering. **JdbcTemplate is strictly forbidden; any query that cannot be handled by JPA must be implemented using jOOQ.**

## Coding Style

- **Entity Definitions**: Use `@Entity` for database mapping. Map table structures explicitly using `@Table`, `@Column`, and use `@PrePersist`/`@PreUpdate` for auditing timestamps (e.g., `createdAt`, `updatedAt`).
- **Repository Implementations**: Adapter classes must be annotated with `@Repository` and implement `core/repository` interfaces (e.g., `MemberStorageRepository implements MemberRepository`).
- **Lombok**: Use `@RequiredArgsConstructor` to inject JPA repositories (`*JpaRepository`) and jOOQ `DSLContext`. Ensure entities use `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- **Data Transformation**: Storage adapters must map persistence-specific `*Entity` classes to `core/domain` models before returning data to the caller (e.g., using `toModel()` mapper methods).
- **JOOQ Usage**: Use JOOQ's type-safe DSL for native SQL. Avoid raw string manipulation for SQL queries.

## Verification

- **Transaction Boundaries**: While `@Transactional` is typically managed at the `core` service layer, explicit write or delete operations within storage adapters should declare `@Transactional` to ensure data integrity.
- **Native Mutations**: Native queries for update/delete operations must be implemented using jOOQ. JPA `@Query(nativeQuery = true)` is prohibited.
