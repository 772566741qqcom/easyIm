package com.xiong.easyim.server.handler;

import com.xiong.easyim.common.pojo.User;
import com.xiong.easyim.protocol.MessageProto;
import com.xiong.easyim.server.ServerSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    private static ExecutorService loginRequestHandlerPool = Executors.newFixedThreadPool(4, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("处理登录请求线程");
            return thread;
        }
    });

    /**
     * 校验身份
     *
     * @param loginRequest
     * @return
     */
    private boolean check(MessageProto.LoginRequest loginRequest) {
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg
                || !(msg instanceof MessageProto.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        MessageProto.Message message = (MessageProto.Message) msg;
        if (message.getType() != MessageProto.HeadType.LOGIN_REQUEST) {
            super.channelRead(ctx, msg);
        } else {
            ServerSession serverSession = new ServerSession(ctx.channel());
            loginRequestHandlerPool.submit(() -> {
                if (this.check(message.getLoginRequest())) {
                    User user = User.fromMsg(message.getLoginRequest());
                    serverSession.setUser(user);
                    serverSession.bind();
                    MessageProto.LoginResponse loginResponse = MessageProto.LoginResponse.newBuilder()
                            .setResult(true)
                            .setInfo("登录成功")
                            .setCode(0)
                            .build();
                    //构造登录成功的报文
                    MessageProto.Message response =
                            MessageProto.Message.newBuilder()
                                    .setLoginResponse(loginResponse)
                                    .setSequence(message.getSequence())
                                    .setType(MessageProto.HeadType.LOGIN_RESPONSE)
                                    .setSessionId(serverSession.getSessionId())
                                    .build();
                    serverSession.writeAndFlush(response);
                } else {
                    MessageProto.LoginResponse loginResponse = MessageProto.LoginResponse.newBuilder()
                            .setResult(false)
                            .setInfo("登录异常")
                            .setCode(1)
                            .build();
                    //构造登录失败的报文
                    MessageProto.Message response =
                            MessageProto.Message.newBuilder()
                                    .setLoginResponse(loginResponse)
                                    .setSequence(message.getSequence())
                                    .setType(MessageProto.HeadType.LOGIN_RESPONSE)
                                    .setSessionId("-1")
                                    .build();
                    //发送登录失败的报文
                    serverSession.writeAndFlush(response);
                    ServerSession.closeSession(ctx);
                }
            });
        }
    }
}
