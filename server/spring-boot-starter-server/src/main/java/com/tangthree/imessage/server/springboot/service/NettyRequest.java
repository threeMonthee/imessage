package com.tangthree.imessage.server.springboot.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @author TangThree
 * Created on 2022/1/31 11:12 PM
 **/

@Setter
@Getter
public class NettyRequest<T> {
    private ChannelHandlerContext ctx;
    private Long messageId;
    private T body;
}
