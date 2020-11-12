package com.kasania.ui.scenes;

import com.kasania.net.DataType;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class LoginScene extends Scene {


    private JButton loginButton;
    private JTextField textField;
    {

        textField = new JTextField();
        loginButton = new JButton("Login");

        JPanel panel = new JPanel(new GridLayout(10,3));
        JPanel panel2 = new JPanel(new BorderLayout());
        JLabel logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        logoLabel.setFont(new Font("Consolas",Font.BOLD,54));

        
        logoLabel.setText("LOGO");
        for(int i = 0; i<10; ++i){
            panel.add(new JLabel(" "));
        }
        for(int i = 0; i<2; ++i){
            panel.add(new JLabel(" "));
        }
        panel.add(logoLabel,BorderLayout.NORTH);
        panel.add(new JLabel(" "));

        panel2.add(new JLabel("Nickname"),BorderLayout.WEST);
        panel2.add(textField,BorderLayout.CENTER);
        panel.add(panel2);
        for(int i = 0; i<4; ++i) {
            panel.add(new JLabel(" "), BorderLayout.SOUTH);
        }
        panel.add(loginButton);

        contentPanel.add(panel,BorderLayout.CENTER);

        contentPanel.add(new JLabel(" "),BorderLayout.WEST);
        contentPanel.add(new JLabel(" "),BorderLayout.EAST);
        contentPanel.add(new JLabel(" "), BorderLayout.SOUTH);

    }

    public void addOnLoginButtonPressed(Runnable action){
        loginButton.addActionListener(e -> {
            DataType.LOGIN.send(textField.getText().getBytes(StandardCharsets.UTF_8));
            action.run();
        });
    }



}
