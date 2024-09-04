package com.weimin.server.service;

public class HelloServiceZhImpl implements HelloService {
    @Override
    public String sayHi(String name) {
        return "嗨, " + name;
    }
}
