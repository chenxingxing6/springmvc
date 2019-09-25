package com.test.controller;

import com.test.service.ITestService;
import org.springframework.annotation.*;
import org.springframework.core.MyModeAndView;

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

    @RequestMapping("/view")
    public MyModeAndView view(HttpServletRequest req){
        String name = req.getParameter("name");
        String path = req.getParameter("path");
        MyModeAndView modeAndView = new MyModeAndView();
        modeAndView.setViewName(path);
        modeAndView.addObject("name", name);
        return modeAndView;
    }

    @RequestMapping("/logintest")
    public String view(){
        return "/login";
    }

    @RequestMapping(value = "/get.json", method = RequestMethod.GET)
    @ResponseBody
    public String test(@RequestParam("name") String name){
        return testService.test(name);
    }

    @RequestMapping("/login")
    public MyModeAndView login(@RequestParam("name") String name, @RequestParam("pwd") String pwd){
        MyModeAndView modeAndView = new MyModeAndView();
        modeAndView.setViewName("index");
        modeAndView.addObject("name", name);
        return modeAndView;
    }

    @RequestMapping("/getUser")
    @ResponseBody
    public User getUser(){
        User user = new User();
        user.setAge(20);
        user.setName("superboycxx");
        return user;
    }

    @RequestMapping("/add")
    @ResponseBody
    public int test(HttpServletResponse resp, @RequestParam("a") Integer a, @RequestParam("b") Integer b){
        return a + b;
    }

    class User{
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
