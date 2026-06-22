<script setup lang="ts">
const props = defineProps<{
  currentStep: number
  totalSteps: number
  playing: boolean
  speed: 1 | 2 | 4
}>()

const emit = defineEmits<{
  prev: []
  next: []
  toggle: []
  'update:speed': [speed: 1 | 2 | 4]
  'update:currentStep': [step: number]
}>()

function onSliderChange(value: number) {
  emit('update:currentStep', value)
}
</script>

<template>
  <div class="replay-controls">
    <div class="buttons">
      <el-button :disabled="currentStep <= 0" @click="emit('prev')">上一步</el-button>
      <el-button type="primary" @click="emit('toggle')">{{ playing ? '暂停' : '播放' }}</el-button>
      <el-button :disabled="currentStep >= totalSteps - 1" @click="emit('next')">下一步</el-button>
      <el-radio-group :model-value="speed" size="small" @update:model-value="emit('update:speed', $event as 1 | 2 | 4)">
        <el-radio-button :value="1">1x</el-radio-button>
        <el-radio-button :value="2">2x</el-radio-button>
        <el-radio-button :value="4">4x</el-radio-button>
      </el-radio-group>
    </div>
    <div class="progress">
      <span>步骤 {{ totalSteps === 0 ? 0 : currentStep + 1 }} / {{ totalSteps }}</span>
      <el-slider
        :model-value="currentStep"
        :min="0"
        :max="Math.max(0, totalSteps - 1)"
        :disabled="totalSteps === 0"
        @update:model-value="onSliderChange"
      />
    </div>
  </div>
</template>

<style scoped>
.replay-controls {
  margin-top: 12px;
}
.buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.progress {
  margin-top: 12px;
}
</style>
