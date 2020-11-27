package com.kasania.ui;

import com.kasania.sound.AudioReader;
import com.kasania.net.Connection;
import com.kasania.ui.scenes.SceneNavigator;

import javax.swing.*;

public class MainFrame {

    private Connection connection;
    private AudioReader audioReader;

    private JFrame frame;

    private SceneNavigator navigator;


    public MainFrame(int videoPort, int audioPort){

        initComponents();
        connection.connect("223.194.154.137",11111, videoPort, audioPort);

        navigator.navigateTo(SceneNavigator.Items.LOGIN);

    }


    private void initComponents(){
        audioReader = new AudioReader();
        connection = new Connection();
        navigator = new SceneNavigator();

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        navigator.setContentRoot((JPanel) frame.getContentPane());

        frame.setBounds(0,0,1280,720);

        frame.setVisible(true);

    }

}
