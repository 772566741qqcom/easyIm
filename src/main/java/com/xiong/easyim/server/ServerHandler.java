package com.xiong.easyim.server;

import com.xiong.easyim.protocol.MessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageProto.Message message = (MessageProto.Message) msg;
        log.info("收到消息,{}", message);
        switch (message.getType()) {
            case LOGIN_REQUEST:
                break;
            default:
                log.error("不支持的消息类型,{}", message);
        }
        super.channelRead(ctx, msg);
    }
}
