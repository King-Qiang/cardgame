<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import DailySignConfigForm from './DailySignConfigForm.vue'
import NewbieGiftConfigForm from './NewbieGiftConfigForm.vue'
import LimitedActivityConfigForm from './LimitedActivityConfigForm.vue'
import type { ActivityType, DailySignConfig, NewbieGiftConfig } from '../../types/activity'
import {
  defaultConfigForType,
  parseDailySignConfig,
  parseNewbieGiftConfig,
  validateDailySignConfig,
  validateNewbieGiftConfig,
  validateLimitedConfigJson,
} from '../../utils/activityConfig'

const props = defineProps<{
  type: ActivityType | string
}>()

const configJson = defineModel<unknown>('configJson', { required: true })
const startAt = defineModel<string | null | undefined>('startAt')
const endAt = defineModel<string | null | undefined>('endAt')

const dailySignConfig = ref<DailySignConfig>(parseDailySignConfig(configJson.value))
const newbieGiftConfig = ref<NewbieGiftConfig>(parseNewbieGiftConfig(configJson.value))
const limitedConfigText = ref(JSON.stringify(configJson.value ?? { rules: {} }, null, 2))

const newbieFormRef = ref<InstanceType<typeof NewbieGiftConfigForm> | null>(null)
const limitedFormRef = ref<InstanceType<typeof LimitedActivityConfigForm> | null>(null)

const activityType = computed(() => props.type as ActivityType)

watch(
  () => props.type,
  (type, prev) => {
    if (prev && type !== prev) {
      applyDefaults(type as ActivityType)
    }
  },
)

watch(
  configJson,
  (value) => {
    if (activityType.value === 'DAILY_SIGN') {
      dailySignConfig.value = parseDailySignConfig(value)
    } else if (activityType.value === 'NEWBIE_GIFT') {
      newbieGiftConfig.value = parseNewbieGiftConfig(value)
    } else if (activityType.value === 'LIMITED') {
      limitedConfigText.value = JSON.stringify(value ?? { rules: {} }, null, 2)
    }
  },
  { deep: true },
)

function applyDefaults(type: ActivityType) {
  const defaults = defaultConfigForType(type)
  configJson.value = defaults
  if (type === 'DAILY_SIGN') {
    dailySignConfig.value = parseDailySignConfig(defaults)
  } else if (type === 'NEWBIE_GIFT') {
    newbieGiftConfig.value = parseNewbieGiftConfig(defaults)
  } else {
    limitedConfigText.value = JSON.stringify(defaults, null, 2)
    startAt.value = null
    endAt.value = null
  }
}

function validate(): string | null {
  if (activityType.value === 'DAILY_SIGN') {
    const error = validateDailySignConfig(dailySignConfig.value)
    if (error) {
      return error
    }
    configJson.value = {
      rewards: [...dailySignConfig.value.rewards],
      ...(dailySignConfig.value.description?.trim()
        ? { description: dailySignConfig.value.description.trim() }
        : {}),
    }
    return null
  }

  if (activityType.value === 'NEWBIE_GIFT') {
    const itemsError = newbieFormRef.value?.syncItemsFromText()
    if (itemsError) {
      return itemsError
    }
    const error = validateNewbieGiftConfig(newbieGiftConfig.value)
    if (error) {
      return error
    }
    configJson.value = {
      gold: newbieGiftConfig.value.gold,
      items: newbieGiftConfig.value.items,
    }
    return null
  }

  if (activityType.value === 'LIMITED') {
    const error = limitedFormRef.value?.validate() ?? null
    if (error) {
      return error
    }
    const parsed = validateLimitedConfigJson(limitedConfigText.value)
    if (parsed.ok) {
      configJson.value = parsed.value
    }
    return null
  }

  return null
}

defineExpose({ validate, applyDefaults })
</script>

<template>
  <DailySignConfigForm v-if="activityType === 'DAILY_SIGN'" v-model="dailySignConfig" />
  <NewbieGiftConfigForm
    v-else-if="activityType === 'NEWBIE_GIFT'"
    ref="newbieFormRef"
    v-model="newbieGiftConfig"
  />
  <LimitedActivityConfigForm
    v-else-if="activityType === 'LIMITED'"
    ref="limitedFormRef"
    v-model:config-text="limitedConfigText"
    v-model:start-at="startAt"
    v-model:end-at="endAt"
  />
</template>
