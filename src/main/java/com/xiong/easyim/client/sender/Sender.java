package com.xiong.easyim.client.sender;

import com.xiong.easyim.protocol.MessageProto;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sender {
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    private Channel channel;

    public Sender(Channel channel) {
        this.channel = channel;
    }

    public void send(MessageProto.Message message) {
        executorService.submit(() -> {
            this.channel.writeAndFlush(message);
            this.channel.flush();
        });
    }
}
