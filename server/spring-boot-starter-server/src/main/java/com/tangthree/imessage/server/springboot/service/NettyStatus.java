package com.tangthree.imessage.server.springboot.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TangThree
 * Created on 2022/1/31 11:41 PM
 **/

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NettyStatus {

    SUCCESS           (200),
    BAD_REQUEST       (400),
    SERVICE_NOT_FOUND (404),
    INTERNAL_ERROR    (500),
    ;
    @Getter
    private int val;
}
