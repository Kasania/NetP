package com.kasania.server.ui.scenes;

import com.kasania.server.net.DataType;
import com.kasania.server.net.UserInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ConversationScene extends Scene{

    private Canvas canvas;

    {
        canvas = new Canvas();
        DataType.IMAGE.addReceiver(this::drawImage);
        DataType.TEXT.addReceiver((userInfo, bytes) -> {});
        contentPanel.add(canvas);
    }

    public void drawImage(UserInfo info, byte[] data){
        if(canvas.getBufferStrategy() == null){
            canvas.createBufferStrategy(2);
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            BufferedImage image = ImageIO.read(byteArrayInputStream);
            Graphics2D g2d = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
            g2d.clearRect(0,0, image.getWidth(),image.getHeight());
            g2d.drawImage(image,0,0,null);
            g2d.dispose();
            canvas.getBufferStrategy().show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
