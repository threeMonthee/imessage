package com.tangthree.imessage.client;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import com.tangthree.imessage.protocol.util.RemotingHelper;
import com.tangthree.imessage.protocol.util.RemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * @author TangThree
 * Created on 2022/1/30 5:44 PM
 **/

@Slf4j
public class NettyClientConnectManagerHandler extends ChannelDuplexHandler {
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        final String local = localAddress == null ? "UNKNOWN" : RemotingHelper.parseSocketAddressAddr(localAddress);
        final String remote = remoteAddress == null ? "UNKNOWN" : RemotingHelper.parseSocketAddressAddr(remoteAddress);
        log.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
        super.close(ctx, promise);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.WRITER_IDLE)) {
                log.debug("NETTY CLIENT PIPELINE: WRITER_IDLE");
                ctx.channel().writeAndFlush(Message.newHeartbeatMessage());
            } else if (event.state().equals(IdleState.READER_IDLE)) {
                log.info("NETTY CLIENT PIPELINE: READER_IDLE");
                RemotingUtil.closeChannel(ctx.channel());
            }
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
        log.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
        RemotingUtil.closeChannel(ctx.channel());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.debug("NETTY CLIENT PIPELINE: WRITE {}", msg);
        super.write(ctx, msg, promise);
    }
}
