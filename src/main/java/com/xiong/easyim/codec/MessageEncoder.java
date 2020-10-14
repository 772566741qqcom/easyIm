package com.xiong.easyim.codec;

import com.xiong.easyim.protocol.MessageProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<MessageProto.Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProto.Message msg, ByteBuf out) throws Exception {
        byte[] data = msg.toByteArray();
        int dataLen = data.length;
        out.writeShort(dataLen);
        out.writeBytes(data);
    }
}
