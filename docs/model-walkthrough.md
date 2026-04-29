# model XML 入门导读

dbfound 的核心开发体验是把常见后台接口声明在 `src/main/resources/model/*.xml` 中。本文按本项目已有文件说明每个 model 适合学习什么，以及怎么从一个简单示例逐步扩展。

## 文件和接口关系

文件名会成为默认接口路径的一部分：

- `model/user.xml` 对应 `/user.query`、`/user.execute!add` 等。
- `model/userBatch.xml` 对应 `/userBatch.execute!batchAdd` 等。
- `model/userPart.xml` 对应 `/userPart.query` 等。

默认节点不写 `name`，具名节点通过 `!name` 访问。

## user.xml：基础查询和增删改

建议先从 `user.xml` 开始阅读。它包含最常用的 CRUD 形态。

默认 `<query>` 展示了分页、基础 SQL 和过滤条件：

```xml
<query pagerSize="10" maxPagerSize="100">
    <sql>
        SELECT
            u.user_id,
            u.user_name,
            u.user_code,
            u.create_date,
            u.create_by
        FROM user u
        #WHERE_CLAUSE#
    </sql>
    <filter name="time_from" dataType="date" express="create_date &gt;= ${@time_from}" />
    <filter name="time_to" dataType="date" express="create_date &lt;= ${@time_to}" />
    <filter name="user_code" dataType="varchar" express="user_code like ${@user_code}" />
    <filter name="user_name" dataType="varchar" express="user_name like ${@user_name}" />
</query>
```

关键点：

- `#WHERE_CLAUSE#` 会放置由 `<filter>` 生成的条件。
- `${@param}` 表示参数绑定，适合值参数。
- `dataType` 会影响参数转换和 SQL 拼接方式。
- `pagerSize` 和 `maxPagerSize` 控制默认分页大小与最大分页大小。

`add` 示例展示了新增前的业务校验：

```xml
<collisionSql
    where="exists (select 1 from user where user_code= ${@user_code})"
    message="用户编号:#{@user_code} 已经使用！" />
```

`collisionSql` 条件成立时会中断执行并返回业务失败信息。`#{@param}` 是文本替换，通常只适合已经确认安全的场景或错误消息。

`save` 示例展示了条件分支：

```xml
<whenSql when="${@user_id} is null">
    <execute name="add"/>
</whenSql>
<otherwiseSql>
    <execute name="update" />
</otherwiseSql>
```

这类组合适合把新增和修改封装成一个保存接口。

## userBatch.xml：批量和组合执行

`userBatch.xml` 适合学习批量处理和跨 model 复用。

- 默认 `<execute>` 使用 `batchExecuteSql` 一次性生成批量 SQL。
- `batchAdd` 使用 `batchSql` 遍历 `userList`，每条数据再走 `user.add` 或 `user.update`。
- `addTwoTable` 展示 `generatedKeyParam`，把插入 `user` 后生成的主键传给下一条 SQL。
- `caseWhen` 展示多分支选择执行。

批量新增请求通常长这样：

```json
{
  "userList": [
    {
      "user_code": "batch001",
      "user_name": "批量用户一",
      "password": "123456"
    }
  ]
}
```

## userPart.xml：动态 SQL

`userPart.xml` 展示了 `sqlPart` 和 `sqlTrim`。

`sqlPart type="if"` 会在参数存在时才输出 SQL 片段：

```xml
<sqlPart type="if" sourcePath="sort">
    order by #{@sort}
</sqlPart>
```

`sqlPart type="for"` 会遍历集合或逗号分隔参数：

```xml
<sqlPart type="for" begin="," sourcePath="fields" separator="," item="field">
    <sqlPart condition="${@field} in ('user_code','user_name','password')">
        #{@field}
    </sqlPart>
</sqlPart>
```

注意：动态字段、动态排序这类 SQL 片段通常需要白名单。示例中的 `sec`、`sec2` 正是为了说明如何限制可输出字段。

`sqlTrim` 适合动态更新语句，避免最后多出逗号：

```xml
<sqlTrim>
    <sqlPart condition="${@user_name} is not null">
        user_name = ${@user_name},
    </sqlPart>
    <sqlPart condition="${@user_code} is not null">
        user_code = ${@user_code},
    </sqlPart>
</sqlTrim>
```

## userAdapter.xml：适配器和实体返回

`userAdapter.xml` 展示三种常见扩展点：

- 查询前处理参数：`CollectionParamAdapter` 把空格、分号、换行等分隔符统一成集合参数。
- 查询后处理结果：`UserAdapter` 把 `tags` 字符串拆成数组。
- 改写分页 count SQL：`CountAdapter` 在 `beforeCount` 中调整 count SQL。

默认查询声明了两个适配器：

```xml
<query adapter="com.dbfound.world.adapter.UserAdapter
                com.dbfound.world.adapter.CollectionParamAdapter">
```

如果查询需要直接返回 Java 实体，可以配置 `entity`：

```xml
<query name="basic" entity="com.dbfound.world.entity.User">
```

## userExcel.xml：Excel 导入导出

`userExcel.xml` 配合 `ExcelService` 使用：

- 导出：`ExcelService.exportData` 调用 `modelExecutor.queryList(context, "userExcel", "")`，再通过 `ExcelWriter` 输出文件。
- 导入：`ExcelService.importData` 用 `ExcelReader` 读取上传文件，再把 `userList` 传给 `userExcel` 默认 execute。

这个示例适合学习“复杂 IO 放在 Java，数据落库交给 model”的写法。

## function.xml：函数和校验

`function.xml` 主要展示内置 SQL 函数和自定义函数在 `collisionSql` 里的用法。

自定义函数来自 `StartWith`：

```java
@PostConstruct
public void init() {
    register("start_with");
}
```

注册后可以在 XML 中这样使用：

```xml
<collisionSql where="start_with(${@message},'abc')" message="不能以abc开头" />
```

## Java 代码何时介入

建议优先使用 XML model 表达通用数据接口。当出现下面情况时，再进入 Java Service：

- 请求参数需要复杂组装。
- 执行结果需要二次判断或改写响应。
- 涉及文件上传、Excel、第三方系统调用等非 SQL 逻辑。
- 需要复用多个 model 调用并组织成一个业务接口。

本项目的 `UserService` 和 `ExcelService` 分别展示了普通业务调用和文件类业务调用。
