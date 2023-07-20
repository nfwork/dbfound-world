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
创建文件user.xml放入到resources下的model目录，定义用户查询接口，可以根据user_name、user_code、time_from、time_to进行查询； limit和start控制分页逻辑；
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
        <filter name="user_code" express="user_code like ${@user_code}" />
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
### 5、保存案列
当user_id为空的时候调用新增，否则调用修改；注意otherwise 需要dbfound-3.0.1后 才支持；
```xml
<execute name="save">
    <sqls>
        <whenSql when="${@user_id} is null">
            <execute name="add"/>
        </whenSql>
        <otherwiseSql>
            <execute name="update" />
        </otherwiseSql>
    </sqls>
</execute>
```

### 6、批量导入用户
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
### 7、批量查询案例 
有时候一个功能，查询条件有多个下拉框需要从数据库取值；我们可以一次性查询多个返回；
在userBatch.xml中，添加一个名为batchGet的execute；
```xml
<execute name="batchGet">
    <sqls>
        <query modelName="user" name="" rootPath="outParam.data1"/>
        <query modelName="user" name="" rootPath="outParam.data2"/>
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
### 8、批量添加 有user_id就修改，没有就新增；
在userBatch.xml 新增一个名为batchAdd的execute，定义批量添加接口；注意新增和修改调用之前user.xml中定义的方法
```xml
<execute name="batchAdd">
<sqls>
    <batchSql sourcePath="userList">
        <whenSql when="${@user_id} is null">
            <execute modelName="user" name="add"/>
        </whenSql>
        <otherwiseSql>
            <execute modelName="user" name="update"/>
        </otherwiseSql>
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
### 9、dbfound内置批量新增GridData
当提交数据中包含了一个GridData的数组，则会触发dbfound内置的批量新增；
同时内置了一个addOrUpdate的特定execute名，根据属性 _status 动态判断是新增，还是修改；
_status为old则表面是老数据进行修改，为new则表示新数据进行新增；分别调用model中 名为 add 和 update的execute方法；
拿user.xml举例，请求 http://localhost:8080/user.execute!addOrUpdate 请求参数如下：
```json
{
  "GridData": [
    {
      "user_name" : "小明",
      "user_code" : "xiaoming",
      "password"  : "123456123",
      "_status"   : "new"
    },
    {
      "user_name" : "小杨",
      "user_id" : "2",
      "password"  : "123456123",
      "_status"   : "old"
    }
  ]
}
```

### 10、多表插入 第一张表自动生成主键id，作为第二张表的外键
```xml
<execute name="addTwoTable">
    <param name="user_id" ioType="out"/>
    <sqls>
        <executeSql generatedKeyParam="user_id">
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
        <executeSql>
            <![CDATA[
            insert into table2(user_id)
            values (${@user_id})				
            ]]>
        </executeSql>
    </sqls>
</execute>
```

### 11、集合参数
dbfound 2.6.1后支持 dataType="collection" 用于处理sql集合类赋值，如in场景；
```xml
<query name="getByIds">
    <sql>
        <![CDATA[
        SELECT
            u.user_id,
            u.user_name,
            u.user_code,
            u.create_date,
            u.create_by
        FROM user u
        where u.user_id in (${@ids})
     ]]>
    </sql>
</query>
```
请求参数如下
```json
{
	"ids":[14,15]
}
```
返回结果
```json
{
    "success": true,
    "message": "success",
    "outParam": {},
    "datas": [
        {
            "create_by": 1,
            "user_code": "xiaoyang",
            "user_id": 14,
            "user_name": "小杨",
            "create_date": "2022-06-29 16:12:24"
        },
        {
            "create_by": 1,
            "user_code": "xiaoming1",
            "user_id": 15,
            "user_name": "小明",
            "create_date": "2022-07-09 09:02:22"
        }
    ],
    "totalCounts": 2
}
```
注意： 对于普通类型的集合直接使用即可，如果是对象类集合，则需要额外指定下属性路径；比如集合中位一个User对象，业务是需要取值user_id这个属性；
```xml
<param name="ids" dataType="collection" innerPath="user_id"/>
```

### 12、适配器
框架为query和execute提供了适配器，用于数据适配；
```xml

<query adapter="com.dbfound.world.adapter.UserAdapter">
    <sql>
     <![CDATA[
        SELECT
            u.user_id,
            u.user_name,
            u.user_code,
            'sing,dance,chess' tags
        FROM user u
        #WHERE_CLAUSE#
     ]]>
    </sql>
</query>
```

```java

@Component
public class UserAdapter implements QueryAdapter<User> {

    @Override
    public void beforeQuery(Context context, Map<String, Param> params) {
    }

    @Override
    public void beforeCount(Context context, Map<String, Param> params, Count count) {
    }

    @Override
    public void afterQuery(Context context, Map<String, Param> params, QueryResponseObject<User> responseObject) {
        List<User> dataList = responseObject.getDatas();

        for (User user : dataList){
            if(DataUtil.isNotNull(user.getTags())){
                String[] tags = user.getTags().split(",");
                user.setTagArray(tags);
            }
        }
    }
}
```
### 13、caseWhen条件判断案列
```xml
<execute name="caseWhen">
    <sqls>
        <caseSql>
            <whenSql when="${@flag} = 1">
                <execute modelName="user" name="add" />
            </whenSql>
            <whenSql  when="${@flag} = 2">
                <execute modelName="user" name="update" />
            </whenSql>
            <otherwiseSql>
                <execute modelName="user" name="delete" />
            </otherwiseSql>
        </caseSql>
    </sqls>
</execute>
```

### 14、id集合删除，并返回成功删除的条数
```xml
<execute name="deleteByList">
    <param name="delete_num" ioType="out" dataType="number" />
    <sqls>
        <executeSql affectedCountParam="delete_num">
            <![CDATA[
            delete from user where user_id in( ${@user_id_list})
            ]]>
        </executeSql>
    </sqls>
</execute>
```
请求参数
```json
{
  "user_id_list":[1,2,3,18]
}
```
响应体
```json
{
    "success": true,
    "message": "success",
    "code": null,
    "outParam": {
        "delete_num": 1
    }
}
```

### 16、查询返回List<Integer>数据
通常情况下，查询返回的是一个对象；偶尔也有只需要一个list简单类型的情况；3.3.5后开始支持；
```xml
<query name="getAll" entity="java.lang.Integer">
    <sql>
        SELECT u.user_id FROM user u order by user_id
    </sql>
</query>
```
返回如下
```json
{
    "datas": [
        1,
        2,
        3,
        4,
        5,
        6,
        17,
        18
    ],
    "success": true,
    "message": "success",
    "totalCounts": 8
}
```

### 15、excel数据导出
所有的query对象，都支持excel导出；拿user.xml中的 默认query（query没有name)举例；
访问地址：http://localhost:8080/user.export 就可以将数据导出了；需要传入导出参数制定excel列信息；
```json
{
  "parameters": {
    "user_name" : "小明",
    "user_code" : "",
    "time_from" : "2022-05-08",
    "time_to"   : ""
  },
  "columns": [
    {"name": "user_code","content": "用户编号", "width": 150},
    {"name": "user_name","content": "用户名称", "width": 150}
  ]
}
```



