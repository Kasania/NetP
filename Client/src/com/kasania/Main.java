package com.kasania;

import com.kasania.ui.MainFrame;

public class Main {
    public static void main(String[] args) {

        int videoPort = 11114;
        int audioPort = 11115;
        if(args.length>1){
            videoPort = Integer.parseInt(args[0]);
            audioPort = Integer.parseInt(args[1]);
        }

        new MainFrame(videoPort, audioPort);

        //Trying to connect broadcast server
        //if success, get access code
        //fail, connection refuse
        //input given access code to android app
        //if success, camera connection has been establish
        //can join public room (make private room)
        //chat and image (sound?)


    }
}
