<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '../api/http'
import { apiErrorMessage } from '../api/errors'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const submitting = ref(false)

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    const { data } = await http.post<{ token: string }>('/auth/register', {
      username: username.value,
      password: password.value,
    })
    auth.setSession(data.token)
    await auth.fetchMe()
    await router.replace('/books')
  } catch (e) {
    error.value = apiErrorMessage(e, '注册失败：用户名可能已被占用，或不符合规则。')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="card narrow">
    <h1>读者注册</h1>
    <p class="hint">注册后为「待审核」状态，通过管理员审核后方可借书。</p>
    <form class="stack" @submit.prevent="submit">
      <label>
        用户名
        <input v-model="username" type="text" autocomplete="username" required minlength="3" />
      </label>
      <label>
        密码
        <input v-model="password" type="password" autocomplete="new-password" required minlength="6" />
      </label>
      <p v-if="error" class="err">{{ error }}</p>
      <button type="submit" :disabled="submitting">{{ submitting ? '提交中…' : '注册并登录' }}</button>
    </form>
    <p class="muted">
      已有账号？
      <router-link to="/login">登录</router-link>
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
.hint {
  font-size: 0.88rem;
  color: #475569;
  margin: 0 0 0.5rem;
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
  background: #059669;
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
