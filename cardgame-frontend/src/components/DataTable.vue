<script setup lang="ts">
export interface TableColumn {
  prop?: string
  label: string
  width?: string | number
  minWidth?: string | number
  slot?: string
}

defineProps<{
  columns: TableColumn[]
  data: unknown[]
  total: number
  loading?: boolean
  page: number
  pageSize: number
}>()

const emit = defineEmits<{
  'page-change': [page: number]
  'size-change': [size: number]
}>()
</script>

<template>
  <div class="data-table">
    <el-table v-loading="loading" :data="data" border stripe empty-text="暂无数据">
      <el-table-column
        v-for="col in columns"
        :key="col.prop || col.label"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
      >
        <template v-if="col.slot" #default="scope">
          <slot :name="col.slot" v-bind="scope" />
        </template>
      </el-table-column>
      <el-table-column v-if="$slots.actions" label="操作" width="180" fixed="right">
        <template #default="scope">
          <slot name="actions" v-bind="scope" />
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :current-page="page"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="emit('page-change', $event)"
        @size-change="emit('size-change', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
