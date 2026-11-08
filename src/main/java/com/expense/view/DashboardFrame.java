package com.expense.view;

import com.expense.exception.ValidationException;
import com.expense.interfaces.Exporter;
import com.expense.model.Budget;
import com.expense.model.BudgetStatus;
import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.model.Income;
import com.expense.model.Transaction;
import com.expense.model.User;
import com.expense.service.BudgetService;
import com.expense.service.CategoryService;
import com.expense.service.TransactionService;
import com.expense.util.CSVExporter;
import com.expense.util.DatabaseWorker;
import com.expense.util.InputParser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DashboardFrame extends JFrame {

    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-IN"));

    private static final Color INCOME_COLOR = new Color(0x2E7D32);
    private static final Color EXPENSE_COLOR = new Color(0xC62828);
    private static final int AUTO_REFRESH_SECONDS = 30;

    private final User user;

    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;

    private final JLabel incomeLabel = new JLabel(money(BigDecimal.ZERO));
    private final JLabel expenseLabel = new JLabel(money(BigDecimal.ZERO));
    private final JLabel balanceLabel = new JLabel(money(BigDecimal.ZERO));
    private final JLabel statusLabel = new JLabel(" Ready");

    private final ReadOnlyTableModel transactionModel =
            new ReadOnlyTableModel(
                    new Object[]{"ID", "Type", "Category",
                            "Amount", "Description", "Date"},
                    new Class<?>[]{Integer.class, String.class,
                            String.class, BigDecimal.class,
                            String.class, LocalDate.class});

    private final JTable transactionTable =
            new JTable(transactionModel);

    private final ReadOnlyTableModel categoryModel =
            new ReadOnlyTableModel(
                    new Object[]{"Category", "Total spent",
                            "Share of expenses"},
                    new Class<?>[]{String.class, BigDecimal.class,
                            String.class});

    private final JTable categoryTable = new JTable(categoryModel);

    private final JCheckBox autoRefreshBox =
            new JCheckBox("Auto-refresh every "
                    + AUTO_REFRESH_SECONDS + " s");

 
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

 
    private final AtomicBoolean refreshQueued = new AtomicBoolean(false);

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread =
                        new Thread(task, "dashboard-auto-refresh");
                thread.setDaemon(true);
                return thread;
            });

    private ScheduledFuture<?> autoRefreshTask;

    public DashboardFrame(User user) {
        this(user,
                new TransactionService(),
                new BudgetService(),
                new CategoryService());
    }

    public DashboardFrame(User user,
                          TransactionService transactionService,
                          BudgetService budgetService,
                          CategoryService categoryService) {

        this.user = user;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.categoryService = categoryService;

        setTitle("Expense Tracker - " + user.getName());
        setSize(1000, 650);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();

        refreshDashboard();
    }

    @Override
    public void dispose() {
        scheduler.shutdownNow();
        super.dispose();
    }
    private void buildUI() {

        setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("  Personal Expense Tracker");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel welcome =
                new JLabel("Welcome, " + user.getName() + "   ");

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        header.add(title, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);

        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 0));
        cards.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        cards.add(createCard("Total Income", incomeLabel, INCOME_COLOR));
        cards.add(createCard("Total Expenses", expenseLabel,
                EXPENSE_COLOR));
        cards.add(createCard("Balance", balanceLabel, null));

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.add(header, BorderLayout.NORTH);
        north.add(cards, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        styleTable(transactionTable);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(70);

        styleTable(categoryTable);

        JPanel transactionTools =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        transactionTools.add(button("Delete selected",
                this::deleteSelectedTransaction));

        JPanel transactionsTab = new JPanel(new BorderLayout());
        transactionsTab.add(new JScrollPane(transactionTable),
                BorderLayout.CENTER);
        transactionsTab.add(transactionTools, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        tabs.addTab("Transactions", transactionsTab);
        tabs.addTab("Spending by category",
                new JScrollPane(categoryTable));
        add(tabs, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 4, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 15, 4, 15));
        buttons.add(button("Add Income", () -> addTransaction("INCOME")));
        buttons.add(button("Add Expense", () -> addTransaction("EXPENSE")));
        buttons.add(button("Add Category", this::addCategory));
        buttons.add(button("Add Budget", this::addBudget));
        buttons.add(button("View Budgets", this::viewBudgets));
        buttons.add(button("Export CSV", this::exportCSV));
        buttons.add(button("Refresh", this::refreshDashboard));
        buttons.add(button("Logout", this::logout));

        autoRefreshBox.addActionListener(e -> toggleAutoRefresh());

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(autoRefreshBox, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout());
        south.add(buttons, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, JLabel value, Color color) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        value.setFont(new Font("Arial", Font.BOLD, 24));
        value.setHorizontalAlignment(SwingConstants.CENTER);

        if (color != null) {
            value.setForeground(color);
        }

        panel.add(value, BorderLayout.CENTER);

        return panel;
    }

    private static void styleTable(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class,
                new PaddedRenderer(SwingConstants.LEFT));
        table.setDefaultRenderer(Integer.class,
                new PaddedRenderer(SwingConstants.CENTER));
        table.setDefaultRenderer(BigDecimal.class, new MoneyRenderer());
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

  
    private <T> void runAsync(String status,
                              DatabaseWorker.DatabaseTask<T> task,
                              Consumer<T> onSuccess,
                              String errorTitle) {

        statusLabel.setText(" " + status);

        new DatabaseWorker<T>(
                task,
                result -> {
                    statusLabel.setText(" Ready");
                    onSuccess.accept(result);
                },
                error -> {
                    statusLabel.setText(" " + errorTitle);
                    showError(errorTitle, error);
                },
                null
        ).execute();
    }

    private void toggleAutoRefresh() {

        if (autoRefreshBox.isSelected()) {

            autoRefreshTask = scheduler.scheduleAtFixedRate(
                    () -> SwingUtilities.invokeLater(
                            this::refreshDashboard),
                    AUTO_REFRESH_SECONDS,
                    AUTO_REFRESH_SECONDS,
                    TimeUnit.SECONDS
            );

        } else if (autoRefreshTask != null) {

            autoRefreshTask.cancel(false);
            autoRefreshTask = null;
        }
    }

    private record DashboardData(
            List<Transaction> transactions,
            Map<Integer, String> categoryNames,
            BigDecimal income,
            BigDecimal expenses,
            BigDecimal balance,
            Map<String, BigDecimal> expenseByCategory
    ) {
    }

    private void refreshDashboard() {

   
        if (!refreshing.compareAndSet(false, true)) {
            refreshQueued.set(true);
            return;
        }

        statusLabel.setText(" Loading...");

        new DatabaseWorker<DashboardData>(
                () -> {

                    int userId = user.getUserId();

                    categoryService.ensureDefaults(userId);

                    List<Transaction> transactions =
                            transactionService.getTransactions(userId);

                    Map<Integer, String> names =
                            categoryService.getCategoryNames(userId);

                    Map<String, BigDecimal> totalsByType =
                            TransactionService.sumBy(
                                    transactions,
                                    t -> true,
                                    Transaction::getType);

                    return new DashboardData(
                            transactions,
                            names,
                            totalsByType.getOrDefault(
                                    "INCOME", BigDecimal.ZERO),
                            totalsByType.getOrDefault(
                                    "EXPENSE", BigDecimal.ZERO),
                            transactionService.calculateBalance(
                                    transactions),
                            transactionService.expenseTotalsByCategory(
                                    transactions, names)
                    );
                },

                this::showDashboard,

                error -> {
                    statusLabel.setText(" Could not load data");
                    showError("Dashboard Error", error);
                },

                () -> {
                    refreshing.set(false);

                    if (refreshQueued.getAndSet(false)) {
                        refreshDashboard();
                    }
                }

        ).execute();
    }

    private void showDashboard(DashboardData data) {

        incomeLabel.setText(money(data.income()));
        expenseLabel.setText(money(data.expenses()));
        balanceLabel.setText(money(data.balance()));
        balanceLabel.setForeground(
                data.balance().signum() < 0
                        ? EXPENSE_COLOR
                        : INCOME_COLOR);

        transactionModel.setRowCount(0);

        for (Transaction t : data.transactions()) {

            transactionModel.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getType(),
                    data.categoryNames().getOrDefault(
                            t.getCategoryId(), "-"),
                    t.getAmount(),
                    t.getDescription(),
                    t.getTransactionDate()
            });
        }

        categoryModel.setRowCount(0);

        data.expenseByCategory().forEach((name, total) ->
                categoryModel.addRow(new Object[]{
                        name,
                        total,
                        share(total, data.expenses())
                }));

        statusLabel.setText(
                " Updated at " + LocalTime.now().withNano(0));
    }

 
    private void addTransaction(String type) {

        runAsync(
                "Loading categories...",
                () -> categoryService.getCategories(
                        user.getUserId(), type),
                categories -> showTransactionDialog(type, categories),
                "Error"
        );
    }

    private void showTransactionDialog(String type,
                                       List<Category> categories) {

        String label = "INCOME".equals(type) ? "income" : "expense";

        if (categories.isEmpty()) {
            info("You have no " + label
                    + " categories yet. Use \"Add Category\" first.");
            return;
        }

        JComboBox<Category> categoryBox =
                new JComboBox<>(categories.toArray(new Category[0]));

        JTextField amountField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField dateField =
                new JTextField(LocalDate.now().toString());

        JPanel panel = formPanel();
        addRow(panel, "Category:", categoryBox);
        addRow(panel, "Amount:", amountField);
        addRow(panel, "Description:", descriptionField);
        addRow(panel, "Date (YYYY-MM-DD):", dateField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add " + label,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Category category = (Category) categoryBox.getSelectedItem();

        try {

            BigDecimal amount =
                    InputParser.parseAmount(amountField.getText());

            LocalDate date =
                    InputParser.parseDate(dateField.getText());

            String description = descriptionField.getText().trim();

            Transaction transaction = "INCOME".equals(type)
                    ? new Income(user.getUserId(),
                            category.getCategoryId(),
                            amount, description, date)
                    : new Expense(user.getUserId(),
                            category.getCategoryId(),
                            amount, description, date);

            runAsync(
                    "Saving transaction...",
                    () -> {
                        transactionService.addTransaction(transaction);
                        return null;
                    },
                    saved -> refreshDashboard(),
                    "Transaction Error"
            );

        } catch (ValidationException ex) {
            showError("Invalid Input", ex);
        }
    }


    private void deleteSelectedTransaction() {

        int viewRow = transactionTable.getSelectedRow();

        if (viewRow < 0) {
            info("Select a transaction in the table first.");
            return;
        }

      
        int row = transactionTable.convertRowIndexToModel(viewRow);

        int id = (Integer) transactionModel.getValueAt(row, 0);

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete transaction #" + id + "? This cannot be undone.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        runAsync(
                "Deleting transaction...",
                () -> {
                    transactionService.deleteTransaction(
                            id, user.getUserId());
                    return null;
                },
                deleted -> refreshDashboard(),
                "Delete Error"
        );
    }

    private void addCategory() {

        JTextField nameField = new JTextField();

        JComboBox<String> typeBox =
                new JComboBox<>(new String[]{"EXPENSE", "INCOME"});

        JPanel panel = formPanel();
        addRow(panel, "Name:", nameField);
        addRow(panel, "Type:", typeBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Category",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Category category = new Category(
                user.getUserId(),
                nameField.getText(),
                (String) typeBox.getSelectedItem()
        );

        runAsync(
                "Saving category...",
                () -> {
                    categoryService.addCategory(category);
                    return null;
                },
                saved -> info("Category added."),
                "Category Error"
        );
    }



    private record BudgetView(List<BudgetStatus> statuses,
                              Map<Integer, String> names) {
    }

    private void addBudget() {

        runAsync(
                "Loading categories...",
                () -> categoryService.getCategories(
                        user.getUserId(), "EXPENSE"),
                this::showBudgetDialog,
                "Budget Error"
        );
    }

    private void showBudgetDialog(List<Category> expenseCategories) {

        Category overall = new Category(
                0, user.getUserId(), "All expenses", "EXPENSE");

        JComboBox<Category> categoryBox = new JComboBox<>();
        categoryBox.addItem(overall);
        expenseCategories.forEach(categoryBox::addItem);

        LocalDate today = LocalDate.now();

        JTextField amountField = new JTextField();
        JTextField monthField =
                new JTextField(String.valueOf(today.getMonthValue()));
        JTextField yearField =
                new JTextField(String.valueOf(today.getYear()));

        JPanel panel = formPanel();
        addRow(panel, "Applies to:", categoryBox);
        addRow(panel, "Budget amount:", amountField);
        addRow(panel, "Month (1-12):", monthField);
        addRow(panel, "Year:", yearField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Budget",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            BigDecimal limit =
                    InputParser.parseAmount(amountField.getText());

            int month =
                    InputParser.parseInt(monthField.getText(), "Month");

            int year =
                    InputParser.parseInt(yearField.getText(), "Year");

            Category selected = (Category) categoryBox.getSelectedItem();

            Integer categoryId =
                    (selected == null || selected.getCategoryId() == 0)
                            ? null
                            : selected.getCategoryId();

            Budget budget = new Budget(
                    user.getUserId(), categoryId, limit, month, year);

            runAsync(
                    "Saving budget...",
                    () -> {
                        budgetService.addBudget(budget);
                        return null;
                    },
                    saved -> info("Budget added successfully."),
                    "Budget Error"
            );

        } catch (ValidationException ex) {
            showError("Invalid Input", ex);
        }
    }

    private void viewBudgets() {

        runAsync(
                "Loading budgets...",
                () -> new BudgetView(
                        budgetService.getBudgetStatuses(user.getUserId()),
                        categoryService.getCategoryNames(user.getUserId())),
                this::showBudgets,
                "Budget Error"
        );
    }

    private void showBudgets(BudgetView view) {

        if (view.statuses().isEmpty()) {
            info("You have not set any budgets yet.");
            return;
        }

        ReadOnlyTableModel model = new ReadOnlyTableModel(
                new Object[]{"Period", "Category", "Limit", "Spent",
                        "Remaining", "Used", "Status"},
                new Class<?>[]{String.class, String.class,
                        BigDecimal.class, BigDecimal.class,
                        BigDecimal.class, String.class, String.class});

        for (BudgetStatus status : view.statuses()) {

            Budget budget = status.budget();

            String category = budget.getCategoryId() == null
                    ? "All expenses"
                    : view.names().getOrDefault(
                            budget.getCategoryId(),
                            "Category " + budget.getCategoryId());

            model.addRow(new Object[]{
                    String.format("%02d/%d",
                            budget.getMonth(), budget.getYear()),
                    category,
                    budget.getAmountLimit(),
                    status.spent(),
                    status.remaining(),
                    status.percentUsed() + "%",
                    status.isExceeded() ? "Over budget" : "Within budget"
            });
        }

        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(720, 260));

        JOptionPane.showMessageDialog(
                this, scroll, "Budgets", JOptionPane.PLAIN_MESSAGE);
    }


    private void exportCSV() {

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("expense-report.csv"));

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (file.exists() && JOptionPane.showConfirmDialog(
                this,
                file.getName() + " already exists. Replace it?",
                "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        runAsync(
                "Exporting...",
                () -> {

                    int userId = user.getUserId();

                    List<Transaction> transactions =
                            transactionService.getTransactions(userId);

                    Exporter<Transaction> exporter = new CSVExporter(
                            categoryService.getCategoryNames(userId));

                    exporter.export(transactions, file);

                    return transactions.size();
                },
                count -> info("Exported " + count + " transactions."),
                "Export Error"
        );
    }

    private void logout() {

        dispose();

        new LoginFrame().setVisible(true);
    }


    private static String money(BigDecimal amount) {
        return MONEY.format(amount);
    }

    private static String share(BigDecimal part, BigDecimal whole) {

        if (whole.signum() == 0) {
            return "0%";
        }

        return part.multiply(BigDecimal.valueOf(100))
                .divide(whole, 0, RoundingMode.HALF_UP) + "%";
    }

    private static JPanel formPanel() {
        return new JPanel(new GridLayout(0, 2, 8, 8));
    }

    private static void addRow(JPanel panel, String label, Component field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showError(String title, Exception error) {

        String message = error.getMessage() == null
                ? error.toString()
                : error.getMessage();

        JOptionPane.showMessageDialog(
                this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private static final class ReadOnlyTableModel
            extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        private final transient Class<?>[] types;

        ReadOnlyTableModel(Object[] columns, Class<?>[] types) {
            super(columns, 0);
            this.types = types;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return types[column];
        }
    }

 
    private static class PaddedRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        PaddedRenderer(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(
                    table, value, isSelected, false, row, column);

         
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            return this;
        }
    }

    private static final class MoneyRenderer extends PaddedRenderer {

        private static final long serialVersionUID = 1L;

        MoneyRenderer() {
            super(SwingConstants.RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            setText(value instanceof BigDecimal amount
                    ? MONEY.format(amount)
                    : String.valueOf(value));
        }
    }
}
