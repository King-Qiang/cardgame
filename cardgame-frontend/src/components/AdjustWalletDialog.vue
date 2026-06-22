<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adjustWallet, createAdjustRequest } from '../api/economy'
import { fetchSystemConfig } from '../api/system'
import { formatGold } from '../utils/format'
import type { ApiResponse } from '../types/api'

const props = defineProps<{
  visible: boolean
  userId: number
  nickname: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

const form = ref({
  adjustType: 'INCREASE' as 'INCREASE' | 'DECREASE',
  amount: 1000,
  reason: '',
})
const loading = ref(false)
const threshold = ref(100_000)

watch(
  () => props.visible,
  async (open) => {
    if (!open) return
    form.value = { adjustType: 'INCREASE', amount: 1000, reason: '' }
    try {
      const config = await fetchSystemConfig('wallet.adjust_threshold')
      const value = config.configValue
      if (value && typeof value === 'object' && !Array.isArray(value)) {
        const amount = (value as Record<string, unknown>).amount
        if (typeof amount === 'number') {
          threshold.value = amount
        }
      }
    } catch {
      threshold.value = 100_000
    }
  }
)

function close() {
  emit('update:visible', false)
}

async function handleSubmit() {
  if (!form.value.reason.trim()) {
    ElMessage.warning('请填写调账原因')
    return
  }
  if (form.value.amount <= 0) {
    ElMessage.warning('金额须为正整数')
    return
  }
  loading.value = true
  try {
    await adjustWallet(props.userId, form.value)
    ElMessage.success('调账成功')
    emit('success')
    close()
  } catch (err) {
    const body = err as ApiResponse
    if (body.code === 60003) {
      await ElMessageBox.confirm(
        `金额超过阈值（${formatGold(threshold.value)}），是否提交审批？`,
        '需审批',
        { type: 'warning' }
      )
      await createAdjustRequest({ userId: props.userId, ...form.value })
      ElMessage.success('调账申请已提交，等待审批')
      emit('success')
      close()
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="`调账 — ${nickname}（ID: ${userId}）`"
    width="480px"
    @close="close"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
      :title="`低于 ${formatGold(threshold)} 直接执行，超过则走审批`"
    />
    <el-form label-width="80px">
      <el-form-item label="类型">
        <el-radio-group v-model="form.adjustType">
          <el-radio value="INCREASE">增加</el-radio>
          <el-radio value="DECREASE">扣减</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="金额">
        <el-input-number v-model="form.amount" :min="1" :step="100" style="width: 100%" />
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="必填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
    </template>
  </el-dialog>
</template>
