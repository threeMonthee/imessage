package com.tangthree.imessage.protocol;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TangThree
 * Created on 2022/1/30 3:12 PM
 **/

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageType {
    HEARTBEAT ((byte) 0),
    AUTH      ((byte) 1),
    BUSINESS  ((byte) 2)
    ;

    private byte value;

    public static MessageType from(byte val) {
        for (MessageType messageType : values()) {
            if (messageType.value == val) {
                return messageType;
            }
        }
        return null;
    }
}
