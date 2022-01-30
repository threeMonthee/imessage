package com.tangthree.imessage.netty.client.test;

import com.tangthree.imessage.client.NettyClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author TangThree
 * Created on 2022/1/30 6:05 PM
 **/

@Slf4j
public class NettyClientTest {

    @Test
    public void startClient()
    {
        NettyClient client = new NettyClient(10, 60, 5);
        try {
            final String host = "127.0.0.1";
            final int port = 1937;
            ChannelFuture future = client.syncConnect(host, port);
            if (future.cause() != null) {
                log.error("failure to connect {}:{}", host, port);
                return;
            }

            sendMsg(future.channel());

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.destroy();
    }

    private void sendMsg(Channel channel)
    {

    }
}
