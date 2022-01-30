package com.tangthree.imessage.server;

import com.tangthree.imessage.protocol.*;
import com.tangthree.imessage.protocol.util.RemotingUtil;
import com.tangthree.imessage.server.exception.NettyStartException;
import com.tangthree.imessage.server.processor.AuthProcessor;
import com.tangthree.imessage.server.processor.HeartbeatProcessor;
import com.tangthree.imessage.server.processor.ServiceProcessor;
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

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/1/29 8:54 PM
 **/

@Slf4j
public class NettyServer implements Disposable
{
    private final NettyServerConfig nettyServerConfig;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup selectorGroup;
    private EventLoopGroup workerGroup;
    private ScheduledExecutorService authExecutor;
    private ExecutorService processorExecutor;
    private Map<MessageType, MessageProcessor> messageProcessors;
    private DefaultChannelTemplate channelTemplate;

    public NettyServer(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }

    public ChannelTemplate getChannelTemplate() {
        if (channelTemplate == null) {
            throw new NullPointerException("NettyServer has not been initialized!");
        }
        return channelTemplate;
    }

    private void init()
    {
        bootstrap = new ServerBootstrap();
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

        authExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyServerAuthExecutorThread");
            }
        });

        processorExecutor = Executors.newFixedThreadPool(nettyServerConfig.getProcessorThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "MessageProcessorExecutorThread_" + this.threadIndex.incrementAndGet());
            }
        });

        messageProcessors = new HashMap<>();
        registerProcessor(MessageType.HEARTBEAT, new HeartbeatProcessor());
        if (messageProcessors.get(MessageType.AUTH) == null) {
            registerProcessor(MessageType.AUTH, new AuthProcessor());
        }
        if (messageProcessors.get(MessageType.SERVICE) == null) {
            registerProcessor(MessageType.SERVICE, new ServiceProcessor());
        }

        channelTemplate = new DefaultChannelTemplate();

    }

    public void registerProcessor(MessageType type, MessageProcessor processor) {
        messageProcessors.put(type, processor);
    }

    public void start(){
        init();
        bootstrap.group(bossGroup, selectorGroup)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024*1024, 4*1024*1024))
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(workerGroup,
                                        new MessageEncoder(),
                                        new MessageDecoder(),
                                        new IdleStateHandler(nettyServerConfig.getReaderIdleTimeSeconds(), 0, 0),
                                        new NettyServerConnectManagerHandler(authExecutor, nettyServerConfig.getMaxAuthTimeout(), channelTemplate),
                                        new NettyServerChannelHandler());
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind(nettyServerConfig.getPort()).sync();
            if (future.isSuccess()) {
                log.info("NettyServer is running on port:{}", nettyServerConfig.getPort());
            } else {
                throw new NettyStartException("failed to start NettyServer", future.cause());
            }

        } catch (InterruptedException e) {
            throw new NettyStartException(e);
        }
    }

    class NettyServerChannelHandler extends SimpleChannelInboundHandler<Message>{

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
            MessageProcessor processor = messageProcessors.get(msg.getType());
            if (processor == null) {
                String failMsg = String.format("Cannot found process for message type:%s", msg.getType());
                channelTemplate.send(ctx.channel(), Message.fromPayload(MessageType.AUTH, failMsg));
                return;
            }
            processorExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    processor.handleMessage(channelTemplate, ctx, msg);
                }
            });
        }

    }

    @Override
    public void destroy() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (selectorGroup != null) {
            selectorGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        processorExecutor.shutdown();
        log.info("NettyServer has been destroyed");
    }

    private boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform()
                && nettyServerConfig.isUseEpoll()
                && Epoll.isAvailable();
    }
}
