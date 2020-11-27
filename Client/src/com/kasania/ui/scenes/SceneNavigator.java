package com.kasania.ui.scenes;

import com.kasania.net.DataType;

import javax.swing.*;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;


public class SceneNavigator {

    public enum Items{
        LOGIN,SYNC, CONVERSATION
    }

    private final Map<Items,JPanel> scenes;

    private JPanel contentRoot;

    private LoginView loginScene;
    private ConnectionSyncScene connectionSyncPanel;
    private ConversationScene conversationScene;

    {
        scenes = new EnumMap<>(Items.class);

        loginScene = new LoginView();
        connectionSyncPanel = new ConnectionSyncScene();
        conversationScene = new ConversationScene();

        scenes.put(Items.LOGIN, loginScene.getContentPanel());
        scenes.put(Items.SYNC, connectionSyncPanel.contentPanel);
        scenes.put(Items.CONVERSATION, conversationScene.contentPanel);

        bindSceneActions();

    }


    private void bindSceneActions(){
        DataType.SYNC.addReceiver((info, bytes) -> {
            navigateTo(Items.SYNC);
            connectionSyncPanel.setAccessCode(String.valueOf(ByteBuffer.wrap(bytes).getInt()));
        });
        connectionSyncPanel.addOnSyncDone(() -> {
            navigateTo(Items.CONVERSATION);
        });

    }

    public void setContentRoot(JPanel panel){

        contentRoot = panel;
    }

    public void navigateTo(Items scene){
        contentRoot.removeAll();
        contentRoot.add(scenes.get(scene));
        contentRoot.revalidate();
        contentRoot.repaint();
    }

}
