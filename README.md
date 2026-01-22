# Double Entry Accounting System

A comprehensive web-based double-entry accounting system designed for small to medium businesses. Built with Spring Boot 3.2 and Java 17.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Development Setup](#development-setup)
  - [Production Setup](#production-setup)
- [Configuration](#configuration)
- [User Guide](#user-guide)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Deployment](#deployment)
- [Maintenance](#maintenance)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Accounting
- **Double-Entry Bookkeeping** - Full support for debit and credit accounting with automatic balance validation
- **Chart of Accounts** - Flexible hierarchical account structure with 5 account types (Asset, Liability, Equity, Revenue, Expense)
- **Journal Entries** - Create, edit, post, and void journal entries with comprehensive validation
- **General Ledger** - Complete transaction history with running balances

### Financial Reports
- **Trial Balance** - Verify debits equal credits at any point in time
- **Profit & Loss Statement** - Income statement for any date range
- **Balance Sheet** - Financial position as of any date
- **General Ledger Report** - Detailed account transaction history

### Business Operations
- **Invoicing** - Customer management and invoice generation with automatic journal entries
- **Bank Reconciliation** - Import bank statements and match with journal entries
- **Multi-Currency Support** - Track transactions in multiple currencies

### Administration
- **User Management** - Role-based access control (Admin, Accountant, Viewer)
- **Audit Trail** - Track who created and modified entries
- **Dashboard** - Real-time financial overview

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Backend Framework | Spring Boot | 3.2.0 |
| Java Version | Java | 17 |
| Template Engine | Thymeleaf | 3.x |
| Database (Production) | MySQL | 8.x |
| Database (Development) | H2 | 2.x |
| ORM | Hibernate/JPA | 6.3.x |
| Security | Spring Security | 6.x |
| Build Tool | Maven | 3.x |
| CSS Framework | Bootstrap | 5.x |

## Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd accounting-system

# Run with Maven (uses H2 database by default)
mvn spring-boot:run

# Access the application
# URL: http://localhost:8080
# Username: admin
# Password: admin
```

## Installation

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.x** (for production)

Verify your Java installation:
```bash
java -version
# Should show: openjdk version "17.x.x" or higher
```

### Development Setup

The application is pre-configured to use H2 in-memory database for easy development.

1. **Navigate to project directory**
   ```bash
   cd accounting-system
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Application: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:file:./data/accounting_db`
     - Username: `sa`
     - Password: (leave empty)

### Production Setup

#### 1. Create MySQL Database

```sql
CREATE DATABASE accounting_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'accounting_user'@'localhost'
  IDENTIFIED BY 'your_secure_password';

GRANT ALL PRIVILEGES ON accounting_db.*
  TO 'accounting_user'@'localhost';

FLUSH PRIVILEGES;
```

#### 2. Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
# Comment out H2 configuration
# spring.datasource.url=jdbc:h2:file:./data/accounting_db
# spring.h2.console.enabled=true

# Enable MySQL configuration
spring.datasource.url=jdbc:mysql://localhost:3306/accounting_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=accounting_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

#### 3. Build and Run

```bash
# Build production JAR
mvn clean package -DskipTests

# Run the application
java -jar target/accounting-system-1.0.0-SNAPSHOT.jar
```

## Configuration

### Key Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | HTTP port | 8080 |
| `spring.datasource.url` | Database connection URL | H2 file |
| `spring.jpa.hibernate.ddl-auto` | Schema generation | update |
| `spring.h2.console.enabled` | Enable H2 console | true |
| `logging.level.com.accounting` | Application log level | DEBUG |

### Environment Variables

Override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/accounting_db
export SPRING_DATASOURCE_USERNAME=accounting_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export SERVER_PORT=80
```

## User Guide

### User Roles

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access: user management, all operations, system configuration |
| **ACCOUNTANT** | Create/edit/post/void entries, manage accounts and invoices, bank reconciliation |
| **VIEWER** | Read-only access to dashboard, accounts, entries, and reports |

### Account Types

| Type | Normal Balance | Statement | Examples |
|------|----------------|-----------|----------|
| ASSET | Debit | Balance Sheet | Cash, Accounts Receivable, Equipment |
| LIABILITY | Credit | Balance Sheet | Accounts Payable, Loans |
| EQUITY | Credit | Balance Sheet | Capital, Retained Earnings |
| REVENUE | Credit | Income Statement | Sales, Service Revenue |
| EXPENSE | Debit | Income Statement | Rent, Salaries, Utilities |

### Recommended Account Code Structure

| Range | Type | Examples |
|-------|------|----------|
| 1000-1999 | Assets | 1000 Cash, 1200 Accounts Receivable |
| 2000-2999 | Liabilities | 2000 Accounts Payable |
| 3000-3999 | Equity | 3000 Capital |
| 4000-4999 | Revenue | 4000 Sales Revenue |
| 5000-5999 | Expenses | 5000 COGS, 5100 Salaries |

### Journal Entry Workflow

```
DRAFT → POSTED → VOID
  ↓       ↓       ↓
 Edit    View    View
Delete   Void    Only
 Post
```

### Invoice Workflow

```
DRAFT → SENT → OVERDUE → PAID
  ↓      ↓       ↓        ↓
 Edit   Mark    Mark     Final
Cancel  Paid    Paid
 Send  Cancel  Cancel
```

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│         (Thymeleaf Templates, Bootstrap, JavaScript)         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     Controller Layer                         │
│   (DashboardController, AccountController, JournalController │
│    ReportController, InvoiceController, UserController)      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                           │
│    (AccountService, JournalService, ReportService,           │
│     InvoiceService, BankReconciliationService, UserService)  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                          │
│         (Spring Data JPA Repositories)                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     Database Layer                           │
│              (MySQL / H2)                                    │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns Used

- **MVC Pattern** - Model-View-Controller separation
- **Repository Pattern** - Data access abstraction
- **Service Layer Pattern** - Business logic encapsulation
- **DTO Pattern** - Data transfer between layers
- **Dependency Injection** - Constructor-based injection

### Project Structure

```
accounting-system/
├── pom.xml
├── README.md
├── docs/
│   └── documentation.html
├── src/
│   ├── main/
│   │   ├── java/com/accounting/
│   │   │   ├── AccountingApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
└── data/                    # H2 database files
```

## Database Schema

### Core Tables

| Table | Description |
|-------|-------------|
| `users` | User accounts and authentication |
| `roles` | User roles (ADMIN, ACCOUNTANT, VIEWER) |
| `accounts` | Chart of accounts |
| `currencies` | Currency definitions |
| `journal_entries` | Journal entry headers |
| `journal_entry_lines` | Journal entry line items |
| `customers` | Customer master data |
| `invoices` | Invoice headers |
| `invoice_items` | Invoice line items |
| `bank_accounts` | Bank account definitions |
| `bank_statements` | Imported bank transactions |

### Key Relationships

- `User` → `Role` (Many-to-One)
- `Account` → `Account` (Self-referential for hierarchy)
- `JournalEntry` → `JournalEntryLine` (One-to-Many)
- `JournalEntryLine` → `Account` (Many-to-One)
- `Invoice` → `Customer` (Many-to-One)
- `Invoice` → `InvoiceItem` (One-to-Many)
- `BankAccount` → `Account` (Many-to-One, GL link)

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/login` | Login page |
| POST | `/login` | Process login |
| GET/POST | `/logout` | Logout |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/`, `/dashboard` | Main dashboard |

### Accounts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/accounts` | List active accounts |
| GET | `/accounts/new` | New account form |
| POST | `/accounts/save` | Save account |
| GET | `/accounts/edit/{id}` | Edit account |
| GET | `/accounts/view/{id}` | View account |
| POST | `/accounts/deactivate/{id}` | Deactivate account |

### Journal Entries
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/journal` | List entries |
| GET | `/journal/new` | New entry form |
| POST | `/journal/save` | Save entry |
| GET | `/journal/edit/{id}` | Edit entry |
| GET | `/journal/view/{id}` | View entry |
| POST | `/journal/post/{id}` | Post entry |
| POST | `/journal/void/{id}` | Void entry |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/reports/trial-balance` | Trial Balance |
| GET | `/reports/profit-loss` | Profit & Loss |
| GET | `/reports/balance-sheet` | Balance Sheet |
| GET | `/reports/general-ledger` | General Ledger |

## Security

### Authentication
- Form-based authentication with Spring Security
- BCrypt password encryption
- Session-based authentication with CSRF protection

### Authorization
- Role-based access control
- Method-level security with `@PreAuthorize`
- URL-based security rules

### Security Configuration Highlights

```java
// Public endpoints
.requestMatchers("/css/**", "/js/**", "/login").permitAll()

// Admin only
.requestMatchers("/admin/**", "/users/**").hasRole("ADMIN")

// Accountant or Admin
.requestMatchers("/journal/post/**", "/journal/void/**")
    .hasAnyRole("ADMIN", "ACCOUNTANT")

// All authenticated users
.anyRequest().authenticated()
```

## Deployment

### JAR Deployment

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/accounting-system-1.0.0-SNAPSHOT.jar
```

### Systemd Service (Linux)

```ini
# /etc/systemd/system/accounting.service
[Unit]
Description=Accounting System
After=mysql.service

[Service]
User=accounting
WorkingDirectory=/opt/accounting
ExecStart=/usr/bin/java -jar accounting-system.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable accounting
sudo systemctl start accounting
```

### Docker

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/accounting-system-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t accounting-system .
docker run -d -p 8080:8080 accounting-system
```

## Maintenance

### Database Backup

```bash
# MySQL backup
mysqldump -u root -p accounting_db > backup_$(date +%Y%m%d).sql

# Restore
mysql -u root -p accounting_db < backup_20240101.sql
```

### Log Management

Configure in `application.properties`:
```properties
logging.file.name=logs/accounting.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### Regular Tasks

| Task | Frequency |
|------|-----------|
| Database Backup | Daily |
| Log Rotation | Weekly |
| Security Updates | Monthly |
| Performance Review | Quarterly |

## Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check port availability
lsof -i :8080

# Check Java version
java -version

# Check database connection
mysql -u username -p -h hostname
```

#### Login Issues
- Clear browser cookies
- Verify user is enabled in database
- Enable security debug: `logging.level.org.springframework.security=DEBUG`

#### Journal Entry Won't Balance
- Verify total debits = total credits
- Check for empty amounts
- Ensure each line has only debit OR credit

#### Slow Performance
- Check database indexes
- Increase JVM heap: `java -Xmx2g -jar app.jar`
- Enable query logging to identify slow queries

### Viewing Logs

```bash
# Application logs
tail -f logs/accounting.log

# Systemd logs
journalctl -u accounting -f

# Docker logs
docker logs -f container_name
```

## Documentation

Full documentation is available at:
- **HTML Documentation**: `docs/documentation.html`

Open in browser for comprehensive guides including:
- Complete User Manual
- Development Guide
- Database Schema Details
- API Reference
- Configuration Options
- Deployment Instructions
- Troubleshooting Guide

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow standard Java conventions
- Use Lombok annotations where appropriate
- Write meaningful commit messages
- Add tests for new features

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Support

For issues and questions:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review the full documentation in `docs/documentation.html`
3. Open an issue on the repository

---

**Version**: 1.0.0-SNAPSHOT
**Java**: 17+
**Spring Boot**: 3.2.0