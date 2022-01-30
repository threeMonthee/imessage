package com.tangthree.imessage.client;

import com.tangthree.imessage.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author TangThree
 * Created on 2022/1/30 5:49 PM
 **/

@Slf4j
public class NettyClientChannelHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    }
}
