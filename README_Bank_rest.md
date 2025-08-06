````markdown
# Система управления банковскими картами

Безопасное приложение на Spring Boot для управления банковскими картами для пользователей и администраторов.

---
## Быстрый старт

### 1. Клонирование и настройка
```bash
git clone https://github.com/PaatoM/Bank_REST.git
cd Bank_REST
````

### 2. Переменные окружения

Создайте файл `.env.prod` в корне проекта:
```bash
cat > .env.prod <<EOF
DB_URL=jdbc:postgresql://postgres:5432/card_db
DB_USER=rockio
DB_PASSWORD=tatooo22
JWT_SECRET=your_jwt_secret_key
ENCRYPTION_KEY=SecEreTAESKeYToGenerAte256BitOnl
ENCRYPTION_IV=InItViCT0R128Bit
EOF

```
```env
DB_URL=jdbc:postgresql://postgres:5432/card_db
DB_USER=rockio
DB_PASSWORD=tatooo22
JWT_SECRET=your_jwt_secret_key
ENCRYPTION_KEY=SecEreTAESKeYToGenerAte256BitOnl
ENCRYPTION_IV=InItViCT0R128Bit
```

### 3. Запуск через Docker

```bash
docker-compose --env-file .env.prod up --build
```

* Приложение будет доступно по адресу: [http://localhost:8083](http://localhost:8083)


---

## Технологии

* **Java 24**
* **Spring Boot 3.5.4**
* **Spring Security + JWT**
* **Spring Data JPA**
* **PostgreSQL + Liquibase**
* **SpringDoc OpenAPI (Swagger)**
* **AES-256 Шифрование**
* **Docker + Docker Compose**
* **JUnit + Mockito для тестирования**

---

## Возможности

### Возможности пользователя

* Просмотр своих карт (маскированный и полный номер)
* Фильтрация карт по статусу
* Запрос на блокировку карты
* Переводы между своими картами
* Проверка баланса

### Возможности администратора

* Создание, удаление, активация, блокировка любой карты
* Просмотр всех карт (с маскировкой номера)
* Управление пользователями (список, детали, удаление)
* Поиск пользователей по имени с пагинацией

### Безопасность

* Ролевой доступ (USER / ADMIN)
* AES шифрование номеров карт
* Маскирование номеров карт
* Глобальная обработка исключений
* Аутентификация на основе JWT

---

## Аутентификация

Используйте `/api/v1/auth/register` и `/api/v1/auth/login` для получения JWT токена.

Передавайте токен в заголовке:

```
Authorization: Bearer <your_token_here>
```

---

## Swagger Документация

* UI: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
* YAML: `docs/openapi.yaml`

---

## Миграции Liquibase

Миграции находятся в папке:

```
src/main/resources/db/changelog/
```

Автоматически применяются при старте приложения. Используется PostgreSQL 16 (Docker).

---

## Сводка API эндпоинтов

### Эндпоинты пользователя

| Метод | Эндпоинт                               |
| ----- | -------------------------------------- |
| GET   | `/api/v1/cards/all`                    |
| GET   | `/api/v1/cards/{cardId}`               |
| GET   | `/api/v1/cards/raw/{cardId}`           |
| POST  | `/api/v1/cards/block-request/{cardId}` |
| POST  | `/api/v1/cards/transfers`              |
| GET   | `/api/v1/cards/{cardId}/balance`       |

### Эндпоинты администратора

| Метод  | Эндпоинт                              |
| ------ | ------------------------------------- |
| POST   | `/api/v1/admin/cards/new`             |
| PATCH  | `/api/v1/admin/cards/{cardId}/status` |
| DELETE | `/api/v1/admin/cards/{cardId}/delete` |
| GET    | `/api/v1/admin/cards/all`             |
| GET    | `/api/v1/admin/cards/all/{userId}`    |
| GET    | `/api/v1/admin/users/all`             |
| GET    | `/api/v1/admin/users/{userId}`        |
| DELETE | `/api/v1/admin/users/{userId}`        |

---

## Автор

**Разработчик:** Rockio

**Контакты:** [Email](mailto:tarekshawesh23@gmail.com) [Telegram](https://t.me/rockio23)

---