package com.tangthree.imessage.netty.client.test;

import cn.hutool.json.JSONUtil;
import com.tangthree.imessage.client.NettyClient;
import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TangThree
 * Created on 2022/1/30 6:05 PM
 **/

@Slf4j
public class NettyClientTest {

    @Test
    public void startClient()
    {
        NettyClient client = new NettyClient(10, 60, 30);
        try {
            final String host = "127.0.0.1";
            final int port = 10937;
            ChannelFuture future = client.connect(host, port).sync();
            if (future.cause() != null) {
                log.error("failure to connect {}:{}", host, port);
                return;
            }

            Thread.sleep(1000);
            auth(future.channel());

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.destroy();
    }

    private void auth(Channel channel)
    {
        Map<String,Object> messageBody = new HashMap<>();
        messageBody.put("service", "auth");
        messageBody.put("body", "123456");
        channel.writeAndFlush(Message.fromPayload(MessageType.SERVICE.getValue(), JSONUtil.toJsonStr(messageBody)));
    }
}
