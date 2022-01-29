package com.imessage.protocol;

import lombok.*;

/**
 * @author TangThree
 * Created on 2022/1/29 8:40 PM
 **/

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Message
{
    public static final int TYPE_HEARTBEAT = 0;
    public static final int TYPE_OTHER     = 1;
    public static final int TYPE_AUTH      = 2;

    private long id;
    private short type;
    private byte[] body;
}
