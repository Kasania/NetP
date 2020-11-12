package com.kasania.ui.scenes;

import com.kasania.net.DataType;

public class ConversationScene extends Scene{

    {
        DataType.IMAGE.addReceiver((userInfo, bytes) -> {});
        DataType.TEXT.addReceiver((userInfo, bytes) -> {});
    }

}
