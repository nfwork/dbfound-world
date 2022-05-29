## dbfound-world 后台接口十大场景案例，原来后台可以如此简单
### 准备工作
创建一个springboot项目，引入dbfound-spring-boot-start依赖，写好启动类；
创建数据库表user；配置好dbfound数据库链接信息；
```sql
CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_code` varchar(100) NOT NULL,
  `user_name` varchar(200) NOT NULL,
  `password` varchar(50) NOT NULL,
  `create_date` datetime NOT NULL,
  `create_by` int(11) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY  (`user_code`) 
);
```
### 1、查询案例
创建文件user.xml放入到resources目录，定义用户查询接口，可以根据user_name、user_code、time_from、time_to进行查询； limit和start控制分页逻辑；
```xml
<?xml version="1.0" encoding="UTF-8"?>
<model xmlns="http://dbfound.googlecode.com/model" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://dbfound.googlecode.com/model https://raw.githubusercontent.com/nfwork/dbfound/master/tags/model.xsd">
    <query>
        <sql>
        <![CDATA[
            SELECT
                u.user_id,
                u.user_name,
                u.user_code,
                u.create_date,
                u.create_by
            FROM user u
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
    "time_to"   : "",
    "limit"     : 10,
    "start"     : 0
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
            "create_by": 1,
            "password": "123456123",
            "user_code": "xiaoming",
            "user_id": 1,
            "user_name": "小明",
            "create_date": "2021-08-01 00:00:00"
        }
    ],
    "totalCounts": 1
}
```
### 2、新增案例
在案例1中创建的user.xml中,创建一个名为add的execute，用来添加用户;正常情况下create_by从登陆session中获取，案例中为了方便直接写死1；
```xml
<execute name="add">
    <sqls>
        <collisionSql
            where="exists (select 1 from user where user_code= ${@user_code})"
            message="用户编号:#{@user_code} 已经使用！" />
        <executeSql>
         <![CDATA[
        INSERT INTO user
               (user_code,
                user_name,
                password,
                create_by,
                create_date)
            VALUES
                (${@user_code},
                ${@user_name},
                ${@password},
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
	"password"  : "123456123"
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
### 3、修改案例
创建一个名为update的execute，用来修改用户
```xml
<execute name="update">
    <sqls>
        <executeSql>
          <![CDATA[
            update user set 
                user_name = ${@user_name}
            where user_id = ${@user_id} 
          ]]>
        </executeSql>
    </sqls>
</execute>
```
请求地址http://localhost:8080/user.execute!update ，请求参数如下：
```json
{
	"user_name" : "小明1",
	"user_id" : 1
}
```
### 4、删除案例
创建一个名为delete的execute，用来删除用户；这次我们允许批量删除
```xml
<execute name="delete">
    <sqls>
        <batchSql sourcePath="userList">
            <executeSql>
            <![CDATA[
                delete from sys_user where user_id= ${@user_id} 
            ]]>
            </executeSql>
        </batchSql>
    </sqls>
</execute>
```
请求地址 http://localhost:8080/user.execute!delete ,请求参数如下：
```json
{
	"userList":[
		{"user_id":1},
		{"user_id":2}
	]
}
```
### 5、批量导入用户
创建一个userBatch.xml文件，用来定义导入接口；
```xml
<?xml version="1.0" encoding="UTF-8"?>
<model xmlns="http://dbfound.googlecode.com/model" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://dbfound.googlecode.com/model https://raw.githubusercontent.com/nfwork/dbfound/master/tags/model.xsd">
    <execute>
        <sqls>
            <batchExecuteSql sourcePath="userList">
            <![CDATA[
                 INSERT INTO user
                       (user_code,
                        user_name,
                        password,
                        create_by,
                        create_date)
                    VALUES
                    #BATCH_TEMPLATE_BEGIN#
                        (${@user_code},
                        ${@user_name},
                        ${@password},
                        1,
                        NOW())		
                    #BATCH_TEMPLATE_END#	
                    ON DUPLICATE KEY update user_name = values(user_name)
            ]]>
            </batchExecuteSql>
        </sqls>
    </execute>
</model>
```
请求地址http://localhost:8080/userBatch.execute ，请求参数如下：
```json
{
  "userList":[
    {
      "user_name" : "小明",
      "user_code" : "xiaoming",
      "password"  : "123456123"
    },
    {
      "user_name" : "小杨",
      "user_code" : "xiaoyang",
      "password"  : "123456123"
    }
  ]
}
```
### 6、批量查询案例 
有时候一个功能，查询条件有多个下拉框需要从数据库取值；我们可以一次性查询多个返回；
在userBatch.xml中，添加一个名为batchGet的execute；
```xml
<execute name="batchGet">
    <sqls>
        <query modelName="user" name="" rootPath="data1"/>
        <query modelName="user" name="" rootPath="data2"/>
    </sqls>
</execute>
```
请求接口地址：http://localhost:8080/userBatch.execute!batchGet
返回接口如下：
```json
{
    "success": true,
    "message": "success",
    "outParam": {
        "data2": [
            {
                "create_by": 1,
                "user_code": "xiaoming",
                "user_id": 2,
                "user_name": "小明",
                "create_date": "2022-05-29 10:53:19"
            },
            {
                "create_by": 1,
                "user_code": "xiao杨",
                "user_id": 3,
                "user_name": "小杨",
                "create_date": "2022-05-29 10:53:19"
            }
        ],
        "data1": [
            {
                "create_by": 1,
                "user_code": "xiaoming",
                "user_id": 2,
                "user_name": "小明",
                "create_date": "2022-05-29 10:53:19"
            },
            {
                "create_by": 1,
                "user_code": "xiao杨",
                "user_id": 3,
                "user_name": "小杨",
                "create_date": "2022-05-29 10:53:19"
            }
        ]
    }
}
```
### 7、批量添加 有user_id就修改，没有就新增；
在userBatch.xml 新增一个名为batchAdd的execute，定义批量添加接口；注意新增和修改调用之前user.xml中定义的方法
```xml
<execute name="batchAdd">
    <sqls>
        <batchSql sourcePath="userList">
            <whenSql when="${@user_id} is null">
                <execute modelName="user" name="add"/>
            </whenSql>
            <whenSql when="${@user_id} is not null">
                <execute modelName="user" name="update"/>
            </whenSql>
        </batchSql>
    </sqls>
</execute>
```
请求地址http://localhost:8080/userBatch.execute!batchAdd 请求参数如下：
```json
{
	"userList":[
		{
			"user_name" : "小明",
			"user_id" : "1"
		},
		{
			"user_name" : "小李",
			"user_code" : "xiaoli",
			"password"  : "123456123"
		}
	]
}
```
