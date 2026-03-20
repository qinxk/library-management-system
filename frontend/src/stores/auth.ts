import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { http, getStoredToken, setStoredToken } from '../api/http'
import type { MeUser, ReaderStatus } from '../types/me'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getStoredToken())
  const user = ref<MeUser | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isReader = computed(() => user.value?.role === 'READER')
  const readerStatus = computed(() => user.value?.readerStatus ?? null)
  const isApprovedReader = computed(
    () => isReader.value && readerStatus.value === 'APPROVED',
  )

  async function fetchMe(): Promise<void> {
    if (!token.value) {
      user.value = null
      return
    }
    loading.value = true
    try {
      const { data } = await http.get<MeUser>('/me')
      user.value = {
        username: data.username,
        role: data.role,
        readerStatus: (data.readerStatus as ReaderStatus | null) ?? null,
      }
    } finally {
      loading.value = false
    }
  }

  function setSession(jwt: string) {
    setStoredToken(jwt)
    token.value = jwt
  }

  function logout() {
    setStoredToken(null)
    token.value = null
    user.value = null
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    isAdmin,
    isReader,
    readerStatus,
    isApprovedReader,
    fetchMe,
    setSession,
    logout,
  }
})
