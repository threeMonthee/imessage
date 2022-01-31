package com.tangthree.imessage.server.springboot.netty;


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

    public void register(String clientId, Channel channel) {
        Channel oldChannel = channelMap.get(clientId);
        if (oldChannel != null) {
            closeAndRemove(oldChannel);
        }
        channelMap.put(clientId, channel);
    }

    @Override
    public Channel getChannel(String clientId) {
        return channelMap.get(clientId);
    }

    @Override
    public ChannelFuture send(String clientId, Message message) {
        Channel channel = getChannel(clientId);
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
    public ChannelFuture send(String clientId, Message message, ChannelPromise promise) {
        Channel channel = getChannel(clientId);
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
    public void closeAndRemove(String clientId) {
        Channel channel = channelMap.remove(clientId);
        RemotingUtil.closeChannel(channel);
    }

    @Override
    public void closeAndRemove(Channel channel) {
        RemotingUtil.closeChannel(channel);
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(ChannelAttributes.USER_ID));
        String clientId = attribute.get();
        if (clientId != null) {
            channelMap.remove(clientId);
        }
    }
}
