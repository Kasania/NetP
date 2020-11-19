package com.kasania.ui.scenes;

import javax.swing.*;
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
        loginScene.addOnLoginButtonPressed(() -> navigateTo(Items.SYNC));
        connectionSyncPanel.addOnSyncDone(() -> navigateTo(Items.CONVERSATION));

    }

    public void setContentRoot(JPanel panel){

        contentRoot = panel;
    }

    public void navigateTo(Items scene){
        contentRoot.removeAll();
        contentRoot.add(scenes.get(scene));
        contentRoot.revalidate();
    }

}
