# 图书管理系统（本机演示）

Spring Boot 后端 + Vue 3 前端。设计见 [`docs/plans/2026-03-20-library-management-design.md`](docs/plans/2026-03-20-library-management-design.md)，实现任务见 [`docs/plans/2026-03-20-library-management-system.md`](docs/plans/2026-03-20-library-management-system.md)。

## 开发时运行（两个终端）

1. **后端**（默认端口 `8080`）：

   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run
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
