package com.xiong.easyim.server.handler;

import com.xiong.easyim.protocol.MessageProto;
import com.xiong.easyim.server.ServerSession;
import com.xiong.easyim.server.SessionMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ChatRedirectHandler extends ChannelInboundHandlerAdapter {
    private static ExecutorService chatRedirectHandlerThredPool = Executors.newFixedThreadPool(4);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof MessageProto.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        MessageProto.Message pkg = (MessageProto.Message) msg;
        MessageProto.HeadType headType = ((MessageProto.Message) msg).getType();
        if (headType != MessageProto.HeadType.MESSAGE_REQUEST) {
            super.channelRead(ctx, msg);
            return;
        }

        ServerSession session = ServerSession.getSession(ctx);
        if (null == session || !session.isLogin()) {
            log.error("用户尚未登录，不能发送消息");
            ReferenceCountUtil.release(msg);
            return;
        }

        chatRedirectHandlerThredPool.submit(() -> {
            // 聊天处理
            MessageProto.MessageRequest messageRequest = pkg.getMessageRequest();
            log.info("chatMsg | from="
                    + messageRequest.getFrom()
                    + " , to=" + messageRequest.getTo()
                    + " , content=" + messageRequest.getContent());
            // 获取接收方的chatID
            String to = messageRequest.getTo();
            List<ServerSession> toSessions = SessionMap.inst().getSessionsByUid(to);
            if (toSessions == null || toSessions.isEmpty()) {
                log.error("[" + to + "] 不在线，发送失败!");
            } else {
                toSessions.forEach((session1) -> {
                    // 将IM消息发送到接收方
                    session1.writeAndFlush(pkg);
                });
            }
        });

    }
}
