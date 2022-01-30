package com.tangthree.imessage.protocol;

import cn.hutool.core.lang.Snowflake;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author TangThree
 * Created on 2022/1/29 8:40 PM
 **/

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message
{
    private static final Snowflake snowflake = new Snowflake(
            new Random().nextInt(31),
            new Random().nextInt(31),
            true);

    public static final byte MAGIC_CODE = 88;

    private MessageType type;
    private long id;
    private byte[] body;

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", type=" + type +
                ", body=" + (body == null ? null : new String(body, StandardCharsets.UTF_8)) +
                '}';
    }

    public static Message fromPayload(MessageType type) {
        return Message.builder()
                .type(type)
                .id(snowflake.nextId())
                .build();
    }

    public static Message fromPayload(MessageType type, String body) {
        return fromPayload(type, body.getBytes(StandardCharsets.UTF_8));
    }

    public static Message fromPayload(MessageType type, byte[] body) {
        return Message.builder()
                .type(type)
                .id(snowflake.nextId())
                .body(body)
                .build();
    }
}
