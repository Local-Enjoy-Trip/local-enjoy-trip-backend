package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.storage.entity.AuthLogEntity;
import com.ssafy.enjoytrip.storage.entity.BoardPostEntity;
import com.ssafy.enjoytrip.storage.entity.HotplaceEntity;
import com.ssafy.enjoytrip.storage.entity.MemberEntity;
import com.ssafy.enjoytrip.storage.entity.NoticeEntity;
import com.ssafy.enjoytrip.storage.entity.TravelPlanEntity;
import com.ssafy.enjoytrip.storage.jpa.AuthLogJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.BoardPostJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.HotplaceJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.MemberJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.NoticeJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.PlanItemJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.TravelPlanJpaRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageRepositoryAdapterTest {
    @Test
    void boardAdapterMapsFindAllInNewestFirstOrderAndBlankUpdatedAt() {
        BoardFakeJpaRepository fake = new BoardFakeJpaRepository();
        fake.add(board("board-old", "Old", "old content", "author", "2026-05-15T09:00:00"));
        fake.add(board("board-new", "New", "new content", "author", "2026-05-15T10:00:00"));
        BoardStorageRepository repository = new BoardStorageRepository(fake.proxy());

        List<BoardPost> found = repository.findAll();

        assertAll(
                () -> assertEquals(List.of("board-new", "board-old"), found.stream().map(BoardPost::id).toList()),
                () -> assertTrue(found.getFirst().createdAt().contains("2026-05-15T10:00")),
                () -> assertEquals("", found.getFirst().updatedAt())
        );
    }

    @Test
    void boardAdapterInsertsUpdatesDeletesAndReportsMissingRows() {
        BoardFakeJpaRepository fake = new BoardFakeJpaRepository();
        BoardStorageRepository repository = new BoardStorageRepository(fake.proxy());

        repository.insert(new BoardPost("board-1", "Title", "content", "author", "", ""));
        boolean updated = repository.update(new BoardPost("board-1", "Updated", "updated content", "ignored", "", ""));

        BoardPostEntity saved = fake.rows.get("board-1");
        assertAll(
                () -> assertTrue(updated),
                () -> assertEquals("Updated", saved.getTitle()),
                () -> assertEquals("updated content", saved.getContent()),
                () -> assertEquals("author", saved.getAuthor()),
                () -> assertFalse(repository.update(new BoardPost("missing", "title", "content", "author", "", ""))),
                () -> assertFalse(repository.delete("missing")),
                () -> assertTrue(repository.delete("board-1")),
                () -> assertFalse(fake.rows.containsKey("board-1"))
        );
    }

    @Test
    void memberAdapterMapsLookupsAndPartialUpdateRules() {
        MemberFakeJpaRepository memberFake = new MemberFakeJpaRepository();
        AuthLogFakeJpaRepository authFake = new AuthLogFakeJpaRepository();
        MemberStorageRepository repository = new MemberStorageRepository(memberFake.proxy(), authFake.proxy());

        repository.insert(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", ""));

        assertAll(
                () -> assertTrue(repository.existsByUserId("ssafy")),
                () -> assertTrue(repository.existsByEmail("ssafy@example.com")),
                () -> assertEquals("secret", repository.findPassword("ssafy", "ssafy@example.com")),
                () -> assertNull(repository.findPassword("ssafy", "other@example.com")),
                () -> assertNull(repository.findByUserId("missing")),
                () -> assertNull(repository.findByEmail("missing@example.com")),
                () -> assertEquals("SSAFY", repository.findByUserId("ssafy").name())
        );

        assertTrue(repository.update(new Member("ssafy", "", "updated@example.com", null, "")));
        MemberEntity updated = memberFake.byUserId.get("ssafy");
        repository.insertAuthLog("ssafy", "LOGIN");

        assertAll(
                () -> assertEquals("SSAFY", updated.getName()),
                () -> assertEquals("updated@example.com", updated.getEmail()),
                () -> assertEquals("secret", updated.getPassword()),
                () -> assertNotNull(field(updated, "updatedAt")),
                () -> assertFalse(repository.update(new Member("missing", "Name", "email@example.com", "pw", ""))),
                () -> assertEquals(1, authFake.saved.size()),
                () -> assertFalse(repository.delete("missing")),
                () -> assertTrue(repository.delete("ssafy")),
                () -> assertFalse(memberFake.byUserId.containsKey("ssafy"))
        );
    }

    @Test
    void memberAdapterPropagatesDuplicateUserIdFailureFromJpaRepository() {
        MemberFakeJpaRepository fake = new MemberFakeJpaRepository();
        MemberStorageRepository repository = new MemberStorageRepository(fake.proxy(), new AuthLogFakeJpaRepository().proxy());

        repository.insert(new Member("duplicate", "One", "one@example.com", "secret", ""));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.insert(new Member("duplicate", "Two", "two@example.com", "secret", ""))
        );
    }

    @Test
    void noticeAdapterMapsGeneratedIdsUpdateDeleteAndMissingRows() {
        NoticeFakeJpaRepository fake = new NoticeFakeJpaRepository();
        NoticeStorageRepository repository = new NoticeStorageRepository(fake.proxy());

        repository.insert(new Notice(null, "Title", "content", "admin", "", ""));
        NoticeEntity saved = fake.rows.values().iterator().next();

        assertAll(
                () -> assertEquals(saved.getId(), repository.findAll().getFirst().id()),
                () -> assertEquals("Title", repository.findAll().getFirst().title()),
                () -> assertEquals("", repository.findAll().getFirst().updatedAt()),
                () -> assertTrue(repository.update(new Notice(saved.getId(), "Updated", "updated content", "ignored", "", ""))),
                () -> assertEquals("Updated", saved.getTitle()),
                () -> assertEquals("updated content", saved.getContent()),
                () -> assertEquals("admin", saved.getAuthor()),
                () -> assertFalse(repository.update(new Notice(-1L, "Missing", "content", "author", "", ""))),
                () -> assertFalse(repository.delete(-1L)),
                () -> assertTrue(repository.delete(saved.getId())),
                () -> assertFalse(fake.rows.containsKey(saved.getId()))
        );
    }

    @Test
    void hotplaceAdapterFindsAllFiltersByUserAndPreservesNullableFields() {
        HotplaceFakeJpaRepository fake = new HotplaceFakeJpaRepository();
        fake.add(hotplace("hot-1", "ssafy", "Old", "2026-05-15T09:00:00", "description", null));
        fake.add(hotplace("hot-2", "other", "Other", "2026-05-15T10:00:00", null, "photo"));
        fake.add(hotplace("hot-3", "ssafy", "New", "2026-05-15T11:00:00", "new", "photo"));
        HotplaceStorageRepository repository = new HotplaceStorageRepository(fake.proxy());

        List<Hotplace> all = repository.findAll();

        assertAll(
                () -> assertEquals(List.of("hot-3", "hot-2", "hot-1"), all.stream().map(Hotplace::id).toList()),
                () -> assertEquals(List.of("hot-3", "hot-1"), repository.findByUser("ssafy").stream().map(Hotplace::id).toList()),
                () -> assertTrue(repository.findByUser("missing").isEmpty()),
                () -> assertNull(all.get(1).description()),
                () -> assertEquals("photo", all.get(1).photo())
        );
    }

    @Test
    void hotplaceAdapterInsertsDeletesAndReportsMissingRows() {
        HotplaceFakeJpaRepository fake = new HotplaceFakeJpaRepository();
        HotplaceStorageRepository repository = new HotplaceStorageRepository(fake.proxy());

        repository.insert(new Hotplace("hot-1", "ssafy", "Title", "cafe", "2026-05-15",
                37.5, 127.0, "description", "photo", ""));

        assertAll(
                () -> assertEquals("ssafy", fake.rows.get("hot-1").getUserId()),
                () -> assertFalse(repository.delete("missing")),
                () -> assertTrue(repository.delete("hot-1")),
                () -> assertFalse(fake.rows.containsKey("hot-1"))
        );
    }

    @Test
    void planAdapterFindsAllFiltersByUserAndPreservesNullableRouteData() {
        PlanFakeJpaRepository fake = new PlanFakeJpaRepository();
        fake.add(plan("plan-1", "ssafy", "Old", "2026-05-15T09:00:00", null, "[]"));
        fake.add(plan("plan-2", "other", "Other", "2026-05-15T10:00:00", "note", null));
        fake.add(plan("plan-3", "ssafy", "New", "2026-05-15T11:00:00", "new note", "[{\"id\":1}]"));
        PlanStorageRepository repository = planRepository(fake);

        List<TravelPlan> all = repository.findAll();

        assertAll(
                () -> assertEquals(List.of("plan-3", "plan-2", "plan-1"), all.stream().map(TravelPlan::id).toList()),
                () -> assertEquals(List.of("plan-3", "plan-1"), repository.findByUser("ssafy").stream().map(TravelPlan::id).toList()),
                () -> assertTrue(repository.findByUser("missing").isEmpty()),
                () -> assertEquals("note", all.get(1).note()),
                () -> assertNull(all.get(1).routeItemsJson())
        );
    }

    @Test
    void planAdapterInsertsDeletesAndReportsMissingRows() {
        PlanFakeJpaRepository fake = new PlanFakeJpaRepository();
        PlanStorageRepository repository = planRepository(fake);

        repository.insert(new TravelPlan("plan-1", "ssafy", "Trip", "2026-05-15", "2026-05-16",
                1000, "note", "[]", ""));

        assertAll(
                () -> assertEquals("ssafy", fake.rows.get("plan-1").getUserId()),
                () -> assertFalse(repository.delete("missing")),
                () -> assertTrue(repository.delete("plan-1")),
                () -> assertFalse(fake.rows.containsKey("plan-1"))
        );
    }

    private static BoardPostEntity board(String id, String title, String content, String author, String createdAt) {
        BoardPostEntity entity = new BoardPostEntity(id, title, content, author);
        setField(entity, "createdAt", LocalDateTime.parse(createdAt));
        return entity;
    }

    private static HotplaceEntity hotplace(String id, String userId, String title, String createdAt,
                                           String description, String photo) {
        HotplaceEntity entity = new HotplaceEntity(id, userId, title, "type", "2026-05-15",
                37.5, 127.0, description, photo);
        setField(entity, "createdAt", LocalDateTime.parse(createdAt));
        return entity;
    }

    private static TravelPlanEntity plan(String id, String userId, String title, String createdAt,
                                         String note, String routeItemsJson) {
        TravelPlanEntity entity = new TravelPlanEntity(id, userId, title, "2026-05-15",
                "2026-05-16", 1000, note, routeItemsJson);
        setField(entity, "createdAt", LocalDateTime.parse(createdAt));
        return entity;
    }

    private static Object field(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static PlanStorageRepository planRepository(PlanFakeJpaRepository fake) {
        return new PlanStorageRepository(
                fake.proxy(),
                new PlanItemFakeJpaRepository().proxy(),
                proxy(DSLContext.class, new RepositoryInvocationHandler() {
                    @Override
                    protected Object handle(String methodName, Object[] args) {
                        throw new UnsupportedOperationException(methodName);
                    }
                })
        );
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private abstract static class RepositoryInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" -> getClass().getSimpleName();
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> handle(method.getName(), defaultArgs(args));
            };
        }

        private static Object[] defaultArgs(Object[] args) {
            if (args == null) {
                return new Object[0];
            }
            return args;
        }

        protected abstract Object handle(String methodName, Object[] args);
    }

    private static class BoardFakeJpaRepository extends RepositoryInvocationHandler {
        private final Map<String, BoardPostEntity> rows = new LinkedHashMap<>();

        BoardPostJpaRepository proxy() {
            return StorageRepositoryAdapterTest.proxy(BoardPostJpaRepository.class, this);
        }

        void add(BoardPostEntity entity) {
            rows.put(entity.getId(), entity);
        }

        @Override
        protected Object handle(String methodName, Object[] args) {
            return switch (methodName) {
                case "findAllByOrderByCreatedAtDesc" -> newest(rows.values());
                case "save" -> {
                    BoardPostEntity entity = (BoardPostEntity) args[0];
                    if (entity.getCreatedAt() == null) {
                        setField(entity, "createdAt", LocalDateTime.now());
                    }
                    rows.put(entity.getId(), entity);
                    yield entity;
                }
                case "findById" -> Optional.ofNullable(rows.get((String) args[0]));
                case "existsById" -> rows.containsKey((String) args[0]);
                case "deleteById" -> rows.remove((String) args[0]);
                default -> throw new UnsupportedOperationException(methodName);
            };
        }
    }

    private static class MemberFakeJpaRepository extends RepositoryInvocationHandler {
        private final Map<String, MemberEntity> byUserId = new LinkedHashMap<>();

        MemberJpaRepository proxy() {
            return StorageRepositoryAdapterTest.proxy(MemberJpaRepository.class, this);
        }

        @Override
        protected Object handle(String methodName, Object[] args) {
            return switch (methodName) {
                case "findAllByOrderByCreatedAtDesc" -> newest(byUserId.values());
                case "save" -> {
                    MemberEntity entity = (MemberEntity) args[0];
                    if (byUserId.containsKey(entity.getUserId())) {
                        throw new DataIntegrityViolationException("duplicate user_id");
                    }
                    if (entity.getCreatedAt() == null) {
                        setField(entity, "createdAt", LocalDateTime.now());
                    }
                    byUserId.put(entity.getUserId(), entity);
                    yield entity;
                }
                case "findByUserId" -> Optional.ofNullable(byUserId.get((String) args[0]));
                case "findByEmail" -> byUserId.values().stream()
                        .filter(member -> member.getEmail().equals(args[0]))
                        .findFirst();
                case "findByUserIdAndEmail" -> byUserId.values().stream()
                        .filter(member -> member.getUserId().equals(args[0]) && member.getEmail().equals(args[1]))
                        .findFirst();
                case "existsByUserId" -> byUserId.containsKey((String) args[0]);
                case "existsByEmail" -> byUserId.values().stream()
                        .anyMatch(member -> member.getEmail().equals(args[0]));
                case "deleteByUserId" -> byUserId.remove((String) args[0]);
                default -> throw new UnsupportedOperationException(methodName);
            };
        }
    }

    private static class AuthLogFakeJpaRepository extends RepositoryInvocationHandler {
        private final List<AuthLogEntity> saved = new ArrayList<>();

        AuthLogJpaRepository proxy() {
            return StorageRepositoryAdapterTest.proxy(AuthLogJpaRepository.class, this);
        }

        @Override
        protected Object handle(String methodName, Object[] args) {
            if ("save".equals(methodName)) {
                AuthLogEntity entity = (AuthLogEntity) args[0];
                setField(entity, "loggedAt", LocalDateTime.now());
                saved.add(entity);
                return entity;
            }
            throw new UnsupportedOperationException(methodName);
        }
    }

    private static class NoticeFakeJpaRepository extends RepositoryInvocationHandler {
        private final Map<Long, NoticeEntity> rows = new LinkedHashMap<>();
        private long nextId = 1L;

        NoticeJpaRepository proxy() {
            return StorageRepositoryAdapterTest.proxy(NoticeJpaRepository.class, this);
        }

        @Override
        protected Object handle(String methodName, Object[] args) {
            return switch (methodName) {
                case "findAllByOrderByCreatedAtDesc" -> newest(rows.values());
                case "save" -> {
                    NoticeEntity entity = (NoticeEntity) args[0];
                    setField(entity, "id", nextId++);
                    setField(entity, "createdAt", LocalDateTime.now());
                    rows.put(entity.getId(), entity);
                    yield entity;
                }
                case "findById" -> Optional.ofNullable(rows.get((Long) args[0]));
                case "existsById" -> rows.containsKey((Long) args[0]);
                case "deleteById" -> rows.remove((Long) args[0]);
                default -> throw new UnsupportedOperationException(methodName);
            };
        }
    }

    private static class HotplaceFakeJpaRepository extends RepositoryInvocationHandler {
        private final Map<String, HotplaceEntity> rows = new LinkedHashMap<>();

        HotplaceJpaRepository proxy() {
            return StorageRepositoryAdapterTest.proxy(HotplaceJpaRepository.class, this);
        }

        void add(HotplaceEntity entity) {
            rows.put(entity.getId(), entity);
        }

        @Override
        protected Object handle(String methodName, Object[] args) {
            return switch (methodName) {
                case "findAllByOrderByCreatedAtDesc" -> newest(rows.values());
                case "findByUserIdOrderByCreatedAtDesc" -> newest(rows.values().stream()
                        .filter(entity -> entity.getUserId().equals(args[0]))
                        .toList());
                case "save" -> {
                    HotplaceEntity entity = (HotplaceEntity) args[0];
                    if (entity.getCreatedAt() == null) {
                        setField(entity, "createdAt", LocalDateTime.now());
                    }
                    rows.put(entity.getId(), entity);
                    yield entity;
                }
                case "existsById" -> rows.containsKey((String) args[0]);
                case "deleteById" -> rows.remove((String) args[0]);
                default -> throw new UnsupportedOperationException(methodName);
            };
        }
    }

    private static class PlanFakeJpaRepository extends RepositoryInvocationHandler {
        private final Map<String, TravelPlanEntity> rows = new LinkedHashMap<>();

        TravelPlanJpaRepository proxy() {
            return StorageRepositoryAdapterTest.proxy(TravelPlanJpaRepository.class, this);
        }

        void add(TravelPlanEntity entity) {
            rows.put(entity.getId(), entity);
        }

        @Override
        protected Object handle(String methodName, Object[] args) {
            return switch (methodName) {
                case "findAllByOrderByCreatedAtDesc" -> newest(rows.values());
                case "findByUserIdOrderByCreatedAtDesc" -> newest(rows.values().stream()
                        .filter(entity -> entity.getUserId().equals(args[0]))
                        .toList());
                case "save" -> {
                    TravelPlanEntity entity = (TravelPlanEntity) args[0];
                    if (entity.getCreatedAt() == null) {
                        setField(entity, "createdAt", LocalDateTime.now());
                    }
                    rows.put(entity.getId(), entity);
                    yield entity;
                }
                case "existsById" -> rows.containsKey((String) args[0]);
                case "deleteById" -> rows.remove((String) args[0]);
                default -> throw new UnsupportedOperationException(methodName);
            };
        }
    }

    private static class PlanItemFakeJpaRepository extends RepositoryInvocationHandler {
        PlanItemJpaRepository proxy() {
            return StorageRepositoryAdapterTest.proxy(PlanItemJpaRepository.class, this);
        }

        @Override
        protected Object handle(String methodName, Object[] args) {
            return switch (methodName) {
                case "findByPlanIdOrderByPositionAsc" -> List.of();
                case "deleteByPlanId", "flush" -> null;
                default -> throw new UnsupportedOperationException(methodName);
            };
        }
    }

    private static <T> List<T> newest(Collection<T> rows) {
        return rows.stream()
                .sorted(Comparator.comparing(row -> (LocalDateTime) field(row, "createdAt"), Comparator.reverseOrder()))
                .toList();
    }
}
