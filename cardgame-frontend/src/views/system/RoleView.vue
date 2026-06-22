<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PermissionButton from '../../components/PermissionButton.vue'
import { fetchRoles, createRole, updateRole, deleteRole } from '../../api/system'
import { formatDateTime } from '../../utils/format'
import { PERMISSIONS } from '../../constants/permissions'
import { ALL_PERMISSIONS, PERMISSION_LABELS } from '../../constants/permissionLabels'
import type { AdminRoleItem } from '../../api/system'

const loading = ref(false)
const roles = ref<AdminRoleItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ name: '', description: '', permissions: [] as string[] })

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await fetchRoles()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { name: '', description: '', permissions: [] }
  dialogVisible.value = true
}

function openEdit(row: AdminRoleItem) {
  editingId.value = row.id
  form.value = { name: row.name, description: row.description, permissions: [...row.permissions] }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请填写角色名')
    return
  }
  if (form.value.permissions.length === 0) {
    ElMessage.warning('请至少选择一项权限')
    return
  }
  if (editingId.value) {
    await updateRole(editingId.value, form.value)
    ElMessage.success('角色已更新')
  } else {
    await createRole(form.value)
    ElMessage.success('角色已创建')
  }
  dialogVisible.value = false
  loadRoles()
}

async function handleDelete(row: AdminRoleItem) {
  await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '删除角色', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('已删除')
  loadRoles()
}

onMounted(loadRoles)
</script>

<template>
  <el-card v-loading="loading" shadow="never">
    <div class="toolbar">
      <span class="hint">配置运营账号可访问的菜单与操作权限</span>
      <PermissionButton :permission="PERMISSIONS.SYSTEM_ROLE" type="primary" @click="openCreate">
        新增角色
      </PermissionButton>
    </div>
    <el-table :data="roles" border stripe empty-text="暂无角色">
      <el-table-column prop="name" label="角色名" min-width="140" />
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column prop="permissionCount" label="权限数" width="90" />
      <el-table-column prop="createdAt" label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <PermissionButton
            :permission="PERMISSIONS.SYSTEM_ROLE"
            link
            type="primary"
            @click="openEdit(row)"
          >
            编辑
          </PermissionButton>
          <PermissionButton
            v-if="row.id !== 1"
            :permission="PERMISSIONS.SYSTEM_ROLE"
            link
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </PermissionButton>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="560px">
      <el-form label-width="80px">
        <el-form-item label="角色名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="form.permissions" class="perm-group">
            <el-checkbox v-for="perm in ALL_PERMISSIONS" :key="perm" :value="perm">
              {{ PERMISSION_LABELS[perm] || perm }}
            </el-checkbox>
          </el-checkbox-group>
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
  align-items: center;
  margin-bottom: 16px;
}

.hint {
  color: #909399;
  font-size: 14px;
}

.perm-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
</style>
