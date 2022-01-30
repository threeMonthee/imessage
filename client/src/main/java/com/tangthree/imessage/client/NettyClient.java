package com.tangthree.imessage.client;


import com.tangthree.imessage.protocol.Disposable;
import com.tangthree.imessage.protocol.MessageDecoder;
import com.tangthree.imessage.protocol.MessageEncoder;
import com.tangthree.imessage.protocol.util.RemotingUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/1/30 5:27 PM
 **/

@Slf4j
public class NettyClient implements Disposable {

    private final Bootstrap bootstrap;
    private final EventLoopGroup selectorGroup;
    private final EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    public NettyClient(int connectTimeoutSeconds, int readerIdleTimeSeconds, int writerIdleTimeSeconds) {
        this(NettyRuntime.availableProcessors(), connectTimeoutSeconds, readerIdleTimeSeconds, writerIdleTimeSeconds);
    }

    public NettyClient(int workerThreads, int connectTimeoutSeconds, int readerIdleTimeSeconds, int writerIdleTimeSeconds) {
        this.bootstrap = new Bootstrap();
        this.selectorGroup = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelectorThread_%d", this.threadIndex.incrementAndGet()));
            }
        });
        this.workerGroup = new NioEventLoopGroup(workerThreads, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientWorkerThread_%d", this.threadIndex.incrementAndGet()));
            }
        });
        bootstrap.group(selectorGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutSeconds * 1000)
                .handler(new ChannelInitializer<NioSocketChannel>(){
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(workerGroup,
                                new MessageDecoder(),
                                new MessageEncoder(),
                                new IdleStateHandler(readerIdleTimeSeconds, writerIdleTimeSeconds, 0),
                                new NettyClientConnectManagerHandler(),
                                new NettyClientChannelHandler());
                    }
                });
    }

    public ChannelFuture connect(String host, int port) throws InterruptedException {
        log.info("NettyClient is connecting to remote server:{}:{}", host, port);
        channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
        log.info("NettyClient is running");
        return channelFuture;
    }

    public void disconnect() {
        RemotingUtil.closeChannel(channelFuture.channel());
    }

    @Override
    public void destroy() {
        if (selectorGroup != null) {
            selectorGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
