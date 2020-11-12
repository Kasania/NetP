package com.kasania.ui;

import com.kasania.net.Connection;
import com.kasania.ui.scenes.SceneNavigator;

import javax.swing.*;

public class MainFrame {

    private Connection connection;

    private JFrame frame;

    private SceneNavigator navigator;

    {

        initComponents();

        navigator.navigateTo(SceneNavigator.Items.LOGIN);

    }


    private void initComponents(){
        connection = new Connection();
        navigator = new SceneNavigator();

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        navigator.setContentRoot((JPanel) frame.getContentPane());

        frame.setBounds(0,0,1280,720);

        frame.setVisible(true);

    }

}
