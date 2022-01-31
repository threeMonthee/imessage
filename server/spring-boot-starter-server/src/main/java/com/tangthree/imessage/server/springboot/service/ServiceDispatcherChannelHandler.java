package com.tangthree.imessage.server.springboot.service;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import com.tangthree.imessage.server.springboot.util.JsonUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangThree
 * Created on 2022/1/31 11:09 PM
 **/

@Slf4j
@ChannelHandler.Sharable
public class ServiceDispatcherChannelHandler extends SimpleChannelInboundHandler<Message> {

    private Map<String, NettyServiceHandler> nettyServices = new ConcurrentHashMap<>();

    public void registerService(String serviceName, NettyServiceHandler nettyServiceHandler) {
        if (nettyServices.containsKey(serviceName)) {
            throw new RuntimeException(String.format("The service [%s] has been registered", serviceName));
        }
        nettyServices.put(serviceName, nettyServiceHandler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message.getType() != MessageType.SERVICE.getValue()) {
            return;
        }
        NettyResponse response = new NettyResponse();
        ServiceMessageBody messageBody;
        try {
            messageBody = JsonUtils.readObject(message.getBody(), ServiceMessageBody.class);
        } catch (Exception e) {
            log.error("parse message body fail", e);
            response.setStatus(NettyStatus.BAD_REQUEST, "Cannot parse message body");
            writeResponse(ctx, message, response);
            return;
        }
        String serviceName = messageBody.getService();
        if (StringUtils.isEmpty(serviceName)) {
            response.setStatus(NettyStatus.BAD_REQUEST, "The Service must not be null");
            writeResponse(ctx, message, response);
            return;
        }
        NettyServiceHandler<?> nettyServiceHandler = nettyServices.get(serviceName);
        if (nettyServiceHandler == null) {
            response.setStatus(NettyStatus.SERVICE_NOT_FOUND, String.format("The Service [%s] is not exist", serviceName));
            writeResponse(ctx, message, response);
            return;
        }

        try {
            Type bodyType = ((ParameterizedType)nettyServiceHandler.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            NettyRequest<?> request = new NettyRequest();
            request.setCtx(ctx);
            request.setMessageId(message.getId());
            request.setBody(JsonUtils.readObject(messageBody.getMessageBody(), bodyType));
            Object responseBody = nettyServiceHandler.doRequest(request);
            response.setData(responseBody);
            writeResponse(ctx, message, response);
        } catch (Exception e) {
            log.error(String.format("Execute %s causeException", nettyServiceHandler), e);
            response.setStatus(NettyStatus.INTERNAL_ERROR, "internal error");
            writeResponse(ctx, message, response);
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, Message source, NettyResponse response) {
        log.debug("netty response:{}", response);
        ctx.writeAndFlush(source.newReplyMessage(JsonUtils.toJson(response)));
    }
}
