package com.expense.view;

import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.model.Income;
import com.expense.model.Transaction;
import com.expense.model.User;
import com.expense.service.BudgetService;
import com.expense.service.TransactionService;
import com.expense.util.CSVExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final User user;

    private final TransactionService transactionService =
            new TransactionService();

    private final BudgetService budgetService =
            new BudgetService();

    private final JLabel incomeLabel =
            new JLabel("₹0.00");

    private final JLabel expenseLabel =
            new JLabel("₹0.00");

    private final JLabel balanceLabel =
            new JLabel("₹0.00");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new Object[]{
                            "ID",
                            "Type",
                            "Amount",
                            "Description",
                            "Date"
                    }, 0);

    private final JTable transactionTable =
            new JTable(tableModel);

    public DashboardFrame(User user) {

        this.user = user;

        setTitle(
                "Expense Tracker - " +
                        user.getName()
        );

        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        buildUI();

        refreshDashboard();
      
        private record DashboardData(
        List<Transaction> transactions,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
) {
}
    }

    private void buildUI() {

        setLayout(new BorderLayout());

        JPanel header =
                new JPanel(new BorderLayout());

        JLabel title =
                new JLabel(
                        "  Personal Expense Tracker"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );

        JLabel userLabel =
                new JLabel(
                        "Welcome, " +
                                user.getName() +
                                "   "
                );

        header.add(
                title,
                BorderLayout.WEST
        );

        header.add(
                userLabel,
                BorderLayout.EAST
        );

        add(
                header,
                BorderLayout.NORTH
        );

        JPanel cards =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                15,
                                15
                        )
                );

        cards.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        cards.add(
                createCard(
                        "Total Income",
                        incomeLabel
                )
        );

        cards.add(
                createCard(
                        "Total Expenses",
                        expenseLabel
                )
        );

        cards.add(
                createCard(
                        "Balance",
                        balanceLabel
                )
        );

        JPanel top =
                new JPanel(
                        new BorderLayout()
                );

        top.add(
                cards,
                BorderLayout.CENTER
        );

        add(
                top,
                BorderLayout.CENTER
        );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        JButton addIncome =
                new JButton(
                        "Add Income"
                );

        JButton addExpense =
                new JButton(
                        "Add Expense"
                );

        JButton refresh =
                new JButton(
                        "Refresh"
                );

        JButton export =
                new JButton(
                        "Export CSV"
                );

        JButton budget =
                new JButton(
                        "Add Budget"
                );

        JButton logout =
                new JButton(
                        "Logout"
                );

        bottom.add(addIncome);
        bottom.add(addExpense);
        bottom.add(budget);
        bottom.add(export);
        bottom.add(refresh);
        bottom.add(logout);

        add(
                bottom,
                BorderLayout.SOUTH
        );

        add(
                new JScrollPane(
                        transactionTable
                ),
                BorderLayout.EAST
        );

        transactionTable.setPreferredScrollableViewportSize(
                new Dimension(500, 300)
        );

        addIncome.addActionListener(
                e -> addTransaction("INCOME")
        );

        addExpense.addActionListener(
                e -> addTransaction("EXPENSE")
        );

        refresh.addActionListener(
                e -> refreshDashboard()
        );

        export.addActionListener(
                e -> exportCSV()
        );

        budget.addActionListener(
                e -> addBudget()
        );

        logout.addActionListener(e -> {

            dispose();

            new LoginFrame()
                    .setVisible(true);
        });
    }

    private JPanel createCard(
            String title,
            JLabel value) {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        title
                )
        );

        value.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        value.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        panel.add(
                value,
                BorderLayout.CENTER
        );

        return panel;
    }

    private void addTransaction(
            String type) {

        try {

           CategoryService categoryService =
               new CategoryService();

          List<Category> categories =
                categoryService.getCategories(
                user.getUserId(),
                type
        );

            JComboBox<Category> categoryBox =
                    new JComboBox<>();

            for (Category category :
                    categories) {

                if (category.getType()
                        .equals(type)) {

                    categoryBox.addItem(
                            category
                    );
                }
            }

            JTextField amountField =
                    new JTextField();

            JTextField descriptionField =
                    new JTextField();

            JTextField dateField =
                    new JTextField(
                            LocalDate.now().toString()
                    );

            JPanel panel =
                    new JPanel(
                            new GridLayout(
                                    0,
                                    2,
                                    8,
                                    8
                            )
                    );

            panel.add(
                    new JLabel("Category:")
            );

            panel.add(
                    categoryBox
            );

            panel.add(
                    new JLabel("Amount:")
            );

            panel.add(
                    amountField
            );

            panel.add(
                    new JLabel("Description:")
            );

            panel.add(
                    descriptionField
            );

            panel.add(
                    new JLabel("Date (YYYY-MM-DD):")
            );

            panel.add(
                    dateField
            );

            int result =
                    JOptionPane.showConfirmDialog(
                            this,
                            panel,
                            "Add " + type,
                            JOptionPane.OK_CANCEL_OPTION
                    );

            if (result !=
                    JOptionPane.OK_OPTION) {

                return;
            }

            Category category =
                    (Category)
                            categoryBox
                                    .getSelectedItem();

            if (category == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select a category."
                );

                return;
            }

            BigDecimal amount =
                    new BigDecimal(
                            amountField.getText()
                    );

            LocalDate date =
                    LocalDate.parse(
                            dateField.getText()
                    );

            Transaction transaction;

            if ("INCOME".equals(type)) {

                transaction =
                        new Income(
                                user.getUserId(),
                                category.getCategoryId(),
                                amount,
                                descriptionField
                                        .getText(),
                                date
                        );

            } else {

                transaction =
                        new Expense(
                                user.getUserId(),
                                category.getCategoryId(),
                                amount,
                                descriptionField
                                        .getText(),
                                date
                        );
            }

            transactionService
                    .addTransaction(
                            transaction
                    );

            refreshDashboard();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

 private void refreshDashboard() {

    new com.expense.util.DatabaseWorker<>(
            () -> {

                List<Transaction> transactions =
                        transactionService
                                .getTransactions(
                                        user.getUserId()
                                );

                BigDecimal balance =
                        transactionService
                                .calculateBalance(
                                        transactions
                                );

                BigDecimal income =
                        BigDecimal.ZERO;

                BigDecimal expenses =
                        BigDecimal.ZERO;

                for (Transaction transaction :
                        transactions) {

                    if ("INCOME".equals(
                            transaction.getType())) {

                        income = income.add(
                                transaction.getAmount()
                        );

                    } else {

                        expenses = expenses.add(
                                transaction.getAmount()
                        );
                    }
                }

                return new DashboardData(
                        transactions,
                        income,
                        expenses,
                        balance
                );
            },

            data -> {

                incomeLabel.setText(
                        "₹" + data.income()
                );

                expenseLabel.setText(
                        "₹" + data.expenses()
                );

                balanceLabel.setText(
                        "₹" + data.balance()
                );

                tableModel.setRowCount(0);

                for (Transaction t :
                        data.transactions()) {

                    tableModel.addRow(
                            new Object[]{
                                    t.getTransactionId(),
                                    t.getType(),
                                    "₹" + t.getAmount(),
                                    t.getDescription(),
                                    t.getTransactionDate()
                            }
                    );
                }
            },

            error -> JOptionPane.showMessageDialog(
                    this,
                    error.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE
            ),

            null

    ).execute();
}

    private void loadTransactions()
            throws Exception {

        tableModel.setRowCount(0);

        List<Transaction> transactions =
                transactionService
                        .getTransactions(
                                user.getUserId()
                        );

        for (Transaction t :
                transactions) {

            tableModel.addRow(
                    new Object[]{
                            t.getTransactionId(),
                            t.getType(),
                            "₹" + t.getAmount(),
                            t.getDescription(),
                            t.getTransactionDate()
                    }
            );
        }
    }

private void exportCSV() {

    JFileChooser chooser =
            new JFileChooser();

    chooser.setSelectedFile(
            new File("expense-report.csv")
    );

    if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {

        return;
    }

    File file =
            chooser.getSelectedFile();

    new com.expense.util.DatabaseWorker<>(
            () -> transactionService
                    .getTransactions(
                            user.getUserId()
                    ),

            transactions -> {

                try {

                    CSVExporter.exportTransactions(
                            transactions,
                            file
                    );

                    JOptionPane.showMessageDialog(
                            this,
                            "CSV exported successfully."
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            this,
                            ex.getMessage(),
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            },

            error -> JOptionPane.showMessageDialog(
                    this,
                    error.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            ),

            null

    ).execute();
}

    private void addBudget() {

        try {

            JTextField amount =
                    new JTextField();

            JTextField month =
                    new JTextField(
                            String.valueOf(
                                    java.time.LocalDate
                                            .now()
                                            .getMonthValue()
                            )
                    );

            JTextField year =
                    new JTextField(
                            String.valueOf(
                                    java.time.LocalDate
                                            .now()
                                            .getYear()
                            )
                    );

            JPanel panel =
                    new JPanel(
                            new GridLayout(
                                    0,
                                    2,
                                    8,
                                    8
                            )
                    );

            panel.add(
                    new JLabel(
                            "Budget Amount:"
                    )
            );

            panel.add(amount);

            panel.add(
                    new JLabel(
                            "Month:"
                    )
            );

            panel.add(month);

            panel.add(
                    new JLabel(
                            "Year:"
                    )
            );

            panel.add(year);

            int result =
                    JOptionPane.showConfirmDialog(
                            this,
                            panel,
                            "Add Budget",
                            JOptionPane.OK_CANCEL_OPTION
                    );

            if (result !=
                    JOptionPane.OK_OPTION) {

                return;
            }

            com.expense.model.Budget budget =
                    new com.expense.model.Budget(
                            user.getUserId(),
                            null,
                            new BigDecimal(
                                    amount.getText()
                            ),
                            Integer.parseInt(
                                    month.getText()
                            ),
                            Integer.parseInt(
                                    year.getText()
                            )
                    );

            budgetService.addBudget(
                    budget
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Budget added successfully."
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Budget Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
