import { computed } from 'vue'
import { useUserStore } from '../stores/user'

export function usePermission() {
  const userStore = useUserStore()

  const permissions = computed(() => userStore.permissions)

  function hasPermission(permission?: string): boolean {
    if (!permission) return true
    return permissions.value.includes(permission)
  }

  return { permissions, hasPermission }
}
