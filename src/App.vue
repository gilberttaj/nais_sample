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
        @search="handleSearch"
        @logout="logout"
      />

      <!-- Main content-->
      <main class="flex-1 overflow-auto">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import TopNavbar from './components/atoms/TopNavbar.vue'
import SidebarMenu from './components/atoms/SideMenu.vue'

const route = useRoute()

const isAuthPage = ref(false)
watchEffect(() => {
  // This will reactively update on route change
  isAuthPage.value = ['SignIn', 'SignUp', 'AuthValidation'].includes(route.name)
})

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
