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
            @click="handleAction(button.action, idDetail)"
            class="bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 w-full px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 flex items-center justify-center"
          >
            <component :is="button.icon" class="w-4 h-4 mr-2" />
            {{ button.label }}
          </button>
        </div>
      </div>

      <div class="bg-white border border-gray-300 rounded-lg p-6 space-y-6">

        <div class="flex items-center">
          <div class="w-[15%] font-bold">業務ID:</div>
          <div>{{ businessId }} 　 -　  請求合計表　日酒販</div>
          <label class="text-xs font-medium text-gray-700 ml-4"></label>
        </div>
        <hr class="border-orange-100" />

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-3">
          <div class="flex items-center">
            <div class="w-[46%] font-bold">事業所:</div>
            <div>{{ businessOfficeCode }}　 -　 広域卸営業部</div>
          </div>
          <div class="flex items-center ml-[4rem]">
            <div class="w-[46%] font-bold">得意先:</div>
            <div>{{ customerCode }} - 千葉県酒類販売</div>
          </div>
          <div class="flex items-center ml-[9rem]">
            <div class="w-[44%] font-bold">チェーン店:</div>
            <div>{{ chainStoreName }}</div>
          </div>
        </div>
        <hr class="border-orange-100" />

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div class="flex items-center">
            <div class="w-[46%] font-bold">仕入先:</div>
            <div class="whitespace-pre-wrap">{{ supplier }}</div>
          </div>
          <div class="flex items-center ml-[4rem]">
            <div class="w-[46%] font-bold">発注先設者:</div>
            <div class="whitespace-pre-wrap">{{ orderDestination }}</div>
          </div>
        </div>
        <hr class="border-orange-100" />

        <div class="space-y-3">
          <div class="flex items-center">
            <div class="w-[11.9rem] font-bold">拡張振分コード:</div>
            <div>{{ extendedDistributionCode }}</div>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-300 rounded-lg p-6 space-y-6">

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="flex items-center">
            <div class="w-[30%] font-bold">送信先名称:</div>
            <div>{{ destinationName }}</div>
          </div>
          <div class="flex items-center">
            <div class="w-[30%] font-bold">送信モード:</div>
            <div>{{ transmissionMode }}</div>
          </div>
        </div>
        <hr class="border-blue-200" />

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="flex items-center">
            <div class="w-[30%] font-bold">検索ファイル名:</div>
            <div>{{ searchFileName }}</div>
          </div>
          <div class="flex items-center">
            <div class="w-[30%] font-bold">検索ディレクトリ:</div>
            <div>{{ searchDirectory }}</div>
          </div>
        </div>
        <hr class="border-blue-200" />

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="flex items-center">
            <div class="w-[30%] font-bold">送信ディレクトリ:</div>
            <div>{{ transmissionDirectory }}</div>
          </div>
          <div class="flex items-center">
            <div class="w-[30%] font-bold">メールタイトル:</div>
            <div>{{ mailTitle }}</div>
          </div>
        </div>
        <hr class="border-blue-200" />

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="flex items-center">
            <div class="w-[30%] font-bold">本文ファイルパス:</div>
            <div>{{ bodyFilePath }}</div>
          </div>
          <div class="flex items-center">
            <div class="w-[30%] font-bold">添付ファイルパス:</div>
            <div>{{ attachmentFilePath }}</div>
          </div>
        </div>
        <hr class="border-blue-200" />

        <div class="flex items-center">
          <div class="w-[12rem] font-bold">メーリングリスト名:</div>
          <div>{{ mailingListName }}</div>
        </div>
      </div>
      <!-- Destination Management -->
      <div class="bg-white border border-gray-300 rounded-lg">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="px-6 py-3 text-left text-sm font-medium text-gray-500 uppercase tracking-wider">
                  送付宛先
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
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="bg-white border border-gray-300 rounded-lg p-6 space-y-6">
        <div class="flex items-center">
          <div class="w-[12rem] font-bold">自動更新ロック区分:</div>
          <div>{{ autoUpdateLock }}</div>
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
import { Search, Plus, Settings, Trash2, Edit, AlertTriangle, PenIcon } from 'lucide-vue-next'
import { useRoute } from 'vue-router'
import router from '@/router'

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
    email: "aaa@nishuhan.co.jp",
  },
  {
    id: 2,
    email: "xxx@yyyy.co.jp",
  },
  {
    id: 3,
    email: "zzz@yyyy.co.jp"
  },
])

const actionButtons = ref([
  { id: 1, label: "検索-S", action: "searchS", icon: SearchIcon },
  { id: 2, label: "検索-6", action: "search6", icon: SearchIcon },
  { id: 3, label: "追加-1", action: "add1", icon: PlusIcon },
  { id: 4, label: "変更-3", action: "modify3", icon: PenIcon },
  { id: 5, label: "削除-2", action: "delete2", icon: Trash2Icon },
])
// form data (same as in original form)
const businessId = ref('psurm22a');
const businessOfficeCode = ref('0020');
const businessOfficeName = ref('8527');
const customerCode = ref('sdsd');
const chainStoreName = ref('ss');
const supplier = ref('ss');
const orderDestination = ref('ss');
const extendedDistributionCode = ref('1213');
const destinationName = ref('広域卸）千葉県酒類販売');
const transmissionMode = ref('MPDF');
const transmissionModes = ['SMTP', 'FTP'];
const searchFileName = ref('psurm22a_00208527_*.PDF');
const searchDirectory = ref('Z:\\HOSTON\\BSP');
const transmissionDirectory = ref('D:\\Mail_Sender\\SEND');
const mailTitle = ref('請求合計表【日本酒類販売株式会社】');
const bodyFilePath = ref("D:\\Mail_Sender\\MAIL\\file\\MPDF.TXT");
const attachmentFilePath = ref('');
const mailingListName = ref('psurm22a_00208527@nishuhan.co.jp');
const emails = ref(Array(10).fill('aaa@minhuhan.co.jp'));
const autoUpdateLock = ref('0:無効');
const autoUpdateLockOpts = ['あり', 'なし'];

const route = useRoute()

const idDetail = route.params.id

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

const handleAction = (action, idDetail) => {
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
      actionMessages = "追加-3"
      router.push(`/edit/${idDetail}`)
      break
    case "delete2":
      openDeleteDialog({
        id: idDetail,
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