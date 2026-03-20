# 图书管理系统 — 设计文档

**日期:** 2026-03-20  
**状态:** 已评审通过  

---

## 1. 背景与目标

在本机 `localhost` 上演示的 **Web 图书管理系统**：管理员维护馆藏与读者审核、办理借阅视图；读者注册后经管理员审核方可借书；**匿名用户可检索馆藏**。

---

## 2. 约束与选型

| 项 | 决策 |
|----|------|
| 部署 | 本机演示为主 |
| 角色 | 管理员 + 读者 |
| 后端 | Java，Spring Boot 3 |
| 前端 | Vue 3 SPA，REST 消费 |
| 数据库 | H2 内嵌，**文件模式**（非纯内存），便于重启后保留数据 |
| 交付形态 | **方案 1**：开发时 Vite 代理 `/api` → Spring；打包时将 Vue `build` 产物放入 Spring 静态资源，**同源**访问，减少 CORS 问题 |

---

## 3. 整体架构

- **前端**：Vue 3、Vue Router、Pinia、Axios；路由分 **读者门户**（注册、登录、检索、我的借阅）与 **管理后台**（图书、待审核读者、全馆借阅）。管理页需 `ADMIN`；借书与「我的借阅」需登录。
- **后端**：Spring Boot 3、Spring Web、Spring Data JPA、Spring Security；API 前缀 `/api`；密码 **BCrypt**。
- **鉴权**：**JWT**（`Authorization: Bearer`）。读者 `PENDING` 可登录，**借书接口返回 403**。
- **匿名**：仅允许 `GET /api/books`、`GET /api/books/{id}` 以及 `POST /api/auth/register`、`POST /api/auth/login`。
- **SPA 回退**：打包后 Spring 对非 `/api/**` 请求回退 `index.html`，支持 Vue History 模式。

---

## 4. 领域模型

### 4.1 User

- `username`（唯一）、`passwordHash`、`role`（`ADMIN` | `READER`）。
- 读者：`readerStatus` — `PENDING` | `APPROVED` | `REJECTED`。
- 首个管理员通过 **数据初始化**（如 `data.sql` 或启动配置）创建。

### 4.2 Book

- `title`、`author`、`isbn`（可选）、`category`（可选）、`description`（可选）。
- **简化复本**：`totalCopies`、`availableCopies`（不做逐册条码子表）。调整 `totalCopies` 时需校验与已借出数量一致。

### 4.3 Loan

- 关联读者 `User` 与 `Book`；`borrowedAt`、`dueAt`、`returnedAt`（未还为空）。
- **借书**：事务内校验读者 `APPROVED`、`availableCopies > 0`、未超每用户最大未还册数、**同一本书对该用户存在未还记录时不可再借**；成功后 `availableCopies--`。
- **还书**：置 `returnedAt`；`availableCopies++`；**幂等**（已还则 200 且不重复加库存）。

### 4.4 业务常量

- 默认借期天数（如 14）、单用户最大未还册数（如 5）：`application.yml` 或常量类。

---

## 5. API 清单（MVP）

| 方法 | 路径 | 匿名 | 说明 |
|------|------|------|------|
| POST | `/api/auth/register` | ✓ | 读者注册 → `PENDING` |
| POST | `/api/auth/login` | ✓ | 返回 JWT |
| GET | `/api/books` | ✓ | 分页 + 关键词检索 |
| GET | `/api/books/{id}` | ✓ | 详情 |
| POST | `/api/loans` | ✗ | 借书；非 `APPROVED` → 403 |
| GET | `/api/me/loans` | ✗ | 当前用户借阅列表 |
| POST | `/api/loans/{id}/return` | ✗ | 读者自助还书（幂等） |
| POST | `/api/admin/books` | Admin | 新建 |
| PUT/PATCH | `/api/admin/books/{id}` | Admin | 更新 |
| DELETE | `/api/admin/books/{id}` | Admin | 有未还关联则禁止删除 |
| GET | `/api/admin/users/pending` | Admin | 待审核列表 |
| POST | `/api/admin/users/{id}/approve` | Admin | 通过 |
| POST | `/api/admin/users/{id}/reject` | Admin | 拒绝 |
| GET | `/api/admin/loans` | Admin | 全馆借阅 |

**注册策略**：同一 `username` 不可重复。`REJECTED` 用户 MVP 不开放自助再注册；由管理员 **删除用户** 后重新注册。

---

## 6. 关键流程与错误处理

### 6.1 流程摘要

1. 读者注册 → `PENDING` → 管理员通过/拒绝。  
2. 登录签发 JWT；前端存 token（如 sessionStorage）。  
3. 匿名浏览书目；登录后借书/还书/我的借阅。  
4. 借书使用事务 + 行级控制或乐观锁处理并发。  
5. 开发：Vite 代理；交付演示：静态资源由 Spring 提供。

### 6.2 HTTP 与错误体

- `401` 未认证；`403` 无权限或读者未审核借书；`404`；`400` 校验失败；`409` 冲突（重复用户名、库存不足等）；`500` 统一处理且不返回堆栈。  
- 统一 JSON 错误体，含可读 `message` 与可选 `code`（如 `READER_NOT_APPROVED`）。

---

## 7. 测试策略（MVP）

- `@SpringBootTest` + H2：注册 → 审核 → 借 → 还主路径。  
- Security：匿名 GET books；PENDING 借书 403；ADMIN/READER 接口隔离。  
- 可选：最后一本并发借书仅一人成功。  
- 前端：手工清单为主；有余力可 Vitest 测 store/工具。

---

## 8. 未纳入 MVP 的扩展

- 管理员代借代还 API。  
- 复本级条码、预约排队、罚金。  
- `REJECTED` 自助重新提交申请。

---

## 9. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-03-20 | 初版定稿（头脑风暴评审通过） |
