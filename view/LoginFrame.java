package com.expense.view;

import com.expense.model.User;
import com.expense.service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField =
            new JTextField();

    private final JPasswordField passwordField =
            new JPasswordField();

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

        JButton loginButton =
                new JButton("Login");

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

        try {

            String email =
                    emailField.getText();

            String password =
                    new String(
                            passwordField.getPassword()
                    );

            UserService service =
                    new UserService();

            User user =
                    service.login(
                            email,
                            password
                    );

            if (user == null) {

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

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
