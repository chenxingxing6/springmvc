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

    @RequestMapping("/view.jsp")
    public MyModeAndView view(HttpServletRequest req){
        MyModeAndView modeAndView = new MyModeAndView();
        modeAndView.setViewName("index");
        modeAndView.addObject("name", "lanxinghua");
        return modeAndView;
    }

    @RequestMapping("/get.json")
    @ResponseBody
    public String test(@RequestParam("name") String name){
        return testService.test(name);
    }

    @RequestMapping("/getUser")
    @ResponseBody
    public User getUser(){
        User user = new User();
        user.setAge(20);
        user.setName("superboycxx");
        return user;
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

    @RequestMapping("/add")
    @ResponseBody
    public String test(HttpServletResponse resp, @RequestParam("a") Integer a, @RequestParam("b") Integer b){
        return String.valueOf(a + b);
    }
}
