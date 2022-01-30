package com.tangthree.imessage.server;

import com.tangthree.imessage.protocol.MessageDecoder;
import com.tangthree.imessage.protocol.MessageEncoder;
import com.tangthree.imessage.protocol.util.RemotingUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/1/29 8:54 PM
 **/

@Slf4j
@Component
public class NettyServer implements ApplicationRunner, DisposableBean
{
    @Autowired
    NettyServerConfig nettyServerConfig;
    @Autowired
    NettyServerChannelHandler nettyServerChannelHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup selectorGroup;
    private EventLoopGroup workerGroup;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (useEpoll()) {
            bossGroup = new EpollEventLoopGroup(1, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            selectorGroup = new EpollEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerSelectorThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            workerGroup = new EpollEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            selectorGroup = new NioEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerSelectorThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            workerGroup = new NioEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }


        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, selectorGroup)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                //.childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())
                //.childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize())
                //.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                //        nettyServerConfig.getWriteBufferLowWaterMark(), nettyServerConfig.getWriteBufferHighWaterMark()))
                //.localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(workerGroup,
                                        new MessageEncoder(),
                                        new MessageDecoder(),
                                        new IdleStateHandler(nettyServerConfig.getReaderIdleTimeSeconds(), 0, 0),
                                        new NettyServerConnectManagerHandler(),
                                        nettyServerChannelHandler);
                    }
                });
        bootstrap.bind(nettyServerConfig.getPort()).sync();
        log.info("NettyServer is running on port:{}", nettyServerConfig.getPort());
    }

    @Override
    public void destroy() throws Exception {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (selectorGroup != null) {
            selectorGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("NettyServer has been destroyed");
    }

    private boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform()
                && nettyServerConfig.isUseEpoll()
                && Epoll.isAvailable();
    }
}
