<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { http } from '../../api/http'
import { apiErrorMessage } from '../../api/errors'
import type { AdminLoan } from '../../types/admin'

const loans = ref<AdminLoan[]>([])
const loading = ref(false)
const err = ref('')
const filter = ref<'all' | 'active' | 'returned'>('all')

const filtered = computed(() => {
  if (filter.value === 'active') {
    return loans.value.filter((l) => !l.returnedAt)
  }
  if (filter.value === 'returned') {
    return loans.value.filter((l) => !!l.returnedAt)
  }
  return loans.value
})

async function load() {
  loading.value = true
  err.value = ''
  try {
    const { data } = await http.get<AdminLoan[]>('/admin/loans')
    loans.value = data
  } catch (e) {
    err.value = apiErrorMessage(e, '加载借阅列表失败')
    loans.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="wrap">
    <h1>借阅总览</h1>
    <div class="toolbar">
      <label>
        筛选
        <select v-model="filter">
          <option value="all">全部</option>
          <option value="active">在借</option>
          <option value="returned">已还</option>
        </select>
      </label>
      <button type="button" class="ghost" :disabled="loading" @click="load">刷新</button>
    </div>
    <p v-if="err" class="err">{{ err }}</p>
    <p v-if="loading">加载中…</p>
    <p v-else-if="!filtered.length" class="muted">没有记录。</p>
    <div v-else class="scroll">
      <table class="table">
        <thead>
          <tr>
            <th>读者</th>
            <th>图书</th>
            <th>借阅日</th>
            <th>应还</th>
            <th>归还</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in filtered" :key="l.id">
            <td>{{ l.readerUsername }}</td>
            <td>{{ l.bookTitle }} · {{ l.bookAuthor }}</td>
            <td>{{ l.borrowedAt }}</td>
            <td>{{ l.dueAt }}</td>
            <td>{{ l.returnedAt ?? '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 56rem;
}
h1 {
  margin-top: 0;
}
.toolbar {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 0.75rem;
}
.toolbar label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.85rem;
}
select {
  padding: 0.35rem 0.5rem;
  border-radius: 4px;
  border: 1px solid #cbd5e1;
}
.ghost {
  padding: 0.4rem 0.75rem;
  background: #fff;
  border: 1px solid #94a3b8;
  border-radius: 4px;
  cursor: pointer;
  align-self: center;
}
.scroll {
  overflow-x: auto;
}
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}
.table th,
.table td {
  text-align: left;
  padding: 0.45rem 0.35rem;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}
.table td:nth-child(2) {
  white-space: normal;
  max-width: 14rem;
}
.err {
  color: #b91c1c;
}
.muted {
  color: #64748b;
}
</style>
