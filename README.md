---

### 🌿 Git 브랜치 네이밍 가이드

팀 협업 시 일관된 브랜치 네이밍으로 관리 효율성을 높이기 위해 아래 규칙을 따릅니다.

---

#### ✅ 브랜치 네이밍 규칙

`<브랜치타입>/<도메인>/<기능 또는 작업내용>`

- `브랜치타입`: 작업 목적에 따른 구분 (`feature`, `fix`, `hotfix`, `refactor`, `chore` 등)  
- `도메인`: 기능이 속하는 모듈 또는 영역 (`product`, `order`, `review` 등)  
- `기능명`: 구현하거나 수정할 기능 명칭 (`create`, `update`, `option-add` 등)

---

#### ✅ 브랜치 타입 목록

| 타입       | 설명                             |
|------------|----------------------------------|
| `feature`  | 새로운 기능 개발 시              |
| `fix`      | 버그 수정 시                     |
| `hotfix`   | 운영 중 긴급 패치 필요 시        |
| `refactor` | 코드 리팩토링 (기능 변화 없음)   |
| `chore`    | 빌드 설정, 문서 등 잡일 변경 시  |

---

#### ✅ 브랜치 이름 예시

| 브랜치 이름                    | 설명                       |
|-------------------------------|----------------------------|
| `feature/product/create`      | 상품 등록 기능 개발        |
| `feature/product/option-add`  | 상품 옵션 추가 기능        |
| `feature/order/create`        | 주문 생성 기능             |
| `fix/order/cancel-error`      | 주문 취소 오류 수정        |
| `refactor/review/handler`     | 리뷰 관련 로직 리팩토링    |
| `chore/env/config-cleanup`    | 환경설정 정리              |

---

#### ✅ 브랜치 전략 예시

```
main or master        # 실제 서비스 배포용  
develop               # 전체 개발 병합용 (테스트 포함)  
feature/*/*           # 기능 개발 브랜치  
fix/*/*               # 버그 수정 브랜치  
refactor/*/*          # 코드 개선 브랜치  
hotfix/*/*            # 운영 중 긴급 수정 브랜치  
```

---

#### 🔖 참고

- 브랜치명은 **소문자**로 작성하고, 단어 구분은 **하이픈(-)** 을 사용합니다.  
- 브랜치명은 **간결하고 명확하게** 작성합니다.  
- 모든 기능 브랜치는 `develop` 브랜치에서 분기하여 작업 후 Pull Request로 병합합니다.

---

#### ✅ 사용 예시

```bash
# 상품 등록 기능 시작
git checkout -b feature/product/create

# 주문 생성 버그 수정
git checkout -b fix/order/submit-failure
```

---
