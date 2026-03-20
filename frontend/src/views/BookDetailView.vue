<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '../api/http'
import { apiErrorMessage } from '../api/errors'
import { useAuthStore } from '../stores/auth'
import type { Book } from '../types/book'

const props = defineProps<{ id: string }>()
const router = useRouter()
const auth = useAuthStore()

const book = ref<Book | null>(null)
const loading = ref(true)
const err = ref('')
const actionMsg = ref('')
const borrowing = ref(false)

const bookId = computed(() => Number(props.id))

async function load() {
  loading.value = true
  err.value = ''
  try {
    const { data } = await http.get<Book>(`/books/${bookId.value}`)
    book.value = data
  } catch (e) {
    err.value = apiErrorMessage(e, '图书不存在或加载失败。')
    book.value = null
  } finally {
    loading.value = false
  }
}

async function borrow() {
  actionMsg.value = ''
  borrowing.value = true
  try {
    await http.post('/loans', { bookId: bookId.value })
    actionMsg.value = '借阅成功。'
    await load()
  } catch (e) {
    actionMsg.value = apiErrorMessage(e, '借阅失败')
  } finally {
    borrowing.value = false
  }
}

onMounted(load)
watch(() => props.id, () => {
  void load()
})
</script>

<template>
  <div class="page">
    <p>
      <button type="button" class="linkish" @click="router.push('/books')">← 返回目录</button>
    </p>
    <p v-if="loading">加载中…</p>
    <p v-else-if="err" class="err">{{ err }}</p>
    <article v-else-if="book">
      <h1>{{ book.title }}</h1>
      <p class="meta">{{ book.author }}</p>
      <p v-if="book.isbn"><strong>ISBN</strong> {{ book.isbn }}</p>
      <p v-if="book.category"><strong>分类</strong> {{ book.category }}</p>
      <p v-if="book.description" class="desc">{{ book.description }}</p>
      <p><strong>馆藏</strong> 可借 {{ book.availableCopies }} / 共 {{ book.totalCopies }}</p>

      <div v-if="auth.isReader" class="actions">
        <p v-if="auth.readerStatus === 'PENDING'" class="banner">您的账号待管理员审核，暂不可借书。</p>
        <p v-else-if="auth.readerStatus === 'REJECTED'" class="banner warn">注册未通过审核，无法借书。</p>
        <template v-else-if="auth.isApprovedReader">
          <button type="button" :disabled="borrowing || book.availableCopies <= 0" @click="borrow">
            {{ borrowing ? '处理中…' : '借阅' }}
          </button>
          <p v-if="actionMsg" class="action-msg">{{ actionMsg }}</p>
        </template>
      </div>
      <p v-else-if="!auth.isAuthenticated" class="muted">
        <router-link to="/login">登录</router-link>
        读者账号后可借阅（需已审核）。
      </p>
    </article>
  </div>
</template>

<style scoped>
.page {
  max-width: 40rem;
  margin: 0 auto;
  padding: 1rem;
}
.linkish {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  padding: 0;
  font: inherit;
}
.meta {
  color: #64748b;
}
.desc {
  white-space: pre-wrap;
}
.banner {
  background: #fef3c7;
  padding: 0.65rem 0.8rem;
  border-radius: 6px;
  font-size: 0.9rem;
}
.banner.warn {
  background: #fee2e2;
}
.actions {
  margin-top: 1.25rem;
}
.actions button {
  padding: 0.5rem 1rem;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.actions button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.action-msg {
  margin-top: 0.5rem;
  font-size: 0.9rem;
}
.err {
  color: #b91c1c;
}
.muted {
  color: #64748b;
}
</style>
