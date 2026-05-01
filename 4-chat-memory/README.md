# 🐳 MariaDB Docker 실행 가이드

프로젝트 실행 전에 MariaDB 컨테이너를 먼저 실행해야 합니다.

---

## 1. MariaDB 컨테이너 실행

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

## 2. Spring Boot DB 설정
프로젝트는 아래 설정을 기준으로 MariaDB에 연결됩니다.

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/mydb
spring.datasource.username=myuser
spring.datasource.password=mypass
```

## 3. 컨테이너 상태 확인

```bash
docker ps
```
# ⚠️ 참고
포트 충돌 시
```bash
-p 3307:3306
```
데이터 영속화 (권장)
```bash
docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -v mariadb_data:/var/lib/mysql \
  -e MARIADB_ROOT_PASSWORD=rootpass \
  -e MARIADB_DATABASE=mydb \
  -e MARIADB_USER=myuser \
  -e MARIADB_PASSWORD=mypass \
  mariadb
```