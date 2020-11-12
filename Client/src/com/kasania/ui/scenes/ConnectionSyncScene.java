package com.kasania.ui.scenes;

import com.kasania.net.DataType;

import javax.swing.*;
import java.awt.*;

public class ConnectionSyncScene extends Scene {

    private JLabel codeLabel;

    {

        JLabel notificationLabel = new JLabel("아래 접속코드를 안드로이드 어플리케이션에 입력해 주세요.");
        codeLabel = new JLabel();

        contentPanel.add(notificationLabel, BorderLayout.NORTH);
        contentPanel.add(codeLabel, BorderLayout.CENTER);

        DataType.SYNC.addReceiver((userInfo, bytes) -> {
            setAccessCode(new String(bytes));
        });

    }

    private void setAccessCode(String code){
        SwingUtilities.invokeLater(() -> codeLabel.setText(code));
    }

    public void addOnSyncDone(Runnable runnable){
        DataType.SYNC.addReceiver((userInfo, bytes) -> runnable.run());
    }
}
