package com.tangthree.imessage.server.processor;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.server.ChannelTemplate;
import com.tangthree.imessage.server.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author TangThree
 * Created on 2022/1/31 3:54 AM
 **/
public class ServiceProcessor implements MessageProcessor {
    @Override
    public void handleMessage(ChannelTemplate channelTemplate, ChannelHandlerContext ctx, Message msg) {

    }
}
