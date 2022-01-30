package com.tangthree.imessage.server;

import com.tangthree.imessage.protocol.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

/**
 * @author TangThree
 * Created on 2022/1/31 1:47 AM
 **/
public interface ChannelTemplate {

    Channel getChannel(String userId);

    /**
     * @param userId
     * @param message
     * @return null when userId is not found
     */
    ChannelFuture send(String userId, Message message);

    ChannelFuture send(Channel channel, Message message);

    /**
     * @param userId
     * @param message
     * @param promise
     * @return null when userId is not found
     */
    ChannelFuture send(String userId, Message message, ChannelPromise promise);

    ChannelFuture send(Channel channel, Message message, ChannelPromise promise);

    void closeAndRemove(String userId);

    void closeAndRemove(Channel channel);
}
