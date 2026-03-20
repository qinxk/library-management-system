<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  void router.push('/books')
}
</script>

<template>
  <div class="admin">
    <aside class="side">
      <h2>管理</h2>
      <nav>
        <RouterLink to="/admin/books">图书</RouterLink>
        <RouterLink to="/admin/readers">待审核读者</RouterLink>
        <RouterLink to="/admin/loans">借阅总览</RouterLink>
      </nav>
      <p class="foot">
        <button type="button" class="ghost" @click="logout">退出</button>
      </p>
    </aside>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.admin {
  display: flex;
  min-height: calc(100vh - 4rem);
}
.side {
  width: 11rem;
  padding: 1rem;
  border-right: 1px solid #e2e8f0;
  background: #f8fafc;
}
.side h2 {
  margin: 0 0 0.75rem;
  font-size: 1rem;
}
.side nav {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.side a {
  color: #1e40af;
  text-decoration: none;
  font-size: 0.92rem;
}
.side a.router-link-active {
  font-weight: 700;
}
.foot {
  margin-top: 1.5rem;
}
.ghost {
  background: none;
  border: 1px solid #94a3b8;
  padding: 0.35rem 0.6rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
.main {
  flex: 1;
  padding: 1rem 1.25rem;
}
</style>
