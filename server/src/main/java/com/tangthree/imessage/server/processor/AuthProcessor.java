package com.tangthree.imessage.server.processor;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import com.tangthree.imessage.server.ChannelAttributes;
import com.tangthree.imessage.server.ChannelTemplate;
import com.tangthree.imessage.server.DefaultChannelTemplate;
import com.tangthree.imessage.server.MessageProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/1/31 3:44 AM
 **/

@Slf4j
public class AuthProcessor implements MessageProcessor {

    private static final AtomicInteger threadIndex = new AtomicInteger(0);

    @Override
    public void handleMessage(ChannelTemplate channelTemplate, ChannelHandlerContext ctx, Message msg) {
        log.info("start to auth...");
        String userId = auth(channelTemplate, ctx, msg);
        Channel channel = ctx.channel();
        if (userId == null) {
            channelTemplate.send(channel, Message.fromPayload(MessageType.SERVICE, "auth fail!!!"));
            return;
        }
        log.info("auth success");
        channel.attr(AttributeKey.valueOf(ChannelAttributes.USER_ID)).set(userId);
        channelTemplate.send(channel, Message.fromPayload(MessageType.SERVICE, "auth success"));
        ((DefaultChannelTemplate)channelTemplate).register(userId, ctx.channel());
    }

    protected String auth(ChannelTemplate channelTemplate, ChannelHandlerContext ctx, Message msg) {
        return threadIndex.getAndIncrement() + "";
    }
}
