package com.weimin.server.service;

public class HelloServiceZhImpl implements HelloService {
    @Override
    public String sayHi(String name) {
        return "嗨, " + name;
    }

    @Override
    public String sayHello(String name) {
        int a = 10 / 0;
        return "你好，" + name;
    }
}
