package com.xiong.easyim.client;

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

@Slf4j
public class Client {
    private final static String remoteAttr = "127.0.0.1";
    private final static int port = 1234;
    private final static String appVersion = "1.0.0";
    private final static String deviceId ="111";
    private final static int platform = 1;


    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
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
            bootstrap.remoteAddress(new InetSocketAddress(remoteAttr, port));

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
            Channel channel = future.channel();

            MessageProto.LoginRequest loginRequest = MessageProto.LoginRequest.newBuilder()
                    .setAppVersion(appVersion)
                    .setDeviceId(deviceId)
                    .setPlatform(platform)
                    .setUid("1234")
                    .setToken("token")
                    .build();
            MessageProto.Message message = MessageProto.Message.newBuilder()
                    .setType(MessageProto.HeadType.LOGIN_REQUEST)
                    .setLoginRequest(loginRequest)
                    .build();
            channel.writeAndFlush(message);
            channel.flush();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
