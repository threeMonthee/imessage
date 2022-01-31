package com.tangthree.imessage.server.springboot.autoconfigure;

import com.tangthree.imessage.server.core.NettyServer;
import com.tangthree.imessage.server.core.NettyServerConfig;
import com.tangthree.imessage.server.springboot.netty.ChannelTemplate;
import com.tangthree.imessage.server.springboot.netty.DefaultChannelTemplate;
import com.tangthree.imessage.server.springboot.service.NettyService;
import com.tangthree.imessage.server.springboot.service.NettyServiceHandler;
import com.tangthree.imessage.server.springboot.service.ServiceDispatcherChannelHandler;
import io.netty.channel.ChannelHandler;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author TangThree
 * Created on 2022/1/31 3:57 AM
 **/

@Configuration
@EnableConfigurationProperties(IMessageProperties.class)
@ConditionalOnProperty(prefix = "imessage")
public class IMessageAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDispatcherChannelHandler.class)
    public ServiceDispatcherChannelHandler serviceDispatcherChannelHandler() {
        ServiceDispatcherChannelHandler serviceDispatcherChannelHandler = new ServiceDispatcherChannelHandler();
        Map<String, NettyServiceHandler> nettyServiceHandlerBeans = applicationContext.getBeansOfType(NettyServiceHandler.class);
        if (nettyServiceHandlerBeans.size() > 0) {
            nettyServiceHandlerBeans.values().forEach(nettyServiceHandler -> {
                NettyService nettyService = nettyServiceHandler.getClass().getAnnotation(NettyService.class);
                if (nettyService == null) {
                    throw new ServiceRegisterException(String.format("[%s] must contains annotation:NettyService", nettyServiceHandler.getClass()));
                }
                serviceDispatcherChannelHandler.registerService(nettyService.value(), nettyServiceHandler);
            });
        }
        return serviceDispatcherChannelHandler;
    }

    @Bean
    @ConditionalOnMissingBean(ChannelTemplate.class)
    public ChannelTemplate channelTemplate() {
        return new DefaultChannelTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(NettyServer.class)
    @ConditionalOnBean(ServiceDispatcherChannelHandler.class)
    @ConditionalOnProperty(prefix = "imessage.server")
    public NettyServer nettyServer(IMessageProperties iMessageProperties,
                                   ServiceDispatcherChannelHandler serviceDispatcherChannelHandler) {
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
        nettyServer.registerServiceChannelHandler(serviceDispatcherChannelHandler);
        Map<String, ChannelHandler> channelHandlers = applicationContext.getBeansOfType(ChannelHandler.class);
        if (channelHandlers.size() > 0) {
            channelHandlers.values().forEach(nettyServer::registerServiceChannelHandler);
        }
        nettyServer.start();
        return nettyServer;
    }
}
