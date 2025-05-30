# Build stage
FROM eclipse-temurin:21 AS build

# 작업 디렉토리를 /app으로 설정
WORKDIR /app

# Gradle Wrapper 및 소스 코드 복사
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src

# 애플리케이션 빌드 (테스트는 패스)
RUN ./gradlew build -x test --no-daemon

# Production stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 변수 정의
ARG JAR_FILE=build/libs/Notice-BE-Spring-0.0.1-SNAPSHOT.jar

# 빌드된 JAR 파일 및 설정 파일 복사
COPY --from=build /app/${JAR_FILE} app.jar

# 시간 동기화
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone

EXPOSE 8080

# 로컬용
ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:/application.yml", "-Dspring.profiles.active=docker-prod", "/app/app.jar"]
