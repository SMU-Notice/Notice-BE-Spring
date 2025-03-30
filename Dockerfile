# Build stage
FROM openjdk:21-jdk-alpine AS build

# 작업 디렉토리를 /app으로 설정
WORKDIR /app

# Gradle Wrapper 및 소스 코드 복사
COPY gradle gradle
COPY gradlew build.gradle settings.gradle gradle.properties ./
COPY src src

# 애플리케이션 빌드 (테스트는 패스)
RUN ./gradlew build -x test --no-daemon

# Production stage
# 실행용이므로 jdk 대신 jre 사용
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 변수 정의
ARG JAR_FILE=build/libs/Notice-BE-Spring-0.0.1-SNAPSHOT.jar
ARG YML_FILE=src/main/resources/application.yml
ARG DEV_YML_FILE=src/main/resources/application-docker-test.yml
#ARG PROD_YML_FILE=src/main/resources/application-prod.yml

COPY --from=build /app/${JAR_FILE} app.jar
COPY ${YML_FILE} /application.yml
COPY ${DEV_YML_FILE} /application-docker-test.yml
#COPY ${PROD_YML_FILE} /application-prod.yml

# 시간 동기화
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone

EXPOSE 8080

# 로컬용
ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:/application.yml", "-Dspring.profiles.active=docker-test", "/app/app.jar"]
# production 프로파일을 활성화하여 실행
#ENTRYPOINT ["java", "-jar", "-Dspring.config.location=file:/application-prod.yml", "-Dspring.profiles.active=prod", "/app/app.jar"]
