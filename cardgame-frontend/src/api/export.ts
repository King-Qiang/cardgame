import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '../utils/auth'

export async function downloadExport(
  path: string,
  params: Record<string, unknown>,
  filename: string,
) {
  try {
    const res = await axios.get(`/api/admin/v1${path}`, {
      params: { ...params, format: 'csv' },
      responseType: 'blob',
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
    })
    const blob = res.data as Blob
    if (blob.type.includes('json')) {
      const text = await blob.text()
      const body = JSON.parse(text) as { message?: string }
      ElMessage.error(body.message || '导出失败')
      return
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('导出失败，请缩小筛选范围后重试')
  }
}
