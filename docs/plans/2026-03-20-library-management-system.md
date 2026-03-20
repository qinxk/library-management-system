# Library Management System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Deliver a localhost-demo library web app: Spring Boot + JPA + H2 + JWT, Vue 3 SPA, anonymous catalog browse, reader registration with admin approval before borrowing, admin book CRUD and loan oversight.

**Architecture:** Monolithic Spring Boot serves REST under `/api` and (in packaged mode) Vue static assets with SPA fallback. Development uses Vite dev server with proxy to Spring. Auth is JWT; roles ADMIN vs READER with readerStatus for approval.

**Tech Stack:** Java 17+ / Spring Boot 3, Spring Security, Spring Data JPA, H2 (file), jjwt or spring-security-oauth2-resource-server style JWT; Vue 3, Vue Router, Pinia, Axios, Vite; optional Element Plus for UI speed.

**Design reference:** `docs/plans/2026-03-20-library-management-design.md`

**Project root (this repo):** `library-management-system/` with subfolders `backend/` and `frontend/`.

---

### Task 1: Repository scaffold + design commit

**Files:**
- Create: `backend/` (Spring Boot project via start.spring.io or manual `pom.xml`)
- Create: `frontend/` (`npm create vite@latest` Vue + TS)
- Create: `README.md` (how to run dev: two terminals, proxy; how to build combined jar)

**Step 1:** Create `backend` Spring Boot 3 project with dependencies: Web, Security, Data JPA, Validation, H2, Lombok (optional).

**Step 2:** Create `frontend` with Vite + Vue 3 + TypeScript.

**Step 3:** Add root `README.md` with ports (8080 backend, 5173 frontend) and `vite.config` proxy stub (next task fills detail).

**Step 4:** Commit

```bash
git add .
git commit -m "chore: scaffold backend and frontend"
```

---

### Task 2: Backend configuration (H2 file + JPA + CORS for dev)

**Files:**
- Create/modify: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/.../config/CorsConfig.java` (or WebMvcConfigurer) allowing `http://localhost:5173` for dev only via profile `dev`

**Step 1:** Set `spring.datasource.url` to `jdbc:h2:file:./data/librarydb` (relative to working directory) and `spring.jpa.hibernate.ddl-auto=update`, `spring.h2.console.enabled=true` with `spring.h2.console.settings.web-allow-others=false`.

**Step 2:** Add `application-dev.yml` with CORS if not using Vite proxy exclusively (proxy preferred; CORS as backup).

**Step 3:** Run `./mvnw spring-boot:run` — expect app starts, no entities yet OK.

**Step 4:** Commit `feat(backend): configure h2 file and dev profile`

---

### Task 3: Domain entities + repositories (TDD-style: repositories exist)

**Files:**
- Create: `backend/src/main/java/.../domain/User.java`
- Create: `backend/src/main/java/.../domain/Book.java`
- Create: `backend/src/main/java/.../domain/Loan.java`
- Create: `backend/src/main/java/.../domain/Role.java` (enum) / `ReaderStatus.java` (enum)
- Create: `backend/src/main/java/.../repository/UserRepository.java` etc.

**Step 1:** Define enums `Role { ADMIN, READER }`, `ReaderStatus { PENDING, APPROVED, REJECTED }`.

**Step 2:** Implement `User` with `id`, `username` unique, `passwordHash`, `role`, `readerStatus` (nullable for admin).

**Step 3:** Implement `Book` with `totalCopies`, `availableCopies`, `@Version` optional for optimistic locking.

**Step 4:** Implement `Loan` with `ManyToOne` user, book, timestamps, `returnedAt`.

**Step 5:** Add Spring Data repositories.

**Step 6:** Integration test: `@DataJpaTest` save user/book/loan — **write test first**, run expect pass after entities wired.

```bash
cd backend && ./mvnw test -Dtest=UserRepositoryTest
```

**Step 7:** Commit `feat(backend): add jpa entities and repositories`

---

### Task 4: Security foundation — password encoder + UserDetailsService

**Files:**
- Create: `backend/src/main/java/.../security/CustomUserDetailsService.java`
- Create: `backend/src/main/java/.../config/SecurityBeansConfig.java` (`PasswordEncoder` bean)

**Step 1:** Write failing test: load user by username returns roles (use `@SpringBootTest` + `@Transactional` + test user inserted).

**Step 2:** Implement `UserDetailsService` mapping `Role` to `GrantedAuthority` (`ROLE_ADMIN`, `ROLE_READER`).

**Step 3:** Run test — PASS.

**Step 4:** Commit `feat(backend): user details service and bcrypt bean`

---

### Task 5: JWT filter + token service

**Files:**
- Create: `backend/src/main/java/.../security/JwtService.java` (generate/parse, secret from `app.jwt.secret` in yml)
- Create: `backend/src/main/java/.../security/JwtAuthFilter.java`
- Modify: `SecurityFilterChain` (next task may split)

**Step 1:** Add dependency (e.g. `io.jsonwebtoken:jjwt-*` 0.12.x) in `pom.xml`.

**Step 2:** Unit test `JwtService`: issue token, parse `username` and roles — write test first.

**Step 3:** Implement HMAC-SHA256 with configurable secret and expiration (e.g. 24h for demo).

**Step 4:** Commit `feat(backend): jwt service`

---

### Task 6: SecurityFilterChain — public routes + JWT for the rest

**Files:**
- Create: `backend/src/main/java/.../config/SecurityConfig.java`

**Step 1:** Permit all: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/books`, `GET /api/books/**` (careful: only list+detail, not admin).

**Step 2:** Authenticate all other `/api/**` with JWT filter before `UsernamePasswordAuthenticationFilter`.

**Step 3:** `@EnableMethodSecurity` for `@PreAuthorize` on admin controllers.

**Step 4:** Integration test with `MockMvc`: GET `/api/books` without token → 200 empty or []; GET `/api/me/loans` without token → 401.

```bash
./mvnw test -Dtest=SecurityIntegrationTest
```

**Step 5:** Commit `feat(backend): security filter chain and jwt filter`

---

### Task 7: Auth controller — register + login

**Files:**
- Create: `backend/src/main/java/.../dto/RegisterRequest.java`, `LoginRequest.java`, `AuthResponse.java`
- Create: `backend/src/main/java/.../controller/AuthController.java`
- Create: `backend/src/main/java/.../service/AuthService.java`

**Step 1:** `RegisterRequest` validation: username not blank, password min length.

**Step 2:** Test: `POST /api/auth/register` creates READER PENDING; duplicate username → 409.

**Step 3:** Test: `POST /api/auth/login` returns JWT for valid credentials; wrong password → 401.

**Step 4:** Implement services using `PasswordEncoder` and `JwtService`.

**Step 5:** Commit `feat(backend): auth register and login`

---

### Task 8: Data initializer — seed admin user

**Files:**
- Create: `backend/src/main/java/.../config/DataInitializer.java` implementing `ApplicationRunner` **or** `import.sql` guarded by profile

**Step 1:** On startup, if no user with `ROLE_ADMIN`, create `admin` / password from `app.seed.admin-password` (default only for demo, document in README).

**Step 2:** Test: context loads and admin exists (optional `@SpringBootTest`).

**Step 3:** Commit `feat(backend): seed admin user`

---

### Task 9: Public Book API (read)

**Files:**
- Create: `BookController` (public GETs under `/api/books`)
- Create: `BookService`, `BookDto`, pagination via `Pageable`

**Step 1:** Test: anonymous GET `/api/books?page=0&size=10` → 200.

**Step 2:** Test: keyword query param filters title/author.

**Step 3:** Implement list + getById; 404 for missing id.

**Step 4:** Commit `feat(backend): public book read api`

---

### Task 10: Admin Book API (write)

**Files:**
- Create: `AdminBookController` under `/api/admin/books`

**Step 1:** Test: POST as ADMIN creates book with totalCopies == availableCopies.

**Step 2:** Test: READER token → 403 on POST.

**Step 3:** Test: DELETE blocked if active loans exist.

**Step 4:** Implement CRUD with validation (non-negative copies, available <= total).

**Step 5:** Commit `feat(backend): admin book crud`

---

### Task 11: Admin user approval API

**Files:**
- Create: `AdminUserController` `/api/admin/users/...`

**Step 1:** Test: GET pending returns only PENDING readers.

**Step 2:** Test: approve sets APPROVED; reject sets REJECTED.

**Step 3:** Commit `feat(backend): admin reader approval`

---

### Task 12: Loan service — borrow with transaction

**Files:**
- Create: `LoanController` `/api/loans`, `/api/me/loans`, `/api/loans/{id}/return`
- Create: `LoanService` with `@Transactional`

**Step 1:** Test: APPROVED reader borrows → loan created, availableCopies decremented.

**Step 2:** Test: PENDING reader POST `/api/loans` → 403 with code/message.

**Step 3:** Test: duplicate borrow same book while unreturned → 409.

**Step 4:** Test: exceeds max active loans → 409.

**Step 5:** Implement pessimistic lock on `Book` or `@Version` retry — document choice in code comment (minimal: `synchronized` service method insufficient for cluster; OK for demo single instance).

**Step 6:** Commit `feat(backend): loan borrow rules`

---

### Task 13: Return loan (idempotent) + admin loan list

**Files:**
- Extend: `LoanService`, `AdminLoanController` `GET /api/admin/loans`

**Step 1:** Test: return sets returnedAt and increments availableCopies once.

**Step 2:** Test: second return same id → 200, copies unchanged.

**Step 3:** Test: wrong user cannot return another's loan → 403.

**Step 4:** Commit `feat(backend): loan return and admin list`

---

### Task 14: Global exception handler

**Files:**
- Create: `backend/src/main/java/.../web/GlobalExceptionHandler.java` with `@RestControllerAdvice`

**Step 1:** Map `MethodArgumentNotValidException` → 400 + field errors.

**Step 2:** Map custom `ConflictException` → 409.

**Step 3:** Commit `feat(backend): global api error handling`

---

### Task 15: SPA static resources + fallback (packaged build)

**Files:**
- Modify: `SecurityConfig` permit static and `/` 
- Create: `backend/src/main/java/.../config/SpaWebConfig.java` forwarding non-api to `index.html`
- Modify: `frontend/vite.config.ts` `base: '/'` and `build.outDir` pointing to `backend/src/main/resources/static` **or** document copy step in Maven `frontend-maven-plugin`

**Step 1:** Add Maven plugin or npm script: `npm run build` copies to `backend/src/main/resources/static`.

**Step 2:** Manual test: `java -jar backend.jar` open `/` shows Vue app; refresh deep link works.

**Step 3:** Commit `feat: integrate vue build into spring static`

---

### Task 16: Frontend — Vite proxy + API client

**Files:**
- Modify: `frontend/vite.config.ts` proxy `/api` → `http://localhost:8080`
- Create: `frontend/src/api/http.ts` Axios instance with baseURL `/api`, interceptor attaches Bearer from Pinia/local store

**Step 1:** Run backend + `npm run dev`; fetch `/api/books` from browser network tab → 200.

**Step 2:** Commit `feat(frontend): axios and dev proxy`

---

### Task 17: Frontend — Pinia auth store + router guards

**Files:**
- Create: `frontend/src/stores/auth.ts`
- Modify: `frontend/src/router/index.ts`

**Step 1:** Routes: `/login`, `/register`, `/books`, `/books/:id`, `/me/loans` (auth), `/admin/*` (admin).

**Step 2:** Guard: admin routes require decoded role or lightweight `/api/me` endpoint (optional Task 17b: add `GET /api/me` returning user info — recommended).

**Step 3:** Commit `feat(frontend): router and auth store`

---

### Task 18: Optional but recommended — GET `/api/me`

**Files:**
- Create: `MeController.java` returns username, role, readerStatus

**Step 1:** Test: with JWT returns correct payload.

**Step 2:** Frontend uses this after login instead of parsing JWT in browser.

**Step 3:** Commit `feat(backend): current user endpoint`

---

### Task 19: Frontend pages — public catalog + detail

**Files:**
- Create: `frontend/src/views/BooksView.vue`, `BookDetailView.vue`

**Step 1:** List with search box + pagination calling GET `/api/books`.

**Step 2:** Detail page GET `/api/books/:id`.

**Step 3:** Commit `feat(frontend): catalog pages`

---

### Task 20: Frontend — register + login forms

**Files:**
- Create: `RegisterView.vue`, `LoginView.vue`

**Step 1:** Wire to POST register/login; store token; redirect by role.

**Step 2:** Show error messages from API error body.

**Step 3:** Commit `feat(frontend): auth pages`

---

### Task 21: Frontend — reader loans + borrow button

**Files:**
- Create: `MyLoansView.vue`, borrow action from detail page

**Step 1:** If user PENDING, show banner; borrow button disabled or hidden based on `/api/me`.

**Step 2:** POST borrow; list `/api/me/loans`; return button POST return.

**Step 3:** Commit `feat(frontend): reader loan flows`

---

### Task 22: Frontend — admin layout

**Files:**
- Create: `admin/BookManageView.vue`, `admin/PendingReadersView.vue`, `admin/AllLoansView.vue`

**Step 1:** CRUD books (simple forms/table).

**Step 2:** Approve/reject pending users.

**Step 3:** Table of all loans with filters optional.

**Step 4:** Commit `feat(frontend): admin pages`

---

### Task 23: Polish + README runbook

**Files:**
- Modify: `README.md`

**Step 1:** Document default admin credentials (dev only warning), H2 console URL, main API table.

**Step 2:** Final `mvn test` + `npm run build` verification.

**Step 3:** Commit `docs: runbook and demo credentials`

---

## Execution handoff

Plan complete and saved to `docs/plans/2026-03-20-library-management-system.md`. Two execution options:

**1. Subagent-Driven (this session)** — dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Parallel Session (separate)** — open a new session with **superpowers:executing-plans**, batch execution with checkpoints.

Which approach do you want?
