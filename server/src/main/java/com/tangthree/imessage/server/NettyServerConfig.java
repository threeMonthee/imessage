package com.tangthree.imessage.server;

import io.netty.util.NettyRuntime;
import lombok.Getter;
import lombok.Setter;

/**
 * @author TangThree
 * Created on 2022/1/30 8:15 PM
 **/

@Setter
@Getter
public class NettyServerConfig {

    private static final int availableProcessors = NettyRuntime.availableProcessors();

    private boolean useEpoll             = true;

    private int selectorThreads          = availableProcessors * 2;

    private int workerThreads            = availableProcessors * 4;

    private int processorThreads         = availableProcessors * 8;

    private int port                     = 1937;

    private int readerIdleTimeSeconds    = 60;

    private int maxAuthTimeout           = 30;

    public static void main(String[] args) {
        System.out.println(availableProcessors);
    }

}
