package com.tangthree.imessage.server.processor;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import com.tangthree.imessage.server.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

/**
 * @author TangThree
 * Created on 2022/1/30 7:57 PM
 **/

@Service
public class HeartbeatMessageProcessor implements MessageProcessor {
    @Override
    public MessageType getMessageType() {
        return MessageType.HEARTBEAT;
    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, Message msg) {
        ctx.writeAndFlush(Message.fromPayload(MessageType.HEARTBEAT, null));
    }
}
