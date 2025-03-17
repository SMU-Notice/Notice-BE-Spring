
### 📂 **프로젝트 폴더 구조**

```bash
src/main/java/com/example/noticebespring
│── common
│   ├── config        # 프로젝트 전역 설정 (예: Security, CORS, JWT 등)
│   ├── response      # 공통 응답 객체 및 예외 처리
│   ├── util          # 유틸리티 클래스 (날짜 변환, 문자열 처리, Redis 등)
│── controller        # REST API 엔드포인트 관리
│── dto              # 데이터 전송 객체 (Request, Response DTO)
│── entity           # JPA 엔티티 클래스 (DB 테이블 매핑)
│── repository       # 데이터 접근 레이어 (Spring Data JPA)
│── service          # 비즈니스 로직을 처리하는 서비스 클래스
```
<br>
