package com.tangthree.imessage.server.springboot.autoconfigure;

import com.tangthree.imessage.server.core.NettyServer;
import com.tangthree.imessage.server.core.NettyServerConfig;
import com.tangthree.imessage.server.springboot.netty.IMessageTemplate;
import com.tangthree.imessage.server.springboot.netty.DefaultIMessageTemplate;
import com.tangthree.imessage.server.springboot.netty.IMessageServiceRegisterException;
import com.tangthree.imessage.server.springboot.service.IMessageService;
import com.tangthree.imessage.server.springboot.service.IMessageServiceHandler;
import com.tangthree.imessage.server.springboot.netty.ServiceDispatcherChannelHandler;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.Map;

/**
 * @author TangThree
 * Created on 2022/1/31 3:57 AM
 **/

@Slf4j
@Configuration
@EnableConfigurationProperties(IMessageProperties.class)
@ConditionalOnProperty(prefix = "imessage", name = "server", matchIfMissing = true)
public class IMessageAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDispatcherChannelHandler.class)
    public ServiceDispatcherChannelHandler serviceDispatcherChannelHandler() {
        ServiceDispatcherChannelHandler dispatcherChannelHandler = new ServiceDispatcherChannelHandler();
        Map<String, IMessageServiceHandler> nettyServiceHandlerBeans = applicationContext.getBeansOfType(IMessageServiceHandler.class);
        if (nettyServiceHandlerBeans.size() > 0) {
            nettyServiceHandlerBeans.values().forEach(IMessageServiceHandler -> {
                IMessageService IMessageService = IMessageServiceHandler.getClass().getAnnotation(IMessageService.class);
                if (IMessageService == null) {
                    throw new IMessageServiceRegisterException(String.format("[%s] must contains annotation:NettyService", IMessageServiceHandler.getClass()));
                }
                log.info("Registering NettyService:[{}]", IMessageServiceHandler.getClass());
                dispatcherChannelHandler.registerService(IMessageService.value(), IMessageServiceHandler);
            });
        }
        return dispatcherChannelHandler;
    }

    @Bean
    @ConditionalOnMissingBean(IMessageTemplate.class)
    public IMessageTemplate channelTemplate() {
        return new DefaultIMessageTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(NettyServer.class)
    @ConditionalOnBean(ServiceDispatcherChannelHandler.class)
    public NettyServer nettyServer(IMessageProperties iMessageProperties,
                                   ServiceDispatcherChannelHandler dispatcherServiceHandler) {
        NettyServerConfig config = new NettyServerConfig();
        if (iMessageProperties.getUseEpoll() != null) {
            config.setUseEpoll(iMessageProperties.getUseEpoll());
        }
        if (iMessageProperties.getSelectorThreads() != null) {
            config.setSelectorThreads(iMessageProperties.getSelectorThreads());
        }
        if (iMessageProperties.getWorkerThreads() != null) {
            config.setWorkerThreads(iMessageProperties.getWorkerThreads());
        }
        if (iMessageProperties.getServiceThreads() != null) {
            config.setServiceThreads(iMessageProperties.getServiceThreads());
        }
        if (iMessageProperties.getHeartbeatTimeout() != null) {
            config.setReaderIdleTimeSeconds(iMessageProperties.getHeartbeatTimeout());
        }
        if (iMessageProperties.getMaxAuthTimeout() != null) {
            config.setMaxAuthTimeout(iMessageProperties.getMaxAuthTimeout());
        }
        if (iMessageProperties.getPort() != null) {
            config.setPort(iMessageProperties.getPort());
        }
        NettyServer nettyServer = new NettyServer(config);
        nettyServer.registerServiceChannelHandler(dispatcherServiceHandler);
        Map<String, ChannelHandler> channelHandlers = applicationContext.getBeansOfType(ChannelHandler.class);
        if (channelHandlers.size() > 1) {
            Iterator<ChannelHandler> channelHandlerIterator = channelHandlers.values().iterator();
            int i = 0;
            ChannelHandler[] channelHandlerArray = new ChannelHandler[channelHandlers.size() - 1];
            while (channelHandlerIterator.hasNext()) {
                ChannelHandler channelHandler = channelHandlerIterator.next();
                if (channelHandler != dispatcherServiceHandler) {
                    log.info("Registering ChannelHandler: [{}]", channelHandler);
                    channelHandlerArray[i] = channelHandler;
                    i++;
                }
            }
            nettyServer.registerServiceChannelHandler(channelHandlerArray);
        }
        nettyServer.start();
        return nettyServer;
    }
}
