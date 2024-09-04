package com.weimin.server.service;

public class HelloServiceEnImpl implements HelloService {
    @Override
    public String sayHi(String name) {
        return "hi, " + name;
    }
}
