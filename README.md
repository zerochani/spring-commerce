# 🚀 풀스택 커머스 플랫폼 MVP (Spring Boot)
이 프로젝트는 Gemini Pro와 대화하며 만든 프로젝트입니다. 
네이버 쇼핑 API의 상품 데이터를 활용하여 구축한 커머스 플랫폼 백엔드 MVP 프로젝트입니다. DDD와 클린 아키텍처를 적용하여 확장성과 유지보수성을 높이는 데 중점을 두었으며, 소셜 로그인 및 JWT 기반의 인증 시스템을 구현했습니다.

## ✨ 주요 기능

- **외부 API 연동 및 데이터 구축**: 네이버 쇼핑 API를 호출하여 상품 정보를 검색하고, 자체 데이터베이스에 저장합니다.
- **상품 목록 조회 (커서 기반 페이지네이션)**: '더보기' 및 '무한 스크롤'에 최적화된 커서 기반 페이지네이션 API를 구현하여 효율적인 데이터 조회를 지원합니다.
- **소셜 로그인 (OAuth 2.0)**: Google, Naver, Kakao 계정을 사용한 간편 로그인 기능을 제공하며, 동일 이메일 사용자는 같은 회원으로 통합 관리합니다.
- **API 인증 (JWT)**: 로그인 성공 시 Access Token과 Refresh Token을 발급하여, 이후의 API 요청에 대한 사용자 인증 및 인가를 처리합니다.
- **(구현 예정) 주문 및 결제**: 장바구니, 주문 생성, 외부 결제 PG 연동 기능을 구현할 예정입니다.
- **(구현 예정) 분석 대시보드**: 관리자를 위한 매출 및 사용자 통계 대시보드 기능을 구현할 예정입니다.

## 🏛️ 아키텍처

본 프로젝트는 도메인 주도 설계(DDD) 및 클린 아키텍처 원칙을 적용하여 각 계층의 역할과 책임을 명확히 분리했습니다. 이를 통해 코드의 응집도를 높이고 결합도를 낮추어 유연한 구조를 지향합니다.

```
src
└── main
    └── java
        └── com/example/commerce_mvp
            ├── config/              // SecurityConfig, JwtConfig 등 설정 클래스
            ├── application/         // 서비스 로-직, DTO, UseCase
            ├── domain/              // 핵심 도메인 모델 (Entity, Repository 인터페이스)
            ├── infrastructure/      // 외부 시스템 연동 (DB 구현체, 외부 API 클라이언트)
            └── presentation/        // 외부 노출 계층 (Controller)
```

## 🛠️ 기술 스택

- **Backend**: `Java 17`, `Spring Boot`, `Spring Data JPA`, `Spring Security`
- **Database**: `MySQL`
- **Authentication**: `OAuth 2.0`, `JSON Web Token (JWT)`
- **Build Tool**: `Gradle`

## ⚙️ 시작하기

### Prerequisites

- `Java 17`
- `MySQL`
- `Gradle`

### Installation & Run

1.  **프로젝트 클론**
    ```bash
    git clone [https://github.com/your-username/commerce-mvp.git](https://github.com/your-username/commerce-mvp.git)
    cd commerce-mvp
    ```

2.  **`application.yml` 설정**
    `src/main/resources/` 경로의 `application.yml` 파일에 아래 내용을 참고하여 실제 값으로 채워주세요. (보안을 위해 환경 변수나 `git secret` 사용을 권장합니다.)

    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/commerce_db
        username: your_db_username
        password: ${DB_PASSWORD} # 환경 변수 사용 예시

      security:
        oauth2:
          client:
            registration:
              google:
                client-id: ${GOOGLE_CLIENT_ID}
                client-secret: ${GOOGLE_CLIENT_SECRET}
              naver:
                client-id: ${NAVER_LOGIN_CLIENT_ID}
                client-secret: ${NAVER_LOGIN_CLIENT_SECRET}
              kakao:
                client-id: ${KAKAO_CLIENT_ID}
    
    # 네이버 검색 API 설정
    naver:
      api:
        client-id: ${NAVER_SEARCH_CLIENT_ID}
        client-secret: ${NAVER_SEARCH_CLIENT_SECRET}

    # JWT 설정 (예시)
    jwt:
      secret: ${JWT_SECRET_KEY}
      access-token-expiration: 3600000 # 1시간
      refresh-token-expiration: 1209600000 # 2주
    ```

3.  **애플리케이션 실행**
    ```bash
    ./gradlew build
    java -jar build/libs/commerce_mvp-0.0.1-SNAPSHOT.jar
    ```
