package com.tangthree.imessage.server.exception;

/**
 * @author TangThree
 * Created on 2022/1/31 2:38 AM
 **/
public class NettyStartException extends RuntimeException{
    public NettyStartException() {
    }

    public NettyStartException(String message) {
        super(message);
    }

    public NettyStartException(String message, Throwable cause) {
        super(message, cause);
    }

    public NettyStartException(Throwable cause) {
        super(cause);
    }

    public NettyStartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
