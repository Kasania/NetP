package com.kasania.ui.scenes;

import com.kasania.net.Connection;
import com.kasania.net.DataType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;

public class LoginView {
    private JTextField textField;
    private JButton loginButton;
    private JPanel contentPanel;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    {
        loginButton.addActionListener(this::sendNickname);
        textField.addActionListener(this::sendNickname);
    }

    private void sendNickname(ActionEvent e){
        if(!textField.getText().contentEquals("")){
            DataType.LOGIN.send((textField.getText() + "::"+ Connection.VideoPort + "::"+Connection.AudioPort).getBytes(StandardCharsets.UTF_8));
        }
    }
}
