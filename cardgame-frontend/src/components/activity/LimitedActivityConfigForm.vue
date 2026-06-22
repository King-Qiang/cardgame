<script setup lang="ts">
import { ref, watch } from 'vue'
import { validateLimitedConfigJson } from '../../utils/activityConfig'

const configText = defineModel<string>('configText', { required: true })
const startAt = defineModel<string | null | undefined>('startAt')
const endAt = defineModel<string | null | undefined>('endAt')

const localError = ref('')

watch(configText, () => {
  localError.value = ''
})

function validate(): string | null {
  if (startAt.value && endAt.value && startAt.value >= endAt.value) {
    return '活动结束时间须晚于开始时间'
  }
  const result = validateLimitedConfigJson(configText.value)
  if (!result.ok) {
    localError.value = result.message
    return result.message
  }
  configText.value = JSON.stringify(result.value, null, 2)
  localError.value = ''
  return null
}

defineExpose({ validate })
</script>

<template>
  <div class="limited-form">
    <el-form-item label="活动时间" label-width="96px">
      <div class="date-range">
        <el-date-picker
          v-model="startAt"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
        <span class="sep">至</span>
        <el-date-picker
          v-model="endAt"
          type="datetime"
          placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
      </div>
    </el-form-item>
    <el-form-item label="活动规则" label-width="96px">
      <el-input
        v-model="configText"
        type="textarea"
        :rows="8"
        placeholder='JSON 对象，如 {"rules":{"maxClaims":1},"rewards":{"gold":500}}'
      />
      <p v-if="localError" class="error">{{ localError }}</p>
      <p v-else class="hint">复杂规则仍使用 JSON 编辑，Phase 4 不做拖拽式编辑器。</p>
    </el-form-item>
  </div>
</template>

<style scoped>
.limited-form {
  width: 100%;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.sep {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--el-color-danger);
}
</style>
