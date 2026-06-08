package com.ssafy.enjoytrip.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainRecordContractTest {

    private static final List<RecordSpec> DOMAIN_RECORDS = List.of(
            new RecordSpec(
                    Attraction.class,
                    new String[]{"id", "title", "addr1", "addr2", "zipcode", "tel", "firstImage", "firstImage2", "readcount", "sidoCode", "gugunCode", "latitude", "longitude", "mlevel", "contentTypeId", "overview", "favoriteCount", "ratingAverage", "ratingCount", "tags", "favorited", "myRating"},
                    new Class<?>[]{Long.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Integer.class, Integer.class, Integer.class, Double.class, Double.class, String.class, String.class, String.class, int.class, double.class, int.class, List.class, boolean.class, Integer.class},
                    new Object[]{1L, "Beach", "addr1", "addr2", "12345", "051", "first.jpg", "second.jpg", 7, 6, 12, 35.1532, 129.1187, "6", "12", "overview", 0, 0.0d, 0, List.of(), false, null},
                    new Object[]{2L, "Mountain", "other1", "other2", "54321", "02", "other-first.jpg", "other-second.jpg", -1, 1, 2, -90.0, 180.0, "1", "14", "other overview", 5, 4.5d, 2, List.of(), true, 4}
            ),
            new RecordSpec(
                    BoardPost.class,
                    new String[]{"id", "title", "content", "author", "createdAt", "updatedAt"},
                    new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class},
                    new Object[]{"board-1", "Title", "Content", "author", "2026-05-15T09:00:00", "2026-05-15T10:00:00"},
                    new Object[]{"board-2", "Other title", "Other content", "other", "2026-05-16T09:00:00", "2026-05-16T10:00:00"}
            ),
            new RecordSpec(
                    ChargerItem.class,
                    new String[]{"statId", "statNm", "chgerId", "chgerType", "addr", "location", "lat", "lng", "useTime", "busiNm", "busiCall", "stat"},
                    new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class, Double.class, Double.class, String.class, String.class, String.class, String.class},
                    new Object[]{"ST-1", "Station", "01", "fast", "Seoul", "B1", 37.5665, 126.9780, "24h", "KEPCO", "1234", "available"},
                    new Object[]{"ST-2", "Station 2", "02", "slow", "Busan", "B2", -90.0, 180.0, "day", "Other", "5678", "busy"}
            ),
            new RecordSpec(
                    Hotplace.class,
                    new String[]{"id", "userId", "title", "type", "visitDate", "lat", "lng", "description", "photo", "createdAt"},
                    new Class<?>[]{String.class, String.class, String.class, String.class, String.class, Double.class, Double.class, String.class, String.class, String.class},
                    new Object[]{"hot-1", "user-1", "Cafe", "food", "2026-05-15", 35.0, 129.0, "nice", "photo.jpg", "2026-05-15T09:00:00"},
                    new Object[]{"hot-2", "user-2", "Park", "nature", "2026-05-16", -35.0, -129.0, "green", "park.jpg", "2026-05-16T09:00:00"}
            ),
            new RecordSpec(
                    Member.class,
                    new String[]{"userId", "name", "email", "password", "createdAt"},
                    new Class<?>[]{String.class, String.class, String.class, String.class, String.class},
                    new Object[]{"user-1", "Alice", "alice@example.com", "secret", "2026-05-15T09:00:00"},
                    new Object[]{"user-2", "Bob", "bob@example.com", "secret2", "2026-05-16T09:00:00"}
            ),
            new RecordSpec(
                    NewsItem.class,
                    new String[]{"id", "title", "link", "summary", "source", "publishedAt"},
                    new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class},
                    new Object[]{"news-1", "Title", "https://example.com/1", "Summary", "Source", "2026-05-15T09:00:00"},
                    new Object[]{"news-2", "Other", "https://example.com/2", "Other summary", "Other source", "2026-05-16T09:00:00"}
            ),
            new RecordSpec(
                    Notice.class,
                    new String[]{"id", "title", "content", "author", "createdAt", "updatedAt"},
                    new Class<?>[]{Long.class, String.class, String.class, String.class, String.class, String.class},
                    new Object[]{1L, "Notice", "Content", "admin", "2026-05-15T09:00:00", "2026-05-15T10:00:00"},
                    new Object[]{2L, "Other", "Other content", "operator", "2026-05-16T09:00:00", "2026-05-16T10:00:00"}
            ),
            new RecordSpec(
                    Point.class,
                    new String[]{"lat", "lng", "index"},
                    new Class<?>[]{double.class, double.class, int.class},
                    new Object[]{-90.0d, 180.0d, Integer.MIN_VALUE},
                    new Object[]{90.0d, -180.0d, Integer.MAX_VALUE}
            ),
            new RecordSpec(
                    TravelPlan.class,
                    new String[]{"id", "userId", "title", "startDate", "endDate", "budget", "note", "routeItemsJson", "createdAt"},
                    new Class<?>[]{String.class, String.class, String.class, String.class, String.class, int.class, String.class, String.class, String.class},
                    new Object[]{"plan-1", "user-1", "Trip", "2026-05-15", "2026-05-20", 0, "", "[]", "2026-05-15T09:00:00"},
                    new Object[]{"plan-2", "user-2", "Trip 2", "2026-06-15", "2026-06-20", -1, "note", "[{\"id\":1}]", "2026-06-15T09:00:00"}
            )
    );

    @Test
    void domainModelsExposeStableRecordComponents() {
        for (RecordSpec spec : DOMAIN_RECORDS) {
            assertAll(
                    spec.type().getSimpleName(),
                    () -> assertTrue(spec.type().isRecord()),
                    () -> assertTrue(Modifier.isFinal(spec.type().getModifiers())),
                    () -> assertArrayEquals(spec.componentNames(), componentNames(spec.type())),
                    () -> assertArrayEquals(spec.componentTypes(), componentTypes(spec.type()))
            );
        }
    }

    @Test
    void domainRecordsAreShallowlyImmutableAndDoNotExposeSetters() {
        for (RecordSpec spec : DOMAIN_RECORDS) {
            assertAll(
                    spec.type().getSimpleName(),
                    () -> assertTrue(Arrays.stream(spec.type().getDeclaredFields())
                            .filter(field -> !field.isSynthetic())
                            .allMatch(DomainRecordContractTest::isPrivateFinal)),
                    () -> assertFalse(Arrays.stream(spec.type().getMethods())
                            .map(Method::getName)
                            .anyMatch(name -> name.startsWith("set")))
            );
        }
    }

    @Test
    void canonicalConstructorsPreserveValuesAndGeneratedRecordMethods() throws Exception {
        for (RecordSpec spec : DOMAIN_RECORDS) {
            Object actual = construct(spec, spec.sampleValues());
            Object same = construct(spec, spec.sampleValues());
            Object different = construct(spec, spec.differentValues());

            assertAll(
                    spec.type().getSimpleName(),
                    () -> assertEquals(same, actual),
                    () -> assertEquals(same.hashCode(), actual.hashCode()),
                    () -> assertNotEquals(different, actual),
                    () -> assertTrue(actual.toString().startsWith(spec.type().getSimpleName() + "[")),
                    () -> assertAccessors(actual, spec.sampleValues())
            );
        }
    }

    @Test
    void canonicalConstructorsAllowNullReferenceComponentsAndPrimitiveEdgeValues() {
        for (RecordSpec spec : DOMAIN_RECORDS) {
            Object[] nullAndEdgeValues = Arrays.stream(spec.componentTypes())
                    .map(DomainRecordContractTest::nullOrPrimitiveEdgeValue)
                    .toArray();

            assertDoesNotThrow(
                    () -> construct(spec, nullAndEdgeValues),
                    spec.type().getSimpleName() + " should not add validation beyond record construction"
            );
        }
    }

    private static void assertAccessors(Object record, Object[] expectedValues) throws Exception {
        RecordComponent[] components = record.getClass().getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            assertEquals(expectedValues[i], components[i].getAccessor().invoke(record), components[i].getName());
        }
    }

    private static Object construct(RecordSpec spec, Object[] values) throws Exception {
        Constructor<?> constructor = spec.type().getDeclaredConstructor(spec.componentTypes());
        return constructor.newInstance(values);
    }

    private static boolean isPrivateFinal(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPrivate(modifiers) && Modifier.isFinal(modifiers);
    }

    private static String[] componentNames(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName)
                .toArray(String[]::new);
    }

    private static Class<?>[] componentTypes(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
    }

    private static Object nullOrPrimitiveEdgeValue(Class<?> type) {
        if (type == int.class) {
            return Integer.MIN_VALUE;
        }
        if (type == double.class) {
            return Double.NEGATIVE_INFINITY;
        }
        if (type == boolean.class) {
            return false;
        }
        return null;
    }

    private record RecordSpec(
            Class<?> type,
            String[] componentNames,
            Class<?>[] componentTypes,
            Object[] sampleValues,
            Object[] differentValues
    ) {
    }
}
