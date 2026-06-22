import axios from 'axios'
import type { ApiResponse } from '../types/api'
import { clearAuth, getToken } from '../utils/auth'
import { ElMessage } from 'element-plus'

const client = axios.create({
  baseURL: '/api/admin/v1',
  timeout: 15000,
})

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse
    if (body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      if (body.code === 10002) {
        clearAuth()
        window.location.href = '/login'
      }
      return Promise.reject(body)
    }
    return response
  },
  (error) => {
    ElMessage.error('网络错误，请稍后重试')
    return Promise.reject(error)
  }
)

export default client
