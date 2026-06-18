# Storage Module Coding Style & Rules

## Operating Principles

- **Persistence Layer**: The `storage` namespace keeps `storage/db-core`, which handles database schema mapping and persistence infrastructure.
- **DB Core Only**: `storage/db-core` owns storage row/entity contracts, MyBatis mapper interfaces, XML SQL, type handlers, Flyway migrations, and database configuration only.
- **Technology Stack**: Uses MyBatis mapper/XML for persistence SQL. PostGIS, pg_vector, dynamic predicates, joins, projections, native mutations, `on conflict`, and `returning` are expressed as explicit mapper SQL. **JdbcTemplate, Spring Data JPA repositories, and jOOQ runtime/codegen are not used for new persistence paths.**

## Coding Style

- **Storage Row Contracts**: Storage row/entity classes are plain Java contracts used by MyBatis. Do not add JPA annotations or Spring Data auditing callbacks. Timestamp fields are populated by DB defaults or explicit mapper SQL.
- **MyBatis Mappers**: Mapper interfaces/XML stay in `storage/db-core` and return storage contracts directly; do not wrap them in core-domain repository ports or custom storage repository/model layers.
- **Lombok**: Ensure entities use `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- **Data Transformation**: Do not add separate mapping layers for core-domain ports. Storage entities must not expose
  `toModel`/`toDomain` methods that return core-api domain models. In the monolithic target, core-api services that need
  a domain object should instantiate it directly from the storage entity at the service call path.
- **MyBatis SQL**: Keep SQL in mapper XML and use parameter binding; do not build SQL through ad-hoc string concatenation in services.

## Verification

- **Transaction Boundaries**: Core-api service/helper write/delete methods should declare `@Transactional` when needed.
- **Native Mutations**: Native update/delete/upsert operations must be implemented in MyBatis mapper XML. JPA `@Query(nativeQuery = true)` and jOOQ DSL are prohibited.
