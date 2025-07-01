<template>
  <div id="app" class="flex h-screen bg-gray-100">
    <SidebarMenu
      v-if="!isAuthPage"
      :activeMenu="activeMenu"
      :currentUser="currentUser"
      @update:activeMenu="activeMenu = $event"
    />
    
    <div class="flex-1 flex flex-col overflow-hidden">
      <TopNavbar
        v-if="!isAuthPage"
        v-model="searchQuery"
        :activeMenu="activeMenu"
        @search="handleSearch"
        @logout="logout"
      />
      
      <!-- Main content -->
      <main :class="[
        'flex-1 overflow-auto transition-all duration-300',
        !isAuthPage ? 'ml-72 mt-16' : ''
      ]">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import TopNavbar from './components/atoms/TopNavbar.vue'
import SidebarMenu from './components/atoms/SideMenu.vue'

// Simulate route checking - replace with actual vue-router logic
const isAuthPage = ref(false)

const activeMenu = ref('dashboard')
const searchQuery = ref('')

const currentUser = reactive({
  name: '作業担当',
  lastLogin: new Date('2024-12-01T10:45:00'),
  executionUser: '実行担当'
})

const menuLabels = {
  'dashboard': 'ダッシュボード',
  'email-master': 'メール宛先マスター',
  'list': '宛先一覧',
  'new': '新規登録',
  'search': '検索',
  'settings': 'システム設定',
  'users': 'ユーザー管理',
  'reports': 'レポート'
}

const pageTitle = computed(() => {
  return menuLabels[activeMenu.value] || 'ダッシュボード'
})

function handleSearch() {
  console.log('Searching for:', searchQuery.value)
  alert(`検索中: ${searchQuery.value}`)
}

function logout() {
  console.log('Logout')
  alert('ログアウトしました')
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

* {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>