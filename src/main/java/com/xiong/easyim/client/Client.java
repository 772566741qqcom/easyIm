package com.xiong.easyim.client;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {
    private final static String remoteAttr = "127.0.0.1";
    private final static int port = 1234;
    private final static String appVersion = "1.0.0";
    private final static String deviceId = "111";
    private final static int platform = 1;


    public static void main(String[] args) {
        CommandController commandController = new CommandController();
        commandController.initCommandMap();
        commandController.start();
    }
}
