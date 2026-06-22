<script setup lang="ts">
import { computed } from 'vue'
import type { DailySignConfig } from '../../types/activity'
import { sumDailyRewards } from '../../utils/activityConfig'

const model = defineModel<DailySignConfig>({ required: true })

const cycleTotal = computed(() => sumDailyRewards(model.value.rewards))
</script>

<template>
  <div class="daily-sign-form">
    <p class="hint">连续签到 7 天循环奖励，第 8 天起从第 1 天重新计算。</p>
    <div class="reward-grid">
      <div v-for="(_, index) in model.rewards" :key="index" class="reward-item">
        <span class="day-label">第 {{ index + 1 }} 天</span>
        <el-input-number
          v-model="model.rewards[index]"
          :min="0"
          :step="10"
          controls-position="right"
          style="width: 100%"
        />
      </div>
    </div>
    <p class="summary">7 天周期总奖励：<strong>{{ cycleTotal }}</strong> 金币</p>
    <el-form-item label="说明" label-width="80px" class="description-item">
      <el-input v-model="model.description" placeholder="活动说明（可选）" />
    </el-form-item>
  </div>
</template>

<style scoped>
.daily-sign-form {
  width: 100%;
}

.hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.reward-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}

.reward-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.day-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.summary {
  margin: 14px 0 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.description-item {
  margin-bottom: 0;
}
</style>
