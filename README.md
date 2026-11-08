# Personal Expense Tracker

A Java Swing based Personal Expense Tracker developed as a university project.

## Features

- User registration and login
- Income and expense management
- Transaction categories
- Dashboard
- Balance calculation
- Transaction history
- Monthly budgets
- CSV report export
- MySQL database integration
- JDBC connectivity
- Object-oriented architecture
- Generic Repository interface
- DAO and Service layers

## Technologies

- Java 17+
- Java Swing
- MySQL 8+
- JDBC
- Maven
- Git/GitHub

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

