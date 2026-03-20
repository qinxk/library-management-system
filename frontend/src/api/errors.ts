import axios from 'axios'

interface ProblemBody {
  detail?: string
  title?: string
  code?: string
  fieldErrors?: Array<{ field: string; message: string }>
}

export function apiErrorMessage(err: unknown, fallback = '请求失败'): string {
  if (!axios.isAxiosError(err)) {
    return fallback
  }
  const data = err.response?.data
  if (typeof data === 'string' && data.trim()) {
    return data.trim()
  }
  if (data && typeof data === 'object') {
    const p = data as ProblemBody
    if (typeof p.detail === 'string' && p.detail) {
      return p.code ? `${p.detail}（${p.code}）` : p.detail
    }
    if (Array.isArray(p.fieldErrors) && p.fieldErrors.length > 0) {
      return p.fieldErrors.map((f) => `${f.field}: ${f.message}`).join('；')
    }
  }
  if (err.response?.status === 401) {
    return '未登录或账号密码错误'
  }
  return fallback
}
