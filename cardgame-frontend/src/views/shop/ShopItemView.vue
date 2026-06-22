<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import PermissionButton from '../../components/PermissionButton.vue'
import { useTable } from '../../composables/useTable'
import { fetchShopItems, createShopItem, updateShopItem, deleteShopItem } from '../../api/shop'
import { formatDateTime, formatGold } from '../../utils/format'
import { PERMISSIONS } from '../../constants/permissions'
import type { SearchField } from '../../types/table'
import type { ShopItem, ShopItemPayload } from '../../api/shop'

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref<ShopItemPayload>({
  name: '',
  price: 100,
  currency: 'GOLD',
  payload: { gold: 100 },
  status: 1,
  sortOrder: 0,
})
const payloadText = ref('{"gold": 100}')

const searchFields: SearchField[] = [
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '上架', value: '1' },
      { label: '下架', value: '0' },
    ],
  },
]

const columns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'name', label: '商品名', minWidth: 140 },
  { prop: 'price', label: '价格', slot: 'price', minWidth: 120 },
  { prop: 'currency', label: '货币', width: 90 },
  { prop: 'statusLabel', label: '状态', slot: 'status', width: 90 },
  { prop: 'sortOrder', label: '排序', width: 80 },
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
} = useTable<ShopItem>(fetchShopItems)

function openCreate() {
  editingId.value = null
  form.value = { name: '', price: 100, currency: 'GOLD', payload: { gold: 100 }, status: 1, sortOrder: 0 }
  payloadText.value = '{"gold": 100}'
  dialogVisible.value = true
}

function openEdit(row: ShopItem) {
  editingId.value = row.id
  form.value = {
    name: row.name,
    price: row.price,
    currency: row.currency as 'GOLD' | 'DIAMOND',
    payload: row.payload,
    status: row.status,
    sortOrder: row.sortOrder,
  }
  payloadText.value = JSON.stringify(row.payload ?? {}, null, 2)
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    form.value.payload = JSON.parse(payloadText.value)
  } catch {
    ElMessage.warning('发放内容 JSON 格式不正确')
    return
  }
  if (editingId.value) {
    await updateShopItem(editingId.value, form.value)
    ElMessage.success('商品已更新')
  } else {
    await createShopItem(form.value)
    ElMessage.success('商品已创建')
  }
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(row: ShopItem) {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '删除商品', { type: 'warning' })
  await deleteShopItem(row.id)
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
        新增商品
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
      <template #price="{ row }">{{ formatGold(row.price) }}</template>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑商品' : '新增商品'" width="520px">
      <el-form label-width="80px">
        <el-form-item label="商品名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="货币">
          <el-select v-model="form.currency" style="width: 100%">
            <el-option label="金币" value="GOLD" />
            <el-option label="钻石" value="DIAMOND" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发放内容">
          <el-input v-model="payloadText" type="textarea" :rows="4" placeholder='如 {"gold": 1000}' />
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
</style>
