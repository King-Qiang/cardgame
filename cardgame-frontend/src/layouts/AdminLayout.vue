<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useAppStore } from '../stores/app'
import { getAccessibleMenus } from '../router/routes'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)
const menus = computed(() => getAccessibleMenus())

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="aside">
      <div class="logo">{{ appStore.sidebarCollapsed ? '棋' : '棋牌运营后台' }}</div>
      <el-menu :default-active="activeMenu" router :collapse="appStore.sidebarCollapsed">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="appStore.toggleSidebar">☰</el-button>
          <span class="breadcrumb">{{ route.meta.title || '后台管理' }}</span>
        </div>
        <div class="header-right">
          <span>{{ userStore.user?.realName || userStore.user?.username }}</span>
          <el-button type="primary" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: #304156;
  color: #fff;
  transition: width 0.2s;
}

.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb {
  font-size: 16px;
  font-weight: 500;
}

.main {
  background: #f5f7fa;
}
</style>
