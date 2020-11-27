package com.kasania.ui.scenes;

import com.kasania.net.DataType;
import com.kasania.net.UserInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConversationScene extends Scene{

    private final Canvas canvas;

    private final JTextArea textArea = new JTextArea();
    private final JTextField textField = new JTextField(20);

    {
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        canvas = new Canvas();
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));

        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        chatPanel.add(scrollPane,BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());

        JButton button = new JButton("Send");

        button.addActionListener((e)-> sendChat() );
        textField.addActionListener((e)-> sendChat() );

        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(button, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        contentPanel.add(canvas,BorderLayout.CENTER);
        contentPanel.add(chatPanel,BorderLayout.EAST);


        DataType.IMAGE.addReceiver(this::drawImage);
        DataType.TEXT.addReceiver(this::appendChat);
    }

    public void drawImage(UserInfo info, byte[] data){
        if(canvas.getBufferStrategy() == null){
            canvas.createBufferStrategy(2);
            return;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            BufferedImage image = ImageIO.read(byteArrayInputStream);
            Graphics2D g2d = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
            g2d.drawImage(image, info.idx*240,0,null);
            g2d.dispose();
            canvas.getBufferStrategy().show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendChat(UserInfo info, byte[] data){
        textArea.append(String.format("%s : %s\n",info.name, new String(data, StandardCharsets.UTF_8)));
    }

    public void sendChat(){
        DataType.TEXT.send(textField.getText().getBytes(StandardCharsets.UTF_8));
        textField.setText("");
    }

}
