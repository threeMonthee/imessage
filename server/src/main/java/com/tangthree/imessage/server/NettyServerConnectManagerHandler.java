package com.tangthree.imessage.server;

import com.tangthree.imessage.protocol.util.RemotingHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author TangThree
 * Created on 2022/1/29 9:42 PM
 **/

@Slf4j
public class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

    private final ScheduledExecutorService executor;
    private final int maxAuthTimeout;
    private final ChannelTemplate channelTemplate;

    public NettyServerConnectManagerHandler(ScheduledExecutorService executor, int maxAuthTimeout, ChannelTemplate channelTemplate) {
        this.executor = executor;
        this.maxAuthTimeout = maxAuthTimeout;
        this.channelTemplate = channelTemplate;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                log.debug("SCHEDULE AUTH: the channel:[{}]", remoteAddress);
                Channel channel = ctx.channel();
                if (channel.isActive() && !channel.hasAttr(AttributeKey.valueOf(ChannelAttributes.USER_ID))) {
                    channelTemplate.closeAndRemove(channel);
                    log.info("SCHEDULE AUTH: not auth, close the channel:[{}]", remoteAddress);
                }
            }
        }, maxAuthTimeout, TimeUnit.SECONDS);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                log.warn("NETTY SERVER PIPELINE: READER_IDLE exception [{}]", remoteAddress);
                channelTemplate.closeAndRemove(ctx.channel());
            }
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.error(String.format("NETTY SERVER PIPELINE: exceptionCaught, remoteAddress:%s", remoteAddress), cause);
        channelTemplate.closeAndRemove(ctx.channel());
    }
}
