package com.tangthree.imessage.protocol;

import com.tangthree.imessage.protocol.util.RemotingHelper;
import com.tangthree.imessage.protocol.util.RemotingUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * max data size : 512kb
 * part 1. 1byte (magic code)
 * part 2. 8byte (message id)
 * part 3. 1byte (message type)
 * part 4. 4byte (body length)
 * part 5. body
 * @author TangThree
 * Created on 2022/1/29 8:42 PM
 **/

@Slf4j
public class MessageDecoder  extends LengthFieldBasedFrameDecoder {
    public MessageDecoder() {
        super(512*1024, 10, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }
            ByteBuffer byteBuffer = frame.nioBuffer();

            Message message = new Message();

            //magic code
            byte magicCode = byteBuffer.get();
            if (magicCode != Message.MAGIC_CODE) {
                log.error("Unknown magic code:{}", magicCode);
                RemotingUtil.closeChannel(ctx.channel());
                return null;
            }

            //id
            long id = byteBuffer.getLong();
            message.setId(id);

            //type
            byte type = byteBuffer.get();
            MessageType messageType = MessageType.from(type);
            if (messageType == null) {
                log.error("Unknown message type:{}", messageType);
                RemotingUtil.closeChannel(ctx.channel());
                return null;
            }
            message.setType(messageType);

            //body length
            int bodyLength = byteBuffer.getInt();
            if (bodyLength > 0) {
                //body
                byte[] body = new byte[bodyLength];
                byteBuffer.get(body);
                message.setBody(body);
            }

            log.debug("RECEIVE MESSAGE:{}", message);

            return message;
        } catch (Exception e) {
            log.error("decode exception, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            RemotingUtil.closeChannel(ctx.channel());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
        return null;
    }
}
