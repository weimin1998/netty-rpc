package com.weimin.client;

import com.weimin.handler.RpcResponseMessageHandler;
import com.weimin.message.RpcRequestMessage;
import com.weimin.protocol.MessageCodecSharable;
import com.weimin.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(RPC_HANDLER);
                }

            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();

            // 在本案例中，客户端想要远程调用服务器端的一个方法
            // 建立连接之后，客户端发送一个rpc请求
            channel.writeAndFlush(new RpcRequestMessage(
                    1,
                    "com.weimin.server.service.HelloService",
                    "sayHi",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"tom"}
            ));

            // 测试异常情况
            channel.writeAndFlush(new RpcRequestMessage(
                    1,
                    "com.weimin.server.service.HelloService1",// 服务器没这个接口
                    "sayHi",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"tom"}
            ));

            // 如果执行结果不对，但是又看不到异常，是因为异常是出现在nio线程中，主线程无法直接看到；
            // 可以通过下面的方式查看异常

//            ChannelFuture future = channel.writeAndFlush(new RpcRequestMessage(
//                    1,
//                    "com.weimin.server.service.HelloService1",
//                    "sayHi",
//                    String.class,
//                    new Class[]{String.class},
//                    new Object[]{"tom"}
//            ));
//
//            future.addListener(promise->{
//                if(!promise.isSuccess()){
//                    log.error(promise.cause().toString());
//                }
//            });


            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
