<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '../api/http'
import type { PageBook } from '../types/book'

const keyword = ref('')
const page = ref(0)
const size = 10
const loading = ref(false)
const data = ref<PageBook | null>(null)
const err = ref('')

async function load() {
  loading.value = true
  err.value = ''
  try {
    const { data: body } = await http.get<PageBook>('/books', {
      params: {
        page: page.value,
        size,
        keyword: keyword.value.trim() || undefined,
      },
    })
    data.value = body
  } catch {
    err.value = '加载目录失败。'
    data.value = null
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 0
  void load()
}

function goPage(next: number) {
  page.value = next
  void load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h1>馆藏目录</h1>
    <div class="toolbar">
      <input v-model="keyword" type="search" placeholder="书名 / 作者 / ISBN" @keyup.enter="search" />
      <button type="button" :disabled="loading" @click="search">搜索</button>
    </div>
    <p v-if="err" class="err">{{ err }}</p>
    <p v-else-if="loading">加载中…</p>
    <ul v-else-if="data?.content?.length" class="list">
      <li v-for="b in data.content" :key="b.id">
        <router-link :to="`/books/${b.id}`">{{ b.title }}</router-link>
        <span class="meta">{{ b.author }} · 可借 {{ b.availableCopies }} / {{ b.totalCopies }}</span>
      </li>
    </ul>
    <p v-else class="muted">暂无数据。</p>
    <div v-if="data && data.totalPages > 1" class="pager">
      <button type="button" :disabled="page <= 0 || loading" @click="goPage(page - 1)">上一页</button>
      <span>{{ page + 1 }} / {{ data.totalPages }}</span>
      <button
        type="button"
        :disabled="page >= data.totalPages - 1 || loading"
        @click="goPage(page + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 42rem;
  margin: 0 auto;
  padding: 1rem;
}
.toolbar {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
input[type='search'] {
  flex: 1;
  padding: 0.45rem 0.6rem;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
}
.list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.list li {
  padding: 0.65rem 0;
  border-bottom: 1px solid #e2e8f0;
}
.meta {
  display: block;
  font-size: 0.85rem;
  color: #64748b;
  margin-top: 0.2rem;
}
.pager {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1rem;
}
.err {
  color: #b91c1c;
}
.muted {
  color: #64748b;
}
</style>
