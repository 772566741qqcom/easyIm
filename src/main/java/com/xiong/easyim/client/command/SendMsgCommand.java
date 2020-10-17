package com.xiong.easyim.client.command;

import lombok.Data;

import java.util.Scanner;

@Data
public class SendMsgCommand implements BaseCommand {
    public static final int KEY = 3;
    private String uid;
    private String content;

    @Override
    public void exec(Scanner scanner) {
        System.out.println("请输入对方id");
        while (scanner.hasNext()) {
            if (uid == null) {
                uid = scanner.next();
                System.out.println("请输入要发送的内容");
            } else {
                content = scanner.next();
                break;
            }
        }
    }
}
