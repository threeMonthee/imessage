package com.tangthree.imessage.server.springboot.netty;

import com.tangthree.imessage.protocol.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

/**
 * @author TangThree
 * Created on 2022/1/31 1:47 AM
 **/
public interface IMessageTemplate {

    Channel getChannel(String clientId);

    /**
     * @param clientId
     * @param message
     * @return null when clientId is not found
     */
    ChannelFuture send(String clientId, Message message);

    ChannelFuture send(Channel channel, Message message);

    /**
     * @param clientId
     * @param message
     * @param promise
     * @return null when clientId is not found
     */
    ChannelFuture send(String clientId, Message message, ChannelPromise promise);

    ChannelFuture send(Channel channel, Message message, ChannelPromise promise);

    void closeAndRemove(String clientId);

    void closeAndRemove(Channel channel);
}
