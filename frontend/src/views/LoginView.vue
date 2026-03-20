<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const submitting = ref(false)

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    const { data } = await http.post<{ token: string }>('/auth/login', {
      username: username.value,
      password: password.value,
    })
    auth.setSession(data.token)
    await auth.fetchMe()
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (redirect) {
      await router.replace(redirect)
    } else if (auth.isAdmin) {
      await router.replace('/admin')
    } else {
      await router.replace('/books')
    }
  } catch {
    error.value = '登录失败，请检查用户名和密码。'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="card narrow">
    <h1>登录</h1>
    <form class="stack" @submit.prevent="submit">
      <label>
        用户名
        <input v-model="username" type="text" autocomplete="username" required />
      </label>
      <label>
        密码
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>
      <p v-if="error" class="err">{{ error }}</p>
      <button type="submit" :disabled="submitting">{{ submitting ? '登录中…' : '登录' }}</button>
    </form>
    <p class="muted">
      还没有账号？
      <router-link to="/register">注册读者</router-link>
    </p>
  </div>
</template>

<style scoped>
.card {
  max-width: 22rem;
  margin: 2rem auto;
  padding: 1.5rem;
  border-radius: 8px;
  background: var(--card-bg, #fff);
  box-shadow: 0 4px 20px rgb(0 0 0 / 8%);
}
.stack {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.9rem;
}
input {
  padding: 0.5rem 0.65rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}
button {
  padding: 0.6rem;
  border: none;
  border-radius: 4px;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}
button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
.err {
  color: #b91c1c;
  font-size: 0.9rem;
  margin: 0;
}
.muted {
  margin-top: 1.25rem;
  font-size: 0.9rem;
  color: #64748b;
}
</style>
