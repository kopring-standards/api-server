## 모듈 구조

```text
📦 buildSrc (의존성 관리 모듈)
┣ 📜 build.gradle.kts                    # buildSrc 모듈의 빌드 스크립트
┣ 📜 settings.gradle.kts                 # Gradle 버전 카탈로그 정의
┗ 📂 src/main/kotlin                     # 필요 시 커스텀 Gradle 플러그인 정의 가능
📦 demo-bootstrap (실행 모듈)
┣ 📜 build.gradle.kts                    # bootstrap 모듈에 적용할 의존성, 작업 등을 추가로 정의
┗ 📂 src/main/kotlin/com/demo/bootstrap  # 실행 클래스가 존재하는 위치
📦 demo-common (공통코드 모듈)
┣ 📜 build.gradle.kts                    # common 모듈에 적용할 의존성, 작업 등을 추가로 정의
┗ 📂 src/main/kotlin/com/demo/common     # 도메인 모듈이 사용할 공통 코드를 작성한 모듈
📦 demo-domain1 (도메인 모듈)
┣ 📜 build.gradle.kts                    # 도메인 모듈에 적용할 의존성, 작업 등을 추가로 정의
┗ 📂 src/main/kotlin/com/demo/domain1
    ┗ 📂 controller                      # 컨트롤러
    ┗ 📂 model                           # 도메인 모델
    ┗ 📂 persistence                     # 영속성
    ┗ 📂 service                         # 도메인 서비스
    ┗ 📂 usecase                         # API 사용사례
📦 demo-domain2 (도메인 모듈)
┣ 📜 build.gradle.kts                    # 도메인 모듈에 적용할 의존성, 작업 등을 추가로 정의
┗ 📂 src/main/kotlin/com/demo/domain2
    ┗ 📂 controller                      # 컨트롤러
    ┗ 📂 model                           # 도메인 모델
    ┗ 📂 persistence                     # 영속성
    ┗ 📂 service                         # 도메인 서비스
    ┗ 📂 usecase                         # API 사용사례
📦 gradle (버전 카탈로그)
    ┗ 📜 libs.versions.toml              # Gradle의 버전 카탈로그를 관리하는 파일
```

---

## buildSrc (의존성 관리 모듈)
프로젝트 전역에서 사용할 의존성을 관리하는 모듈이다.

libs.versions.toml을 통해 버전 카탈로그를 정의하고, 이를 활용하여 의존성을 관리한다.

- build.gradle.kts
  - buildSrc 모듈의 빌드 스크립트
- settings.gradle.kts
  - 빌드 스캔을 활성화, 버전 카탈로그 활성화
- src/main/kotlin
  - 필요 시 커스텀 Gradle 플러그인 추가 가능

---

## demo-bootstrap (실행 모듈)
프로젝트의 메인 실행 모듈로, Spring Boot Application 실행 클래스를 포함하여 애플리케이션을 구동하는 역할을 한다. 

- build.gradle.kts
  - 실행 모듈에 필요한 의존성과 작업을 정의
- src/main/kotlin/com/demo/bootstrap
  - 실행 클래스 위치

---

## demo-common (공통 코드 모듈)
여러 도메인 모듈에서 공통으로 사용할 코드를 포함하는 모듈이다.

공통 유틸리티(Cache, Kafka, etc ...), 예외 처리, 상수, DTO, 확장 함수 등을 제공한다.

- build.gradle.kts
  - 공통 모듈에 필요한 의존성을 정의
- src/main/kotlin/com/demo/common
  - 실제 공통 코드

---

## demo-domain (도메인 모듈)
비즈니스 로직을 담당하는 모듈

현재는 demo-bootstrap에서 실행되며 필요에 따라 각 도메인 모듈에 Main Application을 배치하여 MSA로 전환할 수 있다. demo-common 모듈을 참조한다.

- build.gradle.kts
  - domain 모듈의 의존성을 정의
- src/main/kotlin/com/demo/domain
  - controller - API 엔드포인트 제공
  - model - 도메인 모델
  - persistence - 데이터베이스 관련 코드 (Repository, DAO)
  - service - 도메인 비즈니스 로직
  - usecase - API 사용 사례 구현