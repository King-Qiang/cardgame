import { ref } from 'vue'
import type { PageParams, PageResult } from '../types/table'

export function useTable<T>(fetchFn: (params: PageParams) => Promise<PageResult<T>>) {
  const loading = ref(false)
  const tableData = ref<T[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(20)
  const searchParams = ref<Record<string, unknown>>({})

  async function fetchData() {
    loading.value = true
    try {
      const result = await fetchFn({
        page: page.value,
        pageSize: pageSize.value,
        ...searchParams.value,
      })
      tableData.value = result.list
      total.value = result.total
      page.value = result.page
      pageSize.value = result.pageSize
    } finally {
      loading.value = false
    }
  }

  function handleSearch(params: Record<string, unknown>) {
    searchParams.value = params
    page.value = 1
    fetchData()
  }

  function handleReset() {
    searchParams.value = {}
    page.value = 1
    fetchData()
  }

  function handlePageChange(newPage: number) {
    page.value = newPage
    fetchData()
  }

  function handleSizeChange(newSize: number) {
    pageSize.value = newSize
    page.value = 1
    fetchData()
  }

  return {
    loading,
    tableData,
    total,
    page,
    pageSize,
    searchParams,
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleSizeChange,
  }
}
