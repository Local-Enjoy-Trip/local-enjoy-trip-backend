package com.ssafy.enjoytrip.core.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SSAFY EnjoyTrip API",
                version = "1.0.0",
                description = """
                        SSAFY EnjoyTrip backend API 문서입니다.

                        모든 응답은 기본적으로 `success`, `data`, `error` 필드를 가진 공통 응답 envelope로 반환됩니다.
                        인증이 필요한 API는 우측 상단 Authorize 버튼에 로그인 API에서 받은 JWT accessToken을 Bearer 토큰으로 입력하면 됩니다.
                        """,
                contact = @Contact(name = "SSAFY EnjoyTrip Team"),
                license = @License(name = "MIT", url = "https://opensource.org/license/mit")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server")
        },
        tags = {
                @Tag(name = "Health", description = "애플리케이션과 DB 연결 상태 확인 API"),
                @Tag(name = "Members", description = "회원 가입, 로그인, 내 정보, 회원 관리 API"),
                @Tag(name = "Attractions", description = "관광지 검색 API"),
                @Tag(name = "Route", description = "여행 경로 최적화 API")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT access token. 예: Bearer eyJhbGciOiJIUzI1NiJ9..."
)
class OpenApiConfig {
}
