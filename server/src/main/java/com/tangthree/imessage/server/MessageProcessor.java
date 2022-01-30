package com.tangthree.imessage.server;

import com.tangthree.imessage.protocol.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author TangThree
 * Created on 2022/1/30 7:55 PM
 **/
public interface MessageProcessor {

    void handleMessage(ChannelTemplate channelTemplate, ChannelHandlerContext ctx, Message msg);

}
