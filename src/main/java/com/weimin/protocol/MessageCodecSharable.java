package com.weimin.protocol;


import com.weimin.config.AppConfig;
import com.weimin.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
// 可以被多个channel共享rra
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 4字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 1字节的版本
        out.writeByte(1);
        // 1字节序列化算法
        // ordinal，是枚举对象在枚举类中的顺序，java 0 , json 1
        out.writeByte(AppConfig.getSerializerAlgorithm().ordinal());
        // 1字节指令类型
        out.writeByte(msg.getMessageType());
        // 4字节的序号
        out.writeInt(msg.getSequenceId());
        // 1字节的占位符，为了使得固定部分是2的整数倍
        out.writeByte(0xff);
        // 序列化
        byte[] bytes = AppConfig.getSerializerAlgorithm().serialize(msg);
        // 4字节正文的长度
        out.writeInt(bytes.length);
        // 正文的内容
        out.writeBytes(bytes);

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 魔数
        int magicNum = in.readInt();

        byte version = in.readByte();

        byte serializerType = in.readByte();

        byte messageType = in.readByte();

        int sequenceId = in.readInt();

        in.readByte();

        int length = in.readInt();

        byte[] bytes = new byte[length];

        in.readBytes(bytes, 0, length);

        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerType];

        Class<? extends Message> messageClass = Message.getMessageClass(messageType);

        Object message = algorithm.deserialize(messageClass, bytes);

        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);

        log.debug("{}", message);

        out.add(message);
    }
}
