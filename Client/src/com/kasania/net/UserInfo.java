package com.kasania.net;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserInfo {
    public static final Map<Integer, UserInfo> names = new ConcurrentHashMap<>();
    public static final UserInfo SERVER = new UserInfo(-1,0,"SERVER");

    static {
        DataType.UPDATE_USER.addReceiver(UserInfo::updateNickname);
    }

    private static void updateNickname(UserInfo _unused, byte[] data){
        String[] value = new String(data, StandardCharsets.UTF_8).split("//");

        synchronized (names){
            UserInfo.names.clear();
            int idx = 0;
            for (String s : value) {
                String[] userInfo = s.split("::");
                UserInfo.names.putIfAbsent(Integer.parseInt(userInfo[0]), new UserInfo(idx++, Integer.parseInt(userInfo[0]), userInfo[1]));
            }
        }
    }

    public static UserInfo getUserInfo(int src){
        UserInfo ret = names.get(src);
        if(ret == null){
            return SERVER;
        }
        return ret;
    }

    public final int idx;
    public final int id;
    public final String name;

    UserInfo(int idx, int id, String name){
        this.id = id;
        this.idx = idx;
        this.name = name;
    }
}
