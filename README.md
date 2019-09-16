## 手写SpringMvc
项目结构
![avatar](/home/picture/1.png)

---
##### 登陆测试Demo
![avatar](/home/picture/1.png)
![avatar](/home/picture/1.png)

```html
@RequestMapping("/login")
    public MyModeAndView login(@RequestParam("name") String name, @RequestParam("pwd") String pwd){
        MyModeAndView modeAndView = new MyModeAndView();
        modeAndView.setViewName("index");
        modeAndView.addObject("name", name);
        return modeAndView;
    }
```

---
```html
<html>
<head>
    <title>登陆</title>
</head>
<body>
<div>
    <h1>欢迎登陆</h1>
    <form action="/test/login" method="get">
   <div>
       <label>用户名：</label>
       <input name="name" type="text"/>
   </div>
    <div>
        <label>用户名：</label>
        <input name="pwd" type="password"/>
    </div>
    <div>
        <label></label>
        <input type="submit" value="提交"/>
    </div>
    </form>
</div>
</body>
</html>

```

---
```html
<html>
<head>
    <title>Title</title>
</head>
<body>
<div>
    <h1>手写SpringMvc</h1>
    <h3 style="color: blue;">欢迎 ${name} 你使用本系统....</h3>
</div>
</body>
</html>
```
