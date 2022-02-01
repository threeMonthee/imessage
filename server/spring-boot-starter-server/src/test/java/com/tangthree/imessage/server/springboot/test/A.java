package com.tangthree.imessage.server.springboot.test;

import com.tangthree.imessage.server.springboot.service.IMessageServiceInitializeException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author TangThree
 * Created on 2022/2/1 4:26 PM
 **/
public class A <T>{
    public A() {
        Class thisClass = this.getClass();
        Class superClass = null;
        Type superType = null;
        while (true) {
            superClass = thisClass.getSuperclass();
            if (superClass == A.class) {
                superType = thisClass.getGenericSuperclass();
                if (!(superType instanceof ParameterizedType)) {
                    throw new IMessageServiceInitializeException("不是参数化类型");
                }
                System.out.println(((ParameterizedType)superType).getActualTypeArguments()[0]);
                break;
            }
            thisClass = thisClass.getSuperclass();
        }
    }
}
