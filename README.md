# 图书管理系统（本机演示）

Spring Boot 后端 + Vue 3 前端。设计见 [`docs/plans/2026-03-20-library-management-design.md`](docs/plans/2026-03-20-library-management-design.md)，实现任务见 [`docs/plans/2026-03-20-library-management-system.md`](docs/plans/2026-03-20-library-management-system.md)。

## 开发时运行（两个终端）

1. **后端**（默认端口 `8080`）：

   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   - 数据文件：`backend/data/` 下的 H2 库（`librarydb`），重启后数据保留。
   - H2 控制台：`http://localhost:8080/h2-console`（JDBC URL 填 `jdbc:h2:file:./data/librarydb`，用户 `sa`，密码留空）；Security 已放行该路径。
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

## 单进程运行（Spring 托管 Vue 构建产物）

1. 安装 Node 后，在仓库根目录执行 **`cd frontend && npm install && npm run build`**（输出目录为 `backend/src/main/resources/static`，受 `frontend/vite.config.ts` 中 `build.outDir` 约束）。
2. **`cd backend && .\mvnw.cmd package`** 会在 **`prepare-package`** 阶段再次执行 `npm run build`（需本机 `npm` 在 PATH 中）。若只想打包且已手动构建过前端，可加 **`-Dskip.npm.build=true`**。
3. 运行 **`java -jar backend\target\library-backend-0.0.1-SNAPSHOT.jar`**（文件名以实际为准），浏览器打开 `http://localhost:8080`。Vue Router 的深链接由后端 SPA 回退到 `index.html`。

集成测试使用 `backend/src/test/resources/static/index.html` 占位，不依赖主目录下是否已有构建产物。

## 构建摘要

- 开发：继续用 Vite `5173` + 后端 `8080`。
- 交付 jar：上述 `package` + `java -jar`。

## 环境要求

- JDK 17+
- Node.js 18+（当前使用 Vite 6）

## Git 提交说明（Windows）

若 `git commit -m` 报错，可使用：`git commit -F commitmsg.txt`（文件中写提交说明）。
