<template>
        <!-- Page Content -->
      <main class="flex-1 overflow-y-auto">
        <!-- Email Master Form (PDF送信マスタ) -->
        <div v-if="activeMenu === 'email-master'" class="p-6" >
          <div class="flex h-full flex-col gap-6 sm:gap-5 xl:flex-row">
            <!-- Blue Header -->
            <div class="bg-blue-400 text-center text-white p-4 rounded-lg mb-6 xl:w-[95%]">
              <h1 class="text-xl font-bold items-center text-white">請求書PDF送信マスタ</h1>
            </div>
          </div>


          <!-- Main Form -->
          <div class="flex h-full flex-col gap-6 sm:gap-5 xl:flex-row">
            <!-- <div class="rounded-2xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-white/[0.03] xl:w-1/5">
              dfdfd
            </div> -->
            <div class="rounded-2xl bg-white border-b border-gray-200 shadow-lg xl:w-[95%]">
              <div class="p-6 mt-2">
                <DetailDisplay/>
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
import DetailDisplay from '@/components/organisms/DetailDisplay.vue'

// Reactive data
const loading = ref(false)
const selectedEmails = ref([])
const activeMenu = ref('email-master')
const searchQuery = ref('')

const currentUser = reactive({
  name: '作業担当',
  lastLogin: new Date('2024-12-01T10:45:00'),
  executionUser: '実行担当'
})

const message = reactive({
  show: false,
  text: '',
  type: 'success'
})

const formData = reactive({
  businessOffice: 'nishuhan',
  salesOfficeCode: '',
  salesOffice: 'A001',
  department: 'B027',
  companyName: '',
  distributionCode: '',
  sendType: '',
  sendSubType: 'MHCV',
  sendFileName: '',
  sendFileSize: '',
  email1: '',
  emailTitle: '',
  sendFilePath: '',
  emailTemplate: '',
  selectedRecipient: ''
})

const emailList = ref([
  'sales@nishuhan.co.jp',
  'support@nishuhan.co.jp',
  'info@nishuhan.co.jp',
  'admin@nishuhan.co.jp',
  'manager@nishuhan.co.jp',
  'director@nishuhan.co.jp',
  'accounting@nishuhan.co.jp',
  'hr@nishuhan.co.jp'
])

// Methods
const setActiveMenu = (menu) => {
  activeMenu.value = menu
}

const getMenuTitle = (menu) => {
  const titles = {
    'email-master': 'メール宛先マスター',
  }
  return titles[menu] || 'ページ'
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    showMessage(`"${searchQuery.value}" で検索しました`, 'success')
    // Implement search logic here
  }
}

const formatDateTime = (date) => {
  return new Intl.DateTimeFormat('ja-JP', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date)
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