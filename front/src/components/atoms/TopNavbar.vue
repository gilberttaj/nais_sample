<template>
  <!-- Top Navigation -->
  <div class="pl-72 w-full fixed top-0 right-0 z-30 bg-white border-b border-gray-200 h-16 flex items-center justify-between px-6 shadow-sm transition-all duration-300">
    <div class="flex items-center">
      <button 
        class="lg:hidden p-2 rounded-md hover:bg-gray-100 transition-colors duration-200"
        @click="toggleSidebar"
      >
        <i class="fas fa-bars text-gray-600"></i>
      </button>
      <h2 class="text-lg font-semibold text-gray-800 ml-2">{{ pageTitle }}</h2>
    </div>
    
    <!-- Search Bar -->
    <div class="flex-1 max-w-md mx-8">
      <div class="relative">
        <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
        <input 
          type="text" 
          :value="modelValue"
          @input="$emit('update:modelValue', $event.target.value)"
          @keyup.enter="$emit('search')"
          placeholder="検索..." 
          class="w-full pl-10 pr-4 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>
    </div>
    
    <div class="flex items-center space-x-4">
      <button 
        class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-gradient-to-br from-[#667eea] to-[#764ba2] shadow-md hover:-translate-y-0.5 hover:shadow-lg"
        @click="router.push('/register')"
      >
        <i class="fas fa-plus mr-2"></i>
        新規登録
      </button>
      <button 
        class="inline-flex items-center justify-center px-3 py-1.5 text-xs font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50"
        @click="$emit('logout')"
      >
        <i class="fas fa-sign-out-alt mr-2"></i>
        ログアウト
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import router from '@/router'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  activeMenu: {
    type: String,
    default: 'email-master'
  }
})

const emits = defineEmits(['update:modelValue', 'search', 'logout'])

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
  return menuLabels[props.activeMenu] || 'ダッシュボード'
})

function toggleSidebar() {
  console.log('Toggle sidebar')
}

function createNew() {
  console.log('Create new')
  alert('新規登録画面に移動します')
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

* {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>