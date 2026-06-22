<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchSystemConfigs, updateSystemConfig } from '../../api/system'
import { formatDateTime } from '../../utils/format'
import type { SystemConfigItem } from '../../api/system'
import PermissionButton from '../../components/PermissionButton.vue'
import { PERMISSIONS } from '../../constants/permissions'

const loading = ref(false)
const configs = ref<SystemConfigItem[]>([])
const editingKey = ref<string | null>(null)
const editForm = ref<Record<string, unknown>>({})

async function loadConfigs() {
  loading.value = true
  try {
    configs.value = await fetchSystemConfigs()
  } catch {
    configs.value = []
  } finally {
    loading.value = false
  }
}

function startEdit(row: SystemConfigItem) {
  editingKey.value = row.configKey
  if (row.configValue && typeof row.configValue === 'object' && !Array.isArray(row.configValue)) {
    editForm.value = { ...(row.configValue as Record<string, unknown>) }
  } else {
    editForm.value = { value: row.configValue }
  }
}

function cancelEdit() {
  editingKey.value = null
  editForm.value = {}
}

async function saveEdit(key: string) {
  let configValue: unknown
  if (key === 'wallet.adjust_threshold' || key === 'maintenance' || key === 'game.pve' || key === 'game.bot') {
    configValue = editForm.value
  } else if ('value' in editForm.value) {
    configValue = editForm.value.value
  } else {
    configValue = editForm.value
  }
  await updateSystemConfig(key, configValue)
  ElMessage.success('配置已更新')
  editingKey.value = null
  loadConfigs()
}

function renderValue(value: unknown) {
  return JSON.stringify(value, null, 2)
}

onMounted(loadConfigs)
</script>

<template>
  <el-card v-loading="loading" shadow="never">
    <el-table :data="configs" border stripe empty-text="暂无配置">
      <el-table-column prop="configKey" label="配置键" min-width="180" />
      <el-table-column prop="description" label="说明" min-width="160" />
      <el-table-column label="配置值" min-width="280">
        <template #default="{ row }">
          <template v-if="editingKey === row.configKey">
            <template v-if="row.configKey === 'wallet.adjust_threshold'">
              <div class="edit-row">
                <span>阈值：</span>
                <el-input-number v-model="editForm.amount" :min="1" :step="1000" />
              </div>
              <div class="edit-row">
                <span>需审批：</span>
                <el-switch v-model="editForm.require_approval" />
              </div>
            </template>
            <template v-else-if="row.configKey === 'maintenance'">
              <div class="edit-row">
                <span>启用：</span>
                <el-switch v-model="editForm.enabled" />
              </div>
              <el-input v-model="editForm.message" type="textarea" :rows="2" placeholder="维护公告" />
            </template>
            <template v-else-if="row.configKey === 'game.pve'">
              <div class="edit-row">
                <span>允许人机练习：</span>
                <el-switch v-model="editForm.enabled" />
              </div>
              <div class="edit-row">
                <span>默认难度：</span>
                <el-select v-model="editForm.defaultDifficulty" style="width: 140px">
                  <el-option label="EASY" value="EASY" />
                  <el-option label="NORMAL" value="NORMAL" />
                  <el-option label="HARD" value="HARD" />
                </el-select>
              </div>
            </template>
            <template v-else-if="row.configKey === 'game.bot'">
              <div class="edit-row">
                <span>最小延迟(ms)：</span>
                <el-input-number v-model="editForm.minDelayMs" :min="0" :step="100" />
              </div>
              <div class="edit-row">
                <span>最大延迟(ms)：</span>
                <el-input-number v-model="editForm.maxDelayMs" :min="0" :step="100" />
              </div>
              <div class="edit-row">
                <span>启用难度：</span>
                <el-select v-model="editForm.difficulties" multiple style="min-width: 220px">
                  <el-option label="EASY" value="EASY" />
                  <el-option label="NORMAL" value="NORMAL" />
                  <el-option label="HARD" value="HARD" />
                </el-select>
              </div>
            </template>
            <el-input
              v-else
              :model-value="JSON.stringify(editForm.value ?? editForm, null, 2)"
              type="textarea"
              :rows="4"
              @update:model-value="(v: string) => { try { editForm.value = JSON.parse(v) } catch { /* 编辑中允许临时非法 JSON */ } }"
            />
          </template>
          <pre v-else class="json-preview">{{ renderValue(row.configValue) }}</pre>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="editingKey === row.configKey">
            <el-button link type="primary" @click="saveEdit(row.configKey)">保存</el-button>
            <el-button link @click="cancelEdit">取消</el-button>
          </template>
          <PermissionButton
            v-else
            :permission="PERMISSIONS.SYSTEM_CONFIG"
            link
            type="primary"
            @click="startEdit(row)"
          >
            编辑
          </PermissionButton>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.json-preview {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}

.edit-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
</style>
