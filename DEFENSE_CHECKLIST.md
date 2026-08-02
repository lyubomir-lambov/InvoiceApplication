# Defense Checklist

Този файл е кратък маршрут за ръчна демонстрация на проекта.

## 1. Стартиране

Провери, че MySQL работи.

Стартирай `invoice-history-service`:

```bash
cd invoice-history-service
DB_USERNAME=root DB_PASSWORD=1010 INVOICE_HISTORY_API_KEY=lambi-invoice-history-api-key ./mvnw spring-boot:run
```

Стартирай main app:

```bash
INVOICE_HISTORY_API_KEY=lambi-invoice-history-api-key ./mvnw spring-boot:run
```

URL-и:

- Main app: `http://localhost:8080`
- Microservice: `http://localhost:8081`

## 2. Архитектура

Покажи, че проектът има две независими Spring Boot приложения:

- `InvoiceApplication`
- `invoice-history-service`

Обясни накратко:

- main app съдържа UI, потребители, клиенти, фактури, плащания и справки;
- microservice пази immutable история на фактурите;
- main app комуникира с microservice чрез Feign Client.

Файлове за показване:

- `src/main/java/bg/softuni/invoiceapplication/InvoiceApplication.java`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/InvoiceHistoryServiceApplication.java`
- `src/main/java/bg/softuni/invoiceapplication/client/InvoiceHistoryClient.java`

## 3. Основни функционалности

Демонстрирай:

- регистрация на потребител;
- login;
- създаване на клиент;
- редакция на клиент;
- създаване на фактура;
- редакция на фактура;
- преглед на фактура;
- invoice history;
- плащане;
- payment report;
- profile page.

## 4. Invoice history microservice

Покажи:

- `InvoiceHistoryRecord` entity;
- repository;
- service;
- controller;
- DTOs;
- mapper;
- API key security.

Файлове за показване:

- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/model/entity/InvoiceHistoryRecord.java`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/repository/InvoiceHistoryRepository.java`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/service/impl/InvoiceHistoryServiceImpl.java`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/web/InvoiceHistoryController.java`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/config/ApiKeyAuthenticationFilter.java`

REST endpoints:

- `POST /api/invoice-history`
- `GET /api/invoice-history/invoices/{invoiceId}`
- `DELETE /api/invoice-history/invoices/{invoiceId}`

Header:

```text
X-API-Key: lambi-invoice-history-api-key
```

## 5. Security и роли

Покажи:

- `ADMIN` и `USER` роли;
- first registered user става `ADMIN`;
- admin-only операции;
- `@PreAuthorize` защита;
- скрити admin бутони в Thymeleaf.

Файлове за показване:

- `src/main/java/bg/softuni/invoiceapplication/config/SecurityConfiguration.java`
- `src/main/java/bg/softuni/invoiceapplication/model/enums/UserRole.java`
- `src/main/java/bg/softuni/invoiceapplication/service/impl/UserServiceImpl.java`
- `src/main/java/bg/softuni/invoiceapplication/web/InvoiceController.java`

## 6. Validation и exception handling

Покажи:

- DTO validation annotations;
- custom exceptions;
- `GlobalExceptionHandler`;
- `error.html`;
- microservice JSON error response.

Файлове за показване:

- `src/main/java/bg/softuni/invoiceapplication/exception/GlobalExceptionHandler.java`
- `src/main/resources/templates/error.html`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/exception/GlobalExceptionHandler.java`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/exception/ErrorResponseDTO.java`

Лесен manual test:

- отвори невалиден URL с UUID параметър, например `/invoices/abc`;
- спри microservice-а и отвори детайл на фактура.

## 7. Scheduling

Покажи:

- cron job за маркиране на просрочени фактури;
- fixed delay job за връщане към `ISSUED`;
- запис в invoice history при scheduler промяна.

Файлове за показване:

- `src/main/java/bg/softuni/invoiceapplication/scheduler/InvoiceStatusScheduler.java`
- `src/main/java/bg/softuni/invoiceapplication/service/impl/InvoiceServiceImpl.java`
- `src/main/java/bg/softuni/invoiceapplication/model/enums/InvoiceStatus.java`

## 8. Caching

Покажи:

- `@EnableCaching`;
- `@Cacheable`;
- `@CacheEvict`;
- че cache-ът се чисти при промяна на клиент.

Файлове за показване:

- `src/main/java/bg/softuni/invoiceapplication/InvoiceApplication.java`
- `src/main/java/bg/softuni/invoiceapplication/service/impl/ClientServiceImpl.java`

## 9. Logging

Покажи логове при:

- create/edit invoice;
- create/edit client;
- create/edit payment;
- scheduler job;
- Feign call към invoice history service.

Файлове за показване:

- `src/main/java/bg/softuni/invoiceapplication/service/impl/InvoiceServiceImpl.java`
- `src/main/java/bg/softuni/invoiceapplication/service/impl/InvoiceHistoryIntegrationServiceImpl.java`
- `invoice-history-service/src/main/java/bg/softuni/invoicehistoryservice/service/impl/InvoiceHistoryServiceImpl.java`

## 10. Тестове

Пусни main app тестовете:

```bash
./mvnw test
```

Пусни microservice тестовете:

```bash
cd invoice-history-service
./mvnw test
```

Към момента има smoke tests. По-пълни service/controller тестове са отделна финална задача.

## 11. README и Git

Покажи:

- `README.md`;
- публичен repository link;
- commit history.

Важно за предаване:

- repository-то трябва да е публично;
- линкът трябва да бъде submit-нат преди крайния срок;
- не push-вай нови commits след оценяване, ако курсът изисква да се изчака.
