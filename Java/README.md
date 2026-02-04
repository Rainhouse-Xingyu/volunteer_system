# 志愿者管理系统 - Spring Boot 3 基础代码

## 项目结构

```
Java/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── volunteer/
│   │   │           ├── VolunteerSystemApplication.java  # 启动类
│   │   │           ├── common/                          # 通用类
│   │   │           │   └── Result.java                  # 通用返回对象
│   │   │           ├── config/                          # 配置类
│   │   │           │   ├── JacksonConfig.java          # Jackson 配置
│   │   │           │   ├── MybatisPlusConfig.java      # MyBatis Plus 配置
│   │   │           │   └── RedisConfig.java            # Redis 配置
│   │   │           └── exception/                       # 异常处理
│   │   │               ├── GlobalExceptionHandler.java  # 全局异常处理器
│   │   │               └── ServiceException.java        # 自定义异常
│   │   └── resources/
│   │       └── application.yml                          # 应用配置
│   └── test/
└── pom.xml                                              # Maven 依赖配置
```

## 已实现功能

### 1. 通用返回对象 (Result<T>)
- ✅ 包含 `code`、`message`、`data` 字段
- ✅ 提供静态方法：`success()`、`error()`
- ✅ 支持泛型，灵活返回各种数据类型

### 2. 业务异常处理
- ✅ `ServiceException`：自定义业务异常类
- ✅ `GlobalExceptionHandler`：全局异常处理器
  - 处理业务异常
  - 处理参数校验异常
  - 处理空指针异常
  - 处理其他未知异常

### 3. MyBatis Plus 配置
- ✅ 配置分页插件 `MybatisPlusInterceptor`
- ✅ 设置数据库类型为 MySQL
- ✅ 配置最大单页限制 500 条
- ✅ 支持驼峰命名转换
- ✅ 配置逻辑删除

### 4. Redis 配置
- ✅ 配置 `RedisTemplate`
- ✅ 使用 JSON 序列化 Value
- ✅ 使用 String 序列化 Key
- ✅ 解决 Redis 存储乱码问题

### 5. Jackson JSON 配置
- ✅ Long 类型转 String，防止前端精度丢失
- ✅ BigInteger 类型转 String
- ✅ 配置日期时间格式化
- ✅ 忽略未知属性

## 技术栈

- **Spring Boot**: 3.2.2
- **JDK**: 17
- **MyBatis Plus**: 3.5.5
- **Redis**: Spring Data Redis
- **Jackson**: JSON 序列化
- **Lombok**: 简化代码
- **MySQL**: 数据库

## 快速开始

### 1. 配置数据库
修改 `application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer_db
    username: root
    password: your_password
```

### 2. 配置 Redis
修改 `application.yml` 中的 Redis 连接信息：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
```

### 3. 运行项目
```bash
mvn spring-boot:run
```

访问地址：http://localhost:8080/api

## 使用示例

### Controller 示例
```java
@RestController
@RequestMapping("/user")
public class UserController {
    
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
    
    @PostMapping
    public Result<Void> addUser(@RequestBody @Valid UserDTO userDTO) {
        userService.add(userDTO);
        return Result.success();
    }
}
```

### 异常抛出示例
```java
if (user == null) {
    throw new ServiceException("用户不存在");
}
```

## 注意事项

1. 前端接收 Long 类型 ID 时会自动转为 String，防止精度丢失
2. Redis 存储时会自动序列化为 JSON 格式
3. 全局异常会被统一拦截并返回标准格式
4. MyBatis Plus 已配置逻辑删除，实体类添加 `@TableLogic` 注解即可

## 下一步开发建议

1. 创建实体类 (Entity)
2. 创建数据访问层 (Mapper)
3. 创建服务层 (Service)
4. 创建控制层 (Controller)
5. 添加安全认证 (Spring Security / JWT)
6. 添加接口文档 (Swagger / Knife4j)
