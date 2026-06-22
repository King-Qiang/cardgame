import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
import { getToken, hasPermission } from '../utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const isPublic = to.meta.public === true
  const token = getToken()

  if (to.path === '/login') {
    return token ? '/' : true
  }

  if (!isPublic && !token) {
    return '/login'
  }

  const permission = to.meta.permission as string | undefined
  if (permission && !hasPermission(permission)) {
    return '/403'
  }

  return true
})

export default router
