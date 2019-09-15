package com.test.service.impl;

import com.test.service.ITestService;
import org.springframework.annotation.Service;

/**
 * User: lanxinghua
 * Date: 2019/9/15 12:06
 * Desc:
 */
@Service
public class TestService implements ITestService {
    public String test(String name) {
       return name + "-自己手写SpringMvc";
    }
}
