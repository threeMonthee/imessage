package com.imessage.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author TangThree
 * Created on 2022/1/29 8:42 PM
 **/
public class MessageDecoder  extends LengthFieldBasedFrameDecoder {
    public MessageDecoder() {
        super(0, 0, 0);
    }
}
