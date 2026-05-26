-- ============================================================
-- PeopleInfo Employee Management System
-- MySQL Database Setup Script
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS peopleinfo_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE peopleinfo_db;

-- ============================================================
-- NOTE: Hibernate will auto-create tables on first run
-- (spring.jpa.hibernate.ddl-auto=update).
-- This script is provided for reference / manual setup.
-- ============================================================

-- Users / Employees Table
CREATE TABLE IF NOT EXISTS users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name        VARCHAR(100)   NOT NULL,
    last_name         VARCHAR(100)   NOT NULL,
    email             VARCHAR(255)   NOT NULL UNIQUE,
    password          VARCHAR(255)   NOT NULL,
    role              ENUM('HR','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    employee_id       VARCHAR(20)    UNIQUE,
    department        VARCHAR(100),
    position          VARCHAR(150),
    phone             VARCHAR(30),
    date_of_joining   DATE,
    date_of_birth     DATE,
    salary            DECIMAL(12,2),
    address           TEXT,
    status            ENUM('ACTIVE','INACTIVE','ON_LEAVE','TERMINATED') DEFAULT 'ACTIVE',
    created_at        DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Leave Requests Table
CREATE TABLE IF NOT EXISTS leave_requests (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT       NOT NULL,
    leave_type    ENUM('ANNUAL','SICK','MATERNITY','PATERNITY','CASUAL','UNPAID','EMERGENCY') NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    reason        TEXT,
    status        ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    approved_by   BIGINT,
    hr_comments   VARCHAR(500),
    applied_on    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Timesheets Table
CREATE TABLE IF NOT EXISTS timesheets (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT       NOT NULL,
    date          DATE         NOT NULL,
    check_in      TIME,
    check_out     TIME,
    hours_worked  DECIMAL(5,2),
    notes         TEXT,
    project_name  VARCHAR(200),
    status        ENUM('SUBMITTED','APPROVED','REJECTED') DEFAULT 'SUBMITTED',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_emp_date (employee_id, date),
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Hiring Requirements Table
CREATE TABLE IF NOT EXISTS hiring_requirements (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_title           VARCHAR(200)  NOT NULL,
    department          VARCHAR(100),
    description         TEXT,
    requirements        TEXT,
    responsibilities    TEXT,
    experience_level    ENUM('ENTRY','JUNIOR','MID','SENIOR','LEAD','MANAGER','DIRECTOR'),
    job_type            ENUM('FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','REMOTE'),
    positions_available INT           DEFAULT 1,
    salary_range_min    DECIMAL(12,2),
    salary_range_max    DECIMAL(12,2),
    location            VARCHAR(200),
    deadline_date       DATE,
    status              ENUM('OPEN','IN_PROGRESS','FILLED','CANCELLED') DEFAULT 'OPEN',
    posted_by           BIGINT,
    created_at          DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================
-- Useful queries
-- ============================================================

-- View all active employees
-- SELECT id, employee_id, CONCAT(first_name,' ',last_name) AS name, email, department, position, status
-- FROM users WHERE status = 'ACTIVE' ORDER BY first_name;

-- View pending leaves
-- SELECT lr.id, CONCAT(u.first_name,' ',u.last_name) AS employee,
--        lr.leave_type, lr.start_date, lr.end_date, lr.status
-- FROM leave_requests lr JOIN users u ON lr.employee_id = u.id
-- WHERE lr.status = 'PENDING';

-- View open job postings
-- SELECT id, job_title, department, experience_level, positions_available, deadline_date
-- FROM hiring_requirements WHERE status = 'OPEN';
