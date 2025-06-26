<template>
  <div id="app" class="flex h-screen bg-gray-100">
    <SidebarMenu
      :activeMenu="activeMenu"
      :currentUser="currentUser"
      @update:activeMenu="activeMenu = $event"
    />

    <div class="flex-1 flex flex-col overflow-hidden">
      <TopNavbar
        v-model="searchQuery"
        @search="handleSearch"
        @logout="logout"
      />

      <!-- Main content-->
      <main class="flex-1 p-6 overflow-auto">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import TopNavbar from './components/atoms/TopNavbar.vue'
import SidebarMenu from './components/atoms/SideMenu.vue'

const activeMenu = ref('email-master')
const searchQuery = ref('')

const currentUser = reactive({
  name: '作業担当',
  lastLogin: new Date('2024-12-01T10:45:00'),
  executionUser: '実行担当'
})

function handleSearch() {
  console.log('Searching for:', searchQuery.value)
}

function logout() {
  alert('Logged out')
}
</script>
