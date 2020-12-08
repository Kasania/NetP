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

    private int connectedUserNumber = 0;

    private int prevNum = 0;

    {
        textArea.setEditable(false);

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
            connectedUserNumber = UserInfo.names.size();

            Point point = calcPos(info.idx);
            Graphics2D g2d = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
            if(connectedUserNumber != prevNum){
                prevNum = connectedUserNumber;
                g2d.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
            }
            g2d.drawImage(image, point.x, point.y, null);
            
            g2d.drawString(info.name,point.x+110 - (info.name.length()*3), point.y + 370);
            g2d.drawRect(point.x-10, point.y,260,380);
            g2d.dispose();
            canvas.getBufferStrategy().show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Point calcPos(int src){

        int viewOnRow = (int) Math.ceil(Math.sqrt(connectedUserNumber));

        int row = src / viewOnRow;
        int col = src % viewOnRow ;

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int imageWidth = 260;
        int imageHeight = 380;

        int requiredWidth = imageWidth * viewOnRow;
        int requiredHeight = (int) (imageHeight * Math.ceil(connectedUserNumber / (float)viewOnRow));

        int startX = (canvasWidth - requiredWidth) / 2;
        int startY = (canvasHeight - requiredHeight) / 2;

        int x = startX + col * imageWidth;
        int y = startY + row * imageHeight;

        return new Point(x,y);
    }

    public void appendChat(UserInfo info, byte[] data){
        textArea.append(String.format("%s : %s\n",info.name, new String(data, StandardCharsets.UTF_8)));
    }

    public void sendChat(){
        if(!textField.getText().contentEquals("")){
            DataType.TEXT.send(textField.getText().getBytes(StandardCharsets.UTF_8));
            textField.setText("");
        }
    }

}
