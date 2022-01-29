package com.imessage.server;

import com.imessage.protocol.MessageDecoder;
import com.imessage.protocol.MessageEncoder;
import com.imessage.server.remoting.ChannelManagerHandler;
import com.imessage.server.remoting.ServerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * @author TangThree
 * Created on 2022/1/29 8:54 PM
 **/

@Slf4j
@Component
public class RemotingServer implements ApplicationRunner, DisposableBean
{
    @Resource
    ServerChannelHandler serverChannelHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup defaultEventLoopGroup;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        defaultEventLoopGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                //.childOption(ChannelOption.TCP_NODELAY, true)
                //.childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())
                //.childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize())
                //.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                //        nettyServerConfig.getWriteBufferLowWaterMark(), nettyServerConfig.getWriteBufferHighWaterMark()))
                //.localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
                .channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(defaultEventLoopGroup,
                                        new IdleStateHandler(0, 0, 30),
                                        new MessageEncoder(),
                                        new MessageDecoder(),
                                        new ChannelManagerHandler(),
                                        serverChannelHandler);
                    }
                });
        ChannelFuture future = bootstrap.bind(16372).sync();
        log.info("RemotingServer is running");
    }

    @Override
    public void destroy() throws Exception {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (defaultEventLoopGroup != null) {
            defaultEventLoopGroup.shutdownGracefully();
        }
        log.info("RemotingServer has been destroyed");
    }
}
