# SMU Notice BE (Spring)
 SMU Notice의 Spring Boot 기반 서비스이며, 상명대의 게시판을 하나로 통합한 모든 공지와 이메일 구독 알림 서비스의 API를 제공합니다.

## 목차
1. [프로젝트 소개](#1.-프로젝트-소개)
2. [기술 스택](#2.-기술-스택)
3. [시작하기](#3.-시작하기)
4. [아키텍쳐 및 설계](#4.-아키텍쳐-및-설계)
5. [외부 링크](#5.-외부-링크)


## 1. 프로젝트 소개
- 목적: 교내 공지 통합 서비스를 운영하여 교내 소식 전달률, 교내 행사 참여율 증대
- 특징
  - 통합 게시판: 상명대학교의 46개에 달하는 게시판의 공지를 하나의 API로 통합 -> 사용자가 모든 공지를 한곳에서 확인
  - 쿼리 최적화: QueryDSL을 도입하여 동적 쿼리를 효율적으로 작성 -> 복잡한 검색 조건에 대한 성능 최적화
  - 보안 강화: Spring Security와 JWT를 활용 -> 사용자 인증 및 권한 관리를 안전하게 구현
### 기능
- 주요 기능
  - 통합 게시판 서비스: 상명대 내의 46개의 게시판을 하나로 통합한 '모든 공지'를 통해 간편하게 공지 확인
  - 이메일 알림 서비스: 사용자가 구독한 게시판에 새로운 게시물이 등록될 때 이메일 전송(RabbitMQ를 통한 에러 핸들링)
  
- 부가 기능
  - 월간 인기 게시물: 1달 기준 조회수 상위 7개 게시물 표시
  - 게시물 북마크: 게시물을 북마크하여 폴더로 관리
  - 교내 이벤트 지도: 학교에서 발생하는 행사 정보를 지도에 표시
  - 학교 주변 시위 정보 알림: 종로경찰서 관할 시위 정보 팝업

 
## 2. 기술 스택
| 분류 | 기술 | 설명 |
|:---:|:---:|:---|
| **백엔드** | Spring Boot | 마이크로서비스 개발에 최적화된 프레임워크로, 빠른 서버 구축을 위해 채택 |
| **언어** | Java | 안정성과 풍부한 라이브러리를 고려하여 주 개발 언어로 사용 |
| **쿼리** | QueryDSL | 타입 안전성을 보장하며 복잡한 동적 쿼리를 효율적으로 작성하기 위해 도입 |
| **데이터베이스** | MySQL | 오픈 소스 관계형 데이터베이스로, 프로젝트 규모에 적합하여 채택 |
| **ORM** | JPA (Hibernate) | 객체지향적인 방식으로 데이터베이스에 접근하여 개발 생산성 향상 |
| **메시지 큐** | RabbitMQ | 비동기 이메일 전송을 위해 도입. 서버의 효율적인 CPU 사용을 가능하게 함 |
| **캐시/DB** | Redis | | 게시물 정보를 캐싱하여 빠른 접근 속도를 제공하고, 이메일 전송 로직에 활용 |
| **인증** | Spring Security, JWT | 강력한 보안 기능과 상태 없는 인증 방식을 구현하기 위해 사용 |
| **이메일 서비스**|  SMTP | 이메일 전송 기능 구현을 위해 사용 |
|**CI/CD**|	GitHub Actions, Docker Hub | 코드 변경 시 빌드, 테스트, Docker 이미지 빌드 및 푸시를 자동화하여 개발 생산성 향상 |

## 3. 시작하기
프로젝트를 로컬 환경에서 실행하는 방법을 안내합니다.
### 1. 사전 준비
- Java 11 이상: OpenJDK 다운로드
- IDE: IntelliJ IDEA (권장) 또는 VS Code
- MySQL: 로컬 또는 원격 MySQL 서버 설치 및 설정
- RabbitMQ: 공식 웹사이트에서 설치 및 실행
- Redis: 공식 웹사이트에서 설치 및 실행

### 2. 프로젝트 설정
- 저장소를 포크(Fork)하고 클론합니다
  ```bash
  git clone https://github.com/SMU-Notice/Notice-BE-Spring.git
  ```
- src/main/resources/application.properties 파일을 열어 데이터베이스 연결 정보, JWT, 이메일, RabbitMQ, Redis 관련 설정을 입력합니다

### 3. 프로젝트 실행
- 터미널: 프로젝트 루트 디렉토리에서 아래 명령어를 실행합니다.
  ```bash
  ./gradlew bootRun
  ```

- IDE: IntelliJ IDEA에서 NoticeApplication.java 파일을 열고 main 메소드를 실행합니다.

## 4. 아키텍쳐 및 설계
- Application Layered Architecture: Controller, Service, Repository로 구성된 전형적인 3-Layered Architecture 적용
  ### 📂 **프로젝트 폴더 구조**
  ```bash
  src/main/java/com/example/noticebespring
  │── common
  │   ├── config        # 프로젝트 전역 설정 (예: Security, CORS, JWT 등)
  |   ├── filter        # JWT 인증 필터 및 예외 처리
  |   ├── helper        # Redis 캐싱
  │   ├── response      # 공통 응답 객체 및 예외 처리
  │   ├── util          # 유틸리티 클래스 (날짜 변환, 문자열 처리, Redis 등)
  │── controller        # REST API 엔드포인트 관리
  │── dto              # 데이터 전송 객체 (Request, Response DTO)
  │── entity           # JPA 엔티티 클래스 (DB 테이블 매핑)
  │── repository       # 데이터 접근 레이어 (Spring Data JPA)
  │── service          # 비즈니스 로직을 처리하는 서비스 클래스
  ```

- 이메일 전송 로직: 메시지 큐(RabbitMQ)를 활용한 비동기 이메일 전송 로직 설계. 전송 실패 시 재전송을 통한 에러 핸들링

- [데이터베이스 ERD](https://www.erdcloud.com/d/dgjmLydv3dJdfktAx): MySQL을 기반으로 사용자, 게시판, 이메일 구독, 북마크, 지도 이벤트 등 엔티티 설계

## 5. 외부 링크
- [배포된 서비스(SMU Notice)](https://preview.smu-notice.kr/)
- [API 명세(Swagger)](https://test.smu-notice.kr/swagger-ui/index.html)

    

