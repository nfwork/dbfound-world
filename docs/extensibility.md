# 扩展点说明

本项目除了 XML 自动接口，还演示了几类常见扩展方式。它们的定位不同，建议按复杂度从低到高选择。

## 选择建议

优先级从高到低：

1. **model XML**：适合查询、增删改、批量、动态 SQL、简单校验。
2. **Adapter**：适合参数预处理、结果后处理、分页 count SQL 优化。
3. **Java Service + ModelExecutor**：适合组合多个 model、处理文件、改写响应、衔接外部系统。
4. **自定义函数**：适合把可复用的表达式逻辑暴露给动态 SQL 或校验条件。

## Adapter

Adapter 适合在不改变 Controller 的情况下扩展 model 行为。

### 查询前处理参数

`CollectionParamAdapter` 实现了 `beforeQuery` 和 `beforeExecute`，用于把集合参数中的空格、逗号、分号等统一成英文逗号：

```java
if(param.getDataType() == DataType.COLLECTION && param.getValue() instanceof String){
    String value = param.getStringValue();
    value = value.replaceAll("[\\s,;]+",",");
    param.setValue(value);
}
```

这类 Adapter 适合处理不同前端输入习惯，例如搜索框里复制多行 ID、分号分隔的编号等。

### 查询后处理结果

`UserAdapter` 实现了 `afterQuery`，把 SQL 查询出的 `tags` 字符串转成数组：

```java
String[] tags = user.getTags().split(",");
user.setTagArray(tags);
```

这类 Adapter 适合做轻量格式转换。如果转换逻辑涉及复杂业务规则，建议放到 Service。

### 改写 count SQL

`CountAdapter` 实现了 `beforeCount`，在分页 count 前尝试移除无用 join，减少 count 查询成本。

这个示例适合说明 dbfound 的分页 count 可以被定制，但生产环境要配合 SQL 复杂度和测试一起使用。

## Java Service + ModelExecutor

`UserService` 展示了三种常见调用方式：

- `withParam`：逐个放入参数。
- `withMapParam`：把请求参数 Map 直接传给 model。
- `withBeanParam`：把 Java Bean 转成 model 参数。

示例：

```java
Context context = new Context()
        .withBeanParam(user)
        .withParam("last_update_by", 1);
ResponseObject responseObject = modelExecutor.execute(context, "user", "update");
```

随后通过 `outParam` 做业务判断：

```java
int updateNum = responseObject.getOutParam("update_num");
if (updateNum == 0) {
    responseObject.setSuccess(false);
    responseObject.setMessage("没有找到相应的记录");
}
```

这种方式适合在 SQL 执行结果之外补充业务语义。

## 文件类接口

`ExcelController` 通过 `@ContextAware Context` 获取上传文件和响应上下文。

导入流程：

1. 从 `Context` 中读取 `param.file`。
2. 使用 `ExcelReader` 把 Excel 转成 `List<Map<String, Object>>`。
3. 把数据放入 `userList`。
4. 调用 `modelExecutor.execute(context, "userExcel", "")` 执行批量落库。

导出流程：

1. 使用当前请求参数查询 `userExcel`。
2. 定义导出列。
3. 通过 `ExcelWriter.excelExport` 写入响应。

文件解析、列定义和响应输出放在 Java 中，批量插入仍然放在 XML 中，这是一个比较清晰的边界。

## 自定义函数

`StartWith` 继承 `DSqlFunction` 并注册函数名：

```java
@PostConstruct
public void init() {
    register("start_with");
}
```

XML 中可以直接在条件表达式里使用：

```xml
<collisionSql where="start_with(${@message},'abc')" message="不能以abc开头" />
```

自定义函数适合封装短小、可复用、无外部副作用的表达式逻辑。不要把复杂业务流程塞进函数中，否则 XML 可读性会快速下降。

## 配置说明

当前 `application.yaml` 中的关键配置：

```yaml
dbfound:
  system:
    model-modify-check: true
  web:
    open-session: false
  datasource:
    db0:
      url: jdbc:mysql://127.0.0.1:3306/dbfound?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: dbfound
      password: dbfound
      dialect: MySqlDialect
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
```

说明：

- `model-modify-check`：开发阶段便于检测 model 文件变化。
- `open-session`：控制 dbfound Web 请求是否打开会话能力，本示例作为纯接口项目关闭。
- `datasource.db0`：默认数据源配置，XML 中未显式指定时使用该数据源。
- `dialect`：数据库方言，本项目使用 MySQL。
- `SNAKE_CASE`：接口 JSON 字段使用下划线风格，例如 `user_id`、`user_name`。

## 常见问题

### 为什么我只建了 user 表，部分接口会报表不存在？

部分示例为了展示 join、多表插入和 count 适配器，会使用 `table2`、`sys_user`、`sys_role`。请优先执行 `examples/sql/init.sql`。

### 什么时候用 `${@param}`，什么时候用 `#{@param}`？

一般值参数优先使用 `${@param}`，让框架按参数处理。`#{@param}` 会直接拼接文本，适合字段名、排序等 SQL 片段，但必须配合白名单，避免 SQL 注入风险。

### 为什么 Controller 里没有写很多 SQL？

本项目的主要 SQL 都在 `model/*.xml`。Controller 只负责 HTTP 入口，Service 负责参数组装、文件处理和业务结果判断。
