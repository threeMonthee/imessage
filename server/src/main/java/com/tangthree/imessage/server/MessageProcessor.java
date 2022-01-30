package com.tangthree.imessage.server;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author TangThree
 * Created on 2022/1/30 7:55 PM
 **/
public interface MessageProcessor {
    MessageType getMessageType();
    void handleMessage(ChannelHandlerContext ctx, Message msg);
}
