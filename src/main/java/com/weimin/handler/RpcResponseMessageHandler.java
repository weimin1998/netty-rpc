package com.weimin.handler;

import com.weimin.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import static com.weimin.config.AppConfig.PROMISES;

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponseMessage rpcResponseMessage) throws Exception {
        log.debug("{}", rpcResponseMessage);

        // 不要get
        // 必须remove
        // promise用完就没用了
        Promise<Object> promise = PROMISES.remove(rpcResponseMessage.getSequenceId());

        if (promise != null) {
            Object returnValue = rpcResponseMessage.getReturnValue();
            Exception exceptionValue = rpcResponseMessage.getExceptionValue();
            if (exceptionValue != null) {
                promise.setFailure(exceptionValue);
            } else {
                promise.setSuccess(returnValue);
            }
        }
    }
}
