<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '../api/http'
import { apiErrorMessage } from '../api/errors'
import { useAuthStore } from '../stores/auth'
import type { Loan } from '../types/loan'

const auth = useAuthStore()
const loans = ref<Loan[]>([])
const loading = ref(true)
const err = ref('')

async function load() {
  loading.value = true
  err.value = ''
  try {
    const { data } = await http.get<Loan[]>('/me/loans')
    loans.value = data
  } catch (e) {
    err.value = apiErrorMessage(e, '无法加载借阅记录。')
    loans.value = []
  } finally {
    loading.value = false
  }
}

async function returnBook(loanId: number) {
  try {
    await http.post(`/loans/${loanId}/return`)
    await load()
  } catch (e) {
    err.value = apiErrorMessage(e, '还书失败。')
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h1>我的借阅</h1>
    <p v-if="auth.readerStatus === 'PENDING'" class="banner">待审核：通过后即可借书。</p>
    <p v-if="loading">加载中…</p>
    <p v-else-if="err" class="err">{{ err }}</p>
    <ul v-else-if="loans.length" class="list">
      <li v-for="l in loans" :key="l.id">
        <strong>{{ l.bookTitle }}</strong>
        <span class="meta">{{ l.bookAuthor }}</span>
        <div class="dates">
          借于 {{ l.borrowedAt }} · 应还 {{ l.dueAt }}
          <template v-if="l.returnedAt"> · 已还 {{ l.returnedAt }}</template>
        </div>
        <button
          v-if="!l.returnedAt && auth.isApprovedReader"
          type="button"
          class="return-btn"
          @click="returnBook(l.id)"
        >
          还书
        </button>
      </li>
    </ul>
    <p v-else class="muted">暂无借阅记录。</p>
  </div>
</template>

<style scoped>
.page {
  max-width: 40rem;
  margin: 0 auto;
  padding: 1rem;
}
.banner {
  background: #fef3c7;
  padding: 0.65rem 0.8rem;
  border-radius: 6px;
  font-size: 0.9rem;
}
.list {
  list-style: none;
  padding: 0;
  margin: 1rem 0 0;
}
.list li {
  padding: 0.85rem 0;
  border-bottom: 1px solid #e2e8f0;
}
.meta {
  display: block;
  font-size: 0.88rem;
  color: #64748b;
}
.dates {
  font-size: 0.85rem;
  color: #64748b;
  margin-top: 0.35rem;
}
.return-btn {
  margin-top: 0.5rem;
  padding: 0.35rem 0.75rem;
  font-size: 0.85rem;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.err {
  color: #b91c1c;
}
.muted {
  color: #64748b;
}
</style>
