package com.qhit.fsaas.util;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Init implements ApplicationRunner {
    @Resource
    private MainUtil mainUtil;

    @Override
    public void run(ApplicationArguments args) {

        mainUtil.init();
        mainUtil.showSeatCount();
        System.out.println("\n------------INIT FINISH!------------");
    }
}
