# 图书管理系统（本机演示）

Spring Boot 后端 + Vue 3 前端。设计见 [`docs/plans/2026-03-20-library-management-design.md`](docs/plans/2026-03-20-library-management-design.md)，实现任务见 [`docs/plans/2026-03-20-library-management-system.md`](docs/plans/2026-03-20-library-management-system.md)。

## 开发时运行（两个终端）

1. **后端**（默认端口 `8080`）：

   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   - 数据文件：`backend/data/` 下的 H2 库（`librarydb`），重启后数据保留。
   - H2 控制台：`http://localhost:8080/h2-console`（JDBC URL 填 `jdbc:h2:file:./data/librarydb`，用户 `sa`，密码留空）。默认 Spring Security 会拦截，需使用控制台生成的临时密码登录管理端；后续任务会改为应用自有登录与放行规则。
   - 若前端**不经过 Vite 代理**、直接从 `http://localhost:5173` 调 `http://localhost:8080/api`，可启用开发 CORS：

     ```bash
     .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
     ```

2. **前端**（默认端口 `5173`，`/api` 已代理到 `8080`）：

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

浏览器打开 `http://localhost:5173`。接口路径统一为 `/api/...`。

## 构建（后续任务）

- 前端：`cd frontend && npm run build`，产物将按计划复制到 Spring `static` 目录或由 Maven 插件集成。
- 后端：`cd backend && .\mvnw.cmd package`，得到可运行 jar。

## 环境要求

- JDK 17+
- Node.js 18+（当前使用 Vite 6）

## Git 提交说明（Windows）

若 `git commit -m` 报错，可使用：`git commit -F commitmsg.txt`（文件中写提交说明）。
