package com.ds.devsuaccount;

import com.ds.devsuaccount.infraestructure.utils.ScopeUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevsuAccountApplication {

    public static void main(String[] args) {
        ScopeUtils.calculateScopeSuffix();
        SpringApplication.run(DevsuAccountApplication.class, args);
    }

}
