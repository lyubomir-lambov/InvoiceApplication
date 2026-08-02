# Invoice Application

Invoice Application е Spring Boot MVC приложение за управление на клиенти, фактури, плащания, справки и история на промените по фактури.

Проектът е разработен за Spring Advanced Individual Project и се състои от две независими Spring Boot приложения:

- `InvoiceApplication` - основното MVC приложение.
- `invoice-history-service` - REST microservice за immutable история на фактурите.

Основното приложение използва Thymeleaf UI, MySQL база, Spring Security, Feign Client, Scheduling, Caching, централизирано exception handling и logging.

## Съдържание

- [Основни функционалности](#основни-функционалности)
- [Технологии](#технологии)
- [Архитектура](#архитектура)
- [Домейн модел](#домейн-модел)
- [REST microservice](#rest-microservice)
- [Security](#security)
- [Scheduling и caching](#scheduling-и-caching)
- [Exception handling и logging](#exception-handling-и-logging)
- [Изисквания](#изисквания)
- [Конфигурация](#конфигурация)
- [Стартиране](#стартиране)
- [Основни URL адреси](#основни-url-адреси)
- [Тестове](#тестове)
- [Структура на проекта](#структура-на-проекта)

## Основни функционалности

### Клиенти

- Създаване, редактиране, търсене и преглед на клиенти.
- Данни за фирма, лице за контакт, имейл, телефон, адрес, държава и ДДС номер.
- Активиране и деактивиране на клиенти.
- Изтриване на клиент само когато няма свързани фактури или плащания.
- Проверка за дублирано име за показване и ДДС номер.
- Кеширан списък с активни клиенти за формите за фактури.

### Фактури

- Създаване, редактиране, преглед и печат на фактури.
- Автоматична последователна номерация във формат `0000000001`.
- Поддръжка на `INVOICE`, `DEBIT_NOTE`, `CREDIT_NOTE`.
- Поддръжка на валути `BGN`, `EUR`, `USD`, `GBP`.
- Редове във фактурата с описание, количество, мерна единица, единична цена и ДДС ставка.
- Автоматично изчисляване на суми без ДДС, ДДС и обща сума.
- Snapshot на клиентските данни във фактурата.
- Анулиране и възстановяване на фактури.
- Автоматично маркиране на просрочени фактури като `OVERDUE`.
- История на всяка важна промяна, записвана в отделен microservice.

### Invoice history

- Запис на immutable history record при създаване, редакция, анулиране, възстановяване и scheduler промени.
- Всяка версия пази revision number, потребител, действие, статус, суми, клиент и JSON snapshot на фактурата.
- В детайлния екран на фактурата историята се визуализира като разгъваеми редове.
- Администратор може да изчисти историята на конкретна фактура.

### Плащания и справки

- Добавяне, редактиране, търсене и изтриване на плащания.
- Плащанията са свързани с клиент, сума, валута, дата и бележки.
- Справка за фактурирани суми, платени суми и остатък по валута.
- Филтриране на справките за всички клиенти или за конкретен клиент.
- Анулираните фактури не участват в справките.
- Кредитните известия участват със знак минус.

### Потребители

- Регистрация и вход в системата.
- BCrypt хеширане на пароли.
- Първият регистриран потребител автоматично получава роля `ADMIN`.
- Администраторите могат да управляват потребители, роли и активност.
- Всеки логнат потребител има profile page и може да редактира профилните си данни.

## Технологии

- Java 17
- Spring Boot 3.4.0
- Spring MVC + Thymeleaf
- Spring Data JPA
- Spring Security
- Spring Cloud OpenFeign
- Spring Cache
- Spring Scheduling
- Jakarta Bean Validation
- MySQL
- Hibernate
- Lombok
- Maven Wrapper
- JUnit 5 / Spring Boot Test

## Архитектура

Проектът използва layered architecture:

- `web` - MVC и REST controllers.
- `service` - бизнес логика и транзакционни операции.
- `repository` - Spring Data JPA repositories.
- `model.entity` - JPA entities.
- `model.dto` - request, response и view DTO модели.
- `mapper` - преобразуване между entities и DTO обекти.
- `client` - Feign client към microservice.
- `scheduler` - scheduled jobs.
- `exception` - custom exceptions и global exception handling.
- `config` / `security` - security, cache и application configuration.

Основното приложение работи на порт `8080`.
Microservice приложението работи на порт `8081`.

## Домейн модел

Основни entity класове в main app:

- `User` - потребител с username, email, парола, активност и роля.
- `Client` - клиент/фирма с контактни, адресни и ДДС данни.
- `Invoice` - фактура с номер, тип, статус, валута, дати, клиент и редове.
- `InvoiceLineItem` - ред във фактура с количество, цена, мерна единица и ДДС ставка.
- `Payment` - плащане към клиент.

Entity в microservice:

- `InvoiceHistoryRecord` - immutable запис за история на фактура.

Ключови enum типове:

- `UserRole` - `ADMIN`, `USER`
- `InvoiceType` - `INVOICE`, `DEBIT_NOTE`, `CREDIT_NOTE`
- `InvoiceStatus` - `ISSUED`, `OVERDUE`, `CANCELLED`
- `InvoiceCurrency` - `BGN`, `EUR`, `USD`, `GBP`
- `VatRate` - `0%`, `9%`, `20%`
- `InvoiceHistoryAction` - `CREATED`, `UPDATED`, `CANCELLED`, `RESTORED`, `MARKED_OVERDUE`, `MARKED_ISSUED`

## REST microservice

`invoice-history-service` exposes REST API, protected with API key header:

```text
X-API-Key: lambi-invoice-history-api-key
```

Endpoints:

| Method | URL | Описание |
| --- | --- | --- |
| `POST` | `/api/invoice-history` | Създава history record |
| `GET` | `/api/invoice-history/invoices/{invoiceId}` | Връща историята на фактура |
| `DELETE` | `/api/invoice-history/invoices/{invoiceId}` | Изтрива историята на фактура |

Основното приложение извиква microservice-а чрез Feign Client.

## Security

Main app:

- Spring Security form login.
- BCrypt password encoding.
- Role-based достъп с `ADMIN` и `USER`.
- Admin-only операции за потребители, изтриване на плащания, анулиране/възстановяване и clear invoice history.
- `@PreAuthorize` се използва за чувствителни операции.

Microservice:

- Stateless Spring Security configuration.
- Custom API key filter.
- Всички REST endpoints изискват валиден `X-API-Key`.

## Scheduling и caching

Scheduling:

- Cron job маркира просрочени `ISSUED` фактури като `OVERDUE`.
- Fixed delay job връща `OVERDUE` фактури към `ISSUED`, ако due date вече не е в миналото.
- Scheduler промените също се записват в invoice history microservice.

Caching:

- Кешира се списъкът с активни клиенти за invoice формите.
- Cache-ът се изчиства при create/edit/toggle/delete на клиент.

## Exception handling и logging

И двете приложения имат централизирано exception handling.

Main app:

- Показва обща `error.html` страница при application errors.
- Използва custom exceptions като `ApplicationException`, `BusinessRuleException`, `ResourceNotFoundException`.

Microservice:

- Връща еднакъв JSON error response при validation, invalid API key, broken JSON body и unexpected errors.

Logging:

- Логват се важни бизнес операции: фактури, клиенти, плащания, потребители, scheduler jobs и Feign комуникация.
- SQL dump логовете са изключени за по-четима конзола.

## Изисквания

Преди стартиране трябва да имате:

- Java 17+
- MySQL Server
- Maven Wrapper се използва от проекта, отделна Maven инсталация не е задължителна

## Конфигурация

Main app използва база:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/invoice_application?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=1010
invoice.history.service.url=http://localhost:8081
invoice.history.api-key=${INVOICE_HISTORY_API_KEY}
```

Microservice използва отделна база:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/invoice_history_service?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
invoice.history.api-key=${INVOICE_HISTORY_API_KEY}
```

Нужни environment variables:

```text
DB_USERNAME=root
DB_PASSWORD=1010
INVOICE_HISTORY_API_KEY=lambi-invoice-history-api-key
```

За main app е задължителна поне:

```text
INVOICE_HISTORY_API_KEY=lambi-invoice-history-api-key
```

## Стартиране

1. Стартирайте MySQL.

2. Стартирайте microservice-а:

```bash
cd invoice-history-service
DB_USERNAME=root DB_PASSWORD=1010 INVOICE_HISTORY_API_KEY=lambi-invoice-history-api-key ./mvnw spring-boot:run
```

Microservice URL:

```text
http://localhost:8081
```

3. Стартирайте main app в отделен terminal:

```bash
INVOICE_HISTORY_API_KEY=lambi-invoice-history-api-key ./mvnw spring-boot:run
```

Main app URL:

```text
http://localhost:8080
```

При първо стартиране Hibernate създава нужните таблици автоматично чрез `spring.jpa.hibernate.ddl-auto=update`.

## Основни URL адреси

| URL | Описание | Достъп |
| --- | --- | --- |
| `/` | Начална страница | публичен |
| `/login` | Вход | публичен |
| `/users/register` | Регистрация | публичен |
| `/profile` | Потребителски профил | логнат потребител |
| `/profile/edit` | Редакция на профил | логнат потребител |
| `/invoices` | Списък с фактури | логнат потребител |
| `/invoices/create` | Създаване на фактура | логнат потребител |
| `/invoices/{invoiceId}` | Детайли и история на фактура | логнат потребител |
| `/clients` | Списък с клиенти | логнат потребител |
| `/clients/create` | Създаване на клиент | логнат потребител |
| `/payments` | Списък с плащания | логнат потребител |
| `/payments/create` | Добавяне на плащане | логнат потребител |
| `/payment-reports` | Справки за плащания и задължения | логнат потребител |
| `/users` | Управление на потребители | администратор |

## Тестове

Стартиране на тестовете за main app:

```bash
./mvnw test
```

Стартиране на тестовете за microservice:

```bash
cd invoice-history-service
./mvnw test
```

Проектът има unit, service, mapper, controller/API и exception handling тестове.

Покритие към последната проверка:

| Application | Tests | Line coverage |
| --- | ---: | ---: |
| Main app | 205 | 97.23% |
| Invoice history microservice | 23 | 87.15% |

JaCoCo report се генерира след `./mvnw test`:

```text
target/site/jacoco/index.html
invoice-history-service/target/site/jacoco/index.html
```

## Структура на проекта

```text
.
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java/bg/softuni/invoiceapplication
│   │   │   ├── client
│   │   │   ├── config
│   │   │   ├── exception
│   │   │   ├── mapper
│   │   │   ├── model
│   │   │   ├── repository
│   │   │   ├── scheduler
│   │   │   ├── security
│   │   │   ├── service
│   │   │   └── web
│   │   └── resources
│   │       ├── application.properties
│   │       ├── static
│   │       └── templates
│   └── test
└── invoice-history-service
    ├── pom.xml
    ├── src/main/java/bg/softuni/invoicehistoryservice
    │   ├── config
    │   ├── exception
    │   ├── mapper
    │   ├── model
    │   ├── repository
    │   ├── service
    │   └── web
    └── src/main/resources/application.properties
```

## Бележки за разработчици

- При промяна на enum стойности проверете MySQL колоните, ако вече има съществуваща схема.
- При промяна на invoice действията актуализирайте и main app mapper-а, и microservice enum-а.
- При добавяне на нови admin операции добавете защита както в UI, така и със server-side security.
- За production среда пароли и API key трябва да се подават само през environment variables или secret management.
