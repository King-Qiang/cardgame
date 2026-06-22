<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useTable } from '../../composables/useTable'
import { fetchTransactions } from '../../api/economy'
import { downloadExport } from '../../api/export'
import { formatDateTime, formatGold } from '../../utils/format'
import type { SearchField } from '../../types/table'
import type { WalletTransactionItem } from '../../api/economy'

const TX_TYPES = [
  'GAME_WIN',
  'GAME_LOSE',
  'RECHARGE',
  'ADMIN_ADJUST',
  'SHOP_BUY',
  'DAILY_REWARD',
  'ROOM_FEE',
]

const searchFields: SearchField[] = [
  { prop: 'userId', label: '用户 ID', type: 'input', placeholder: '精确匹配' },
  {
    prop: 'type',
    label: '类型',
    type: 'select',
    options: TX_TYPES.map((t) => ({ label: t, value: t })),
  },
]

const columns = [
  { prop: 'id', label: '流水 ID', width: 90 },
  { prop: 'userId', label: '用户 ID', width: 90 },
  { prop: 'type', label: '类型', minWidth: 120 },
  { prop: 'amount', label: '变动', slot: 'amount', minWidth: 120 },
  { prop: 'balanceAfter', label: '余额', slot: 'balanceAfter', minWidth: 120 },
  { prop: 'remark', label: '备注', minWidth: 140 },
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
} = useTable<WalletTransactionItem>(fetchTransactions)

const exportLoading = ref(false)

async function handleExport() {
  exportLoading.value = true
  try {
    await downloadExport('/admin/export/wallet/transactions', searchParams.value, 'wallet_transactions.csv')
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
      <template #amount="{ row }">
        <span :style="{ color: row.amount >= 0 ? '#67c23a' : '#f56c6c' }">
          {{ row.amount >= 0 ? '+' : '' }}{{ formatGold(row.amount) }}
        </span>
      </template>
      <template #balanceAfter="{ row }">{{ formatGold(row.balanceAfter) }}</template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
    </DataTable>
  </el-card>
</template>
