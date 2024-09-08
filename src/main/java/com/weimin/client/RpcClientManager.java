package com.weimin.client;

import com.weimin.handler.RpcResponseMessageHandler;
import com.weimin.message.RpcRequestMessage;
import com.weimin.protocol.MessageCodecSharable;
import com.weimin.protocol.ProtocolFrameDecoder;
import com.weimin.server.service.HelloService;
import com.weimin.utils.SequenceIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

import static com.weimin.config.AppConfig.PROMISES;

@Slf4j
public class RpcClientManager {
    private static Channel channel = null;

    private static final Object LOCK = new Object();

    // 单例
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }

        synchronized (LOCK) {
            if (channel != null) {
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();

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
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }

    public static <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = {serviceClass};
        int id = SequenceIdGenerator.nextId();
        Object o = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
            RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(
                    id,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );

            getChannel().writeAndFlush(rpcRequestMessage);


            DefaultPromise<Object> defaultPromise = new DefaultPromise<>(getChannel().eventLoop());
            PROMISES.put(id, defaultPromise);

            defaultPromise.await();

            if (defaultPromise.isSuccess()) {
                return defaultPromise.getNow();
            } else {
                Throwable cause = defaultPromise.cause();
                throw new RuntimeException(cause);
            }


        });

        return (T) o;
    }

    public static void main(String[] args) {
        HelloService helloService = getProxyService(HelloService.class);

        String s = helloService.sayHi("tom");
        System.out.println(s);
        String s1 = helloService.sayHello("jerry");
        System.out.println(s1);
    }
}
