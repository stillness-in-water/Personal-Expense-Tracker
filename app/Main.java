package com.expense.app;

import com.expense.database.DBConnection;
import com.expense.util.DatabaseWorker;
import com.expense.view.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );

            } catch (Exception ignored) {
            
            }

            LoginFrame login = new LoginFrame();
            login.setVisible(true);
       
            new DatabaseWorker<Boolean>(
                    () -> {
                        DBConnection.testConnection();
                        return true;
                    },
                    connected -> { },
                    error -> JOptionPane.showMessageDialog(
                            login,
                            error.getMessage(),
                            "Database Unavailable",
                            JOptionPane.WARNING_MESSAGE
                    ),
                    null
            ).execute();
        });
    }
}
