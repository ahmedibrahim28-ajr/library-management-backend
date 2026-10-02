-- =====================================================
-- Library Management System - schema + sample data
-- Requires MySQL 8.0.16+ (CHECK constraints are enforced)
-- =====================================================

CREATE DATABASE IF NOT EXISTS library_db;

USE library_db;

-- =========================
-- 1. Roles
-- =========================

CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 2. Users
-- =========================

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30) NULL,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
);

-- =========================
-- 3. Members (library card holders)
-- =========================

CREATE TABLE members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    membership_number VARCHAR(30) NOT NULL UNIQUE,
    address VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at DATE NOT NULL,
    membership_expiry DATE NOT NULL,
    max_borrow_limit INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CHECK (status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED')),
    CHECK (max_borrow_limit > 0),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- 4. Librarians (staff)
-- =========================

CREATE TABLE librarians (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    employee_code VARCHAR(30) NOT NULL UNIQUE,
    hire_date DATE NOT NULL,
    section VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- 5. Categories
-- =========================

CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 6. Books (the title / catalog record)
-- =========================

CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(150) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    publisher VARCHAR(150) NULL,
    publication_year INT NULL,
    language VARCHAR(30) NULL,
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE RESTRICT
);

-- =========================
-- 7. Book copies (physical items on the shelf)
-- =========================

CREATE TABLE book_copies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    barcode VARCHAR(50) NOT NULL UNIQUE,
    shelf_location VARCHAR(50) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    acquired_at DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CHECK (status IN ('AVAILABLE', 'BORROWED', 'LOST', 'DAMAGED')),

    FOREIGN KEY (book_id)
        REFERENCES books(id)
        ON DELETE CASCADE
);

-- =========================
-- 8. Borrow transactions
-- =========================

CREATE TABLE borrow_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    issued_by BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    fine_amount DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CHECK (status IN ('BORROWED', 'RETURNED')),
    CHECK (due_date >= borrow_date),
    CHECK (fine_amount >= 0),

    FOREIGN KEY (member_id)
        REFERENCES members(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (book_copy_id)
        REFERENCES book_copies(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (issued_by)
        REFERENCES librarians(id)
        ON DELETE RESTRICT
);

-- =========================
-- 9. Membership history
-- =========================

CREATE TABLE membership_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CHECK (action IN ('JOINED', 'RENEWED', 'SUSPENDED', 'REACTIVATED')),

    FOREIGN KEY (member_id)
        REFERENCES members(id)
        ON DELETE CASCADE
);

-- =====================================================
-- SAMPLE DATA
-- (dates are relative to today so the demo always shows
--  active, overdue and returned loans)
-- =====================================================

INSERT INTO roles (name, slug, description) VALUES
('Admin',     'admin',     'Full access to the system'),
('Librarian', 'librarian', 'Manages books, copies and loans'),
('Member',    'member',    'Library card holder who can borrow books');

INSERT INTO users (role_id, name, email, phone, password) VALUES
(1, 'Admin User',      'admin@citylibrary.com',   '01000000001', '123456'),
(2, 'Mona Hassan',     'mona@citylibrary.com',    '01000000002', '123456'),
(2, 'Karim Adel',      'karim@citylibrary.com',   '01000000003', '123456'),
(3, 'Youssef Ali',     'youssef@example.com',     '01012345678', '123456'),
(3, 'Salma Ibrahim',   'salma@example.com',       '01023456789', '123456'),
(3, 'Omar Khaled',     'omar@example.com',        '01034567890', '123456'),
(3, 'Nour Mahmoud',    'nour@example.com',        '01045678901', '123456');

INSERT INTO librarians (user_id, employee_code, hire_date, section) VALUES
(2, 'EMP-001', '2021-03-01', 'Circulation Desk'),
(3, 'EMP-002', '2023-09-15', 'Cataloging');

INSERT INTO members (user_id, membership_number, address, status, joined_at, membership_expiry, max_borrow_limit) VALUES
(4, 'MEM-0001', '12 Nile St, Cairo',        'ACTIVE',  DATE_SUB(CURDATE(), INTERVAL 400 DAY), DATE_ADD(CURDATE(), INTERVAL 330 DAY), 5),
(5, 'MEM-0002', '7 Garden City, Cairo',     'ACTIVE',  DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_ADD(CURDATE(), INTERVAL 165 DAY), 5),
(6, 'MEM-0003', '3 Corniche Rd, Alexandria','ACTIVE',  DATE_SUB(CURDATE(), INTERVAL 90 DAY),  DATE_ADD(CURDATE(), INTERVAL 275 DAY), 3),
(7, 'MEM-0004', '45 Tahrir Sq, Giza',       'EXPIRED', DATE_SUB(CURDATE(), INTERVAL 375 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY),  5);

INSERT INTO membership_history (member_id, action, start_date, end_date, notes) VALUES
(1, 'JOINED',  DATE_SUB(CURDATE(), INTERVAL 400 DAY), DATE_SUB(CURDATE(), INTERVAL 35 DAY),  'Membership created'),
(1, 'RENEWED', DATE_SUB(CURDATE(), INTERVAL 35 DAY),  DATE_ADD(CURDATE(), INTERVAL 330 DAY), 'Annual renewal'),
(2, 'JOINED',  DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_ADD(CURDATE(), INTERVAL 165 DAY), 'Membership created'),
(3, 'JOINED',  DATE_SUB(CURDATE(), INTERVAL 90 DAY),  DATE_ADD(CURDATE(), INTERVAL 275 DAY), 'Membership created'),
(4, 'JOINED',  DATE_SUB(CURDATE(), INTERVAL 375 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY),  'Membership created');

INSERT INTO categories (name, slug, description) VALUES
('Fiction',    'fiction',    'Novels and short stories'),
('Science',    'science',    'Popular science and research'),
('Technology', 'technology', 'Programming and computing'),
('History',    'history',    'History and civilizations'),
('Children',   'children',   'Books for young readers');

INSERT INTO books (category_id, title, author, isbn, publisher, publication_year, language, description) VALUES
(1, 'Nineteen Eighty-Four',     'George Orwell',            '9780451524935', 'Signet Classics',  1949, 'English', 'A dystopian novel about surveillance and control.'),
(5, 'The Little Prince',        'Antoine de Saint-Exupery', '9780156012195', 'Harcourt',         1943, 'English', 'A poetic tale about a young prince who travels the universe.'),
(2, 'A Brief History of Time',  'Stephen Hawking',          '9780553380163', 'Bantam',           1988, 'English', 'From the Big Bang to black holes.'),
(3, 'Clean Code',               'Robert C. Martin',         '9780132350884', 'Prentice Hall',    2008, 'English', 'A handbook of agile software craftsmanship.'),
(3, 'Effective Java',           'Joshua Bloch',             '9780134685991', 'Addison-Wesley',   2018, 'English', 'Best practices for the Java platform.'),
(4, 'Sapiens',                  'Yuval Noah Harari',        '9780062316097', 'Harper',           2015, 'English', 'A brief history of humankind.'),
(2, 'Cosmos',                   'Carl Sagan',               '9780345539434', 'Ballantine Books', 1980, 'English', 'A journey through space and time.'),
(1, 'Animal Farm',              'George Orwell',            '9780451526342', 'Signet Classics',  1945, 'English', 'A satirical allegorical novella.');

INSERT INTO book_copies (book_id, barcode, shelf_location, status, acquired_at) VALUES
(1, 'LIB-0001', 'A-01', 'BORROWED',  '2022-01-10'),
(1, 'LIB-0002', 'A-01', 'AVAILABLE', '2022-01-10'),
(2, 'LIB-0003', 'E-02', 'AVAILABLE', '2022-02-05'),
(3, 'LIB-0004', 'B-03', 'BORROWED',  '2022-03-12'),
(3, 'LIB-0005', 'B-03', 'AVAILABLE', '2022-03-12'),
(4, 'LIB-0006', 'C-01', 'BORROWED',  '2023-05-20'),
(4, 'LIB-0007', 'C-01', 'AVAILABLE', '2023-05-20'),
(4, 'LIB-0008', 'C-01', 'AVAILABLE', '2023-05-20'),
(5, 'LIB-0009', 'C-02', 'AVAILABLE', '2024-01-08'),
(5, 'LIB-0010', 'C-02', 'AVAILABLE', '2024-01-08'),
(6, 'LIB-0011', 'D-01', 'LOST',      '2023-07-14'),
(7, 'LIB-0012', 'B-04', 'AVAILABLE', '2022-09-30'),
(7, 'LIB-0013', 'B-04', 'AVAILABLE', '2022-09-30'),
(8, 'LIB-0014', 'A-02', 'DAMAGED',   '2021-11-02'),
(2, 'LIB-0015', 'E-02', 'AVAILABLE', '2024-06-18');

INSERT INTO borrow_transactions (member_id, book_copy_id, issued_by, borrow_date, due_date, return_date, status, fine_amount) VALUES
-- active loans
(1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_ADD(CURDATE(), INTERVAL 9 DAY),  NULL, 'BORROWED', 0.00),
(2, 4, 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL, 'BORROWED', 0.00),
(3, 6, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),  DATE_ADD(CURDATE(), INTERVAL 12 DAY), NULL, 'BORROWED', 0.00),
-- returned loans
(1, 2, 1, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 26 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'RETURNED', 0.00),
(2, 7, 2, DATE_SUB(CURDATE(), INTERVAL 35 DAY), DATE_SUB(CURDATE(), INTERVAL 21 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'RETURNED', 3.00);
