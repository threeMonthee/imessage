package com.tangthree.imessage.server.processor;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import com.tangthree.imessage.server.ChannelTemplate;
import com.tangthree.imessage.server.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author TangThree
 * Created on 2022/1/31 3:44 AM
 **/
public class HeartbeatProcessor implements MessageProcessor {
    @Override
    public void handleMessage(ChannelTemplate channelTemplate, ChannelHandlerContext ctx, Message msg) {
        channelTemplate.send(ctx.channel(), Message.fromPayload(MessageType.HEARTBEAT));
    }
}
