package com.imessage.server.remoting;

import com.imessage.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author TangThree
 * Created on 2022/1/29 9:51 PM
 **/

@Slf4j
@Component
public class ServerChannelHandler extends SimpleChannelInboundHandler<Message> {

    private Executor executor;

    public ServerChannelHandler() {
        executor = Executors.newFixedThreadPool(64);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        short type = msg.getType();
        if (type == Message.TYPE_HEARTBEAT) {
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                handleMessage(msg);
            }
        });
    }

    private void handleMessage(Message msg) {
        log.debug("RECEIVE MESSAGE:{}", msg);
    }
}
