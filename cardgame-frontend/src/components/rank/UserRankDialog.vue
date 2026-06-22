<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchUserRank, fetchUserRankLogs } from '../../api/rank'
import type { PlayerRankInfo, PlayerRankLogItem } from '../../api/rank'
import { formatDateTime } from '../../utils/format'
import { tierLabel, tierTagType } from '../../utils/rankTier'

const props = defineProps<{
  userId: number
  nickname: string
}>()

const visible = defineModel<boolean>('visible', { required: true })

const loading = ref(false)
const rankInfo = ref<PlayerRankInfo | null>(null)
const logs = ref<PlayerRankLogItem[]>([])

const router = useRouter()

watch(
  () => visible.value,
  async (open) => {
    if (!open) return
    loading.value = true
    try {
      const [rank, rankLogs] = await Promise.all([
        fetchUserRank(props.userId),
        fetchUserRankLogs(props.userId),
      ])
      rankInfo.value = rank
      logs.value = rankLogs
    } finally {
      loading.value = false
    }
  },
)

function goRecord(recordId: string) {
  visible.value = false
  router.push(`/records/${recordId}`)
}

function deltaText(delta: number) {
  return delta >= 0 ? `+${delta}` : String(delta)
}
</script>

<template>
  <el-dialog v-model="visible" :title="`排位信息 — ${nickname}`" width="640px">
    <div v-loading="loading">
      <template v-if="rankInfo">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="赛季">{{ rankInfo.seasonId }}</el-descriptions-item>
          <el-descriptions-item label="玩法">{{ rankInfo.gameType }}</el-descriptions-item>
          <el-descriptions-item label="段位">
            <el-tag :type="tierTagType(rankInfo.tier)">{{ tierLabel(rankInfo.tier) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="积分">{{ rankInfo.points }}</el-descriptions-item>
          <el-descriptions-item label="胜场">{{ rankInfo.wins }}</el-descriptions-item>
          <el-descriptions-item label="负场">{{ rankInfo.losses }}</el-descriptions-item>
          <el-descriptions-item v-if="rankInfo.nextTier" label="下一段位" :span="2">
            {{ tierLabel(rankInfo.nextTier) }}（还差 {{ rankInfo.pointsToNextTier ?? 0 }} 分）
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">最近段位变动</h4>
        <el-table v-if="logs.length" :data="logs" size="small" stripe>
          <el-table-column prop="createdAt" label="时间" min-width="160">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column prop="recordId" label="对局 ID" min-width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="goRecord(row.recordId)">{{ row.recordId }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="积分变动" width="100">
            <template #default="{ row }">
              <span :class="row.deltaPoints >= 0 ? 'delta-up' : 'delta-down'">
                {{ deltaText(row.deltaPoints) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="段位" min-width="140">
            <template #default="{ row }">
              {{ tierLabel(row.tierBefore) }} → {{ tierLabel(row.tierAfter) }}
            </template>
          </el-table-column>
          <el-table-column label="积分" width="120">
            <template #default="{ row }">{{ row.pointsBefore }} → {{ row.pointsAfter }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无段位变动记录" :image-size="64" />
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.section-title {
  margin: 16px 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.delta-up {
  color: var(--el-color-success);
}

.delta-down {
  color: var(--el-color-danger);
}
</style>
