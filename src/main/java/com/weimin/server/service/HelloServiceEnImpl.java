package com.weimin.server.service;

public class HelloServiceEnImpl implements HelloService {
    @Override
    public String sayHi(String name) {
        return "hi, " + name;
    }

    @Override
    public String sayHello(String name) {
        return "hello, " + name;
    }
}
