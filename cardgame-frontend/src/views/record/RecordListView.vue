<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useTable } from '../../composables/useTable'
import { fetchRecords } from '../../api/record'
import { downloadExport } from '../../api/export'
import { formatDateTime } from '../../utils/format'
import { roomModeLabel } from '../../utils/game-labels'
import type { SearchField } from '../../types/table'
import type { RecordListItem } from '../../api/record'

const searchFields: SearchField[] = [
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '进行中', value: 'PLAYING' },
      { label: '已结束', value: 'FINISHED' },
      { label: '已中止', value: 'ABORTED' },
    ],
  },
  {
    prop: 'gameType',
    label: '玩法',
    type: 'select',
    options: [{ label: '斗地主', value: 'DOUDIZHU' }],
  },
  {
    prop: 'mode',
    label: '模式',
    type: 'select',
    options: [
      { label: '亲友', value: 'FRIEND' },
      { label: '匹配', value: 'MATCH' },
      { label: '排位', value: 'RANKED' },
      { label: '练习(PVE)', value: 'PVE' },
    ],
  },
]

const columns = [
  { prop: 'recordId', label: '对局 ID', minWidth: 180 },
  { prop: 'roomId', label: '房间号', minWidth: 180 },
  { prop: 'gameType', label: '玩法', width: 100 },
  { prop: 'mode', label: '模式', slot: 'mode', width: 100 },
  { prop: 'status', label: '状态', slot: 'status', width: 100 },
  { prop: 'startAt', label: '开始时间', slot: 'startAt', minWidth: 170 },
  { prop: 'endAt', label: '结束时间', slot: 'endAt', minWidth: 170 },
]

const router = useRouter()

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
} = useTable<RecordListItem>(fetchRecords)

const exportLoading = ref(false)

function statusType(status: string) {
  if (status === 'PLAYING') return 'warning'
  if (status === 'FINISHED') return 'success'
  return 'info'
}

function goDetail(row: RecordListItem) {
  router.push(`/records/${row.recordId}`)
}

async function handleExport() {
  exportLoading.value = true
  try {
    await downloadExport('/admin/export/records', searchParams.value, 'game_records.csv')
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
      <template #mode="{ row }">
        <el-tag v-if="row.mode === 'PVE'" type="info">{{ roomModeLabel(row.mode) }}</el-tag>
        <span v-else>{{ roomModeLabel(row.mode) }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
      </template>
      <template #startAt="{ row }">{{ formatDateTime(row.startAt) }}</template>
      <template #endAt="{ row }">{{ formatDateTime(row.endAt) }}</template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="goDetail(row)">详情</el-button>
      </template>
    </DataTable>
  </el-card>
</template>
