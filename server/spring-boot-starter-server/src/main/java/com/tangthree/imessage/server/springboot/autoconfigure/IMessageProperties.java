package com.tangthree.imessage.server.springboot.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author TangThree
 * Created on 2022/1/31 5:15 PM
 **/

@Setter
@Getter
@ConfigurationProperties(prefix = "imessage.server")
public class IMessageProperties {

    private Boolean useEpoll;

    private Integer selectorThreads;

    private Integer workerThreads;

    private Integer serviceThreads;

    private Integer heartbeatTimeout;

    private Integer maxAuthTimeout;

    private Integer port;
}
