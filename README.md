# Personal Expense Tracker

A Java Swing desktop application for tracking income, expenses and monthly
budgets, backed by a MySQL database through JDBC. Built as a university
project.

## Features

- Registration and login (passwords stored as salted PBKDF2 hashes)
- Starter categories created automatically for every new account
- Add income and expenses, each filed under one of your own categories
- Add your own categories, delete transactions
- Dashboard with total income, total expenses and balance
- Spending by category with each category's share of total expenses
- Monthly budgets, either for all expenses or for one category, with a
  "within budget / over budget" status
- CSV export of all transactions
- Optional auto-refresh of the dashboard every 30 seconds
- Every database call runs on a background thread, so the window never
  freezes

## Technologies

- Java 17+
- Java Swing
- MySQL 8+ and JDBC (MySQL Connector/J)
- Maven, JUnit 5

## Where to find each concept

| **Inheritance and polymorphism** | 
`Transaction` is abstract; `Income` and `Expense` override `calculateImpact()` and `getType()`. `TransactionService.calculateBalance()` adds up a mixed list without checking types. |

| **Interfaces** |
`Repository<T, ID>` and `UserScopedRepository<T, ID>` (implemented by the DAOs), `Exporter<T>` (implemented by `CSVExporter`), `RowMapper<T>` (a functional interface used by every DAO). |

| **Exception handling** |
`ValidationException` for bad user input and `DatabaseException` for connection problems. The UI turns both into readable dialogs. |

| **Generics** |
Generic interfaces above; `DatabaseWorker<T>`; `JdbcHelper.toList(ResultSet, RowMapper<T>)`; `TransactionService.sumBy` is a generic method with a bounded type (`K extends Comparable<K>`). |

| **Collections** | `TreeMap` (sums sorted by key) and `LinkedHashMap` (categories sorted by spending) in `TransactionService`; `ConcurrentHashMap` cache and `Set` duplicate check in `CategoryService`; streams for filtering. |

| **Multithreading** | `DatabaseWorker` extends `SwingWorker`, so every query runs off the UI thread and the result comes back on it. The dashboard's optional auto-refresh uses a `ScheduledExecutorService`. |

| **Synchronization** | Service methods that check-then-save are `synchronized` (all workers of a window share one service instance). `CategoryService` uses `ConcurrentHashMap.computeIfAbsent` so concurrent readers cause one database read. The dashboard uses `AtomicBoolean`s so only one refresh runs at a time and none is lost. |

| **Database classes** | `UserDAO`, `CategoryDAO`, `TransactionDAO`, `BudgetDAO`: full CRUD, plus per-user queries and aggregate queries (totals, budget spending). |

| **Database connectivity** | `DBConnection` builds connections from environment variables and wraps `SQLException` in `DatabaseException`. |

| **JDBC** | `PreparedStatement` everywhere (no string-built SQL values), try-with-resources, generated keys, a JDBC batch (`CategoryDAO.saveAll`), and a real transaction with commit and rollback (`UserDAO.saveWithDefaultCategories` saves a user and their starter categories together). |

| **Testing** | JUnit 5 tests for authentication, password hashing, transaction validation and calculations, budgets, the category cache under concurrent access, CSV export, input parsing and `DatabaseWorker`. |

## Database design

- `users` → `categories`, `transactions`, `budgets` (all cascade on user
  delete)
- `transactions.category_id` is `ON DELETE RESTRICT`, so a category that is
  still in use cannot be deleted by accident
- Money is stored as `DECIMAL(12,2)` with `CHECK (amount > 0)`
- `UNIQUE (user_id, category_name, type)` prevents duplicate categories and
  `UNIQUE (user_id, category_id, month, year)` prevents duplicate budgets
  (MySQL ignores `NULL` in unique keys, so `BudgetService` also checks for a
  second "all expenses" budget in the same month)
- Index on `transactions (user_id, transaction_date)` for the history query

## Project Structure

```text
PROJECT
├---app
  *....Main.java
├---database
  *....DBConnection.java
  *....schema.sql
├---model
  *....Budget.java
  *....Category.java
  *....Expense.java
  *....Income.java
  *....Transaction.java
  *....User.java
├---interfaces
  *....Repository.java
  *....UserScopedRepository.java
├---dao
  *....BudgetDAO.java
  *....CategoryDAO.java
  *,,,,TranasactionDAO.java
  *....UserDAO.java
├---service
  *....BudgetSerice.java
  *....CategorySerice.java
  *....TransactionSerice.java
  *....UserSerice.java
├---exception
  *....ValidationException.java
├---util
  *....CSVExporter.java
  *....DatabaseWorker.java
  *....PasswordUtil.java
|---test
  *....AuthenticationTest.java
  *....TransactionTest.java
  *....UserServiceTest.java
|---view
  *....DashboardFrame.java
  *....LoginFrame.java
  *....RegisterFrame.java
|---pom.xml
|---README.MD

