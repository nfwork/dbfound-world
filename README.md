# dbfound-world

`dbfound-world` 是一个基于 Spring Boot + dbfound 的前后端分离纯接口示例项目，用来演示如何通过 `dbfound` 快速定义查询、保存、批量处理、动态 SQL、适配器、Excel 导入导出等后台接口能力。

本仓库侧重展示一个可运行的后端接口工程结构。项目内已补充数据库初始化脚本、接口快速体验、model XML 入门导读和扩展点说明；更完整的框架细节可继续参考 dbfound wiki。

- 案例文档：[dbfound example](https://github.com/nfwork/dbfound/wiki/dbfound-example)
- dbfound 仓库：[nfwork/dbfound](https://github.com/nfwork/dbfound)

## 入门路径

如果你是第一次接触本项目，建议按下面顺序阅读和运行：

1. 执行 `examples/sql/init.sql` 初始化完整示例数据库。
2. 阅读本 README 的快速启动并启动 Spring Boot 应用。
3. 按 `docs/quickstart-api.md` 复制 `curl` 请求体验接口。
4. 阅读 `docs/model-walkthrough.md` 理解 `model/*.xml` 的写法。
5. 阅读 `docs/extensibility.md` 了解 Adapter、Java Service、自定义函数和配置项。
6. 可选执行 `bash scripts/smoke.sh` 做最小接口冒烟验证。

## 项目特点

- 纯后端接口项目，不依赖前端页面。
- 基于 `dbfound-spring-boot-starter`，接口主要由 `resources/model/*.xml` 声明。
- 同时包含 dbfound 自动接口和 Java Service 手动调用 `ModelExecutor` 两种使用方式。
- 已配置跨域，适合作为前端分离项目的接口服务。
- 内置常见场景示例：查询、增删改、批量处理、动态 SQL、集合参数、适配器、Excel 导入导出。

## 技术栈

- Java 8
- Spring Boot 2.7.15
- dbfound 3.6.8
- Maven
- MySQL

## 目录结构

```text
.
├── pom.xml
├── docs/                              # 入门教程、接口样例、扩展点说明
├── examples/sql/init.sql              # 完整示例数据库初始化脚本
├── scripts/smoke.sh                   # 最小接口冒烟验证脚本
├── src/main/java/com/dbfound/world
│   ├── DBFoundApp.java                 # 应用启动类
│   ├── adapter/                        # dbfound 查询/执行适配器示例
│   ├── config/                         # 跨域、JSON 序列化配置
│   ├── controlller/                    # 自定义 REST 接口示例
│   ├── dfunction/                      # 自定义函数示例
│   ├── entity/                         # 示例实体
│   └── service/                        # Java API 调用 dbfound 示例
└── src/main/resources
    ├── application.yaml                # dbfound 与数据源配置
    └── model/                          # dbfound model XML
```

## 快速启动

### 1. 准备数据库

创建 MySQL 数据库，并准备示例表。推荐直接执行项目内的完整初始化脚本：

```bash
mysql -uroot -p < examples/sql/init.sql
```

脚本会创建 `dbfound` 数据库，并初始化 `user`、`table2`、`sys_user`、`sys_role` 以及最小示例数据。

如果只想了解最核心表结构，`user` 表如下：

```sql
CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_code` varchar(100) NOT NULL,
  `user_name` varchar(200) NOT NULL,
  `password` varchar(50) NOT NULL,
  `create_date` datetime NOT NULL,
  `create_by` int(11) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY (`user_code`)
);
```

部分示例会用到额外测试表，建议以 `examples/sql/init.sql` 为准。

### 2. 修改连接配置

在 `src/main/resources/application.yaml` 中修改数据库连接：

```yaml
dbfound:
  datasource:
    db0:
      url: jdbc:mysql://127.0.0.1:3306/dbfound?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: dbfound
      password: dbfound
      dialect: MySqlDialect
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

启动成功后，默认访问地址为：

```text
http://localhost:8080
```

### 4. 体验接口

可以先阅读并复制 `docs/quickstart-api.md` 中的请求示例，也可以直接执行最小冒烟脚本：

```bash
bash scripts/smoke.sh
```

如果服务地址不是 `http://localhost:8080`，可通过环境变量指定：

```bash
BASE_URL=http://localhost:9000 bash scripts/smoke.sh
```

## 示例入口

dbfound 会根据 `resources/model` 下的 XML 自动暴露接口。例如：

- `POST /user.query`：调用 `model/user.xml` 中默认查询。
- `POST /user.execute!add`：调用 `model/user.xml` 中 `add` 执行逻辑。
- `POST /userBatch.execute!batchAdd`：调用 `model/userBatch.xml` 中批量保存逻辑。
- `POST /userPart.query`：调用 `model/userPart.xml` 中动态 SQL 查询。
- `POST /userAdapter.query`：调用 `model/userAdapter.xml` 中带适配器的查询。

项目中也提供了 Java Controller + Service 方式调用 dbfound 的接口：

- `GET /user/query`：通过 `UserService` 调用 `ModelExecutor.query(...)`。
- `GET /user/search`：通过 Map 参数调用查询。
- `POST /user/update`：通过实体参数调用执行逻辑，并根据影响行数处理业务结果。
- `POST /user/import`：Excel 导入示例。
- `GET /user/export`：Excel 导出示例。

完整请求参数、响应格式和 XML 片段请查看：[dbfound example](https://github.com/nfwork/dbfound/wiki/dbfound-example)。

本项目内的请求示例请查看：`docs/quickstart-api.md`。

## model 文件说明

- `user.xml`：基础用户查询、添加、修改、保存、删除、集合参数、返回简单类型列表等示例。
- `userBatch.xml`：批量新增、批量查询、批量保存、多表插入、条件分支等示例。
- `userPart.xml`：`sqlPart` 动态 SQL、动态字段、动态排序、批量插入等示例。
- `userAdapter.xml`：查询适配器、集合参数适配、实体返回等示例。
- `userExcel.xml`：Excel 导入导出配套 model。
- `function.xml`：内置函数和自定义函数校验示例。

逐段说明请查看：`docs/model-walkthrough.md`。

## 前后端分离说明

本项目返回标准 JSON 响应，且已开启跨域配置：

```java
registry.addMapping("/**")
        .allowCredentials(true)
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowedHeaders("*")
        .exposedHeaders("*");
```

前端可以直接通过 `fetch`、`axios` 或其他 HTTP 客户端调用上述接口。项目中 `spring.jackson.property-naming-strategy` 配置为 `SNAKE_CASE`，接口字段默认使用下划线风格，例如 `user_id`、`user_name`。

## 开发建议

- 新增业务接口时，优先在 `resources/model` 中创建或扩展 XML model。
- 如果需要复杂参数组装、结果二次处理或业务判断，可在 Java Service 中使用 `ModelExecutor` 调用 model。
- 如果只是对查询结果做格式转换，优先使用 dbfound adapter，避免在 Controller 中堆业务逻辑。
- Adapter、Java Service、自定义函数和配置说明请查看 `docs/extensibility.md`。
- README 只维护项目运行和结构导航，框架完整细节仍建议结合 wiki 阅读。
