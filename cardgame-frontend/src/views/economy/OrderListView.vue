<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useTable } from '../../composables/useTable'
import { fetchOrders } from '../../api/order'
import { downloadExport } from '../../api/export'
import { formatDateTime, formatGold } from '../../utils/format'
import type { SearchField } from '../../types/table'
import type { RechargeOrderItem } from '../../api/order'

const searchFields: SearchField[] = [
  { prop: 'userId', label: '用户 ID', type: 'input', placeholder: '精确匹配' },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '待支付', value: 'PENDING' },
      { label: '已支付', value: 'PAID' },
      { label: '失败', value: 'FAILED' },
      { label: '已退款', value: 'REFUNDED' },
    ],
  },
]

const columns = [
  { prop: 'orderNo', label: '订单号', minWidth: 180 },
  { prop: 'userId', label: '用户 ID', width: 90 },
  { prop: 'amount', label: '支付金额(分)', width: 120 },
  { prop: 'goldAmount', label: '金币', slot: 'goldAmount', minWidth: 120 },
  { prop: 'payChannel', label: '渠道', width: 100 },
  { prop: 'status', label: '状态', slot: 'status', width: 100 },
  { prop: 'paidAt', label: '支付时间', slot: 'paidAt', minWidth: 170 },
  { prop: 'createdAt', label: '创建时间', slot: 'createdAt', minWidth: 170 },
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
} = useTable<RechargeOrderItem>(fetchOrders)

const exportLoading = ref(false)

function statusType(status: string) {
  if (status === 'PAID') return 'success'
  if (status === 'PENDING') return 'warning'
  if (status === 'FAILED') return 'danger'
  return 'info'
}

async function handleExport() {
  exportLoading.value = true
  try {
    await downloadExport('/admin/export/orders', searchParams.value, 'recharge_orders.csv')
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
      <template #goldAmount="{ row }">{{ formatGold(row.goldAmount) }}</template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
      </template>
      <template #paidAt="{ row }">{{ row.paidAt ? formatDateTime(row.paidAt) : '-' }}</template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
    </DataTable>
  </el-card>
</template>
