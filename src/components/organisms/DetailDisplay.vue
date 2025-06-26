<template>
  <div class="">
    <div class="mx-auto px-6 pb-6 space-y-6">
      <!-- Business Office Information -->
      <div class="bg-white border border-gray-300 rounded-lg p-6">
        <div class="flex flex-col md:flex-row md:justify-between md:items-center gap-4">
          <div class="flex items-center space-x-3">
            <span class="text-sm font-medium text-gray-600">事業所:</span>
            <span class="bg-yellow-100 text-yellow-800 hover:bg-yellow-200 font-medium px-2 py-1 rounded-md text-sm">0020</span>
            <span class="text-sm text-gray-900">広域営業部</span>
          </div>
          <div class="flex items-center space-x-3">
            <span class="text-sm font-medium text-gray-600">請求書集約得意先:</span>
            <span class="bg-yellow-100 text-yellow-800 hover:bg-yellow-200 font-medium px-2 py-1 rounded-md text-sm">8527</span>
            <span class="text-sm text-gray-900">千葉県酒類販売</span>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="bg-white">
        <div class="grid grid-cols-5 gap-4 w-full">
          <button
            v-for="button in actionButtons"
            :key="button.id"
            @click="handleAction(button.action)"
            class="bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 w-full px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 flex items-center justify-center"
          >
            <component :is="button.icon" class="w-4 h-4 mr-2" />
            {{ button.label }}
          </button>
        </div>
      </div>

      <!-- Output Configuration -->
      <div class="bg-white border border-gray-300 rounded-lg">
        <div class="overflow-x-auto rounded-lg">
          <div class="flex">
            <!-- Left side - 出力区分 -->
            <div class="flex-shrink-0 w-32 bg-gray-50 border-r border-gray-200 flex items-center justify-center rounded-lg">
              <span class="text-sm font-medium text-gray-700">出力区分</span>
            </div>

            <!-- Right side - Main table -->
            <div class="flex-1">
              <table class="w-full ">
                <thead>
                  <tr class="bg-gray-50 rounded-lg">
                    <th class="px-6 py-3 text-center text-sm font-medium text-gray-500 uppercase tracking-wider">
                      請求合計表
                    </th>
                    <th class="px-6 py-3 text-center text-sm font-medium text-gray-500 uppercase tracking-wider">
                      品代金振替通知書
                    </th>
                    <th class="px-6 py-3 text-center text-sm font-medium text-gray-500 uppercase tracking-wider">
                      請求明細書
                    </th>
                    <th class="px-6 py-3 text-center text-sm font-medium text-gray-500 uppercase tracking-wider">
                      客先買入明細
                    </th>
                    <th class="px-6 py-3 text-center text-sm font-medium text-gray-500 uppercase tracking-wider">
                      割戻金計算書
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200 rounded-lg">
                  <tr>
                    <td class="px-6 py-4 whitespace-nowrap text-center text-sm text-gray-900">1:出力する</td>
                    <td class="px-6 py-4 whitespace-nowrap text-center text-sm text-gray-900">0:出力しない</td>
                    <td class="px-6 py-4 whitespace-nowrap text-center text-sm text-gray-900">1:出力する</td>
                    <td class="px-6 py-4 whitespace-nowrap text-center text-sm text-gray-900">1:出力する</td>
                    <td class="px-6 py-4 whitespace-nowrap text-center text-sm text-gray-900">1:出力する</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Status Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white border border-gray-300 rounded-lg">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-900">請求書送付</h3>
          </div>
          <div class="px-6 py-4">
            <span class="text-sm text-gray-900">1:不要</span>
          </div>
        </div>

        <div class="bg-white border border-gray-300 rounded-lg">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-900">リリース状態</h3>
          </div>
          <div class="px-6 py-4">
            <span class="text-sm text-gray-900">1:テスト運用</span>
          </div>
        </div>
      </div>

      <!-- Destination Management -->
      <div class="bg-white border border-gray-300 rounded-lg">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="px-6 py-3 text-left text-sm font-medium text-gray-500 uppercase tracking-wider">
                  有効区分
                </th>
                <th class="px-6 py-3 text-left text-sm font-medium text-gray-500 uppercase tracking-wider">
                  送付先
                </th>
                <th class="px-6 py-3 text-left text-sm font-medium text-gray-500 uppercase tracking-wider">
                  役割
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              <tr 
                v-for="item in destinationData" 
                :key="item.id" 
                class="hover:bg-gray-50 transition-colors"
              >
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="getStatusBadgeClass(item.statusType)">
                    {{ item.status }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-8 w-8">
                      <div class="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center">
                        <span class="text-sm font-medium text-blue-600">
                          {{ item.email.charAt(0).toUpperCase() }}
                        </span>
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{{ item.email }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.role }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Status Message -->
      <div 
        v-if="statusMessage" 
        :class="getStatusMessageClass()"
        class="p-4 rounded-lg border transition-all duration-300"
      >
        <div class="flex items-center">
          <AlertTriangleIcon class="w-5 h-5 mr-2" />
          <span class="text-sm font-medium">{{ statusMessage }}</span>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Dialog -->
    <div 
      v-if="dialogVisible" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click="dialogVisible = false"
    >
      <div 
        class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4"
        @click.stop
      >
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-red-600 flex items-center">
            <AlertTriangleIcon class="w-5 h-5 mr-2" />
            削除の確認
          </h3>
          <p class="text-sm text-gray-600 mt-2">
            以下の送付先を削除してもよろしいですか？この操作は取り消せません。
          </p>
        </div>
        
        <div class="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
          <button
            @click="dialogVisible = false"
            class="bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 px-4 py-2 rounded-md text-sm font-medium transition-colors"
          >
            キャンセル
          </button>
          <button
            @click="confirmDelete"
            class="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
          >
            削除する
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Plus, Settings, Trash2, Edit, AlertTriangle } from 'lucide-vue-next'

// Icons mapping
const SearchIcon = Search
const PlusIcon = Plus
const SettingsIcon = Settings
const Trash2Icon = Trash2
const EditIcon = Edit
const AlertTriangleIcon = AlertTriangle

// Reactive data
const dialogVisible = ref(false)
const selectedItem = ref(null)
const statusMessage = ref('')
const statusType = ref('info')

const destinationData = ref([
  {
    id: 1,
    status: "0:有効",
    email: "aaa@nishuhan.co.jp",
    role: "担当セールス",
    statusType: "active",
  },
  {
    id: 2,
    status: "1:無効(テスト運用)",
    email: "xxx@yyyy.co.jp",
    role: "先方担当者",
    statusType: "test",
  },
  {
    id: 3,
    status: "2:無効(削除)",
    email: "zzz@yyyy.co.jp",
    role: "2023年3月までの先方担当者",
    statusType: "deleted",
  },
])

const actionButtons = ref([
  { id: 1, label: "検索-S", action: "searchS", icon: SearchIcon },
  { id: 2, label: "検索-6", action: "search6", icon: SearchIcon },
  { id: 3, label: "追加-1", action: "add1", icon: PlusIcon },
  { id: 4, label: "変更-3", action: "modify3", icon: SettingsIcon },
  { id: 5, label: "削除-2", action: "delete2", icon: Trash2Icon },
])

// Methods
const openDeleteDialog = (item) => {
  selectedItem.value = item
  dialogVisible.value = true
}

const confirmDelete = () => {
  if (selectedItem.value) {
    destinationData.value = destinationData.value.filter(item => item.id !== selectedItem.value.id)
    showStatus(`${selectedItem.value.email} を削除しました`, "success")
    dialogVisible.value = false
    selectedItem.value = null
  }
}

const editItem = (item) => {
  showStatus(`${item.email} の編集画面に移動します`, "info")
  console.log("Edit item:", item.id)
}

const showStatus = (message, type = "info") => {
  statusMessage.value = message
  statusType.value = type
  setTimeout(() => {
    statusMessage.value = ""
  }, 3000)
}

const handleAction = (action) => {
  let actionMessages;
  switch (action) {
    case "searchS":
      actionMessages = "検索-Sを実行しました"
      break
    case "search6":
      actionMessages = "検索-6を実行しました"
      break
    case "add1":
      actionMessages = "追加-1を実行しました"
      break
    case "modify3":
      editItem({
        id: 3,
        status: "1:無効(テスト運用)",
        email: ""})
      break
    case "delete2":
      openDeleteDialog({
        id: 4,
        status: "0:有効",
        email: ""})
      break
  }

  showStatus(actionMessages[action] || `${action}を実行しました`, "info")
}

const getStatusBadgeClass = (statusType) => {
  const baseClasses = "px-2 py-1 rounded-md text-sm font-medium"
  switch (statusType) {
    case "active":
      return `${baseClasses} bg-green-100 text-green-800`
    case "test":
      return `${baseClasses} bg-orange-100 text-orange-800`
    case "deleted":
      return `${baseClasses} bg-red-100 text-red-800`
    default:
      return `${baseClasses} bg-gray-100 text-gray-800 border border-gray-300`
  }
}

const getStatusMessageClass = () => {
  switch (statusType.value) {
    case "success":
      return "bg-green-50 text-green-800 border-green-200"
    case "error":
      return "bg-red-50 text-red-800 border-red-200"
    default:
      return "bg-blue-50 text-blue-800 border-blue-200"
  }
}
</script>

<style scoped>
/* Custom styles for better appearance */
.transition-colors {
  transition: background-color 0.2s ease-in-out, color 0.2s ease-in-out;
}

/* Ensure table borders are consistent */
table {
  border-collapse: collapse;
}

/* Hover effects for buttons */
button:hover:not(:disabled) {
  transform: translateY(-1px);
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
  .grid-cols-5 {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
  }
  
  .grid-cols-5 button:last-child {
    grid-column: span 2;
  }
}
</style>