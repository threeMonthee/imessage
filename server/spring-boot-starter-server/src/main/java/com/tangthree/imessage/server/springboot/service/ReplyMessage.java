package com.tangthree.imessage.server.springboot.service;

import cn.hutool.json.JSONUtil;
import com.tangthree.imessage.server.springboot.util.JsonUtils;
import lombok.*;

/**
 * @author TangThree
 * Created on 2022/1/31 11:40 PM
 **/

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ReplyMessage {
    private int status;
    private String msg;
    private Object data;

    public ReplyMessage(NettyStatus status) {
        this.status = status.getVal();
    }

    public ReplyMessage(NettyStatus status, String msg) {
        this.status = status.getVal();
        this.msg = msg;
    }

    public void setStatus(NettyStatus status, String msg) {
        this.status = status.getVal();
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "NettyResponse{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + JSONUtil.toJsonStr(data) +
                '}';
    }
}
