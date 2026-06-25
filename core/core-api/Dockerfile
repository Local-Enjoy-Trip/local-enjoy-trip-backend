FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Timezone 서울 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# CI 단계에서 빌드된 core-api 모듈의 JAR 파일을 이미지 내부로 복사
# (plain.jar를 제외한 실행 가능한 executable jar를 가져옵니다.)
COPY core/core-api/core-api-*.jar app.jar

# 외부 노출용 API 포트
EXPOSE 8080

# 컨테이너 실행 명령어 (JVM 튜닝 옵션 등을 받을 수 있도록 java -jar app.jar 구조로 실행)
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
