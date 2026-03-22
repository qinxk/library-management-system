<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '../../api/http'
import { apiErrorMessage } from '../../api/errors'
import type { PendingReader } from '../../types/admin'

interface ChangePasswordRequest {
  password: string
}

const users = ref<{ id: number; username: string; role: string }[]>([])
const loading = ref(false)
const err = ref('')
const busyId = ref<number | null>(null)
const showPasswordDialog = ref(false)
const selectedUserId = ref(0)
const newPassword = ref('')
const passwordErr = ref('')

async function load() {
  loading.value = true
  err.value = ''
  try {
    const { data } = await http.get<any[]>('/admin/users')
    users.value = data
  } catch (e) {
    err.value = apiErrorMessage(e, '加载用户列表失败')
    users.value = []
  } finally {
    loading.value = false
  }
}

async function changePassword() {
  if (!newPassword.value) {
    passwordErr.value = '密码不能为空'
    return
  }
  if (newPassword.value.length< 6) {
    passwordErr.value = '密码至少需要6个字符'
    return
  }
  
  busyId.value = selectedUserId.value
  passwordErr.value = ''
  
  try {
    await http.post<ChangePasswordRequest>(
      `/admin/users/${selectedUserId.value}/password`,
      { password: newPassword.value }
    )
    showPasswordDialog.value = false
    newPassword.value = ''
    alert('密码修改成功')
  } catch (e) {
    passwordErr.value = apiErrorMessage(e, '密码修改失败')
  } finally {
    busyId.value = null
  }
}

function openPasswordDialog(userId: number) {
  selectedUserId.value = userId
  newPassword.value = ''
  passwordErr.value = ''
  showPasswordDialog.value = true
}

onMounted(load)
</script><template><div class="wrap"><h1>用户管理</h1><p v-if="err" class="err">{{ err }}</p><p v-if="loading">加载中…</p><p v-else-if="!users.length" class="muted">暂无用户。</p><table v-else class="table"><thead><tr><th>ID</th><th>用户名</th><th>角色</th><th></th></tr></thead><tbody><tr v-for="user in users" :key="user.id"><td>{{ user.id }}</td><td>{{ user.username }}</td><td>{{ user.role }}</td><td class="actions"><button
              type="button"
              :disabled="busyId === user.id"
              class="edit"
              @click="openPasswordDialog(user.id)"
            >修改密码</button></td></tr></tbody></table><!-- 密码修改对话框 --><div v-if="showPasswordDialog" class="dialog-overlay"><div class="dialog"><h3>修改密码</h3><div class="form-group"><label>新密码：</label><input
            type="password"
            v-model="newPassword"
            placeholder="请输入新密码（至少6位）"
            :disabled="busyId !== null"
          /></div><p v-if="passwordErr" class="err">{{ passwordErr }}</p><div class="dialog-actions"><button
            type="button"
            class="cancel"
            @click="showPasswordDialog = false"
            :disabled="busyId !== null"
          >取消</button><button
            type="button"
            class="ok"
            @click="changePassword"
            :disabled="busyId !== null"
          >确认</button></div></div></div></div></template><style scoped>.wrap {
  max-width: 60rem;
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
.edit {
  padding: 0.3rem 0.65rem;
  background: #2563eb;
  color: #fff;
  border: none;
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

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog {
  background: #fff;
  padding: 1.5rem;
  border-radius: 8px;
  width: 90%;
  max-width: 30rem;
}
.dialog h3 {
  margin-top: 0;
  margin-bottom: 1rem;
}
.form-group {
  margin-bottom: 1rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 0.92rem;
}
.dialog-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}
.cancel {
  padding: 0.4rem 0.8rem;
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
.ok {
  padding: 0.4rem 0.8rem;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
</style>