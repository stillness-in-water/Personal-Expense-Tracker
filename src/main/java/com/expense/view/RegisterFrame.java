package com.expense.view;

import com.expense.service.UserService;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField =
            new JPasswordField();
    private final JButton registerButton =
        new JButton("Register");
    
    public RegisterFrame() {

        setTitle("Create Account");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title =
                new JLabel("Personal Expense Tracker");

        title.setFont(
                new Font("Arial", Font.BOLD, 22)
        );

        addRow(panel, gbc, 0,
                title, null);

        addRow(panel, gbc, 1,
                new JLabel("Name"), nameField);

        addRow(panel, gbc, 2,
                new JLabel("Email"), emailField);

        addRow(panel, gbc, 3,
                new JLabel("Password"), passwordField);

        JButton registerButton =
                new JButton("Register");

        JButton backButton =
                new JButton("Back to Login");

        JPanel buttons = new JPanel();

        buttons.add(registerButton);
        buttons.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;

        panel.add(buttons, gbc);

        add(panel);

        registerButton.addActionListener(e ->
                register());

        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void addRow(JPanel panel,
                        GridBagConstraints gbc,
                        int row,
                        Component label,
                        Component field) {

        gbc.gridy = row;

        if (field == null) {

            gbc.gridx = 0;
            gbc.gridwidth = 2;

            panel.add(label, gbc);

        } else {

            gbc.gridwidth = 1;

            gbc.gridx = 0;
            panel.add(label, gbc);

            gbc.gridx = 1;
            panel.add(field, gbc);
        }
    }

 private void register() {

    String name =
            nameField.getText().trim();

    String email =
            emailField.getText().trim();

    String password =
            new String(
                    passwordField.getPassword()
            );

    registerButton.setEnabled(false);

    new com.expense.util.DatabaseWorker<Void>(
            () -> {

                UserService service =
                        new UserService();

                service.register(
                        name,
                        email,
                        password
                );

                return null;
            },

            ignored -> {

                JOptionPane.showMessageDialog(
                        this,
                        "Registration successful!"
                );

                dispose();

                new LoginFrame()
                        .setVisible(true);
            },

            error -> {

                JOptionPane.showMessageDialog(
                        this,
                        error.getMessage(),
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE
                );
            },

            () -> registerButton.setEnabled(true)

    ).execute();
}
}
