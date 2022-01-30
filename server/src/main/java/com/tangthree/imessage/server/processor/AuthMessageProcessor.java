package com.tangthree.imessage.server.processor;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import com.tangthree.imessage.server.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @author TangThree
 * Created on 2022/1/30 7:56 PM
 **/

@Slf4j
@Service
public class AuthMessageProcessor implements MessageProcessor {
    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH;
    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, Message msg) {
        String token = new String(msg.getBody(), StandardCharsets.UTF_8);
        log.info("auth token:{} success", token);
    }
}
