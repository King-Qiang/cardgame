<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import PermissionButton from '../../components/PermissionButton.vue'
import { useTable } from '../../composables/useTable'
import { fetchAdjustRequests, approveAdjustRequest, rejectAdjustRequest } from '../../api/economy'
import { formatDateTime, formatGold } from '../../utils/format'
import { PERMISSIONS } from '../../constants/permissions'
import type { SearchField } from '../../types/table'
import type { AdjustRequestItem } from '../../api/economy'

const searchFields: SearchField[] = [
  { prop: 'userId', label: '用户 ID', type: 'input', placeholder: '精确匹配' },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '待审批', value: 'PENDING' },
      { label: '已执行', value: 'EXECUTED' },
      { label: '已驳回', value: 'REJECTED' },
    ],
  },
]

const columns = [
  { prop: 'id', label: '申请 ID', width: 90 },
  { prop: 'userId', label: '用户 ID', width: 90 },
  { prop: 'adjustType', label: '类型', slot: 'adjustType', width: 90 },
  { prop: 'amount', label: '金额', slot: 'amount', minWidth: 120 },
  { prop: 'reason', label: '原因', minWidth: 140 },
  { prop: 'status', label: '状态', slot: 'status', width: 100 },
  { prop: 'createdAt', label: '申请时间', slot: 'createdAt', minWidth: 170 },
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
} = useTable<AdjustRequestItem>(fetchAdjustRequests)

function statusType(status: string) {
  if (status === 'PENDING') return 'warning'
  if (status === 'EXECUTED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审批',
    EXECUTED: '已执行',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

async function handleApprove(row: AdjustRequestItem) {
  await ElMessageBox.confirm(`确认通过用户 ${row.userId} 的调账申请？`, '审批通过')
  await approveAdjustRequest(row.id)
  ElMessage.success('已通过并执行调账')
  fetchData()
}

async function handleReject(row: AdjustRequestItem) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回调账', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  if (!value) return
  await rejectAdjustRequest(row.id, value)
  ElMessage.success('已驳回')
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <el-card shadow="never">
    <SearchForm :fields="searchFields" @search="handleSearch" @reset="handleReset" />
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
      <template #adjustType="{ row }">
        {{ row.adjustType === 'INCREASE' ? '增加' : '扣减' }}
      </template>
      <template #amount="{ row }">{{ formatGold(row.amount) }}</template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
      </template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      <template #actions="{ row }">
        <template v-if="row.status === 'PENDING'">
          <PermissionButton
            :permission="PERMISSIONS.ECONOMY_ADJUST"
            link
            type="success"
            @click="handleApprove(row)"
          >
            通过
          </PermissionButton>
          <PermissionButton
            :permission="PERMISSIONS.ECONOMY_ADJUST"
            link
            type="danger"
            @click="handleReject(row)"
          >
            驳回
          </PermissionButton>
        </template>
        <span v-else class="muted">—</span>
      </template>
    </DataTable>
  </el-card>
</template>

<style scoped>
.muted {
  color: #909399;
}
</style>
