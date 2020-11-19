package com.kasania.ui.scenes;

import com.kasania.net.DataType;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class ConnectionSyncScene extends Scene {

    private JLabel codeLabel;

    {

        JLabel notificationLabel = new JLabel("아래 접속코드를 안드로이드 어플리케이션에 입력해 주세요.");
        notificationLabel.setBorder(BorderFactory.createEmptyBorder(25,5,5,5));
        notificationLabel.setFont(new Font("맑은고딕",Font.PLAIN,24));
        notificationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        codeLabel = new JLabel();
        codeLabel.setFont(new Font("맑은고딕",Font.BOLD,72));
        codeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        codeLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        contentPanel.add(notificationLabel, BorderLayout.NORTH);
        contentPanel.add(codeLabel, BorderLayout.CENTER);

        DataType.SYNC.addReceiver((userInfo, bytes) -> {
            setAccessCode(new String(bytes, StandardCharsets.UTF_8));
        });

    }

    private void setAccessCode(String code){
        System.out.println(code);
        SwingUtilities.invokeLater(() -> codeLabel.setText(code));
    }

    public void addOnSyncDone(Runnable runnable){
        DataType.SYNCDone.addReceiver((userInfo, bytes) -> runnable.run());
    }
}
