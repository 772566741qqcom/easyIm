package com.xiong.easyim.client;

import com.xiong.easyim.client.command.BaseCommand;
import com.xiong.easyim.client.command.LoginRequestCommand;
import com.xiong.easyim.client.sender.Sender;
import com.xiong.easyim.codec.MessageDecoder;
import com.xiong.easyim.codec.MessageEncoder;
import com.xiong.easyim.protocol.MessageProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class CommandController {


    private static Bootstrap bootstrap = new Bootstrap();
    private static NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    private boolean isRunning = true;
    private Map<Integer, BaseCommand> commandMap = new HashMap<>();

    static {
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline()
                        .addLast(new MessageDecoder())
                        .addLast(new MessageEncoder());
            }
        });
        bootstrap.remoteAddress(new InetSocketAddress(Config.remoteAttr, Config.port));
    }

    public CommandController() {
        log.info("尝试连接服务器...");
        this.connectServer();
    }

    private void connectServer() {
        new Thread(() -> {
            try {
                ChannelFuture future = bootstrap.connect().sync();
                future.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            log.info("连接服务器成功");
                        } else {
                            log.error("连接失败，{}", future.cause());
                        }
                    }
                });
                channel = future.channel();
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        }).start();

    }

    private void printTips() {
        System.out.println("请输入命令：1 登录，2退出登录，3发消息，4下线");
    }

    private void doLoginRequest(LoginRequestCommand command) {
        MessageProto.LoginRequest loginRequest = MessageProto.LoginRequest.newBuilder()
                .setAppVersion(Config.appVersion)
                .setDeviceId(Config.deviceId)
                .setPlatform(Config.platform)
                .setUid(command.getUid())
                .setToken(command.getPassword())
                .build();
        MessageProto.Message message = MessageProto.Message.newBuilder()
                .setType(MessageProto.HeadType.LOGIN_REQUEST)
                .setLoginRequest(loginRequest)
                .build();
        new Sender(channel).send(message);
        log.info("登录请求已发送");

    }

    public void initCommandMap() {
        commandMap.put(LoginRequestCommand.KEY, new LoginRequestCommand());
    }

    public void start() {
        while (this.channel == null) {
            System.out.println("尝试重连服务器...");
            this.connectServer();
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Scanner scanner = new Scanner(System.in);
        printTips();
        while (isRunning && scanner.hasNext()) {
            int opt = scanner.nextInt();
            BaseCommand baseCommand = commandMap.get(opt);
            if (baseCommand == null) {
                System.out.println("非法命令");
                printTips();
                continue;
            }
            baseCommand.exec(scanner);
            switch (opt) {
                case 4:
                    try {
                        channel.closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isRunning = false;
                    log.info("已下线");
                    break;
                case LoginRequestCommand.KEY:
                    this.doLoginRequest((LoginRequestCommand)baseCommand);
                    break;
            }
            printTips();
        }
    }
}
