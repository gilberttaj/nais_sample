<template>
  <div class="bg-gray-50">
    <div class="mx-auto p-6 space-y-6">
      <!-- Business Office Information Section -->
      <div class="flex justify-between items-center">
        <!-- Left Side -->
        <div class="flex items-center space-x-3">
          <span class="text-sm font-medium text-gray-700">事業所:</span>
          <span class="bg-yellow-400 text-black px-3 py-1 rounded-full text-sm font-medium">0020</span>
          <span class="text-sm text-gray-900">公共事業部</span>
        </div>

        <!-- Right Side -->
        <div class="flex items-center space-x-3">
          <span class="text-sm font-medium text-gray-700">部署事業所情報:</span>
          <span class="bg-yellow-400 text-black px-3 py-1 rounded-full text-sm font-medium">8527</span>
          <span class="text-sm text-gray-900">千葉県支店情報</span>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex space-x-4">
        <button 
          v-for="button in actionButtons" 
          :key="button.id"
          @click="handleAction(button.action)"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
        >
          {{ button.label }}
        </button>
      </div>

      <!-- Main Output Configuration Table -->
      <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table class="w-full">
          <thead>
            <tr>
              <th class="bg-[#d9ead3] px-4 pt-6 text-center mt-9 text-sm font-medium text-gray-900">
                出力区分
              </th>
              <th class="bg-[#d9ead3] px-4 py-3 text-center text-sm font-medium text-gray-900">
                請求合計表
              </th>
              <th class="bg-[#d9ead3] px-4 py-3 text-center text-sm font-medium text-gray-900">
                品代金振替通知書
              </th>
              <th class="bg-[#d9ead3] px-4 py-3 text-center text-sm font-medium text-gray-900">
                請求合計表
              </th>
              <th class="bg-[#d9ead3] px-4 py-3 text-center text-sm font-medium text-gray-900">
                請求合計表
              </th>
              <th class="bg-[#d9ead3] px-4 py-3 text-center text-sm font-medium text-gray-900">
                請求合計表
              </th>
            </tr>
          </thead>
          <tbody>
            <tr class="">
              <td class="px-4 py-3 text-sm text-gray-900 bg-[#d9ead3]">
                <!-- Empty cell for row header -->
              </td>
              <td class="px-4 py-3 text-sm text-gray-900 text-center">
                1:出力する
              </td>
              <td class="px-4 py-3 text-sm text-gray-900 text-center">
                0:出力しない
              </td>
              <td class="px-4 py-3 text-sm text-gray-900 text-center">
                1:出力する
              </td>
              <td class="px-4 py-3 text-sm text-gray-900 text-center">
                1:出力する
              </td>
              <td class="px-4 py-3 text-sm text-gray-900 text-center">
                1:出力する
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Two Column Status Section -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Invoice Delivery -->
        <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
          <div class="bg-[#d9ead3] px-4 py-3">
            <h3 class="text-sm font-medium text-gray-900">請求書送付</h3>
          </div>
          <div class="px-4 py-3">
            <span class="text-sm text-gray-900">1:不要</span>
          </div>
        </div>

        <!-- Release Status -->
        <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
          <div class="bg-[#d9ead3] px-4 py-3">
            <h3 class="text-sm font-medium text-gray-900">リリース状態</h3>
          </div>
          <div class="px-4 py-3">
            <span class="text-sm text-gray-900">1:テスト運用</span>
          </div>
        </div>
      </div>

      <!-- Destination Management Table -->
      <div class="overflow-hidden rounded-2xl border border-gray-100 bg-white">
        <div class="max-w-full overflow-x-auto">
            <div class="min-w-[1026px]">
            <!-- table header start -->
            <div class="grid grid-cols-11 px-5 py-3 bg-[#d9ead3]">
                <div class="flex items-center col-span-2 ">
                <p class="text-sm font-medium text-gray-700">有効区分</p>
                </div>
                <div class="flex items-center col-span-3">
                <p class="text-sm font-medium text-gray-700">
                    送付先先
                </p>
                </div>
                <div class="flex items-center col-span-2">
                <p class="text-sm font-medium text-gray-700">
                    送付先先
                </p>
                </div>
            </div>
            <!-- table header end -->

            <!-- table body start -->
            <!-- table item -->
            <div class="grid grid-cols-11 border-t border-gray-100 px-5 py-3.5">
                <div class="flex items-center col-span-2">
                <p class="text-gray-500 text-theme-sm">0:有効</p>
                </div>
                <div class="flex items-center col-span-3">
                <p class="text-gray-500 text-theme-sm">
                    aaa@nishuhan.co.jp
                </p>
                </div>
                <div class="flex items-center col-span-2">
                <p class="text-center text-gray-500 text-theme-sm">
                        担当セールス
                </p>
                </div>
            </div>

            <!-- table item -->
            <div class="grid grid-cols-11 border-t border-gray-100 px-5 py-3.5">
                <div class="flex items-center col-span-2">
                <p class="text-gray-500 text-theme-sm">1:無効(テスト運用)</p>
                </div>
                <div class="flex items-center col-span-3">
                <p class="text-gray-500 text-theme-sm">
                    xxx@nishuhan.co.jp
                </p>
                </div>
                <div class="flex items-center col-span-2">
                <p class="text-center text-gray-500 text-theme-sm">
                    先方担当者
                </p>
                </div>
            </div>

            <!-- table item -->
            <div class="grid grid-cols-11 border-t border-gray-100 px-5 py-3.5">
                <div class="flex items-center col-span-2">
                <p class="text-gray-500 text-theme-sm">2:無効(削除)</p>
                </div>
                <div class="flex items-center col-span-3">
                <p class="text-gray-500 text-theme-sm">	zzz@nishuhan.co.jp</p>
                </div>
                <div class="flex items-center col-span-2">
                <p class="text-center text-gray-500 text-theme-sm">
                    2023年3月までの先方担当者
                </p>
                </div>
            </div>
            <!-- table body end -->
            </div>
        </div>
      </div>

      <!-- Status Message -->
      <div 
        v-if="statusMessage" 
        class="mt-4 p-3 rounded-md transition-all duration-300"
        :class="statusClass"
      >
        {{ statusMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Action buttons data
const actionButtons = ref([
  { id: 1, label: '検索-5', action: 'search5' },
  { id: 2, label: '検索-6', action: 'search6' },
  { id: 3, label: '追加-1', action: 'add1' },
  { id: 4, label: '変更-3', action: 'modify3' },
  { id: 5, label: '削除-2', action: 'delete2' }
])

// Destination data
const destinationData = ref([
  {
    status: '0:有効',
    destination1: 'aaa@nishuhan.co.jp',
    destination2: '担当セールス'
  },
  {
    status: '1:無効(テスト運用)',
    destination1: 'xxx@nishuhan.co.jp',
    destination2: '先方担当者'
  },
  {
    status: '2:無効(削除)',
    destination1: 'zzz@nishuhan.co.jp',
    destination2: '2023年3月までの先方担当者'
  }
])

// Status management
const statusMessage = ref('')
const statusType = ref('info')

// Computed properties
const statusClass = computed(() => {
  const baseClasses = 'text-sm font-medium'
  switch (statusType.value) {
    case 'success':
      return `${baseClasses} bg-green-100 text-green-800 border border-green-200`
    case 'error':
      return `${baseClasses} bg-red-100 text-red-800 border border-red-200`
    case 'warning':
      return `${baseClasses} bg-yellow-100 text-yellow-800 border border-yellow-200`
    default:
      return `${baseClasses} bg-blue-100 text-blue-800 border border-blue-200`
  }
})

// Methods
const showStatus = (message, type = 'info') => {
  statusMessage.value = message
  statusType.value = type
  setTimeout(() => {
    statusMessage.value = ''
  }, 3000)
}

const handleAction = (action) => {
  const actionMessages = {
    search5: '検索-5を実行しました',
    search6: '検索-6を実行しました',
    add1: '追加-1を実行しました',
    modify3: '変更-3を実行しました',
    delete2: '削除-2を実行しました'
  }
  
  showStatus(actionMessages[action] || `${action}を実行しました`, 'info')
  console.log(`Action executed: ${action}`)
}
</script>

<style scoped>
/* Ensure table borders are consistent */
table {
  border-collapse: collapse;
}

/* Custom styling for yellow badges */
.bg-yellow-400 {
  background-color: #fbbf24;
}

/* Custom styling for green headers */
.bg-green-200 {
  background-color: #bbf7d0;
}

.bg-green-100 {
  background-color: #dcfce7;
}

/* Hover effects for buttons */
button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

button:active:not(:disabled) {
  transform: translateY(0);
}

/* Smooth transitions */
button {
  transition: all 0.2s ease-in-out;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .flex.justify-between {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .flex.space-x-4 {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
}
</style>