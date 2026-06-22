<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import PermissionButton from '../../components/PermissionButton.vue'
import AdjustWalletDialog from '../../components/AdjustWalletDialog.vue'
import UserRankDialog from '../../components/rank/UserRankDialog.vue'
import { useTable } from '../../composables/useTable'
import { fetchUsers, banUser, unbanUser } from '../../api/user'
import { formatDateTime, formatGold } from '../../utils/format'
import { tierLabel, tierTagType } from '../../utils/rankTier'
import { PERMISSIONS } from '../../constants/permissions'
import type { SearchField } from '../../types/table'
import type { UserListItem } from '../../api/user'

const route = useRoute()
const adjustDialogVisible = ref(false)
const adjustTarget = ref<UserListItem | null>(null)
const rankDialogVisible = ref(false)
const rankTarget = ref<UserListItem | null>(null)

const searchFields: SearchField[] = [
  { prop: 'userId', label: '用户 ID', type: 'input', placeholder: '精确匹配' },
  { prop: 'nickname', label: '昵称', type: 'input' },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '正常', value: 'NORMAL' },
      { label: '封禁', value: 'BANNED' },
    ],
  },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'nickname', label: '昵称', minWidth: 120 },
  { prop: 'rankTier', label: '段位', slot: 'rankTier', width: 100 },
  { prop: 'rankPoints', label: '积分', width: 80 },
  { prop: 'openidMasked', label: 'OpenID', minWidth: 140 },
  { prop: 'gold', label: '金币', slot: 'gold', minWidth: 120 },
  { prop: 'statusLabel', label: '状态', slot: 'status', width: 100 },
  { prop: 'createdAt', label: '注册时间', slot: 'createdAt', minWidth: 170 },
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
} = useTable<UserListItem>(fetchUsers)

async function handleBan(row: UserListItem) {
  const { value } = await ElMessageBox.prompt('请输入封禁原因', '封禁用户', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  if (!value) return
  await banUser(row.id, value)
  ElMessage.success('封禁成功')
  fetchData()
}

async function handleUnban(row: UserListItem) {
  await ElMessageBox.confirm(`确认解封用户 ${row.nickname}？`, '解封用户')
  await unbanUser(row.id)
  ElMessage.success('解封成功')
  fetchData()
}

function openAdjust(row: UserListItem) {
  adjustTarget.value = row
  adjustDialogVisible.value = true
}

function openRank(row: UserListItem) {
  rankTarget.value = row
  rankDialogVisible.value = true
}

onMounted(() => {
  const userId = route.query.userId
  if (userId) {
    handleSearch({ userId: String(userId), nickname: '', status: '' })
  } else {
    fetchData()
  }
})
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
      <template #rankTier="{ row }">
        <el-tag v-if="row.rankTier" :type="tierTagType(row.rankTier)">{{ tierLabel(row.rankTier) }}</el-tag>
        <span v-else>-</span>
      </template>
      <template #gold="{ row }">{{ formatGold(row.gold || 0) }}</template>
      <template #status="{ row }">
        <el-tag :type="row.statusLabel === '封禁' ? 'danger' : 'success'">{{ row.statusLabel }}</el-tag>
      </template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openRank(row)">段位</el-button>
        <PermissionButton
          :permission="PERMISSIONS.USER_ADJUST"
          link
          type="primary"
          @click="openAdjust(row)"
        >
          调账
        </PermissionButton>
        <PermissionButton
          v-if="row.statusLabel === '正常'"
          :permission="PERMISSIONS.USER_BAN"
          link
          type="danger"
          @click="handleBan(row)"
        >
          封禁
        </PermissionButton>
        <PermissionButton
          v-else
          :permission="PERMISSIONS.USER_BAN"
          link
          type="success"
          @click="handleUnban(row)"
        >
          解封
        </PermissionButton>
      </template>
    </DataTable>
    <AdjustWalletDialog
      v-if="adjustTarget"
      v-model:visible="adjustDialogVisible"
      :user-id="adjustTarget.id"
      :nickname="adjustTarget.nickname"
      @success="fetchData"
    />
    <UserRankDialog
      v-if="rankTarget"
      v-model:visible="rankDialogVisible"
      :user-id="rankTarget.id"
      :nickname="rankTarget.nickname"
    />
  </el-card>
</template>
