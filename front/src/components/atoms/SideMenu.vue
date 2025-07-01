<template>
  <!-- Sidebar -->
  <div class="fixed top-0 left-0 z-40 h-screen w-72 bg-white border-r border-gray-200 transition-transform duration-300 ease-in-out overflow-y-auto">
    <!-- Sidebar Header -->
    <div class="p-6 border-b border-gray-100">
      <div class="flex items-center">
        <div class="w-10 h-10 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-lg flex items-center justify-center mr-3">
          <i class="fas fa-envelope-circle-check text-white text-lg"></i>
        </div>
        <div>
          <h1 class="text-lg font-bold text-gray-800">NAIS</h1>
          <p class="text-xs text-gray-500">メール宛先マスター</p>
        </div>
      </div>
    </div>

    <!-- Sidebar Menu -->
    <div class="p-4 flex-1">
      <div class="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-4">
        メニュー
      </div>
      <nav>
        <router-link
          v-for="item in menuItems"
          :key="item.id"
          :to="item.url"
          @click.native="setActiveMenu(item.id)"
          :class="[
            'flex items-center px-4 py-3 mb-1 rounded-lg text-sm font-medium transition-all duration-200 cursor-pointer',
            activeMenu === item.id 
              ? 'bg-blue-50 text-blue-700' 
              : 'text-gray-500 hover:bg-gray-100 hover:text-gray-700'
          ]"
        >
          <i :class="`${item.icon} w-5 mr-3 text-center`"></i>
          {{ item.label }}
        </router-link>
      </nav>

      <div class="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-4 mt-8">
        設定
      </div>
      <nav>
        <router-link
          v-for="item in settingsItems"
          :key="item.id"
          :to="item.url"
          @click.native="setActiveMenu(item.id)"
          :class="[
            'flex items-center px-4 py-3 mb-1 rounded-lg text-sm font-medium transition-all duration-200 cursor-pointer',
            activeMenu === item.id 
              ? 'bg-blue-50 text-blue-700' 
              : 'text-gray-500 hover:bg-gray-100 hover:text-gray-700'
          ]"
        >
          <i :class="`${item.icon} w-5 mr-3 text-center`"></i>
          {{ item.label }}
        </router-link>
      </nav>
    </div>

    <!-- Sidebar Footer -->
    <div class="p-4 border-t border-gray-100">
      <div class="flex items-center">
        <div class="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
          <i class="fas fa-user text-white text-sm"></i>
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-700">{{ currentUser.name }}</p>
          <p class="text-xs text-gray-500">{{ currentUser.executionUser }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  activeMenu: {
    type: String,
    default: 'dashboard'
  },
  currentUser: {
    type: Object,
    default: () => ({
      name: '作業担当',
      executionUser: '実行担当'
    })
  }
})

const emits = defineEmits(['update:activeMenu'])

const menuItems = ref([
  { id: 'dashboard', label: 'ダッシュボード', icon: 'fas fa-home', url: '/' },
  { id: 'email-master', label: 'メール宛先マスター', icon: 'fas fa-envelope', url: '/list' },
  { id: 'list', label: '宛先一覧', icon: 'fas fa-list', url: '/address' },
  { id: 'new', label: '新規登録', icon: 'fas fa-plus-circle', url: '/register' },
  { id: 'search', label: '検索', icon: 'fas fa-search', url: '/search' }
])

const settingsItems = ref([
  { id: 'settings', label: 'システム設定', icon: 'fas fa-cog', url: '/settings' },
  { id: 'users', label: 'ユーザー管理', icon: 'fas fa-users', url: '/users' },
  { id: 'reports', label: 'レポート', icon: 'fas fa-chart-bar', url: '/reports' }
])

function setActiveMenu(menuId) {
  emits('update:activeMenu', menuId)
  console.log(`Navigate to: ${menuId}`)
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

* {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>