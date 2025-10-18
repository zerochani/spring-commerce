# 제품 요구사항 문서(PRD): Spring Commerce Platform MVP

-   **문서 버전**: 1.0
-   **작성일**: 2025-10-12
-   **프로젝트 상태**: 개발 요구사항 정의 완료

## 1. 개요 (Introduction)

본 문서는 Spring Boot 기반 커머스 플랫폼 백엔드 MVP(Minimum Viable Product) 개발에 필요한 기능적, 비기능적 요구사항을 정의합니다. 프로젝트의 핵심 목표는 **도메인 주도 설계(DDD)와 클린 아키텍처**를 적용하여 확장성, 테스트 용이성, 유지보수성이 높은 백엔드 시스템을 구축하는 것입니다.

이 문서는 개발팀(또는 코드 생성 AI)이 구현해야 할 기능 명세, 기술 제약 조건, 아키텍처 원칙을 명확히 전달하는 것을 목적으로 합니다.

---

## 2. 목표 및 성공 지표 (Goals & Success Metrics)

### 2.1. 비즈니스 목표
-   사용자가 소셜 계정을 통해 간편하게 가입하고 로그인할 수 있는 인증 시스템을 제공한다.
-   사용자가 상품 목록을 원활하게 탐색할 수 있는 조회 기능을 제공한다.
-   안정적인 API를 통해 클라이언트(웹/앱)에 일관된 데이터와 에러 응답을 제공한다.

### 2.2. 기술 목표
-   **계층 분리**: 클린 아키텍처 원칙에 따라 Presentation, Application, Domain, Infrastructure 계층을 명확히 분리하여 의존성 규칙을 준수한다.
-   **도메인 모델링**: 비즈니스 로직을 외부 기술과 분리된 순수한 Domain 계층에 집중시킨다.
-   **테스트 용이성**: 외부 환경(DB 등)에 의존하지 않는 독립적인 단위 테스트가 가능한 구조를 확보한다.
-   **보안성**: JWT와 OAuth 2.0 기반의 안전한 인증/인가 메커니즘을 구현한다.

### 2.3. 성공 지표
-   모든 API 엔드포인트는 정의된 요청/응답 형식과 HTTP 상태 코드를 준수한다.
-   Application 및 Domain 계층의 주요 비즈니스 로직에 대한 단위 테스트 커버리지를 확보한다.
-   정의된 모든 User Story와 기능 요구사항이 정상적으로 동작한다.

---

## 3. 사용자 스토리 (User Stories)

-   **(신규 사용자)** 로서, 나는 구글/네이버/카카오 계정을 이용해 간편하게 회원가입하고 싶다.
-   **(기존 사용자)** 로서, 나는 이전에 가입했던 소셜 계정이 아닌 다른 소셜 계정으로도 (이메일이 같다면) 내 계정에 로그인하고 싶다.
-   **(로그인한 사용자)** 로서, 나는 로그아웃하여 내 로그인 세션을 안전하게 종료하고 싶다.
-   **(사용자)** 로서, 나는 상품 목록 페이지에서 스크롤을 내릴 때 끊김 없이 다음 상품들을 계속 보고 싶다.
-   **(클라이언트 개발자)** 로서, 나는 API 호출이 실패했을 때 원인을 명확히 알 수 있는 표준화된 에러 응답을 받고 싶다.

---

## 4. 기능 요구사항 (Functional Requirements)

### FR-AUTH-01: 소셜 로그인 (OAuth 2.0)
-   시스템은 Google, Naver, Kakao OAuth 2.0을 이용한 사용자 인증을 지원해야 한다.
-   최초 소셜 로그인 시, 해당 소셜 계정의 이메일과 사용자 정보를 기반으로 시스템에 신규 회원 계정을 생성해야 한다.

### FR-AUTH-02: 계정 통합
-   **사용자 식별의 기준은 `email`로 한다.**
-   이미 시스템에 등록된 이메일로 다른 소셜 로그인을 시도할 경우, 신규 계정을 생성하는 대신 기존 계정에 새로운 소셜 제공자 정보를 연결(업데이트)해야 한다. UNIQUE 제약 조건 위반이 발생해서는 안 된다.

### FR-AUTH-03: JWT 기반 인증 (Stateless)
-   사용자 로그인 성공 시, 시스템은 **Access Token**과 **Refresh Token**을 발급해야 한다.
-   API 요청 시 `Authorization` 헤더의 Bearer 토큰(Access Token)을 검증하여 사용자를 인가해야 한다.
-   Access Token은 상대적으로 짧은 만료 시간(예: 30분)을 가져야 한다.
-   Refresh Token은 상대적으로 긴 만료 시간(예: 14일)을 가져야 한다.

### FR-AUTH-04: 안전한 토큰 관리 및 재발급
-   **Access Token**: 로그인 응답 시 JSON 본문에 포함하여 클라이언트에 전달한다.
-   **Refresh Token**: 보안 강화를 위해 **HttpOnly, Secure 속성을 가진 쿠키**에 담아 전달한다. XSS 공격을 방지하기 위함이다.
-   **Refresh Token 서버 측 저장**: 발급된 Refresh Token은 DB에 사용자 정보와 함께 저장되어야 한다. 이를 통해 서버에서 토큰을 추적하고 강제로 무효화할 수 있다.
-   **토큰 재발급**: Access Token이 만료되었을 때, 클라이언트가 유효한 Refresh Token을 쿠키에 담아 재발급 API를 호출하면 새로운 Access Token과 Refresh Token을 발급해야 한다.
-   **로그아웃**: 로그아웃 요청 시, 서버는 DB에 저장된 해당 사용자의 Refresh Token을 삭제하여 해당 토큰을 즉시 무효화해야 한다.

### FR-PRODUCT-01: 상품 목록 조회 (커서 기반 페이지네이션)
-   상품 목록 조회 API는 **커서 기반 페이지네이션(Cursor-based Pagination)**을 구현해야 한다.
-   클라이언트는 페이지 크기(`size`)와 마지막으로 조회된 상품의 ID(`cursorId`)를 파라미터로 전달한다.
-   `cursorId`가 없으면 첫 페이지(최신 상품)부터 조회한다.
-   `cursorId`가 있으면, 해당 ID보다 작은 ID를 가진 상품들을 ID 내림차순으로 `size`만큼 조회한다.
-   응답 결과에는 상품 목록과 함께 다음 페이지 조회를 위한 마지막 상품 ID(`nextCursor`) 정보를 포함해야 한다.

### FR-API-01: 전역 예외 처리 및 표준 응답
-   `@RestControllerAdvice`를 사용하여 전역 예외 처리기를 구현해야 한다.
-   모든 API 에러 응답은 아래와 같은 일관된 JSON 형식으로 반환되어야 한다.
    ```json
    {
      "errorCode": "ERROR_CODE",
      "message": "상세 에러 메시지"
    }
    ```
-   비즈니스 규칙 위반 시 `BusinessException`을 발생시키고, 사전에 정의된 `ErrorCode` 열거형(Enum)을 사용하여 에러 응답을 체계적으로 관리해야 한다.
-   인증 실패(401), 인가 실패(403), 잘못된 요청(400), 서버 내부 오류(500) 등 상황에 맞는 적절한 HTTP 상태 코드를 반환해야 한다.

### FR-API-02: 입력값 유효성 검사
-   모든 Controller의 DTO(Data Transfer Object)에 대해 Spring Validation (`@Valid`)을 적용해야 한다.
-   `@NotBlank`, `@Email`, `@Min`, `@Max` 등 어노테이션을 활용하여 핵심 입력값에 대한 유효성 검사를 수행해야 한다.
-   유효성 검사 실패 시, `MethodArgumentNotValidException`을 처리하여 `400 Bad Request` 상태 코드와 명확한 실패 사유를 표준 에러 형식으로 응답해야 한다.

---

## 5. 비기능 요구사항 (Non-Functional Requirements)

### NFR-ARCH-01: 클린 아키텍처
-   프로젝트는 반드시 **클린 아키텍처** 구조를 따라야 한다.
    -   **Domain**: 순수 Java/Kotlin 객체로 구성된 비즈니스 핵심 모델(Entity)과 규칙, 그리고 Repository 인터페이스를 포함한다. 외부 프레임워크에 대한 의존성이 없어야 한다.
    -   **Application**: UseCase를 구현하는 서비스 계층. Domain 객체를 사용하여 비즈니스 흐름을 제어한다. DTO 변환이 일어난다.
    -   **Presentation**: Spring MVC를 사용한 Controller 계층. HTTP 요청을 수신하고 응답을 반환한다.
    -   **Infrastructure**: 데이터베이스 연동(JPA Repository 구현체), 외부 API 클라이언트 등 외부 기술과의 통합을 담당한다.

### NFR-TEST-01: 독립적인 테스트 환경
-   테스트 코드는 로컬 환경이나 특정 환경 변수에 의존해서는 안 된다.
-   단위 테스트 실행 시, H2 같은 **인메모리 데이터베이스**를 사용하도록 `test` 프로파일을 구성해야 한다.
-   `Mockito`를 활용하여 외부 의존성(Repository 등)을 Mocking하고, 서비스 로직을 순수하게 테스트할 수 있어야 한다.

### NFR-DB-01: 데이터 모델링
-   데이터베이스 스키마는 아래 제공된 ERD를 준수하여 설계해야 한다.

![ERD Diagram](https://github.com/zerochani/spring-commerce/blob/main/docs/Erd.png)

---

## 6. 기술 스택 (Technology Stack)

| 구분 | 기술 | 버전 | 비고 |
| :--- | :--- | :--- | :--- |
| 언어 | Java | 17 | |
| 프레임워크 | Spring Boot | 3.x | |
| 빌드 도구 | Gradle | | |
| 데이터베이스 | MySQL | 8.0 | |
| 데이터 접근 | Spring Data JPA / Hibernate | | ORM 활용 |
| 인증/보안 | Spring Security, OAuth 2.0, JWT | 6.x | |
| 테스트 | JUnit 5, Mockito | | 단위 테스트 및 Mocking |

---

## 7. 향후 계획 (Future Scope / V2)

*다음에 해당하는 기능은 MVP 범위에 포함되지 않으며, 후속 버전에서 구현될 수 있다.*

-   단위 테스트 및 통합 테스트 코드 전체 작성
-   주문 및 결제 기능 구현
-   Redis를 활용한 캐싱 시스템 도입으로 조회 성능 최적화
-   Swagger/OpenAPI를 이용한 API 문서 자동화

---

## 8. 범위 외 (Out of Scope)

-   프론트엔드 UI 개발
-   결제 PG사 연동
-   상품 등록, 수정, 삭제 (관리자 기능)
-   배포 자동화 (CI/CD)