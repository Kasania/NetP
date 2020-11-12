package com.kasania.ui.scenes;

import javax.swing.*;
import java.awt.*;

public abstract class Scene {

    protected JPanel contentPanel;

    {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

}
