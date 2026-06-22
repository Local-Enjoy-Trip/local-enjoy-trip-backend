package com.ssafy.enjoytrip.core.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.core.api.web.dto.response.NotificationResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotificationUnreadStatusResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotificationsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PopularAttractionResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PopularAttractionsResponse;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiContractModuleBoundaryTest {
    private static final List<String> FORBIDDEN_INFRA_CONTRACT_TERMS = List.of(
            "outbox",
            "kafka",
            "debezium",
            "clickhouse"
    );

    @DisplayName("공개 알림/인기 관광지 응답 DTO는 인프라 내부 필드를 노출하지 않는다")
    @Test
    void apiResponseDtosDoNotExposeInfrastructureInternals() {
        assertRecordComponents(
                NotificationsResponse.class,
                List.of("notifications")
        );
        assertRecordComponents(
                NotificationResponse.class,
                List.of(
                        "id",
                        "type",
                        "referenceType",
                        "referenceId",
                        "payload",
                        "read",
                        "readAt",
                        "createdAt"
                )
        );
        assertRecordComponents(
                NotificationUnreadStatusResponse.class,
                List.of("hasUnread")
        );
        assertRecordComponents(
                PopularAttractionsResponse.class,
                List.of("attractions")
        );
        assertRecordComponents(
                PopularAttractionResponse.class,
                List.of(
                        "id",
                        "title",
                        "addr1",
                        "addr2",
                        "firstImage",
                        "readcount",
                        "latitude",
                        "longitude",
                        "contentTypeId",
                        "saveCount",
                        "saved",
                        "popularityCount",
                        "distanceMeters"
                )
        );
    }

    @DisplayName("settings.gradle는 app 모듈을 되살리지 않고 target 모듈만 포함한다")
    @Test
    void settingsGradleKeepsTargetModulesOnly() throws IOException {
        String settings = Files.readString(projectRoot().resolve("settings.gradle"));

        assertThat(settings)
                .contains(
                        "include 'core:core-api'",
                        "include 'core:core-enum'",
                        "include 'storage:db-core'",
                        "include 'external'",
                        "include 'batch'",
                        "include 'support:logging'",
                        "include 'support:monitoring'"
                )
                .doesNotContain("include 'app")
                .doesNotContain("include \"app");
    }

    @DisplayName("web/background job ingress는 서로의 계약과 storage mapper를 직접 소유하지 않는다")
    @Test
    void webAndBackgroundJobIngressKeepModuleBoundaries() throws IOException {
        Path sourceRoot = projectRoot().resolve("core/core-api/src/main/java");

        List<SourceFile> webSources = javaSources(sourceRoot.resolve(
                "com/ssafy/enjoytrip/core/api/web"
        ));
        List<SourceFile> backgroundJobSources = javaSources(sourceRoot.resolve(
                "com/ssafy/enjoytrip/core/api/worker"
        ));

        assertThat(webSources)
                .noneMatch(source -> source.contains("@KafkaListener"))
                .noneMatch(source -> source.contains("org.springframework.kafka.annotation"))
                .noneMatch(source -> source.contains("com.ssafy.enjoytrip.core.api.worker"))
                .noneMatch(SourceFile::importsStorageMapperOrRecord);
        assertThat(backgroundJobSources)
                .noneMatch(source -> source.contains("org.springframework.web.bind.annotation"))
                .noneMatch(source -> source.contains("com.ssafy.enjoytrip.core.api.web.dto"))
                .noneMatch(source -> source.contains("com.ssafy.enjoytrip.core.support.response"))
                .noneMatch(SourceFile::importsStorageMapperOrRecord);
    }

    @DisplayName("domain service는 web DTO 계약에 의존하지 않는다")
    @Test
    void domainServicesDoNotImportWebDtos() throws IOException {
        Path serviceRoot = projectRoot().resolve(
                "core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/service"
        );
        List<SourceFile> serviceSources = javaSources(serviceRoot);

        assertThat(serviceSources)
                .noneMatch(source -> source.contains("import com.ssafy.enjoytrip.core.api.web.dto"));
    }

    @DisplayName("external 모듈은 core-api/web/storage 타입에 의존하지 않는다")
    @Test
    void externalModuleDoesNotDependOnApplicationOrStorageContracts() throws IOException {
        List<SourceFile> externalSources = javaSources(projectRoot().resolve("external/src/main/java"));

        assertThat(externalSources)
                .noneMatch(source -> source.contains("import com.ssafy.enjoytrip.core."))
                .noneMatch(source -> source.contains("import com.ssafy.enjoytrip.storage."))
                .noneMatch(source -> source.contains("import com.ssafy.enjoytrip.core.api."));
    }


    @DisplayName("리뷰로 제거한 서비스 간 직접 의존을 다시 도입하지 않는다")
    @Test
    void reviewedServiceToServiceDependenciesStayRemoved() throws IOException {
        Path serviceRoot = projectRoot().resolve(
                "core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/service"
        );
        SourceFile attractionService = readSourceFile(serviceRoot.resolve("AttractionService.java"));
        SourceFile friendshipService = readSourceFile(serviceRoot.resolve("FriendshipService.java"));

        assertThat(attractionService.content())
                .doesNotContain("AttractionPopularityStatsService");
        assertThat(friendshipService.content())
                .doesNotContain("NotificationService");
    }

    @DisplayName("인증 주입 경계가 보장한 사용자 ID null 검증을 서비스 쓰기 유스케이스에 반복하지 않는다")
    @Test
    void authenticatedWriteUseCasesDoNotRepeatUserIdNullGuards() throws IOException {
        Path serviceRoot = projectRoot().resolve(
                "core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/service"
        );
        SourceFile attractionService = readSourceFile(serviceRoot.resolve("AttractionService.java"));

        assertThat(attractionService.content())
                .doesNotContain("if (userId == null) {\n            return;\n        }");
    }

    private static void assertRecordComponents(Class<? extends Record> recordType,
                                               List<String> expectedComponents) {
        List<String> actualComponents = Stream.of(recordType.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(actualComponents).containsExactlyElementsOf(expectedComponents);
        assertThat(actualComponents)
                .allSatisfy(component -> assertThat(component.toLowerCase())
                        .doesNotContain(FORBIDDEN_INFRA_CONTRACT_TERMS));
    }

    private static List<SourceFile> javaSources(Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(ApiContractModuleBoundaryTest::readSourceFile)
                    .toList();
        }
    }

    private static SourceFile readSourceFile(Path path) {
        try {
            return new SourceFile(path, Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("소스 파일을 읽을 수 없습니다: " + path, exception);
        }
    }

    private static Path projectRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();

        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }

        throw new IllegalStateException("settings.gradle 기준 프로젝트 루트를 찾을 수 없습니다.");
    }

    private record SourceFile(Path path, String content) {
        private boolean contains(String candidate) {
            return content.contains(candidate);
        }

        private boolean importsStorageMapperOrRecord() {
            return contains("import com.ssafy.enjoytrip.storage.db.core.mybatis")
                    || contains("import com.ssafy.enjoytrip.storage.db.core.model");
        }
    }
}
