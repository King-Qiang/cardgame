<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import PermissionButton from '../../components/PermissionButton.vue'
import ActivityConfigEditor from '../../components/activity/ActivityConfigEditor.vue'
import { useTable } from '../../composables/useTable'
import { fetchActivities, createActivity, updateActivity, deleteActivity } from '../../api/activity'
import { formatDateTime } from '../../utils/format'
import { defaultConfigForType } from '../../utils/activityConfig'
import { PERMISSIONS } from '../../constants/permissions'
import type { SearchField } from '../../types/table'
import type { ActivityItem, ActivityPayload } from '../../api/activity'
import type { ActivityType } from '../../types/activity'

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const configEditorRef = ref<InstanceType<typeof ActivityConfigEditor> | null>(null)
const form = ref<ActivityPayload>({
  code: '',
  name: '',
  type: 'DAILY_SIGN',
  configJson: defaultConfigForType('DAILY_SIGN'),
  status: 1,
})

const searchFields: SearchField[] = [
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '启用', value: '1' },
      { label: '禁用', value: '0' },
    ],
  },
  {
    prop: 'type',
    label: '类型',
    type: 'select',
    options: [
      { label: '每日签到', value: 'DAILY_SIGN' },
      { label: '新手礼包', value: 'NEWBIE_GIFT' },
      { label: '限时活动', value: 'LIMITED' },
    ],
  },
]

const columns = [
  { prop: 'code', label: '编码', minWidth: 120 },
  { prop: 'name', label: '名称', minWidth: 120 },
  { prop: 'type', label: '类型', width: 110 },
  { prop: 'statusLabel', label: '状态', slot: 'status', width: 90 },
  { prop: 'updatedAt', label: '更新时间', slot: 'updatedAt', minWidth: 170 },
]

const {
  loading,
  tableData,
  total,
  page,
  pageSize,
  fetchData,
  handleSearch,
  handleReset,
  handlePageChange,
  handleSizeChange,
} = useTable<ActivityItem>(fetchActivities)

function openCreate() {
  editingId.value = null
  form.value = {
    code: '',
    name: '',
    type: 'DAILY_SIGN',
    configJson: defaultConfigForType('DAILY_SIGN'),
    status: 1,
    startAt: null,
    endAt: null,
  }
  dialogVisible.value = true
}

function openEdit(row: ActivityItem) {
  editingId.value = row.id
  form.value = {
    code: row.code,
    name: row.name,
    type: row.type,
    configJson: row.configJson ?? defaultConfigForType(row.type as ActivityType),
    status: row.status,
    startAt: row.startAt ?? null,
    endAt: row.endAt ?? null,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const configError = configEditorRef.value?.validate()
  if (configError) {
    ElMessage.warning(configError)
    return
  }
  if (editingId.value) {
    await updateActivity(editingId.value, form.value)
    ElMessage.success('活动已更新')
  } else {
    await createActivity(form.value)
    ElMessage.success('活动已创建')
  }
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(row: ActivityItem) {
  await ElMessageBox.confirm(`确认删除活动「${row.name}」？`, '删除活动', { type: 'warning' })
  await deleteActivity(row.id)
  ElMessage.success('已删除')
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <SearchForm :fields="searchFields" @search="handleSearch" @reset="handleReset" />
      <PermissionButton :permission="PERMISSIONS.SHOP_MANAGE" type="primary" @click="openCreate">
        新增活动
      </PermissionButton>
    </div>
    <DataTable
      :columns="columns"
      :data="tableData"
      :total="total"
      :loading="loading"
      :page="page"
      :page-size="pageSize"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #status="{ row }">
        <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusLabel }}</el-tag>
      </template>
      <template #updatedAt="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      <template #actions="{ row }">
        <PermissionButton :permission="PERMISSIONS.SHOP_MANAGE" link type="primary" @click="openEdit(row)">
          编辑
        </PermissionButton>
        <PermissionButton :permission="PERMISSIONS.SHOP_MANAGE" link type="danger" @click="handleDelete(row)">
          删除
        </PermissionButton>
      </template>
    </DataTable>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑活动' : '新增活动'"
      :width="form.type === 'DAILY_SIGN' ? '640px' : '560px'"
    >
      <el-form label-width="80px">
        <el-form-item label="编码">
          <el-input v-model="form.code" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="每日签到" value="DAILY_SIGN" />
            <el-option label="新手礼包" value="NEWBIE_GIFT" />
            <el-option label="限时活动" value="LIMITED" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="活动配置" class="config-item">
          <ActivityConfigEditor
            ref="configEditorRef"
            v-model:config-json="form.configJson"
            v-model:start-at="form.startAt"
            v-model:end-at="form.endAt"
            :type="form.type"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.config-item :deep(.el-form-item__content) {
  display: block;
}
</style>
