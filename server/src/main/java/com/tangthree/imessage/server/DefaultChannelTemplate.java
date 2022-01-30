package com.tangthree.imessage.server;


import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.util.RemotingUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangThree
 * Created on 2022/1/31 1:48 AM
 **/

public class DefaultChannelTemplate implements ChannelTemplate{

    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    public void register(String userId, Channel channel) {
        Channel oldChannel = channelMap.get(userId);
        if (oldChannel != null) {
            closeAndRemove(oldChannel);
        }
        channelMap.put(userId, channel);
    }

    @Override
    public Channel getChannel(String userId) {
        return channelMap.get(userId);
    }

    @Override
    public ChannelFuture send(String userId, Message message) {
        Channel channel = getChannel(userId);
        if (channel == null) {
            return null;
        }
        return send(channel, message);
    }

    @Override
    public ChannelFuture send(Channel channel, Message message) {
        return channel.writeAndFlush(message);
    }

    @Override
    public ChannelFuture send(String userId, Message message, ChannelPromise promise) {
        Channel channel = getChannel(userId);
        if (channel == null) {
            return null;
        }
        return send(channel, message, promise);
    }

    @Override
    public ChannelFuture send(Channel channel, Message message, ChannelPromise promise) {
        return channel.writeAndFlush(message, promise);
    }

    @Override
    public void closeAndRemove(String userId) {
        Channel channel = channelMap.remove(userId);
        RemotingUtil.closeChannel(channel);
    }

    @Override
    public void closeAndRemove(Channel channel) {
        RemotingUtil.closeChannel(channel);
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(ChannelAttributes.USER_ID));
        String userId = attribute.get();
        if (userId != null) {
            channelMap.remove(userId);
        }
    }
}
