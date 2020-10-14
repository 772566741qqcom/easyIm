package com.xiong.easyim.codec;

import com.xiong.easyim.protocol.MessageProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (in.readableBytes() < 2) {
            return;
        }
        int dataLen = in.readShort();
        if (dataLen < 0) {
            ctx.close();
        }
        if (dataLen > in.readableBytes()) {
            in.resetReaderIndex();
            return;
        }
        byte[] array;
        if (in.hasArray()) {
            ByteBuf buf = in.slice();
            array = buf.array();
        } else {
            array = new byte[dataLen];
            in.readBytes(array, 0, dataLen);
        }

        MessageProto.Message msg = MessageProto.Message.parseFrom(array);
        if (msg != null) {
            out.add(msg);
        }
    }
}
