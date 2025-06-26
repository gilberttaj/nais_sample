<template>
  <div class="sidebar fixed top-0 left-0 z-9999 flex h-screen w-[290px] flex-col overflow-y-auto border-r border-gray-200 bg-white px-5 transition-all duration-300 lg:static lg:translate-x-0 -translate-x-full">
    <!-- Sidebar Header -->
    <div class="p-6">
      <h1 class="text-md font-semibold text-gray-800">NAISメール宛先マスター</h1>
    </div>

    <!-- Menu Section -->
    <div class="flex-1 p-4">
      <h2 class="text-sm font-medium text-gray-500 mb-4">Menu</h2>
      <nav class="space-y-2">
        <router-link
          v-for="item in menuItems"
          :key="item.key"
          :to="item.url"
          @click.prevent="setActive(item.key)"
          :class="[
            'flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors',
            activeMenu === item.key
              ? 'bg-blue-100 text-blue-700'
              : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
          ]"
        >
          <component :is="item.icon" class="w-5 h-5 mr-3" />
          {{ item.label }}
        </router-link>
      </nav>
    </div>

    <!-- Sidebar Footer -->
    <div class="p-4 border-t border-gray-200">
      <div class="flex items-center">
        <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
          <UserIcon class="w-4 h-4 text-white" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-700">{{ currentUser.name }}</p>
          <p class="text-xs text-gray-500">管理者</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  HomeIcon,
  GridIcon,
  MailIcon,
  UsersIcon,
  SettingsIcon,
  UserIcon
} from 'lucide-vue-next'
import { useRouter } from 'vue-router'

const props = defineProps({
  activeMenu: String,
  currentUser: Object
})
const emits = defineEmits(['update:activeMenu'])

const menuItems = [
  { key: 'email-master', label: 'メール宛先マスター', icon: MailIcon, url: '/'},
]

function setActive(key) {
  emits('update:activeMenu', key)
}
</script>
