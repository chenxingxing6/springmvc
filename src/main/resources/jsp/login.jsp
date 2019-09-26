<html>
<head>
    <title>登陆</title>
</head>
<body>
<div>
    <h1>Get方式-欢迎登陆</h1>
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
<hr>
<div>
    <h1>Post方式-欢迎登陆</h1>
    <form action="/test/login" method="post">
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
