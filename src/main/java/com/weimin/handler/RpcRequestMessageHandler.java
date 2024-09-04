package com.weimin.handler;

import com.weimin.config.AppConfig;
import com.weimin.message.RpcRequestMessage;
import com.weimin.message.RpcResponseMessage;
import com.weimin.server.service.HelloService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequestMessage rpcRequestMessage) throws Exception {
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setSequenceId(rpcRequestMessage.getSequenceId());

        try {
            HelloService service = (HelloService) AppConfig.getService(Class.forName(rpcRequestMessage.getInterfaceName()));
            Method method = service.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParameterTypes());
            Object invoke = method.invoke(service, rpcRequestMessage.getParameterValue());
            rpcResponseMessage.setReturnValue(invoke);
        } catch (Exception e) {
            log.error(e.getMessage());
            rpcResponseMessage.setExceptionValue(e);
        }
        channelHandlerContext.writeAndFlush(rpcResponseMessage);
    }
}
