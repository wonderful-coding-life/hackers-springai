
# Chat Client 프로젝트

## RAG를 위해 벡터데이터베이스와 샘플데이터 임베딩 준비

마리아DB 준비
```bash
docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=rootpass \
  -e MARIADB_DATABASE=mydb \
  -e MARIADB_USER=myuser \
  -e MARIADB_PASSWORD=mypass \
  mariadb
```
임베딩
- VectorStoreTests.java
- 또는 support/sql/에 있는 schema, data 스크립트 임포트

## UI 변경
- 현재 샘플로 resources/static/index.html, css/chatbot.css, js/chatbot.js가 있는데 새롭게 만들고 싶다면 삭제
- support/docs/ui-spec.md로 ui 생성

## 실행
- bootRun
- 브라우저로 localhost:8080

## 질문 유형
- 단순 변심도 반품이 되나요?
- 제가 교재를 구매했는데 책에 필기를 조금 했습니다. 반품하려면 배송비는 누가 부담하고 환불은 받을 수 있나요?
- 쿠폰과 적립금을 사용해서 결제했는데 일부 상품만 반품하면 환불 금액은 어떻게 계산되나요?
- 주문한 상품과 다른 상품이 배송됐는데 반품 절차와 환불까지 걸리는 시간을 알려주세요.
- 주문 목록을 알려 주세요.

