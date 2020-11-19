package com.kasania.ui.scenes;

import com.kasania.net.DataType;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class LoginView {
    private JTextField textField;
    private JButton loginButton;
    private JPanel contentPanel;

    public JPanel getContentPanel() {
        return contentPanel;
    }


    public void addOnLoginButtonPressed(Runnable action){
        loginButton.addActionListener(e -> {
            DataType.LOGIN.send(textField.getText().getBytes(StandardCharsets.UTF_8));
            action.run();
        });
    }
}
