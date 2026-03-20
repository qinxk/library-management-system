<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '../../api/http'
import { apiErrorMessage } from '../../api/errors'
import type { PendingReader } from '../../types/admin'

const list = ref<PendingReader[]>([])
const loading = ref(false)
const err = ref('')
const busyId = ref<number | null>(null)

async function load() {
  loading.value = true
  err.value = ''
  try {
    const { data } = await http.get<PendingReader[]>('/admin/users/pending')
    list.value = data
  } catch (e) {
    err.value = apiErrorMessage(e, '加载待审核读者失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

async function approve(id: number) {
  busyId.value = id
  err.value = ''
  try {
    await http.post(`/admin/users/${id}/approve`)
    await load()
  } catch (e) {
    err.value = apiErrorMessage(e, '操作失败')
  } finally {
    busyId.value = null
  }
}

async function reject(id: number) {
  if (!confirm('确认拒绝该读者？')) return
  busyId.value = id
  err.value = ''
  try {
    await http.post(`/admin/users/${id}/reject`)
    await load()
  } catch (e) {
    err.value = apiErrorMessage(e, '操作失败')
  } finally {
    busyId.value = null
  }
}

onMounted(load)
</script>

<template>
  <div class="wrap">
    <h1>待审核读者</h1>
    <p v-if="err" class="err">{{ err }}</p>
    <p v-if="loading">加载中…</p>
    <p v-else-if="!list.length" class="muted">暂无待审核读者。</p>
    <table v-else class="table">
      <thead>
        <tr>
          <th>用户名</th>
          <th>状态</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id">
          <td>{{ r.username }}</td>
          <td>{{ r.readerStatus }}</td>
          <td class="actions">
            <button
              type="button"
              :disabled="busyId === r.id"
              class="ok"
              @click="approve(r.id)"
            >
              通过
            </button>
            <button
              type="button"
              :disabled="busyId === r.id"
              class="no"
              @click="reject(r.id)"
            >
              拒绝
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 40rem;
}
h1 {
  margin-top: 0;
}
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.92rem;
}
.table th,
.table td {
  text-align: left;
  padding: 0.55rem 0.35rem;
  border-bottom: 1px solid #e2e8f0;
}
.actions {
  display: flex;
  gap: 0.4rem;
}
.ok {
  padding: 0.3rem 0.65rem;
  background: #059669;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
.no {
  padding: 0.3rem 0.65rem;
  background: #fff;
  color: #b91c1c;
  border: 1px solid #fca5a5;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.err {
  color: #b91c1c;
}
.muted {
  color: #64748b;
}
</style>
