package com.tangthree.imessage.server;

import com.tangthree.imessage.protocol.Message;
import com.tangthree.imessage.protocol.MessageType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/1/29 9:51 PM
 **/

@Slf4j
@ChannelHandler.Sharable
@Component
public class NettyServerChannelHandler extends SimpleChannelInboundHandler<Message> implements ApplicationContextAware, DisposableBean {

    @Autowired
    NettyServerConfig nettyServerConfig;

    private ExecutorService executor;
    private final Map<MessageType, MessageProcessor> messageProcessors = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String,MessageProcessor> processors = applicationContext.getBeansOfType(MessageProcessor.class);
        for (MessageProcessor processor : processors.values()) {
            messageProcessors.put(processor.getMessageType(), processor);
        }

        executor = Executors.newFixedThreadPool(nettyServerConfig.getProcessorThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "MessageProcessorExecutor_" + this.threadIndex.incrementAndGet());
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        MessageProcessor processor = messageProcessors.get(msg.getType());
        if (processor == null) {
            log.error("Cannot found process for message type:{}", msg.getType());
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                processor.handleMessage(ctx, msg);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        executor.shutdown();
    }


}
