# 志愿服务管理系统 (Volunteer Management System)

本项目是一个基于前后端分离架构的志愿服务管理系统，旨在提供一个功能完善、易用且高效的志愿服务和活动管理平台，适用于毕业设计、课程设计以及志愿组织的实际应用场景。

## 🛠 技术栈

### 后端 (`Java/` 目录)
- **核心框架:** Java 17 + Spring Boot 3.2.2
- **持久层:** MyBatis-Plus 3.5.5
- **数据库:** MySQL
- **实时通讯:** WebSocket (用于消息通知中心)
- **工具库:** EasyExcel (Excel数据导出), Lombok 
- **基础与安全:** Spring Boot Validation

### 前端 (`Web/volunteer_web/` 目录)
- **核心框架:** Vue 3.5 (使用 Composition API)
- **构建工具:** Vite 7.3
- **路由/状态管理:** Vue Router 5 + Pinia 3
- **UI 组件库:** Vant 4 (移动端友好)
- **数据可视化:** ECharts 6
- **网络请求:** Axios
- **其他周边:** html5-qrcode(扫码打卡等场景), vue-cropper(图片裁剪)

## 🌟 核心功能模块

系统主要围绕以下业务模块展开（详见 `src/api` 与 `src/controller` 对应的分布）：
1. **用户与角色管理 (`user/auth/admin`)**: 志愿者、组织者、管理员多角色区分，支持独立的注册、登录、信息维护。
2. **志愿活动管理 (`activity`)**: 活动发起、审核发布、报名参与、扫码签到/签退审核。
3. **内容管理 (`news/story`)**: 公告发布、志愿服务新闻、志愿者故事与心得分享。
4. **互动与通知 (`comment/message/notification`)**: 活动评价留言、基于 WebSocket 的站内信即时通知。
5. **数据与报表 (`statistics`)**: 平台运营数据汇总看板，借助 ECharts 呈现统计图表。 
6. **审查与风控 (`violation`)**: 用户违规操作的记录与处理机制。

## 🚀 快速开始

### 1. 环境准备
- JDK 17 及以上
- Node.js 18+ (推荐使用 LTS 版本)
- MySQL 8.x
- Maven 3.8+

### 2. 数据库配置
在 MySQL 中建立你的数据库，并导入提供的 SQL 初始化文件（参考 `Java/target/classes/com/volunteer/db.sql` ）。

### 3. 后端服务启动
1. 切换到 `Java` 目录:
   ```bash
   cd Java
   ```
2. 调整数据库连接配置:
   修改 `src/main/resources/application.yml` 内的 MySQL `url`、`username` 和 `password` 等信息。
3. 编译并运行:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   *或者直接在 IDE (如 VS Code / IntelliJ IDEA) 中运行 `VolunteerApplication` 启动类。*

### 4. 前端服务启动
1. 切换到前端目录:
   ```bash
   cd Web/volunteer_web
   ```
2. 安装依赖包:
   ```bash
   npm install
   ```
3. 运行开发服务器:
   ```bash
   npm run dev
   ```
   随后通过终端输出的本地地址访问应用。

## 📚 开发说明文档
在项目根目录中，附带了详细的 API 文档：**[系统接口文档.md](./系统接口文档.md)**。前后端开发联调、接口新增与测试请参考该文件。
