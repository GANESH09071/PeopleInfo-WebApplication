# PeopleInfo – Employee Management System

A full-stack **Employee Management System** built with **Java 17, Spring Boot 3, Thymeleaf, MySQL**, and a polished HTML/CSS/JS frontend.

---

## Tech Stack

| Layer       | Technology                              |
|-------------|------------------------------------------|
| Backend     | Java 17, Spring Boot 3.2                 |
| Security    | Spring Security 6 (Form-based auth)      |
| ORM         | Spring Data JPA + Hibernate              |
| Database    | MySQL 8                                  |
| Templating  | Thymeleaf + Thymeleaf Security Extras    |
| Frontend    | HTML5, CSS3, Vanilla JS                  |
| Icons       | Remix Icons                              |
| Build Tool  | Maven                                    |

---

## Features

### Authentication & Access Control
- Secure login with email + password
- Role-based access: **HR** and **Employee**
- Self-registration (defaults to Employee role)
- Auto-redirect to role-specific dashboard on login

### HR Portal
| Feature              | Description                                              |
|----------------------|----------------------------------------------------------|
| Dashboard            | Stats: total employees, pending leaves/timesheets, open jobs |
| Employee Management  | Create, view, edit, delete employees with search         |
| Leave Approvals      | Approve/reject leave requests with comments              |
| Timesheets           | Review all employee timesheets, filter by date range     |
| Hiring Requirements  | Post, edit, delete job openings                          |

### Employee Portal
| Feature              | Description                                              |
|----------------------|----------------------------------------------------------|
| Dashboard            | Personal stats, recent leaves and timesheets             |
| My Timesheets        | Log daily work hours (check-in/check-out)                |
| My Leaves            | Apply for leave, track approval status                   |
| Employee Directory   | Browse all colleagues with search                        |
| Open Positions       | View company hiring requirements                         |

---

## Prerequisites

- **Java 17+** — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/)
- **MySQL 8+** — [Download](https://dev.mysql.com/downloads/)

---

## Setup & Run

### 1. Clone / Extract the project

```bash
cd peopleinfo
```

### 2. Configure MySQL

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/peopleinfo_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

> The database `peopleinfo_db` is created automatically if it doesn't exist.

### 3. Build & Run

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

### 4. Open in Browser

```
http://localhost:8080
```

---

## Demo Credentials

| Role     | Email                            | Password |
|----------|----------------------------------|----------|
| HR       | hr@peopleinfo.com                | hr123    |
| Employee | michael.chen@peopleinfo.com      | emp123   |
| Employee | emily.r@peopleinfo.com           | emp123   |
| Employee | david.kim@peopleinfo.com         | emp123   |

> Demo data is seeded automatically on first startup.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/peopleinfo/
│   │   ├── PeopleInfoApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java       # Spring Security setup
│   │   │   └── DataInitializer.java      # Demo data seeder
│   │   ├── controller/
│   │   │   ├── AuthController.java       # /login, /register
│   │   │   ├── HRController.java         # /hr/**
│   │   │   └── EmployeeController.java   # /employee/**
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── LeaveRequest.java
│   │   │   ├── Timesheet.java
│   │   │   └── HiringRequirement.java
│   │   ├── repository/                   # Spring Data JPA repos
│   │   ├── service/                      # Business logic
│   │   ├── dto/
│   │   │   └── RegisterRequest.java
│   │   └── security/
│   │       ├── UserPrincipal.java
│   │       └── CustomUserDetailsService.java
│   └── resources/
│       ├── application.properties
│       ├── static/
│       │   ├── css/style.css
│       │   └── js/app.js
│       └── templates/
│           ├── auth/
│           │   ├── login.html
│           │   └── register.html
│           ├── fragments/
│           │   └── sidebar.html
│           ├── hr/
│           │   ├── dashboard.html
│           │   ├── employees.html
│           │   ├── employee-form.html
│           │   ├── leaves.html
│           │   ├── timesheets.html
│           │   ├── hiring.html
│           │   └── hiring-form.html
│           └── employee/
│               ├── dashboard.html
│               ├── timesheets.html
│               ├── leaves.html
│               ├── directory.html
│               └── hiring.html
└── database_setup.sql                    # Reference SQL schema
```

---

## URL Routes

| URL                         | Role     | Description               |
|-----------------------------|----------|---------------------------|
| `/login`                    | Public   | Login page                |
| `/register`                 | Public   | Self-registration         |
| `/hr/dashboard`             | HR       | HR home dashboard         |
| `/hr/employees`             | HR       | Employee management       |
| `/hr/employees/new`         | HR       | Add employee form         |
| `/hr/leaves`                | HR       | Leave approval queue      |
| `/hr/timesheets`            | HR       | All timesheets            |
| `/hr/hiring`                | HR       | Job postings              |
| `/employee/dashboard`       | Employee | Personal dashboard        |
| `/employee/timesheets`      | Employee | Log + view my timesheets  |
| `/employee/leaves`          | Employee | Apply + track leaves      |
| `/employee/directory`       | Employee | Browse all employees      |
| `/employee/hiring`          | Employee | View open positions       |

---

## Customization

- **Change port**: set `server.port=9090` in `application.properties`
- **Disable demo data**: delete or comment out `DataInitializer.java`
- **Add email notifications**: integrate Spring Mail
- **Extend leave balance tracking**: add a `LeaveBalance` entity

---

## License

MIT — Free to use and modify.
