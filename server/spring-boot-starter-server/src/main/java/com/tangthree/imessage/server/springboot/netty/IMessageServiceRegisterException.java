package com.tangthree.imessage.server.springboot.netty;

/**
 * @author TangThree
 * Created on 2022/1/31 11:52 PM
 **/
public class IMessageServiceRegisterException extends RuntimeException{
    public IMessageServiceRegisterException() {
    }

    public IMessageServiceRegisterException(String message) {
        super(message);
    }

    public IMessageServiceRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public IMessageServiceRegisterException(Throwable cause) {
        super(cause);
    }

    public IMessageServiceRegisterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
