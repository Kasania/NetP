package com.kasania.net;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserInfo {
    public static final Map<Integer, UserInfo> names = new ConcurrentHashMap<>();

    static {
        DataType.UPDATE_USER.addReceiver(UserInfo::updateNickname);
    }

    private static void updateNickname(UserInfo _unused, byte[] data){
        String[] value = new String(data, StandardCharsets.UTF_8).split("//");
        synchronized (UserInfo.names){
            UserInfo.names.clear();
            int idx = 0;
            for (String s : value) {
                String[] userInfo = s.split("::");
                UserInfo.names.putIfAbsent(Integer.parseInt(userInfo[0]), new UserInfo(idx++, userInfo[1]));
            }
        }
    }

    public final int idx;
    public final String name;
    UserInfo(int idx, String name){
        this.idx = idx;
        this.name = name;
    }
}
