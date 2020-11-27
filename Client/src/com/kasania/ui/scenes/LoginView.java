package com.kasania.ui.scenes;

import com.kasania.net.Connection;
import com.kasania.net.DataType;
import com.kasania.ui.MainFrame;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class LoginView {
    private JTextField textField;
    private JButton loginButton;
    private JPanel contentPanel;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    {
        loginButton.addActionListener(e -> DataType.LOGIN.send((textField.getText() + "::"+ Connection.VideoPort + "::"+Connection.AudioPort).getBytes(StandardCharsets.UTF_8)));
    }
}
