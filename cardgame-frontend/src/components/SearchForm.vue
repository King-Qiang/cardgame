<script setup lang="ts">
import { reactive } from 'vue'
import type { SearchField } from '../types/table'

const props = defineProps<{
  fields: SearchField[]
}>()

const emit = defineEmits<{
  search: [params: Record<string, unknown>]
  reset: []
}>()

const form = reactive<Record<string, unknown>>({})

props.fields.forEach((field) => {
  form[field.prop] = field.type === 'date-range' ? [] : ''
})

function handleSearch() {
  emit('search', { ...form })
}

function handleReset() {
  props.fields.forEach((field) => {
    form[field.prop] = field.type === 'date-range' ? [] : ''
  })
  emit('reset')
}
</script>

<template>
  <el-form inline class="search-form" @submit.prevent="handleSearch">
    <el-form-item v-for="field in fields" :key="field.prop" :label="field.label">
      <el-input
        v-if="field.type === 'input'"
        v-model="form[field.prop] as string"
        :placeholder="field.placeholder"
        clearable
        style="width: 180px"
      />
      <el-select
        v-else-if="field.type === 'select'"
        v-model="form[field.prop] as string"
        :placeholder="field.placeholder || '请选择'"
        clearable
        style="width: 140px"
      >
        <el-option
          v-for="opt in field.options || []"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-date-picker
        v-else-if="field.type === 'date-range'"
        v-model="form[field.prop] as string[]"
        type="daterange"
        range-separator="至"
        start-placeholder="开始"
        end-placeholder="结束"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
</style>
