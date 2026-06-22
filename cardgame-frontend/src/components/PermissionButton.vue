<script setup lang="ts">
import { computed } from 'vue'
import { usePermission } from '../composables/usePermission'

const props = withDefaults(
  defineProps<{
    permission?: string
    hideWhenDenied?: boolean
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default'
    link?: boolean
  }>(),
  {
    hideWhenDenied: true,
    type: 'primary',
    link: false,
  }
)

const { hasPermission } = usePermission()
const allowed = computed(() => hasPermission(props.permission))

defineEmits<{ click: [] }>()
</script>

<template>
  <el-button
    v-if="allowed || !hideWhenDenied"
    :type="type"
    :link="link"
    :disabled="!allowed"
    v-bind="$attrs"
    @click="$emit('click')"
  >
    <slot />
  </el-button>
</template>
