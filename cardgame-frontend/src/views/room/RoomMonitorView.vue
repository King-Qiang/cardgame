<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import PermissionButton from '../../components/PermissionButton.vue'
import { useTable } from '../../composables/useTable'
import { fetchRooms, fetchRoomDetail, disbandRoom, kickRoomPlayer } from '../../api/room'
import { formatDateTime } from '../../utils/format'
import { isBotUserId, roomModeLabel } from '../../utils/game-labels'
import { PERMISSIONS } from '../../constants/permissions'
import type { SearchField } from '../../types/table'
import type { RoomDetail, RoomListItem } from '../../api/room'

const searchFields: SearchField[] = [
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '等待中', value: 'WAITING' },
      { label: '进行中', value: 'PLAYING' },
      { label: '已解散', value: 'DISBANDED' },
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
  { prop: 'roomId', label: '房间号', minWidth: 180 },
  { prop: 'gameType', label: '玩法', width: 100 },
  { prop: 'mode', label: '模式', slot: 'mode', width: 100 },
  { prop: 'status', label: '状态', slot: 'status', width: 100 },
  { prop: 'playerCount', label: '人数', width: 80 },
  { prop: 'ownerId', label: '房主 ID', width: 100 },
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
} = useTable<RoomListItem>(fetchRooms)

const detailVisible = ref(false)
const detailLoading = ref(false)
const roomDetail = ref<RoomDetail | null>(null)

let timer: number | undefined

async function openDetail(row: RoomListItem) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    roomDetail.value = await fetchRoomDetail(row.roomId)
  } catch {
    roomDetail.value = null
    ElMessage.error('加载房间详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleDisband(row: RoomListItem) {
  const { value } = await ElMessageBox.prompt('请输入解散原因', '强制解散', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  if (!value) return
  await disbandRoom(row.roomId, value)
  ElMessage.success('房间已解散')
  fetchData()
}

async function handleKick(row: RoomListItem) {
  const { value: userIdStr } = await ElMessageBox.prompt('请输入要踢出的用户 ID', '踢出玩家', {
    confirmButtonText: '下一步',
    cancelButtonText: '取消',
    inputPattern: /^\d+$/,
    inputErrorMessage: '请输入有效的用户 ID',
  })
  if (!userIdStr) return
  const { value: reason } = await ElMessageBox.prompt('请输入踢出原因', '踢出玩家', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  if (reason === null) return
  await kickRoomPlayer(row.roomId, Number(userIdStr), reason || '')
  ElMessage.success('已踢出玩家')
  fetchData()
}

function statusType(status: string) {
  if (status === 'PLAYING') return 'warning'
  if (status === 'DISBANDED') return 'info'
  return 'success'
}

onMounted(() => {
  fetchData()
  timer = window.setInterval(fetchData, 15000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
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
      <template #mode="{ row }">
        <el-tag v-if="row.mode === 'PVE'" type="info">{{ roomModeLabel(row.mode) }}</el-tag>
        <span v-else>{{ roomModeLabel(row.mode) }}</span>
      </template>
      <template #status="{ row }">
        <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
      </template>
      <template #createdAt="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        <PermissionButton
          :permission="PERMISSIONS.ROOM_KICK"
          link
          type="warning"
          :disabled="row.status === 'DISBANDED'"
          @click="handleKick(row)"
        >
          踢人
        </PermissionButton>
        <PermissionButton
          :permission="PERMISSIONS.ROOM_DISBAND"
          link
          type="danger"
          :disabled="row.status === 'DISBANDED'"
          @click="handleDisband(row)"
        >
          解散
        </PermissionButton>
      </template>
    </DataTable>
  </el-card>

  <el-dialog v-model="detailVisible" title="房间详情" width="640px">
    <div v-loading="detailLoading">
      <el-descriptions v-if="roomDetail" :column="2" border>
        <el-descriptions-item label="房间号">{{ roomDetail.roomId }}</el-descriptions-item>
        <el-descriptions-item label="模式">{{ roomModeLabel(roomDetail.mode) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ roomDetail.status }}</el-descriptions-item>
        <el-descriptions-item label="房主 ID">{{ roomDetail.ownerId }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="roomDetail"
        :data="roomDetail.players"
        border
        stripe
        style="margin-top: 12px"
        empty-text="暂无玩家"
      >
        <el-table-column prop="seat" label="座位" width="70">
          <template #default="{ row }">{{ row.seat + 1 }}</template>
        </el-table-column>
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isRobot || isBotUserId(row.userId)" type="info">电脑</el-tag>
            <el-tag v-else type="success">玩家</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="准备" width="80">
          <template #default="{ row }">{{ row.ready ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>
