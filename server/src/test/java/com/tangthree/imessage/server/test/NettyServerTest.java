package com.tangthree.imessage.server.test;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import com.tangthree.imessage.server.ChannelTemplate;
import com.tangthree.imessage.server.NettyServer;
import com.tangthree.imessage.server.NettyServerConfig;
import org.junit.jupiter.api.Test;

/**
 * @author TangThree
 * Created on 2022/1/31 5:03 AM
 **/
public class NettyServerTest {

    @Test
    public void start() throws InterruptedException {
        NettyServerConfig config = new NettyServerConfig();
        NettyServer nettyServer = new NettyServer(config);
        nettyServer.start();
        ChannelTemplate channelTemplate = nettyServer.getChannelTemplate();
        channelTemplate.send("0", Message.fromPayload(MessageType.SERVICE, "123"));

        Thread.sleep(1000000000);
    }

}
