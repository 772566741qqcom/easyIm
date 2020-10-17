package com.xiong.easyim.server;

import com.xiong.easyim.protocol.MessageProto;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSender {
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    private ChannelHandlerContext context;

    public ServerSender(ChannelHandlerContext ctx) {
        this.context = ctx;
    }

    public void send(MessageProto.Message message) {
        executorService.submit(() -> {
            this.context.channel().writeAndFlush(message);
            this.context.channel().flush();
        });
    }
}
