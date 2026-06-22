export interface PageParams {
  page: number
  pageSize: number
  [key: string]: unknown
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface SearchField {
  prop: string
  label: string
  type: 'input' | 'select' | 'date-range'
  placeholder?: string
  options?: { label: string; value: string }[]
}
