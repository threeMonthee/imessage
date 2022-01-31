package com.tangthree.imessage.server.springboot.autoconfigure;

/**
 * @author TangThree
 * Created on 2022/1/31 11:52 PM
 **/
public class ServiceRegisterException extends RuntimeException{
    public ServiceRegisterException() {
    }

    public ServiceRegisterException(String message) {
        super(message);
    }

    public ServiceRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceRegisterException(Throwable cause) {
        super(cause);
    }

    public ServiceRegisterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
