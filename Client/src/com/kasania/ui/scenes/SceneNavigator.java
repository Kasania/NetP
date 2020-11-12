package com.kasania.ui.scenes;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;


public class SceneNavigator {

    public enum Items{
        LOGIN,SYNC,VIEW
    }

    private final Map<Items,Scene> scenes;

    private JPanel contentRoot;

    private LoginScene loginScene;
    private ConnectionSyncScene connectionSyncPanel;
    private ConversationScene conversationScene;

    {
        scenes = new EnumMap<>(Items.class);

        loginScene = new LoginScene();
        connectionSyncPanel = new ConnectionSyncScene();

        scenes.put(Items.LOGIN, loginScene);
        scenes.put(Items.SYNC, connectionSyncPanel);

        bindSceneActions();

    }


    private void bindSceneActions(){
        loginScene.addOnLoginButtonPressed(() -> navigateTo(Items.SYNC));
        connectionSyncPanel.addOnSyncDone(() -> navigateTo(Items.VIEW));

    }

    public void setContentRoot(JPanel panel){

        contentRoot = panel;
    }

    public void navigateTo(Items scene){
        contentRoot.removeAll();
        contentRoot.add(scenes.get(scene).getContentPanel());
        contentRoot.revalidate();
    }

}
