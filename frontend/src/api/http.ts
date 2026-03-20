import axios, { type AxiosInstance } from 'axios'

/** Key used with localStorage; Pinia auth store (next task) should read/write the same. */
export const AUTH_TOKEN_STORAGE_KEY = 'library.jwt'

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
  } catch {
    return null
  }
}

export function setStoredToken(token: string | null): void {
  try {
    if (token) {
      localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token)
    } else {
      localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
    }
  } catch {
    /* ignore quota / private mode */
  }
}

/**
 * Axios for backend REST under `/api`.
 * - Dev (`vite`): Vite proxies `/api` → Spring `8080`.
 * - Prod (`java -jar`): same origin, no proxy.
 */
export const http: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
