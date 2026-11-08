package com.expense.view;

import com.expense.service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField =
            new JTextField();

    private final JPasswordField passwordField =
            new JPasswordField();

    private final JButton loginButton =
            new JButton("Login");

    public LoginFrame() {

        setTitle("Personal Expense Tracker");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel =
                new JPanel(new GridBagLayout());

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(10, 10, 10, 10);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel title =
                new JLabel("Personal Expense Tracker");

        title.setFont(
                new Font("Arial",
                        Font.BOLD,
                        22)
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;

        panel.add(
                new JLabel("Email:"),
                gbc
        );

        gbc.gridx = 1;

        panel.add(
                emailField,
                gbc
        );

        gbc.gridx = 0;
        gbc.gridy = 2;

        panel.add(
                new JLabel("Password:"),
                gbc
        );

        gbc.gridx = 1;

        panel.add(
                passwordField,
                gbc
        );

        JButton registerButton =
                new JButton("Create Account");

        JPanel buttons =
                new JPanel();

        buttons.add(loginButton);
        buttons.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        panel.add(buttons, gbc);

        add(panel);

        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(
                e -> login()
        );

        registerButton.addActionListener(e -> {

            dispose();

            new RegisterFrame()
                    .setVisible(true);
        });
    }

    private void login() {

        String email = emailField.getText().trim();

        String password =
                new String(passwordField.getPassword());

        if (email.isBlank() || password.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Email and password are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        loginButton.setEnabled(false);

        new com.expense.util.DatabaseWorker<>(
                () -> {

                    UserService service =
                            new UserService();

                    return service.login(
                            email,
                            password
                    );
                },

                user -> {

                    if (user == null) {

                        passwordField.setText("");

                        JOptionPane.showMessageDialog(
                                this,
                                "Invalid email or password.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }

                    dispose();

                    new DashboardFrame(user)
                            .setVisible(true);
                },

                error -> JOptionPane.showMessageDialog(
                        this,
                        error.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                ),

                () -> loginButton.setEnabled(true)

        ).execute();
    }
}
