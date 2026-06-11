package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.domain.ChargerItem;
import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.CreateNoteCommand;
import com.ssafy.enjoytrip.domain.NearbyNotesCondition;
import com.ssafy.enjoytrip.domain.NewsItem;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.domain.UpdateNoteCommand;
import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.domain.NearbySearchCondition;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @DisplayName("저장소 포트는 core repository 패키지의 인터페이스로 유지된다")
    @Test
    void repositoryPortsRemainInterfacesInCoreRepositoryPackage() {
        List.of(
                AttractionRepository.class,
                AttractionPopularityRepository.class,
                BoardRepository.class,
                ChargerRepository.class,
                HotplaceRepository.class,
                MemberRepository.class,
                NewsRepository.class,
                NoteRepository.class,
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

    @DisplayName("게시글 저장소 계약은 BoardPost와 boolean 변경 결과를 사용한다")
    @Test
    void boardRepositoryContractUsesBoardPostAndBooleanMutationResult() throws Exception {
        assertListReturn(BoardRepository.class.getMethod("findAll"), BoardPost.class);
        assertMethod(BoardRepository.class, "insert", void.class, BoardPost.class);
        assertMethod(BoardRepository.class, "update", boolean.class, BoardPost.class);
        assertMethod(BoardRepository.class, "delete", boolean.class, String.class);
    }

    @DisplayName("관광지 저장소 계약은 DB 검색 입력과 도메인 모델을 사용한다")
    @Test
    void attractionRepositoryContractUsesDbSearchInputsAndDomainModel() throws Exception {
        assertListReturn(AttractionRepository.class.getMethod(
                "search", AttractionSearchCondition.class), Attraction.class);
        assertListReturn(AttractionRepository.class.getMethod(
                "findNearbyCandidates", NearbySearchCondition.class, String.class), NearbyAttractionCandidate.class);
        assertMethod(AttractionPopularityRepository.class, "findFavoriteCounts", Map.class, Collection.class);
    }

    @DisplayName("충전소 저장소 계약은 도메인 모델과 페이지 입력을 사용한다")
    @Test
    void chargerRepositoryContractUsesDomainModelAndPaginationInputs() throws Exception {
        assertListReturn(ChargerRepository.class.getMethod(
                "findChargers", String.class, String.class, int.class, int.class), ChargerItem.class);
    }

    @DisplayName("뉴스 저장소 계약은 도메인 모델을 사용한다")
    @Test
    void newsRepositoryContractUsesDomainModel() throws Exception {
        assertListReturn(NewsRepository.class.getMethod("findNews"), NewsItem.class);
    }

    @DisplayName("날씨 저장소 계약은 도메인 모델을 사용한다")
    @Test
    void weatherRepositoryContractUsesDomainModel() throws Exception {
        assertListReturn(WeatherRepository.class.getMethod("findWeatherBriefings"), WeatherSummary.class);
    }

    @DisplayName("핫플레이스 저장소 계약은 Hotplace와 사용자 범위 조회를 사용한다")
    @Test
    void hotplaceRepositoryContractUsesHotplaceAndUserScopedLookup() throws Exception {
        assertListReturn(HotplaceRepository.class.getMethod("findAll"), Hotplace.class);
        assertListReturn(HotplaceRepository.class.getMethod("findByUser", String.class), Hotplace.class);
        assertMethod(HotplaceRepository.class, "insert", void.class, Hotplace.class);
        assertMethod(HotplaceRepository.class, "delete", boolean.class, String.class);
    }

    @DisplayName("회원 저장소 계약은 인증 지원을 core 포트에 유지한다")
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

    @DisplayName("공지 저장소 계약은 Long 식별자를 사용한다")
    @Test
    void noticeRepositoryContractUsesLongIdentifier() throws Exception {
        assertListReturn(NoticeRepository.class.getMethod("findAll"), Notice.class);
        assertMethod(NoticeRepository.class, "insert", void.class, Notice.class);
        assertMethod(NoticeRepository.class, "update", boolean.class, Notice.class);
        assertMethod(NoticeRepository.class, "delete", boolean.class, Long.class);
    }

    @DisplayName("쪽지 저장소 계약은 소유권 변경과 주변 접근 가능 조회를 분리한다")
    @Test
    void noteRepositoryContractSeparatesOwnedMutationAndNearbyLookup() throws Exception {
        assertMethod(NoteRepository.class, "save", Note.class, CreateNoteCommand.class);
        assertMethod(NoteRepository.class, "findById", Optional.class, Long.class);
        assertMethod(NoteRepository.class, "updateOwned", Optional.class, UpdateNoteCommand.class);
        assertMethod(NoteRepository.class, "softDeleteOwned", boolean.class, Long.class, String.class);
        assertListReturn(NoteRepository.class.getMethod(
                "findNearbyAccessible", NearbyNotesCondition.class, String.class), Note.class);
    }

    @DisplayName("계획 저장소 계약은 TravelPlan과 사용자 범위 조회를 사용한다")
    @Test
    void planRepositoryContractUsesTravelPlanAndUserScopedLookup() throws Exception {
        assertListReturn(PlanRepository.class.getMethod("findAll"), TravelPlan.class);
        assertListReturn(PlanRepository.class.getMethod("findByUser", String.class), TravelPlan.class);
        assertMethod(PlanRepository.class, "insert", void.class, TravelPlan.class);
        assertMethod(PlanRepository.class, "delete", boolean.class, String.class);
    }

    @DisplayName("저장소 포트는 app, external, storage 타입을 참조하지 않는다")
    @Test
    void repositoryPortsDoNotReferenceAppExternalOrStorageTypes() {
        List.of(
                AttractionRepository.class,
                AttractionPopularityRepository.class,
                BoardRepository.class,
                ChargerRepository.class,
                HotplaceRepository.class,
                MemberRepository.class,
                NewsRepository.class,
                NoteRepository.class,
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
