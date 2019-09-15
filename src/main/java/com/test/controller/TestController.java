package com.test.controller;

import com.test.service.ITestService;
import org.springframework.annotation.Autowired;
import org.springframework.annotation.Controller;
import org.springframework.annotation.RequestMapping;
import org.springframework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: lanxinghua
 * Date: 2019/9/15 12:04
 * Desc:
 */
@Controller
@RequestMapping("/test")
public class TestController {
    @Autowired
    private ITestService testService;

    @RequestMapping("/get.json")
    public void test(HttpServletRequest req, HttpServletResponse res, @RequestParam("name") String name){
        String result =  testService.test(name);
        try {
           res.getWriter().print(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
