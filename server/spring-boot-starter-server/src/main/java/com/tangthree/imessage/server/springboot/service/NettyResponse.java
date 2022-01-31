package com.tangthree.imessage.server.springboot.service;

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
public class NettyResponse {
    private NettyStatus status;
    private String msg;

    public NettyResponse(NettyStatus status) {
        this.status = status;
    }

    public NettyResponse(NettyStatus status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public void setStatus(NettyStatus status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private Object data;

    @Override
    public String toString() {
        return "NettyResponse{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + JsonUtils.toJson(data) +
                '}';
    }
}
