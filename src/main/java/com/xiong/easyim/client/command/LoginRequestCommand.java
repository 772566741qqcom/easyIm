package com.xiong.easyim.client.command;

import lombok.Data;

import java.util.Scanner;

@Data
public class LoginRequestCommand implements BaseCommand {
    private String uid;
    private String password;
    public static final int KEY = 1;

    @Override
    public void exec(Scanner scanner) {
        int i = 0;
        System.out.println("请输入用户名：");
        while (scanner.hasNext()) {
            switch (i) {
                case 0:
                    uid = scanner.next();
                    i++;
                    System.out.println("请输入密码: ");
                case 1:
                    password = scanner.next();
                    i++;
            }
            if (i >= 2) {
                break;
            }
        }
    }


}
