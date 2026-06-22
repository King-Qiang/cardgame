<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import PermissionButton from '../../components/PermissionButton.vue'
import { useTable } from '../../composables/useTable'
import {
  fetchAdmins,
  fetchRoles,
  createAdmin,
  updateAdmin,
  resetAdminPassword,
} from '../../api/system'
import { formatDateTime } from '../../utils/format'
import { PERMISSIONS } from '../../constants/permissions'
import type { SearchField } from '../../types/table'
import type { AdminAccountItem, AdminRoleItem } from '../../api/system'

const roles = ref<AdminRoleItem[]>([])
const createVisible = ref(false)
const editVisible = ref(false)
const editingRow = ref<AdminAccountItem | null>(null)
const createForm = ref({ username: '', password: '', realName: '', roleId: 1 })
const editForm = ref({ roleId: 1, status: 1, realName: '' })

const searchFields: SearchField[] = [
  { prop: 'username', label: '用户名', type: 'input' },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '启用', value: '1' },
      { label: '禁用', value: '0' },
    ],
  },
]

const columns = [
  { prop: 'username', label: '用户名', minWidth: 120 },
  { prop: 'realName', label: '姓名', minWidth: 100 },
  { prop: 'roleName', label: '角色', minWidth: 120 },
  { prop: 'statusLabel', label: '状态', slot: 'status', width: 90 },
  { prop: 'lastLoginAt', label: '最后登录', slot: 'lastLoginAt', minWidth: 170 },
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
} = useTable<AdminAccountItem>(fetchAdmins)

async function loadRoles() {
  roles.value = await fetchRoles()
}

function openCreate() {
  createForm.value = { username: '', password: '', realName: '', roleId: roles.value[0]?.id ?? 1 }
  createVisible.value = true
}

function openEdit(row: AdminAccountItem) {
  editingRow.value = row
  editForm.value = { roleId: row.roleId, status: row.status, realName: row.realName }
  editVisible.value = true
}

async function handleCreate() {
  await createAdmin(createForm.value)
  ElMessage.success('管理员已创建')
  createVisible.value = false
  fetchData()
}

async function handleEdit() {
  if (!editingRow.value) return
  await updateAdmin(editingRow.value.id, editForm.value)
  ElMessage.success('已更新')
  editVisible.value = false
  fetchData()
}

async function handleResetPassword(row: AdminAccountItem) {
  const { value } = await ElMessageBox.prompt('请输入新密码', `重置 ${row.username} 密码`, {
    inputType: 'password',
  })
  if (!value) return
  await resetAdminPassword(row.id, value)
  ElMessage.success('密码已重置')
}

onMounted(async () => {
  await loadRoles()
  fetchData()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <SearchForm :fields="searchFields" @search="handleSearch" @reset="handleReset" />
      <PermissionButton :permission="PERMISSIONS.SYSTEM_ADMIN" type="primary" @click="openCreate">
        新增管理员
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
        <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.statusLabel }}</el-tag>
      </template>
      <template #lastLoginAt="{ row }">{{ formatDateTime(row.lastLoginAt) }}</template>
      <template #actions="{ row }">
        <PermissionButton :permission="PERMISSIONS.SYSTEM_ADMIN" link type="primary" @click="openEdit(row)">
          编辑
        </PermissionButton>
        <PermissionButton
          :permission="PERMISSIONS.SYSTEM_ADMIN"
          link
          type="warning"
          @click="handleResetPassword(row)"
        >
          重置密码
        </PermissionButton>
      </template>
    </DataTable>

    <el-dialog v-model="createVisible" title="新增管理员" width="480px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="createForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="createForm.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="createForm.roleId" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑管理员" width="480px">
      <el-form label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.roleId" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEdit">保存</el-button>
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
</style>
