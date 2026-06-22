<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { fetchOverview, fetchTrends, fetchAlerts } from '../../api/dashboard'
import type { DashboardAlert } from '../../api/dashboard'
import { formatGold } from '../../utils/format'

const router = useRouter()
const loading = ref(false)
const alerts = ref<DashboardAlert[]>([])
const metrics = ref({
  todayDau: 0,
  onlineCount: 0,
  todayGames: 0,
  todayRechargeAmount: 0,
})

const gamesChartRef = ref<HTMLDivElement | null>(null)
const usersChartRef = ref<HTMLDivElement | null>(null)
const revenueChartRef = ref<HTMLDivElement | null>(null)

let gamesChart: echarts.ECharts | null = null
let usersChart: echarts.ECharts | null = null
let revenueChart: echarts.ECharts | null = null

function initChart(el: HTMLDivElement | null, option: echarts.EChartsOption) {
  if (!el) return null
  const chart = echarts.init(el)
  chart.setOption(option)
  return chart
}

function buildLineOption(title: string, dates: string[], data: number[], color: string): echarts.EChartsOption {
  return {
    title: { text: title, left: 0, textStyle: { fontSize: 14, fontWeight: 500 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 40, bottom: 28 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', smooth: true, data, areaStyle: { opacity: 0.15 }, lineStyle: { color }, itemStyle: { color } }],
  }
}

function buildBarOption(title: string, dates: string[], data: number[]): echarts.EChartsOption {
  return {
    title: { text: title, left: 0, textStyle: { fontSize: 14, fontWeight: 500 } },
    tooltip: {
      trigger: 'axis',
      formatter: (params: unknown) => {
        const list = params as Array<{ axisValue: string; data: number }>
        const item = list[0]
        return `${item.axisValue}<br/>充值：${formatGold(item.data)}`
      },
    },
    grid: { left: 48, right: 16, top: 40, bottom: 28 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data, itemStyle: { color: '#409eff' } }],
  }
}

async function loadData() {
  loading.value = true
  try {
    const [overview, trends, alertData] = await Promise.all([fetchOverview(), fetchTrends(7), fetchAlerts()])
    metrics.value = overview
    alerts.value = alertData.alerts
    gamesChart?.setOption(buildLineOption('近 7 日对局数', trends.dates, trends.games, '#67c23a'))
    usersChart?.setOption(buildLineOption('近 7 日新增用户', trends.dates, trends.newUsers, '#e6a23c'))
    revenueChart?.setOption(buildBarOption('近 7 日充值（分）', trends.dates, trends.revenue))
  } finally {
    loading.value = false
  }
}

function alertType(level: string) {
  return level === 'WARNING' ? 'warning' : 'info'
}

function goAlert(link: string) {
  router.push(link)
}

let alertTimer: number | undefined

function handleResize() {
  gamesChart?.resize()
  usersChart?.resize()
  revenueChart?.resize()
}

onMounted(async () => {
  gamesChart = initChart(gamesChartRef.value, buildLineOption('近 7 日对局数', [], [], '#67c23a'))
  usersChart = initChart(usersChartRef.value, buildLineOption('近 7 日新增用户', [], [], '#e6a23c'))
  revenueChart = initChart(revenueChartRef.value, buildBarOption('近 7 日充值（分）', [], []))
  window.addEventListener('resize', handleResize)
  await loadData()
  alertTimer = window.setInterval(loadData, 60000)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (alertTimer) clearInterval(alertTimer)
  gamesChart?.dispose()
  usersChart?.dispose()
  revenueChart?.dispose()
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <div v-if="alerts.length" class="alerts">
      <el-alert
        v-for="alert in alerts"
        :key="alert.code"
        :title="alert.message"
        :type="alertType(alert.level)"
        show-icon
        :closable="false"
        class="alert-item"
      >
        <template #default>
          <el-button link type="primary" @click="goAlert(alert.link)">去处理 →</el-button>
        </template>
      </el-alert>
    </div>

    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-title">注册玩家</div>
          <div class="metric-value">{{ metrics.todayDau }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-title">当前在线</div>
          <div class="metric-value">{{ metrics.onlineCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-title">今日对局</div>
          <div class="metric-value">{{ metrics.todayGames }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-title">今日充值</div>
          <div class="metric-value">{{ formatGold(metrics.todayRechargeAmount) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="8">
        <el-card shadow="never"><div ref="gamesChartRef" class="chart" /></el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never"><div ref="usersChartRef" class="chart" /></el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never"><div ref="revenueChartRef" class="chart" /></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alerts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alert-item {
  margin: 0;
}

.metric-title {
  color: #909399;
  font-size: 14px;
}

.metric-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 600;
}

.charts-row {
  margin-top: 0;
}

.chart {
  height: 280px;
}
</style>
