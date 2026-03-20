<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  void router.push('/books')
}
</script>

<template>
  <header class="top">
    <RouterLink to="/books" class="brand">图书管理</RouterLink>
    <nav>
      <RouterLink to="/books">目录</RouterLink>
      <RouterLink v-if="auth.isReader" to="/me/loans">我的借阅</RouterLink>
      <RouterLink v-if="auth.isAdmin" to="/admin">管理后台</RouterLink>
      <template v-if="!auth.isAuthenticated">
        <RouterLink to="/login">登录</RouterLink>
        <RouterLink to="/register">注册</RouterLink>
      </template>
      <template v-else>
        <span class="who">{{ auth.user?.username }}</span>
        <button type="button" class="link" @click="logout">退出</button>
      </template>
    </nav>
  </header>
  <RouterView />
</template>

<style scoped>
.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.65rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}
.brand {
  font-weight: 700;
  color: #0f172a;
  text-decoration: none;
}
nav {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  font-size: 0.92rem;
}
nav a {
  color: #2563eb;
  text-decoration: none;
}
nav a.router-link-active {
  font-weight: 600;
}
.who {
  color: #64748b;
  font-size: 0.88rem;
}
.link {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  font: inherit;
  padding: 0;
}
</style>
