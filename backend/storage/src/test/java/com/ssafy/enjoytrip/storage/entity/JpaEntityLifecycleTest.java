package com.ssafy.enjoytrip.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaEntityLifecycleTest.TestApplication.class)
class JpaEntityLifecycleTest {
    @PersistenceContext
    private EntityManager entityManager;

    @DisplayName("인증 로그 Auditing은 loggedAt을 초기화하고 테이블 매핑을 유지한다")
    @Test
    void authLogAuditingInitializesLoggedAtAndKeepsTableMapping() {
        AuthLogEntity authLog = persistAndFlush(new AuthLogEntity("ssafy", "LOGIN"));

        assertAll(
                () -> assertEquals("auth_logs", AuthLogEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("ssafy", field(authLog, "userId")),
                () -> assertEquals("LOGIN", field(authLog, "eventType")),
                () -> assertNotNull(field(authLog, "loggedAt")),
                () -> assertColumn(AuthLogEntity.class, "userId", "user_id", 64, false)
        );
    }

    @DisplayName("게시글 Auditing은 생성 시각과 수정 시각을 JPA 저장 시점에 관리한다")
    @Test
    void boardAuditingManagesCreatedAndUpdatedTimestampsOnJpaPersistence() {
        BoardPostEntity board = persistAndFlush(new BoardPostEntity("board-1", "Title", "content", "author"));
        LocalDateTime createdAt = board.getCreatedAt();

        board.update("Updated", "updated content");
        flushAndClear();
        BoardPostEntity found = entityManager.find(BoardPostEntity.class, "board-1");

        assertAll(
                () -> assertEquals("boards", BoardPostEntity.class.getAnnotation(Table.class).name()),
                () -> assertNotNull(createdAt),
                () -> assertEquals(createdAt, found.getCreatedAt()),
                () -> assertEquals("Updated", found.getTitle()),
                () -> assertEquals("updated content", found.getContent()),
                () -> assertEquals("author", found.getAuthor()),
                () -> assertNotNull(found.getUpdatedAt()),
                () -> assertColumn(BoardPostEntity.class, "content", "", 255, false)
        );
    }

    @DisplayName("회원 수정은 null과 빈 패치 필드는 무시하고 변경 시 updatedAt은 Auditing으로 갱신한다")
    @Test
    void memberUpdateIgnoresNullAndBlankPatchFieldsButAuditingRefreshesUpdatedAtOnChange() {
        MemberEntity member = persistAndFlush(new MemberEntity(
                "ssafy",
                "SSAFY",
                "동네핀러",
                "ssafy@example.com",
                "secret",
                "https://cdn.example.com/profile.png",
                37.5665,
                126.9780,
                "서울 중구"
        ));

        member.update("  ", " ", "updated@example.com", "", "", null, null, " ");
        flushAndClear();
        MemberEntity found = entityManager.find(MemberEntity.class, member.getId());

        assertAll(
                () -> assertEquals("members", MemberEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("ssafy", found.getUserId()),
                () -> assertEquals("SSAFY", found.getName()),
                () -> assertEquals("동네핀러", found.getNickname()),
                () -> assertEquals("updated@example.com", found.getEmail()),
                () -> assertEquals("secret", found.getPassword()),
                () -> assertEquals("https://cdn.example.com/profile.png", found.getProfileImageUrl()),
                () -> assertEquals(37.5665, found.getRepresentativeLatitude()),
                () -> assertEquals(126.9780, found.getRepresentativeLongitude()),
                () -> assertEquals("서울 중구", found.getRepresentativeRegionName()),
                () -> assertNotNull(found.getCreatedAt()),
                () -> assertNotNull(found.getUpdatedAt()),
                () -> assertTrue(column(MemberEntity.class, "userId").unique()),
                () -> assertColumn(MemberEntity.class, "userId", "user_id", 64, false)
        );
    }

    @DisplayName("공지 Auditing은 생성 및 수정 타임스탬프를 관리한다")
    @Test
    void noticeAuditingManagesCreatedAndUpdatedTimestamps() {
        NoticeEntity notice = persistAndFlush(new NoticeEntity("Notice", "content", "admin"));

        notice.update("Updated", "updated content");
        flushAndClear();
        NoticeEntity found = entityManager.find(NoticeEntity.class, notice.getId());

        assertAll(
                () -> assertEquals("notices", NoticeEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("Updated", found.getTitle()),
                () -> assertEquals("updated content", found.getContent()),
                () -> assertEquals("admin", found.getAuthor()),
                () -> assertNotNull(found.getCreatedAt()),
                () -> assertNotNull(found.getUpdatedAt())
        );
    }

    @DisplayName("핫플레이스와 여행 계획은 Auditing 생성 시각과 nullable 텍스트 필드를 보존한다")
    @Test
    void hotplaceAndTravelPlanPreserveNullableTextFieldsAndRequiredColumnsWithAuditing() {
        HotplaceEntity hotplace = persistAndFlush(new HotplaceEntity("hot-1", "ssafy", "Cafe", "food",
                "2026-05-15", 37.5, 127.0, null, null));
        TravelPlanEntity plan = persistAndFlush(new TravelPlanEntity("plan-1", "ssafy", "Trip", "2026-05-15",
                "2026-05-16", 1000, null, null));

        assertAll(
                () -> assertEquals("hotplaces", HotplaceEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("plans", TravelPlanEntity.class.getAnnotation(Table.class).name()),
                () -> assertNull(hotplace.getDescription()),
                () -> assertNull(hotplace.getPhoto()),
                () -> assertNull(plan.getNote()),
                () -> assertNull(plan.getRouteItemsJson()),
                () -> assertNotNull(hotplace.getCreatedAt()),
                () -> assertNotNull(plan.getCreatedAt()),
                () -> assertColumn(HotplaceEntity.class, "userId", "user_id", 64, false),
                () -> assertColumn(TravelPlanEntity.class, "routeItemsJson", "route_items", 255, true)
        );
    }

    @DisplayName("여행 계획 항목은 Auditing 생성 시각과 필수 컬럼을 보존한다")
    @Test
    void planItemKeepsRequiredColumnsWithAuditing() {
        PlanItemEntity planItem = persistAndFlush(new PlanItemEntity("plan-1", 1L, 1, 1, null, 30));

        assertAll(
                () -> assertEquals("plan_items", PlanItemEntity.class.getAnnotation(Table.class).name()),
                () -> assertNotNull(planItem.getCreatedAt()),
                () -> assertColumn(PlanItemEntity.class, "planId", "plan_id", 128, false),
                () -> assertColumn(PlanItemEntity.class, "stayMinutes", "stay_minutes", 255, false)
        );
    }

    @DisplayName("DB가 할당하는 엔티티 ID는 IDENTITY 전략을 사용한다")
    @Test
    void generatedIdsUseIdentityStrategyForDatabaseAssignedEntities() throws Exception {
        assertAll(
                () -> assertEquals(GenerationType.IDENTITY,
                        AuthLogEntity.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy()),
                () -> assertEquals(GenerationType.IDENTITY,
                        MemberEntity.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy()),
                () -> assertEquals(GenerationType.IDENTITY,
                        NoticeEntity.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy())
        );
    }

    private <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static void assertColumn(Class<?> type, String fieldName, String expectedName, int expectedLength, boolean nullable) {
        Column column = column(type, fieldName);
        assertEquals(expectedName, column.name());
        assertEquals(expectedLength, column.length());
        assertEquals(nullable, column.nullable());
    }

    private static Column column(Class<?> type, String fieldName) {
        try {
            return declaredField(type, fieldName).getAnnotation(Column.class);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
    }

    private static Object field(Object target, String fieldName) {
        try {
            Field field = declaredField(target.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static Field declaredField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaAuditing(modifyOnCreate = false)
    @EnableTransactionManagement
    static class TestApplication {
        @Bean
        DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:jpa-entity-lifecycle;MODE=PostgreSQL;NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(BaseEntity.class.getPackageName());
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of(
                    "hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.dialect", "org.hibernate.dialect.H2Dialect"
            ));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }
}
