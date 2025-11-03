# API 변경 사항 - industry 필드 제거

## 개요
`SolutionEntity`에서 `industry` 필드를 제거함에 따라 관련된 모든 API의 Request/Response 구조가 변경되었습니다.

## 변경된 API 목록

### 1. 솔루션 생성 API
**엔드포인트**: `POST /api/solution-service/solution`

#### Request 변경 사항
**SaveSolutionEntityRequest**에서 `industry` 필드 제거

**변경 전:**
```json
{
  "vendorSeq": 1,
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "category": "CATEGORY_NAME",
  "industry": "산업분야",
  "recommendedCompanySize": "회사규모",
  "solutionImplementationType": "구현방식",
  "amount": 1000000,
  "duration": 30,
  "solutionEffect": [...],
  "keyword": [...]
}
```

**변경 후:**
```json
{
  "vendorSeq": 1,
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "category": "CATEGORY_NAME",
  "recommendedCompanySize": "회사규모",
  "solutionImplementationType": "구현방식",
  "amount": 1000000,
  "duration": 30,
  "solutionEffect": [...],
  "keyword": [...]
}
```

**제거된 필드:**
- `industry` (String) - 필수 필드였으나 제거됨

---

### 2. 솔루션 수정 API
**엔드포인트**: `PUT /api/solution-service/solution`

#### Request 변경 사항
**ModifySolutionEntityRequest**에서 `industry` 필드 제거

**변경 전:**
```json
{
  "vendorSeq": 1,
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "prevCategory": "이전카테고리",
  "nextCategory": "변경카테고리",
  "industry": "산업분야",
  "recommendedCompanySize": "회사규모",
  "solutionImplementationType": "구현방식",
  "amount": 1000000,
  "duration": 30,
  "solutionEffect": [...],
  "keyword": [...]
}
```

**변경 후:**
```json
{
  "vendorSeq": 1,
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "prevCategory": "이전카테고리",
  "nextCategory": "변경카테고리",
  "recommendedCompanySize": "회사규모",
  "solutionImplementationType": "구현방식",
  "amount": 1000000,
  "duration": 30,
  "solutionEffect": [...],
  "keyword": [...]
}
```

**제거된 필드:**
- `industry` (String) - 필수 필드였으나 제거됨

---

### 3. 전체 솔루션 조회 API (필터링, 키워드 검색)
**엔드포인트**: `GET /api/solution-service/solution/list`

#### Query Parameter 변경 사항
`industry` 쿼리 파라미터 제거

**변경 전:**
```
GET /api/solution-service/solution/list?category=CATEGORY_NAME&industry=산업분야&budget=전체&keyword=키워드&start=0&end=15
```

**변경 후:**
```
GET /api/solution-service/solution/list?category=CATEGORY_NAME&budget=전체&keyword=키워드&start=0&end=15
```

**제거된 파라미터:**
- `industry` (String, optional) - 산업분야 필터링 파라미터 제거

**남아있는 파라미터:**
- `category` (String, optional) - 카테고리 필터
- `budget` (String, optional, default: "전체") - 예산 필터
- `keyword` (String, optional) - 키워드 검색
- `start` (int, default: 0) - 시작 인덱스
- `end` (int, default: 15) - 종료 인덱스

---

### 4. 솔루션 카테고리별 조회 API
**엔드포인트**: `GET /api/solution-service/solution/category`

#### Response 변경 사항
**GetSolutionEntityResponse**에서 `industry` 필드 제거

**변경 전:**
```json
{
  "solutionSeq": 1,
  "representImageUrl": "이미지URL",
  "descriptionPdfUrl": "PDFURL",
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "amount": 1000000,
  "solutionImplementationType": ["방식1", "방식2"],
  "duration": 30,
  "industry": ["산업1", "산업2"],
  "recommendedCompanySize": ["규모1", "규모2"],
  "solutionEffect": [...],
  "keywords": [...]
}
```

**변경 후:**
```json
{
  "solutionSeq": 1,
  "representImageUrl": "이미지URL",
  "descriptionPdfUrl": "PDFURL",
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "amount": 1000000,
  "solutionImplementationType": ["방식1", "방식2"],
  "duration": 30,
  "recommendedCompanySize": ["규모1", "규모2"],
  "solutionEffect": [...],
  "keywords": [...]
}
```

**제거된 필드:**
- `industry` (List<String>) - 산업분야 리스트 제거

---

### 5. 솔루션 조회 API
**엔드포인트**: `GET /api/solution-service/solution?solutionSeq={solutionSeq}`

#### Response 변경 사항
**GetSolutionEntityResponse**에서 `industry` 필드 제거

**변경 전:**
```json
{
  "solutionSeq": 1,
  "representImageUrl": "이미지URL",
  "descriptionPdfUrl": "PDFURL",
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "amount": 1000000,
  "solutionImplementationType": ["방식1", "방식2"],
  "duration": 30,
  "industry": ["산업1", "산업2"],
  "recommendedCompanySize": ["규모1", "규모2"],
  "solutionEffect": [...],
  "keywords": [...]
}
```

**변경 후:**
```json
{
  "solutionSeq": 1,
  "representImageUrl": "이미지URL",
  "descriptionPdfUrl": "PDFURL",
  "solutionName": "솔루션명",
  "solutionDetail": "상세설명",
  "amount": 1000000,
  "solutionImplementationType": ["방식1", "방식2"],
  "duration": 30,
  "recommendedCompanySize": ["규모1", "규모2"],
  "solutionEffect": [...],
  "keywords": [...]
}
```

**제거된 필드:**
- `industry` (List<String>) - 산업분야 리스트 제거

---

## 요약

| API | 변경 유형 | 제거된 항목 |
|-----|----------|------------|
| POST /api/solution-service/solution | Request | `industry` 필드 |
| PUT /api/solution-service/solution | Request | `industry` 필드 |
| GET /api/solution-service/solution/list | Query Parameter | `industry` 파라미터 |
| GET /api/solution-service/solution/category | Response | `industry` 필드 |
| GET /api/solution-service/solution | Response | `industry` 필드 |

**주의사항:**
- 모든 `industry` 필드는 **필수 필드**였으나 제거되었습니다.
- 기존 클라이언트 코드에서 `industry` 필드를 사용하고 있다면 반드시 수정해야 합니다.
- 데이터베이스 스키마 변경이 필요할 수 있습니다.

