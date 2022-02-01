package com.tangthree.imessage.example.server.service;

import com.tangthree.imessage.server.springboot.service.BaseIMessageServiceHandler;
import com.tangthree.imessage.server.springboot.service.MessageContext;
import com.tangthree.imessage.server.springboot.service.IMessageService;
import org.springframework.stereotype.Service;

/**
 * @author TangThree
 * Created on 2022/2/1 12:54 AM
 **/

@IMessageService("auth")
@Service
public class AuthService extends BaseIMessageServiceHandler<String> {
    @Override
    public Object processMessage(MessageContext context, long messageId, String message) {
        return null;
    }
}
