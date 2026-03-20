<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { http } from '../../api/http'
import { apiErrorMessage } from '../../api/errors'
import type { Book, PageBook } from '../../types/book'

const books = ref<Book[]>([])
const loading = ref(false)
const err = ref('')
const saving = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  title: '',
  author: '',
  isbn: '',
  category: '',
  description: '',
  totalCopies: 1,
  availableCopies: 1,
})

function resetForm() {
  editingId.value = null
  form.title = ''
  form.author = ''
  form.isbn = ''
  form.category = ''
  form.description = ''
  form.totalCopies = 1
  form.availableCopies = 1
}

async function load() {
  loading.value = true
  err.value = ''
  try {
    const { data } = await http.get<PageBook>('/books', { params: { page: 0, size: 200 } })
    books.value = data.content
  } catch (e) {
    err.value = apiErrorMessage(e, '无法加载图书列表')
    books.value = []
  } finally {
    loading.value = false
  }
}

function startEdit(b: Book) {
  editingId.value = b.id
  form.title = b.title
  form.author = b.author
  form.isbn = b.isbn ?? ''
  form.category = b.category ?? ''
  form.description = b.description ?? ''
  form.totalCopies = b.totalCopies
  form.availableCopies = b.availableCopies
}

async function save() {
  if (form.availableCopies > form.totalCopies) {
    err.value = '可借册数不能大于总册数'
    return
  }
  saving.value = true
  err.value = ''
  const body = {
    title: form.title.trim(),
    author: form.author.trim(),
    isbn: form.isbn.trim() || null,
    category: form.category.trim() || null,
    description: form.description.trim() || null,
    totalCopies: form.totalCopies,
    availableCopies: form.availableCopies,
  }
  try {
    if (editingId.value == null) {
      await http.post('/admin/books', body)
    } else {
      await http.put(`/admin/books/${editingId.value}`, body)
    }
    resetForm()
    await load()
  } catch (e) {
    err.value = apiErrorMessage(e, '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(b: Book) {
  if (!confirm(`确定删除《${b.title}》？若有未归还借阅将失败。`)) return
  err.value = ''
  try {
    await http.delete(`/admin/books/${b.id}`)
    await load()
  } catch (e) {
    err.value = apiErrorMessage(e, '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="wrap">
    <h1>图书维护</h1>
    <p v-if="err" class="err">{{ err }}</p>
    <section class="panel">
      <h2>{{ editingId == null ? '新增图书' : '编辑 #' + editingId }}</h2>
      <form class="grid" @submit.prevent="save">
        <label>书名 <input v-model="form.title" required /></label>
        <label>作者 <input v-model="form.author" required /></label>
        <label>ISBN <input v-model="form.isbn" /></label>
        <label>分类 <input v-model="form.category" /></label>
        <label class="full">简介 <textarea v-model="form.description" rows="2" /></label>
        <label>总册数 <input v-model.number="form.totalCopies" type="number" min="0" required /></label>
        <label>可借册数 <input v-model.number="form.availableCopies" type="number" min="0" required /></label>
        <div class="actions full">
          <button type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
          <button v-if="editingId != null" type="button" class="ghost" @click="resetForm">取消编辑</button>
        </div>
      </form>
    </section>
    <section class="panel">
      <h2>书目列表</h2>
      <p v-if="loading">加载中…</p>
      <table v-else class="table">
        <thead>
          <tr>
            <th>书名</th>
            <th>作者</th>
            <th>可借/总数</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in books" :key="b.id">
            <td>{{ b.title }}</td>
            <td>{{ b.author }}</td>
            <td>{{ b.availableCopies }} / {{ b.totalCopies }}</td>
            <td class="row-actions">
              <button type="button" class="linkish" @click="startEdit(b)">编辑</button>
              <button type="button" class="linkish danger" @click="remove(b)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
.wrap { max-width: 52rem; }
h1 { margin-top: 0; }
.panel {
  margin-bottom: 1.75rem;
  padding: 1rem 1.1rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}
.grid .full { grid-column: 1 / -1; }
label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.88rem;
}
input, textarea {
  padding: 0.4rem 0.5rem;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
}
.actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}
.actions button[type='submit'] {
  padding: 0.45rem 1rem;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.ghost {
  padding: 0.45rem 0.75rem;
  background: #fff;
  border: 1px solid #94a3b8;
  border-radius: 4px;
  cursor: pointer;
}
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
.table th, .table td {
  text-align: left;
  padding: 0.5rem 0.4rem;
  border-bottom: 1px solid #e2e8f0;
}
.row-actions { white-space: nowrap; }
.linkish {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  font: inherit;
  margin-right: 0.5rem;
}
.linkish.danger { color: #b91c1c; }
.err { color: #b91c1c; }
</style>
