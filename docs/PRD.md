# 📘 PRD.md — Spring Boot 기반 커머스 플랫폼

## 1. 🎯 프로젝트 개요

**프로젝트명:** Spring Commerce  
**목표:**  
DDD와 클린 아키텍처를 적용하여 **확장성**, **유지보수성**, **보안성**을 갖춘 커머스 백엔드 MVP를 개발한다.  
이 프로젝트는 **OAuth2.0 기반 로그인, JWT 인증, 상품 조회, 예외 처리, 데이터 검증** 등의 핵심 기능을 중심으로 설계된다.

---

## 2. 🧱 주요 기능 (Feature List)

| 기능 | 설명 | 우선순위 |
| :-- | :-- | :--: |
| **회원 인증 (Auth)** | OAuth2.0 소셜 로그인, JWT 발급/재발급, 로그아웃 처리 | ⭐⭐⭐⭐ |
| **상품 조회 (Product)** | 커서 기반 페이지네이션을 통한 상품 목록 조회 | ⭐⭐⭐⭐ |
| **입력값 검증 (Validation)** | DTO 단위 유효성 검증 및 에러 응답 표준화 | ⭐⭐⭐ |
| **전역 예외 처리 (Exception)** | GlobalExceptionHandler 및 ErrorCode 관리 체계 | ⭐⭐⭐⭐ |
| **테스트 환경 (Testing)** | H2 DB를 활용한 독립적인 테스트 환경 구성 | ⭐⭐ |
| **향후 추가 예정 (Future)** | 주문/결제 로직, Redis 캐시, Swagger 문서화 | ⭐ |

---

## 3. 🔐 기능 상세 설계

### ✅ 1) 회원 인증(Auth)

**목표:**  
- OAuth2.0 (Google, Kakao, Naver) 로그인 통합  
- JWT 기반의 Stateless 인증 시스템  
- Refresh Token 관리 및 재발급  

**요구사항:**
- 동일 이메일은 하나의 계정으로 통합
- 로그인 시 Access / Refresh Token 발급
- Refresh Token은 HttpOnly 쿠키로 전달
- DB에 Refresh Token 저장 (로그아웃 시 무효화)
- JWT 필터를 통해 요청 헤더의 토큰 검증 수행

**API 예시:**
| 메서드 | 엔드포인트 | 설명 |
| :-- | :-- | :-- |
| `GET` | `/oauth2/authorization/{provider}` | 소셜 로그인 진입점 |
| `POST` | `/auth/reissue` | Refresh Token 기반 Access Token 재발급 |
| `POST` | `/auth/logout` | Refresh Token 무효화 및 로그아웃 |

---

### 🛒 2) 상품 조회(Product)

**목표:**  
대규모 상품 데이터를 효율적으로 조회하기 위한 **커서 기반 페이지네이션(cursor-based pagination)** 구현.

**요구사항:**
- 마지막 상품 ID를 커서로 사용
- 일정 개수(`pageSize`)만큼 이후 상품을 조회
- 커서 값이 없으면 최신 상품부터 조회 시작
- 상품 데이터: id, name, price, imageUrl, description

**API 예시:**
| 메서드 | 엔드포인트 | 설명 |
| :-- | :-- | :-- |
| `GET` | `/products?cursor={id}&limit={n}` | 상품 목록 커서 기반 조회 |

---

### ⚙️ 3) 입력값 검증(Validation)

**목표:**  
DTO 단위 유효성 검증을 통해 데이터 무결성을 보장하고, 사용자 입력 오류를 명확히 전달.

**요구사항:**
- `@NotBlank`, `@Min`, `@Max` 등 어노테이션 활용  
- 실패 시 표준화된 에러 메시지(JSON) 반환  

---

### 🚨 4) 전역 예외 처리(Exception Handling)

**목표:**  
API 응답의 일관성 확보 및 디버깅 편의성 향상.

**요구사항:**
- `@ControllerAdvice` 기반 `GlobalExceptionHandler` 구현  
- `ErrorCode` Enum과 `BusinessException` 기반 구조  
- 예외 발생 시 `{ code, message, status }` 형태로 응답  

---

### 🧪 5) 테스트 환경(Testing)

**목표:**  
외부 의존성을 제거한 독립적인 테스트 환경 확보.

**요구사항:**
- 테스트용 `application.yml` 구성 (H2 DB)
- JUnit5, Mockito 기반 단위 테스트
- Repository/Service 단위 검증

---

## 4. 🧭 시스템 아키텍처

### 구조: **클린 아키텍처 기반 DDD 설계**

| 계층 | 주요 역할 |
| :-- | :-- |
| **Domain** | 엔티티, 도메인 서비스, 핵심 비즈니스 로직 |
| **Application** | 유스케이스 구현 (Service, DTO) |
| **Presentation** | REST API, Request/Response 처리 |
| **Infrastructure** | JPA Repository 구현체, 외부 API 연동 |

---

## 5. 🧩 기술 스택

| 구분 | 기술 |
| :-- | :-- |
| Backend | Java 17, Spring Boot 3.x |
| Database | MySQL 8.0 (테스트: H2) |
| ORM | JPA / Hibernate |
| Security | Spring Security 6.x, OAuth 2.0, JWT |
| Build | Gradle |
| Test | JUnit5, Mockito |
| Docs (추가예정) | Swagger / OpenAPI |
| Cache (추가예정) | Redis |

---

## 6. 🚧 개발 일정 (예상 로드맵)

| 단계 | 기간 | 내용 |
| :-- | :-- | :-- |
| **1단계** | 1주 | 프로젝트 초기 설정, ERD 설계, 아키텍처 설계 |
| **2단계** | 1~2주 | OAuth2.0 로그인 및 JWT 인증 구현 |
| **3단계** | 1주 | 상품 조회, 페이지네이션 로직 구현 |
| **4단계** | 1주 | 예외 처리, Validation, 테스트 환경 구축 |
| **5단계** | (추가예정) | 주문/결제, 캐싱, Swagger 문서화 |

---

## 7. 🧠 문제 해결 포인트 (AI 주석용)

> Cursor AI가 코드를 생성할 때 참고해야 할 설계 철학

- **DDD 원칙 준수**: 비즈니스 로직은 Domain 계층에 위치해야 함.  
- **의존성 역전**: Application → Domain, Infrastructure → Domain 구조를 유지.  
- **Stateless 인증**: 세션을 사용하지 않고 JWT 기반 인증만 사용.  
- **테스트 우선 접근**: 가능한 Service 계층부터 단위 테스트 작성.  
- **에러 일관성**: 모든 예외는 GlobalExceptionHandler를 통해 통합 관리.

---

## 8. 📈 기대 효과

- 구조적 확장성 확보 (도메인 중심 설계)  
- 서비스 품질 향상 (보안, 검증, 에러 관리)  
- 실무 수준의 인증 및 예외 처리 로직 구축  
- AI 코딩 워크플로우와 결합 시 개발 효율성 극대화

---

## 9. 🔮 향후 확장 계획

1. 주문 / 결제 서비스 추가 (Order, Payment 도메인)  
2. Redis 캐시 및 상품 조회 성능 개선  
3. Swagger/OpenAPI 기반 자동 문서화  
4. Docker Compose 기반 로컬 배포 환경 구성  

