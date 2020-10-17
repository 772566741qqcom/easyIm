package com.xiong.easyim.client.handler;

import com.xiong.easyim.client.ClientSession;
import com.xiong.easyim.protocol.MessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

public class LoginResponseHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null || !(msg instanceof MessageProto.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        MessageProto.Message message = (MessageProto.Message) msg;
        if (message.getType() == MessageProto.HeadType.LOGIN_RESPONSE) {
            MessageProto.LoginResponse loginResponse = message.getLoginResponse();
            if (loginResponse.getCode() == 0) {
                //登录成功
                ClientSession.loginSuccess(ctx, message);
                ChannelPipeline p = ctx.pipeline();
                //移除登录响应处理器
                p.remove(this);
            } else {
                System.out.println("登录失败：" + loginResponse.getInfo());
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
