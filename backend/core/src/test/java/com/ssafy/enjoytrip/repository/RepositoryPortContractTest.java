package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.domain.ChargerItem;
import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.NewsItem;
import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryPortContractTest {

    private static final String CORE_PACKAGE = "com.ssafy.enjoytrip.";
    private static final Set<String> FORBIDDEN_LAYER_PACKAGES = Set.of(
            "com.ssafy.enjoytrip.app",
            "com.ssafy.enjoytrip.external",
            "com.ssafy.enjoytrip.storage"
    );

    @Test
    void repositoryPortsRemainInterfacesInCoreRepositoryPackage() {
        List.of(
                AttractionRepository.class,
                BoardRepository.class,
                ChargerRepository.class,
                HotplaceRepository.class,
                MemberRepository.class,
                NewsRepository.class,
                NoticeRepository.class,
                PlanRepository.class,
                WeatherRepository.class
        ).forEach(repositoryType -> assertAll(
                repositoryType.getSimpleName(),
                () -> assertTrue(repositoryType.isInterface()),
                () -> assertEquals("com.ssafy.enjoytrip.repository", repositoryType.getPackageName()),
                () -> assertTrue(Arrays.stream(repositoryType.getDeclaredMethods()).allMatch(RepositoryPortContractTest::isPublicAbstract))
        ));
    }

    @Test
    void boardRepositoryContractUsesBoardPostAndBooleanMutationResult() throws Exception {
        assertListReturn(BoardRepository.class.getMethod("findAll"), BoardPost.class);
        assertMethod(BoardRepository.class, "insert", void.class, BoardPost.class);
        assertMethod(BoardRepository.class, "update", boolean.class, BoardPost.class);
        assertMethod(BoardRepository.class, "delete", boolean.class, String.class);
    }

    @Test
    void attractionRepositoryContractUsesDbSearchInputsAndDomainModel() throws Exception {
        assertListReturn(AttractionRepository.class.getMethod(
                "search", AttractionSearchCondition.class), Attraction.class);
    }

    @Test
    void chargerRepositoryContractUsesDomainModelAndPaginationInputs() throws Exception {
        assertListReturn(ChargerRepository.class.getMethod(
                "findChargers", String.class, String.class, int.class, int.class), ChargerItem.class);
    }

    @Test
    void newsRepositoryContractUsesDomainModel() throws Exception {
        assertListReturn(NewsRepository.class.getMethod("findNews"), NewsItem.class);
    }

    @Test
    void weatherRepositoryContractUsesDomainModel() throws Exception {
        assertListReturn(WeatherRepository.class.getMethod("findWeatherBriefings"), WeatherSummary.class);
    }

    @Test
    void hotplaceRepositoryContractUsesHotplaceAndUserScopedLookup() throws Exception {
        assertListReturn(HotplaceRepository.class.getMethod("findAll"), Hotplace.class);
        assertListReturn(HotplaceRepository.class.getMethod("findByUser", String.class), Hotplace.class);
        assertMethod(HotplaceRepository.class, "insert", void.class, Hotplace.class);
        assertMethod(HotplaceRepository.class, "delete", boolean.class, String.class);
    }

    @Test
    void memberRepositoryContractKeepsAuthenticationSupportInCorePort() throws Exception {
        assertListReturn(MemberRepository.class.getMethod("findAll"), Member.class);
        assertMethod(MemberRepository.class, "findByUserId", Member.class, String.class);
        assertMethod(MemberRepository.class, "findByEmail", Member.class, String.class);
        assertMethod(MemberRepository.class, "findPassword", String.class, String.class, String.class);
        assertMethod(MemberRepository.class, "existsByUserId", boolean.class, String.class);
        assertMethod(MemberRepository.class, "existsByEmail", boolean.class, String.class);
        assertMethod(MemberRepository.class, "insert", void.class, Member.class);
        assertMethod(MemberRepository.class, "update", boolean.class, Member.class);
        assertMethod(MemberRepository.class, "delete", boolean.class, String.class);
        assertMethod(MemberRepository.class, "insertAuthLog", void.class, String.class, String.class);
    }

    @Test
    void noticeRepositoryContractUsesLongIdentifier() throws Exception {
        assertListReturn(NoticeRepository.class.getMethod("findAll"), Notice.class);
        assertMethod(NoticeRepository.class, "insert", void.class, Notice.class);
        assertMethod(NoticeRepository.class, "update", boolean.class, Notice.class);
        assertMethod(NoticeRepository.class, "delete", boolean.class, Long.class);
    }

    @Test
    void planRepositoryContractUsesTravelPlanAndUserScopedLookup() throws Exception {
        assertListReturn(PlanRepository.class.getMethod("findAll"), TravelPlan.class);
        assertListReturn(PlanRepository.class.getMethod("findByUser", String.class), TravelPlan.class);
        assertMethod(PlanRepository.class, "insert", void.class, TravelPlan.class);
        assertMethod(PlanRepository.class, "delete", boolean.class, String.class);
    }

    @Test
    void repositoryPortsDoNotReferenceAppExternalOrStorageTypes() {
        List.of(
                AttractionRepository.class,
                BoardRepository.class,
                ChargerRepository.class,
                HotplaceRepository.class,
                MemberRepository.class,
                NewsRepository.class,
                NoticeRepository.class,
                PlanRepository.class,
                WeatherRepository.class
        ).forEach(repositoryType -> {
            assertCorePackage(repositoryType);
            Arrays.stream(repositoryType.getDeclaredMethods()).forEach(method -> assertAll(
                    repositoryType.getSimpleName() + "." + method.getName(),
                    () -> assertAllowedType(method.getGenericReturnType()),
                    () -> Arrays.stream(method.getGenericParameterTypes()).forEach(RepositoryPortContractTest::assertAllowedType)
            ));
        });
    }

    private static void assertMethod(Class<?> repositoryType, String methodName, Class<?> returnType, Class<?>... parameterTypes) throws Exception {
        Method method = repositoryType.getMethod(methodName, parameterTypes);

        assertAll(
                repositoryType.getSimpleName() + "." + methodName,
                () -> assertEquals(returnType, method.getReturnType()),
                () -> assertTrue(Modifier.isPublic(method.getModifiers())),
                () -> assertTrue(Modifier.isAbstract(method.getModifiers()))
        );
    }

    private static boolean isPublicAbstract(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && (Modifier.isAbstract(modifiers) || method.isDefault());
    }

    private static void assertListReturn(Method method, Class<?> itemType) {
        assertEquals(List.class, method.getReturnType(), method.getName());
        Type genericReturnType = method.getGenericReturnType();
        assertTrue(genericReturnType instanceof ParameterizedType, method.getName());
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        assertEquals(itemType, parameterizedType.getActualTypeArguments()[0], method.getName());
    }

    private static void assertCorePackage(Class<?> type) {
        assertTrue(type.getName().startsWith(CORE_PACKAGE), type.getName());
    }

    private static void assertAllowedType(Type type) {
        String typeName = type.getTypeName();
        FORBIDDEN_LAYER_PACKAGES.forEach(forbiddenPackage ->
                assertTrue(!typeName.startsWith(forbiddenPackage) && !typeName.contains("<" + forbiddenPackage),
                        () -> "Forbidden layer type in core repository port: " + typeName)
        );
    }
}
