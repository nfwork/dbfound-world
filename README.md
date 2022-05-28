# dbfound-world 后台接口十大场景案例，原来后台可以如此简单
##1、查询案例
创建文件user.xml放入到resources目录，定义用户查询接口，可以根据user_name、user_code、time_from、time_to进行查询；
```xml
<?xml version="1.0" encoding="UTF-8"?>
<model xmlns="http://dbfound.googlecode.com/model" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://dbfound.googlecode.com/model https://raw.githubusercontent.com/nfwork/dbfound/master/tags/model.xsd">
    <query>
        <sql>
            <![CDATA[
			SELECT
				u.user_id,
				u.user_name,
				u.role_id,
				u.user_code,
				u.status,
				u.create_date,
				u.create_by
			FROM SYS_USER u
			#WHERE_CLAUSE#
		 ]]>
        </sql>
        <filter name="time_from" express="create_date &gt;= ${@time_from}" />
        <filter name="time_to" express="create_date &lt;= ${@time_to}" />
        <filter name="user_code" express="user_code like '#{@user_code}'" />
        <filter name="user_name" express="user_name like ${@user_name}" />
    </query>
</model>
```
请求地址http://localhost:8080/user.query 即可调用该接口，demo中采用Content-Type：application/json方式请求；
```json
{
	"user_name" : "小明",
	"user_code" : "",
	"time_from" : "2022-05-08",
	"time_to"   : ""
}
```
返回结构如下：
```json
{
    "success": true,
    "message": "success",
    "outParam": null,
    "datas": [
        {
            "create_by": 2,
            "password": "123456123",
            "user_code": "07420101",
            "user_id": 7,
            "role_id": 6,
            "user_name": "小明",
            "create_date": "2021-08-01 00:00:00",
            "status": "Y"
        }
    ],
    "totalCounts": 1
}
```

##2、新增案例
在案例1中创建的user.xml中,创建一个名为add的execute，用来添加用户
```xml
<execute name="add">
    <sqls>
        <collisionSql
            where="exists (select 1 from sys_user where user_code= ${@user_code})"
            message="用户编号:#{@user_code} 已经使用！" />
        <executeSql>
         <![CDATA[
            INSERT INTO sys_user
               (user_code,
                user_name,
                password,
                role_id,
                status,
                create_by,
                create_date,
                last_update_by,
                last_update_date)
            VALUES
                (${@user_code},
                ${@user_name},
                ${@password},
                ${@role_id},
                ${@status},
                1,
                NOW(),
                1,
                NOW())
         ]]>
        </executeSql>
    </sqls>
</execute>
```
请求地址http://localhost:8080/user.execute!add 传入如下参数：
```json
{
	"user_name" : "小明",
	"user_code" : "xiaoming",
	"password"  : "123",
	"role_id"   : 2,
	"status"    : "Y"
}
```
返回结果如下：
```json
{
    "success": true,
    "message": "success",
    "outParam": null
}
```