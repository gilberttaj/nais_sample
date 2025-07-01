<template>
        <!-- Page Content -->
      <main class="flex-1 overflow-y-auto">
        <!-- Email Master Form (PDF送信マスタ) -->
        <div v-if="activeMenu === 'email-master'" class="p-6" >
          <!-- Main Form -->
          <div class="flex h-full flex-col gap-6 sm:gap-5 xl:flex-row">
            <!-- <div class="rounded-2xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-white/[0.03] xl:w-1/5">
              dfdfd
            </div> -->
            <div class="xl:w-[93%] mx-auto">
              <div class="p-6 mt-2">
                <RegisterConfirmation/>
              </div>
            </div>
          </div>
        </div>

        <!-- Other Menu Items -->
        <div v-else class="p-6">
          <div class="text-center py-12">
            <h2 class="text-2xl font-bold text-gray-900 mb-4">{{ getMenuTitle(activeMenu) }}</h2>
            <p class="text-gray-600">この機能は開発中です。</p>
          </div>
        </div>
      </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import RegisterConfirmation from '@/components/organisms/RegisterConfirmation.vue'

// Reactive data
const loading = ref(false)
const selectedEmails = ref([])
const activeMenu = ref('email-master')

// Try scrolling the window
  window.scrollTo({ top: 0, behavior: 'auto' });
  // Also try scrolling the document element and body
  document.documentElement.scrollTop = 0;
  document.body.scrollTop = 0;

// Methods
const setActiveMenu = (menu) => {
  activeMenu.value = menu
}

const getMenuTitle = (menu) => {
  const titles = {
    'email-master': 'メール宛先マスター'
  }
  return titles[menu] || 'ページ'
}
</script>

<style scoped>
/* Custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Focus styles */
input:focus, select:focus, textarea:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* Smooth transitions */
.transition-colors {
  transition: background-color 0.2s ease, color 0.2s ease;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .grid-cols-2 {
    grid-template-columns: 1fr;
  }
}
</style>