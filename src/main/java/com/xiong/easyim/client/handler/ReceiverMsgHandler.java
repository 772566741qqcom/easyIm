package com.xiong.easyim.client.handler;

import com.xiong.easyim.protocol.MessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ReceiverMsgHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof MessageProto.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        //判断类型
        MessageProto.Message pkg = (MessageProto.Message) msg;
        MessageProto.HeadType headType = pkg.getType();
        if (!headType.equals(MessageProto.HeadType.MESSAGE_REQUEST)) {
            super.channelRead(ctx, msg);
            return;
        }

        MessageProto.MessageRequest req = pkg.getMessageRequest();
        String content = req.getContent();
        String uid = req.getFrom();
        System.out.println(" 收到消息 from uid:" + uid + " -> " + content);
        ReferenceCountUtil.release(msg);
    }
}
