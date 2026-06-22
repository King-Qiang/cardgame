<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import ReplayPlayer from '../../components/replay/ReplayPlayer.vue'
import { fetchRecordDetail } from '../../api/record'
import { formatDateTime } from '../../utils/format'
import { isBotUserId, roomModeLabel } from '../../utils/game-labels'
import type { RecordDetail } from '../../api/record'

interface ParticipantRow {
  userId: number
  seat: number | null
  goldDelta: number
  isRobot: boolean
}

const route = useRoute()
const loading = ref(false)
const detail = ref<RecordDetail | null>(null)
const showRaw = ref<string[]>([])

const columns = [
  { prop: 'seq', label: '序号', width: 80 },
  { prop: 'userId', label: '用户 ID', slot: 'userId', width: 120 },
  { prop: 'action', label: '操作', minWidth: 120 },
  { prop: 'payload', label: '载荷', slot: 'payload', minWidth: 240 },
  { prop: 'createdAt', label: '时间', slot: 'createdAt', minWidth: 170 },
]

const recordMode = computed(() => {
  if (!detail.value?.resultJson) return '—'
  const mode = detail.value.resultJson.mode
  return roomModeLabel(typeof mode === 'string' ? mode : '')
})

const participants = computed<ParticipantRow[]>(() => {
  const raw = detail.value?.resultJson?.participants
  if (!Array.isArray(raw)) return []
  return raw.map((item) => {
    const row = item as Record<string, unknown>
    return {
      userId: Number(row.userId ?? 0),
      seat: row.seat == null ? null : Number(row.seat),
      goldDelta: Number(row.goldDelta ?? 0),
      isRobot: Boolean(row.isRobot) || isBotUserId(Number(row.userId ?? 0)),
    }
  })
})

async function loadDetail() {
  const recordId = route.params.recordId as string
  if (!recordId) return
  loading.value = true
  try {
    detail.value = await fetchRecordDetail(recordId)
  } finally {
    loading.value = false
  }
}

function statusType(status: string) {
  if (status === 'PLAYING') return 'warning'
  if (status === 'FINISHED') return 'success'
  return 'info'
}

onMounted(loadDetail)
</script>

<template>
  <el-card v-loading="loading" shadow="never">
    <template #header>
      <div style="display: flex; align-items: center; gap: 12px">
        <el-button link type="primary" @click="$router.back()">← 返回</el-button>
        <span>对局详情</span>
        <el-tag v-if="detail" :type="statusType(detail.status)">{{ detail.status }}</el-tag>
      </div>
    </template>

    <el-descriptions v-if="detail" :column="2" border style="margin-bottom: 16px">
      <el-descriptions-item label="对局 ID">{{ detail.recordId }}</el-descriptions-item>
      <el-descriptions-item label="房间号">{{ detail.roomId }}</el-descriptions-item>
      <el-descriptions-item label="玩法">{{ detail.gameType }}</el-descriptions-item>
      <el-descriptions-item label="模式">{{ recordMode }}</el-descriptions-item>
      <el-descriptions-item label="开始时间">{{ formatDateTime(detail.startAt) }}</el-descriptions-item>
      <el-descriptions-item label="结束时间">
        {{ detail.endAt ? formatDateTime(detail.endAt) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="操作数">{{ detail.actions.length }}</el-descriptions-item>
    </el-descriptions>

    <el-table
      v-if="participants.length"
      :data="participants"
      border
      stripe
      style="margin-bottom: 16px"
      empty-text="暂无参与者"
    >
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isRobot" type="info">电脑</el-tag>
          <el-tag v-else type="success">玩家</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="seat" label="座位" width="80">
        <template #default="{ row }">{{ row.seat ?? '—' }}</template>
      </el-table-column>
      <el-table-column prop="goldDelta" label="金币变化" width="100" />
    </el-table>

    <ReplayPlayer
      v-if="detail && detail.actions.length"
      :game-type="detail.gameType"
      :actions="detail.actions"
    />

    <el-collapse v-if="detail" v-model="showRaw" style="margin-top: 12px">
      <el-collapse-item title="原始 action 列表（调试）" name="raw">
        <el-table :data="detail.actions" border stripe empty-text="暂无操作记录">
          <el-table-column
            v-for="col in columns"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth"
          >
            <template v-if="col.slot === 'userId'" #default="{ row }">
              <span>{{ row.userId }}</span>
              <el-tag v-if="isBotUserId(row.userId)" size="small" type="info" style="margin-left: 6px">电脑</el-tag>
            </template>
            <template v-else-if="col.slot === 'payload'" #default="{ row }">
              <code style="font-size: 12px">{{ JSON.stringify(row.payload ?? {}) }}</code>
            </template>
            <template v-else-if="col.slot === 'createdAt'" #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </el-card>
</template>
