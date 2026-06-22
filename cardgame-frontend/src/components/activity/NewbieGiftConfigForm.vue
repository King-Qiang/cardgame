<script setup lang="ts">
import { ref, watch } from 'vue'
import type { NewbieGiftConfig } from '../../types/activity'

const model = defineModel<NewbieGiftConfig>({ required: true })

const itemsText = ref('[]')

watch(
  () => model.value.items,
  (items) => {
    itemsText.value = JSON.stringify(items ?? [], null, 2)
  },
  { immediate: true, deep: true },
)

function syncItemsFromText(): string | null {
  try {
    const parsed = JSON.parse(itemsText.value) as unknown
    if (!Array.isArray(parsed)) {
      return '道具配置须为 JSON 数组，例如 [{"itemId":1,"count":1}]'
    }
    model.value.items = parsed
    return null
  } catch {
    return '道具 JSON 格式不正确'
  }
}

defineExpose({ syncItemsFromText })
</script>

<template>
  <div class="newbie-gift-form">
    <el-form-item label="金币奖励" label-width="96px">
      <el-input-number v-model="model.gold" :min="0" :step="100" controls-position="right" />
      <span class="unit">金币（一次性发放）</span>
    </el-form-item>
    <el-form-item label="道具配置" label-width="96px">
      <el-input
        v-model="itemsText"
        type="textarea"
        :rows="5"
        placeholder='可选，JSON 数组，如 [{"itemId":1,"count":1}]'
      />
      <p class="hint">留空数组表示仅发放金币，无道具。</p>
    </el-form-item>
  </div>
</template>

<style scoped>
.newbie-gift-form {
  width: 100%;
}

.unit {
  margin-left: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
