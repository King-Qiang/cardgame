<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useTable } from '../../composables/useTable'
import { fetchOperationLogs } from '../../api/system'
import { downloadExport } from '../../api/export'
import { formatDateTime } from '../../utils/format'
import type { SearchField } from '../../types/table'
import type { OperationLogItem } from '../../api/system'

const searchFields: SearchField[] = [
  { prop: 'operatorId', label: '操作人 ID', type: 'input', placeholder: '精确匹配' },
  { prop: 'action', label: '操作类型', type: 'input', placeholder: '如 USER_BAN' },
  {
    prop: 'targetType',
    label: '目标类型',
    type: 'select',
    options: [
      { label: '用户', value: 'USER' },
      { label: '房间', value: 'ROOM' },
      { label: '配置', value: 'CONFIG' },
      { label: '商品', value: 'SHOP_ITEM' },
      { label: '活动', value: 'ACTIVITY' },
      { label: '角色', value: 'ROLE' },
      { label: '管理员', value: 'ADMIN' },
    ],
  },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'operatorName', label: '操作人', minWidth: 100 },
  { prop: 'action', label: '操作', minWidth: 140 },
  { prop: 'targetType', label: '目标类型', width: 100 },
  { prop: 'targetId', label: '目标 ID', minWidth: 100 },
  { prop: 'ip', label: 'IP', minWidth: 120 },
  { prop: 'createdAt', label: '时间', slot: 'createdAt', minWidth: 170 },
]

const {
  loading,
  tableData,
  total,
  page,
  pageSize,
  fetchData,
  handleSearch,
  handleReset,
  handlePageChange,
  handleSizeChange,
  searchParams,
} = useTable<OperationLogItem>(fetchOperationLogs)

const exportLoading = ref(false)

async function handleExport() {
  exportLoading.value = true
  try {
    await downloadExport('/admin/export/operation-logs', searchParams.value, 'operation_logs.csv')
  } finally {
    exportLoading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <el-card shadow="never">
    <SearchForm :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <div style="margin-bottom: 12px">
      <el-button :loading="exportLoading" @click="handleExport">导出 CSV</el-button>
    </div>
    <DataTable
      :columns="columns"
      :data="tableData"
      :total="total"
      :loading="loading"
      :page="page"
      :page-size="pageSize"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
    </DataTable>
  </el-card>
</template>
