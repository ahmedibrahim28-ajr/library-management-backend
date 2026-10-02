# Library Management System - Backend

REST API for managing a city library: members, librarians, books, physical book copies, borrowing and returning, fines, and membership history. Built with Spring Boot and MySQL.

## Project Status

The backend and database structure are implemented. There is currently no frontend application; the APIs are tested directly through Postman.

## Technologies

- Java 17+
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA / Hibernate
- Bean Validation
- MySQL 8.0.16+
- Maven Wrapper
- Postman for API testing

## Features

- User management with roles (admin, librarian, member)
- Member management (library cards, expiry, borrow limit)
- Librarian (staff) management
- Book catalog with categories
- Book copy management (one book title -> many physical copies)
- Borrow and return transactions with automatic due dates
- Overdue tracking and late-return fines
- Membership history (joined, renewed, suspended, reactivated)
- Book availability tracking (total / available / borrowed copies)

## Database

Database name:

```text
library_db
```

The database contains these 9 tables:

```text
roles
users
members
librarians
categories
books
book_copies
borrow_transactions
membership_history
```

### Relationships

```text
roles 1 --- * users
users 1 --- 0..1 members
users 1 --- 0..1 librarians
categories 1 --- * books
books 1 --- * book_copies
members 1 --- * borrow_transactions
book_copies 1 --- * borrow_transactions
librarians 1 --- * borrow_transactions   (issued_by)
members 1 --- * membership_history
```

The SQL schema and sample data are in `database.sql`.

## Project Structure

```text
src/main/java/com/library/management/
│
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── enums/
├── exception/
└── LibraryManagementApplication.java

src/main/resources/
├── application.properties
└── application-example.properties

pom.xml
mvnw.cmd
README.md
database.sql
```

### Architecture

```text
Controller
    ↓
DTO
    ↓
Service
    ↓
Repository
    ↓
Entity
    ↓
MySQL Database
```

## Business Rules

- A **book** is the catalog record; a **book copy** is a physical item with its own barcode and status (`AVAILABLE`, `BORROWED`, `LOST`, `DAMAGED`).
- A copy can only be borrowed when it is `AVAILABLE`.
- A member can only borrow when their status is `ACTIVE`, their membership has not expired, and they are under their `maxBorrowLimit`.
- The due date defaults to today + `library.loan-days` (14) unless a `dueDate` is sent.
- Returning a book sets the copy back to `AVAILABLE`. Late returns are fined `library.fine-per-day` (0.50) per day.
- The `BORROWED` status of a copy is managed only by borrow/return transactions, not by editing the copy directly.
- Creating a member writes a `JOINED` history entry; renewing writes `RENEWED`; suspending / reactivating writes `SUSPENDED` / `REACTIVATED`.
- Renewing extends from the current expiry date, or from today if the membership already expired.

## API Endpoints

### Users

```text
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

### Members

```text
GET    /api/members
GET    /api/members/{id}
POST   /api/members
PUT    /api/members/{id}
POST   /api/members/{id}/renew
DELETE /api/members/{id}
```

### Librarians

```text
GET    /api/librarians
GET    /api/librarians/{id}
POST   /api/librarians
PUT    /api/librarians/{id}
DELETE /api/librarians/{id}
```

### Categories

```text
GET    /api/categories
GET    /api/categories/{id}
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

### Books

```text
GET    /api/books                    (optional ?keyword= searches title and author)
GET    /api/books/available          (books with at least one available copy)
GET    /api/books/{id}
GET    /api/books/{id}/availability
POST   /api/books
PUT    /api/books/{id}
DELETE /api/books/{id}
```

### Book Copies

```text
GET    /api/book-copies              (optional ?bookId= and ?status=)
GET    /api/book-copies/{id}
POST   /api/book-copies
PUT    /api/book-copies/{id}
DELETE /api/book-copies/{id}
```

### Borrow Transactions

```text
GET    /api/borrow-transactions      (optional ?memberId= and ?status=BORROWED|RETURNED)
GET    /api/borrow-transactions/overdue
GET    /api/borrow-transactions/{id}
POST   /api/borrow-transactions      (borrow a book)
PUT    /api/borrow-transactions/{id}/return
```

Transactions are an audit trail, so they cannot be edited or deleted through the API.

### Membership History

```text
GET    /api/membership-history
GET    /api/membership-history/member/{memberId}
```

## How to Run the Project

### 1. Clone the repository

```bash
git clone <repository-url>
cd library-management
```

### 2. Prepare MySQL

Run the SQL script (it creates the `library_db` database, the tables, and sample data):

```bash
mysql -u root -p < database.sql
```

### 3. Configure the database connection

Open:

```text
src/main/resources/application.properties
```

Use your local MySQL credentials. Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

library.loan-days=14
library.fine-per-day=0.50
```

Do not commit real database passwords to GitHub (`application.properties` is git-ignored; share `application-example.properties` instead).

### 4. Run the application on Windows

Open PowerShell in the project directory and run:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux / macOS:

```bash
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

## Testing with Postman

Check which books can be borrowed right now:

```http
GET http://localhost:8080/api/books/available
```

Borrow a book:

```http
POST http://localhost:8080/api/borrow-transactions
Content-Type: application/json
```

```json
{
    "memberId": 1,
    "bookCopyId": 2,
    "librarianId": 1
}
```

Return it (the response includes the fine, if any):

```http
PUT http://localhost:8080/api/borrow-transactions/6/return
```

Renew a membership for 12 months:

```http
POST http://localhost:8080/api/members/4/renew
Content-Type: application/json
```

```json
{
    "months": 12,
    "notes": "Annual renewal"
}
```

Add a new book and one copy of it:

```http
POST http://localhost:8080/api/books
```

```json
{
    "categoryId": 3,
    "title": "Refactoring",
    "author": "Martin Fowler",
    "isbn": "9780134757599",
    "publisher": "Addison-Wesley",
    "publicationYear": 2018,
    "language": "English"
}
```

```http
POST http://localhost:8080/api/book-copies
```

```json
{
    "bookId": 9,
    "barcode": "LIB-0016",
    "shelfLocation": "C-03"
}
```

### Error responses

```text
400  validation error, malformed JSON, or a broken business rule (e.g. copy not available)
404  record not found
409  duplicate value (email, ISBN, barcode...) or record still referenced by other data
```

Error body: `{ "error": "message" }`, or `{ "field": "message" }` for validation errors.

## Sample Data

`database.sql` includes 3 roles, 7 users, 2 librarians, 4 members (one with an expired membership), 5 categories, 8 books, 15 copies, 5 borrow transactions (3 active, 1 of them overdue, 2 returned) and membership history. Dates are relative to the day you run the script, so the overdue example always works.

## Notes

- The current project is backend-only; no frontend is included.
- `spring.jpa.hibernate.ddl-auto=none` is used so Hibernate does not create or modify the existing database schema automatically.
- Passwords are stored as plain text in the sample data, and there is no authentication yet. Password hashing and security (e.g. Spring Security) are planned as a later stage.
