<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useTable } from '../../composables/useTable'
import { fetchCurrentSeasonId, fetchRankLeaderboard } from '../../api/rank'
import type { RankLeaderboardItem } from '../../api/rank'
import { GAME_TYPE_OPTIONS, TIER_OPTIONS, tierLabel, tierTagType } from '../../utils/rankTier'
import type { SearchField } from '../../types/table'

const currentSeason = ref('')
const router = useRouter()

const searchFields: SearchField[] = [
      {
        prop: 'gameType',
        label: '玩法',
        type: 'select',
        options: GAME_TYPE_OPTIONS,
      },
  {
    prop: 'seasonId',
    label: '赛季',
    type: 'input',
    placeholder: '留空使用当前赛季',
  },
  {
    prop: 'tier',
    label: '段位',
    type: 'select',
    options: [{ label: '全部', value: '' }, ...TIER_OPTIONS],
  },
]

const columns = [
  { prop: 'rank', label: '排名', width: 70 },
  { prop: 'userId', label: '用户 ID', width: 90 },
  { prop: 'nickname', label: '昵称', minWidth: 120 },
  { prop: 'tier', label: '段位', slot: 'tier', width: 100 },
  { prop: 'points', label: '积分', width: 90 },
  { prop: 'wins', label: '胜', width: 70 },
  { prop: 'losses', label: '负', width: 70 },
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
} = useTable<RankLeaderboardItem>(fetchRankLeaderboard)

function openUser(userId: number) {
  router.push({ path: '/users', query: { userId: String(userId) } })
}

onMounted(async () => {
  currentSeason.value = await fetchCurrentSeasonId()
  fetchData()
})
</script>

<template>
  <el-card shadow="never">
    <p v-if="currentSeason" class="season-hint">当前赛季：<strong>{{ currentSeason }}</strong>（赛季筛选留空时默认）</p>
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
      <template #tier="{ row }">
        <el-tag :type="tierTagType(row.tier)">{{ tierLabel(row.tier) }}</el-tag>
      </template>
      <template #actions="{ row }">
        <el-button link type="primary" @click="openUser(row.userId)">查看用户</el-button>
      </template>
    </DataTable>
  </el-card>
</template>

<style scoped>
.season-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
