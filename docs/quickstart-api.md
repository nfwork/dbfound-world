# 接口快速体验

本文档提供一组可直接复制的 `curl` 示例，帮助你从 HTTP 层理解 dbfound 自动接口和 Java Controller 调用方式。

默认服务地址为 `http://localhost:8080`。如果端口不同，请替换下面命令中的 `BASE_URL`。

```bash
export BASE_URL=http://localhost:8080
```

## 启动前准备

1. 准备数据库：

```bash
mysql -uroot -p < examples/sql/init.sql
```

2. 修改 `src/main/resources/application.yaml` 中的数据库账号和密码。

3. 启动应用：

```bash
mvn spring-boot:run
```

如果本机没有 Maven，也可以先安装 Maven，或在项目后续补充 Maven Wrapper 后使用 `./mvnw spring-boot:run`。

## 自动接口

dbfound 会根据 `src/main/resources/model` 下的 XML 自动暴露接口。常见路径格式如下：

- `POST /{modelName}.query`：调用默认查询。
- `POST /{modelName}.query!{queryName}`：调用指定名称的查询。
- `POST /{modelName}.execute`：调用默认执行逻辑。
- `POST /{modelName}.execute!{executeName}`：调用指定名称的执行逻辑。

### 查询用户

对应 `src/main/resources/model/user.xml` 的默认 `<query>`。

```bash
curl -s -X POST "$BASE_URL/user.query" \
  -H "Content-Type: application/json" \
  -d '{
    "user_name": "小",
    "page_start": 0,
    "page_limit": 10
  }'
```

### 新增用户

对应 `user.xml` 中的 `<execute name="add">`。

```bash
curl -s -X POST "$BASE_URL/user.execute!add" \
  -H "Content-Type: application/json" \
  -d '{
    "user_code": "demo001",
    "user_name": "演示用户",
    "password": "123456"
  }'
```

如果 `user_code` 已存在，`collisionSql` 会返回业务失败信息。

### 修改用户

对应 `user.xml` 中的 `<execute name="update">`。

```bash
curl -s -X POST "$BASE_URL/user.execute!update" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": 1,
    "user_name": "小明-已修改"
  }'
```

### 保存用户

对应 `user.xml` 中的 `<execute name="save">`。没有 `user_id` 时走新增，有 `user_id` 时走修改。

```bash
curl -s -X POST "$BASE_URL/user.execute!save" \
  -H "Content-Type: application/json" \
  -d '{
    "user_code": "demo002",
    "user_name": "保存示例用户",
    "password": "123456"
  }'
```

### 批量删除

对应 `user.xml` 中的 `<execute name="delete">`。

```bash
curl -s -X POST "$BASE_URL/user.execute!delete" \
  -H "Content-Type: application/json" \
  -d '{
    "userList": [
      { "user_id": 2 },
      { "user_id": 3 }
    ]
  }'
```

### 按 ID 集合查询

对应 `user.xml` 中的 `<query name="getByIds">`。

```bash
curl -s -X POST "$BASE_URL/user.query!getByIds" \
  -H "Content-Type: application/json" \
  -d '{
    "ids": "1,2,3"
  }'
```

### 批量保存

对应 `userBatch.xml` 中的 `<execute name="batchAdd">`。

```bash
curl -s -X POST "$BASE_URL/userBatch.execute!batchAdd" \
  -H "Content-Type: application/json" \
  -d '{
    "userList": [
      {
        "user_code": "batch001",
        "user_name": "批量用户一",
        "password": "123456"
      },
      {
        "user_code": "batch002",
        "user_name": "批量用户二",
        "password": "123456"
      }
    ]
  }'
```

### 动态字段和动态排序

对应 `userPart.xml` 的默认 `<query>`。`fields` 和 `sort` 使用 `#{}` 拼接，真实业务中要注意白名单控制。

```bash
curl -s -X POST "$BASE_URL/userPart.query" \
  -H "Content-Type: application/json" \
  -d '{
    "fields": "user_code,user_name",
    "sort": "user_id desc",
    "user_name": "小"
  }'
```

### 动态字段白名单

对应 `userPart.xml` 中的 `<query name="sec2">`，通过条件分支输出允许的字段。

```bash
curl -s -X POST "$BASE_URL/userPart.query!sec2" \
  -H "Content-Type: application/json" \
  -d '{
    "fields": "user_code,user_name"
  }'
```

### 适配器示例

对应 `userAdapter.xml` 默认查询。`CollectionParamAdapter` 会把空格、逗号、分号等分隔符整理成集合参数，`UserAdapter` 会把 `tags` 字符串转成 `tag_array`。

```bash
curl -s -X POST "$BASE_URL/userAdapter.query" \
  -H "Content-Type: application/json" \
  -d '{
    "user_code": "xiaoming; xiaohong"
  }'
```

### Count SQL 适配器

对应 `userAdapter.xml` 中的 `<query name="count">`，依赖 `sys_user` 和 `sys_role` 两张表。

```bash
curl -s -X POST "$BASE_URL/userAdapter.query!count" \
  -H "Content-Type: application/json" \
  -d '{
    "role_code": "admin",
    "page_start": 0,
    "page_limit": 10
  }'
```

### 自定义函数校验

对应 `function.xml` 中的 `<execute name="hello">`。`start_with` 来自 `com.dbfound.world.dfunction.StartWith`。

```bash
curl -s -X POST "$BASE_URL/function.execute!hello" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "hello world"
  }'
```

## Java Controller 接口

项目也演示了在 Java 代码中创建 `Context` 并调用 `ModelExecutor`。

### Controller 查询

对应 `UserController.query` 和 `UserService.query`。

```bash
curl -s "$BASE_URL/user/query?user_name=小&page_start=0&page_limit=10"
```

### Controller Map 参数查询

对应 `UserController.search` 和 `UserService.search`。

```bash
curl -s "$BASE_URL/user/search?user_code=xiaoming"
```

### Controller 实体更新

对应 `UserController.updateUser` 和 `UserService.updateUser`。

```bash
curl -s -X POST "$BASE_URL/user/update" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": 1,
    "user_name": "Controller 更新"
  }'
```

### Excel 导出

对应 `ExcelController.exportUser` 和 `ExcelService.exportData`。

```bash
curl -L -o users.xlsx "$BASE_URL/user/export?user_name=小"
```

### Excel 导入

对应 `ExcelController.importUser` 和 `ExcelService.importData`。上传文件的表单字段名为 `file`。

```bash
curl -s -X POST "$BASE_URL/user/import" \
  -F "file=@users.xlsx"
```

## 响应结构

dbfound 返回 `ResponseObject` 或 `QueryResponseObject` 的 JSON 序列化结果。字段会受到 `spring.jackson.property-naming-strategy: SNAKE_CASE` 影响，Java 字段会变成下划线风格。

常见响应包含：

- `success`：业务是否成功。
- `message`：错误或提示信息。
- `datas`：查询数据列表。
- `total_count`：分页查询总数。
- `out_param`：执行逻辑中的输出参数，例如 `update_num`、`delete_num`。

实际字段以 dbfound 当前版本序列化结果为准。
