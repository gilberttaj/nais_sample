<template>
  <div v-if="activeMenu === 'dashboard'" class="p-6" >
    <!-- Main Content -->
    <div :class="[
      'transition-all duration-300',
      sidebarVisible ? 'ml-0' : 'ml-0'
    ]">
      <div class="p-4">
        
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div 
            v-for="stat in stats" 
            :key="stat.id"
            :class="`${stat.gradient} text-white rounded-xl p-5 text-center transform hover:scale-105 transition-transform duration-200 cursor-pointer`"
            @click="viewStat(stat.id)"
          >
            <div class="text-3xl font-bold mb-1">{{ stat.value }}</div>
            <div class="text-sm opacity-90">{{ stat.label }}</div>
          </div>
        </div>

        <!-- Search Filter Card -->
        <div class="bg-white rounded-2xl shadow-lg border border-gray-200 p-6 mb-6">
          <div class="flex flex-col lg:flex-row justify-between items-start lg:items-center mb-6">
            <h3 class="text-lg font-semibold text-gray-800 mb-4 lg:mb-0">
              <i class="fas fa-filter mr-2"></i>
              検索・フィルター
            </h3>
            <div class="flex gap-2">
              <button 
                class="inline-flex items-center justify-center px-3 py-1.5 text-xs font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50 hover:-translate-y-0.5"
                @click="exportData"
              >
                <i class="fas fa-download mr-2"></i>
                CSV出力
              </button>
              <button 
                class="inline-flex items-center justify-center px-3 py-1.5 text-xs font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50 hover:-translate-y-0.5"
                @click="printData"
              >
                <i class="fas fa-print mr-2"></i>
                印刷
              </button>
            </div>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <!-- Search Input -->
            <div class="relative">
              <i class="fas fa-search absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-500"></i>
              <input 
                type="text" 
                v-model="filters.search"
                placeholder="業務ID、業務名で検索..." 
                class="w-full pl-11 pr-4 py-3 text-sm text-gray-900 bg-white border-2 border-gray-200 rounded-xl transition-all duration-200 shadow-sm focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100"
                @input="applyFilters"
              />
            </div>
            
            <!-- Filter Selects -->
            <select 
              v-model="filters.office" 
              class="px-4 py-3 text-sm text-gray-900 bg-white border-2 border-gray-200 rounded-xl transition-all duration-200 shadow-sm focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100"
              @change="applyFilters"
            >
              <option value="">事業所で絞り込み</option>
              <option value="tokyo">東京営業部</option>
              <option value="osaka">大阪営業部</option>
              <option value="kyushu">九州営業部</option>
            </select>

            <select 
              v-model="filters.status" 
              class="px-4 py-3 text-sm text-gray-900 bg-white border-2 border-gray-200 rounded-xl transition-all duration-200 shadow-sm focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100"
              @change="applyFilters"
            >
              <option value="">ステータスで絞り込み</option>
              <option value="active">アクティブ</option>
              <option value="inactive">非アクティブ</option>
            </select>

            <select 
              v-model="filters.type" 
              class="px-4 py-3 text-sm text-gray-900 bg-white border-2 border-gray-200 rounded-xl transition-all duration-200 shadow-sm focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100"
              @change="applyFilters"
            >
              <option value="">タイプで絞り込み</option>
              <option value="MPDF">MPDF</option>
              <option value="TXT">TXT</option>
              <option value="ZIP">ZIP</option>
            </select>
          </div>
        </div>

        <!-- Data Table -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-6 mb-6">
          <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6">
            <div>
              <h3 class="text-lg font-semibold text-gray-800">登録データ一覧</h3>
              <p class="text-sm text-gray-500 mt-1">
                全{{ filteredData.length }}件中 {{ paginationInfo.start }}-{{ paginationInfo.end }} 件を表示
              </p>
            </div>
            <div class="flex items-center gap-2 mt-4 sm:mt-0">
              <button 
                class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-gradient-to-br from-[#667eea] to-[#764ba2] shadow-md hover:-translate-y-0.5 hover:shadow-lg"
                @click="router.push('/register')"
              >
                <i class="fas fa-plus mr-2"></i>
                新規登録
              </button>
              <span class="text-sm text-gray-500">表示件数:</span>
              <select 
                v-model="pagination.perPage" 
                class="px-3 py-2 text-sm border-2 border-gray-200 rounded-lg focus:border-indigo-500 focus:outline-none"
                @change="updatePagination"
              >
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
              </select>
            </div>
          </div>

          <div class="overflow-x-auto rounded-xl">
            <table class="w-full border-collapse bg-white rounded-xl overflow-hidden shadow-sm">
              <thead>
                <tr>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">
                    <input 
                      type="checkbox" 
                      class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                      :checked="allSelected"
                      @change="toggleSelectAll"
                    >
                  </th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">業務ID</th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">業務名</th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">事業所名</th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">得意先名</th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">送信モード</th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">ステータス</th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">最終更新</th>
                  <th class="bg-slate-50 px-3 py-4 text-left font-semibold text-sm text-gray-700 border-b border-gray-200">アクション</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="item in paginatedData" 
                  :key="item.id"
                  @click="router.push(`/detail/${item.id}`)"
                  class="hover:bg-slate-50 transition-colors duration-200"
                >
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">
                    <input 
                      type="checkbox" 
                      class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                      v-model="selectedItems"
                      :value="item.id"
                    >
                  </td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">
                    <span class="font-mono font-semibold">{{ item.businessId }}</span>
                  </td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">{{ item.businessName }}</td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">{{ item.officeName }}</td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">{{ item.clientName }}</td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">
                    <span :class="getSendModeClass(item.sendMode)">
                      <i :class="getSendModeIcon(item.sendMode)" class="mr-1"></i>
                      {{ item.sendMode }}
                    </span>
                  </td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">
                    <span :class="[
                      'inline-flex items-center px-3 py-1 text-xs font-semibold rounded-full uppercase tracking-wide border',
                      item.isActive 
                        ? 'bg-green-100 text-green-600 border-green-200' 
                        : 'bg-red-100 text-red-600 border-red-200'
                    ]">
                      {{ item.isActive ? 'アクティブ' : '非アクティブ' }}
                    </span>
                  </td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">{{ formatDate(item.lastUpdated) }}</td>
                  <td class="px-3 py-4 border-b border-gray-100 text-sm text-gray-900">
                    <div class="flex gap-2">
                      <button 
                        class="inline-flex items-center justify-center px-2 py-1.5 text-xs font-medium rounded-lg transition-all duration-200 bg-emerald-500 text-white shadow-sm hover:bg-emerald-600 hover:-translate-y-0.5"
                        @click.stop="viewDetail(item.id)"
                        title="詳細表示"
                      >
                        <i class="fas fa-eye"></i>
                      </button>
                      <button 
                        class="inline-flex items-center justify-center px-2 py-1.5 text-xs font-medium rounded-lg transition-all duration-200 bg-gradient-to-br from-[#667eea] to-[#764ba2] text-white shadow-sm hover:-translate-y-0.5"
                        @click.stop="editItem(item.id)"
                        title="編集"
                      >
                        <i class="fas fa-edit"></i>
                      </button>
                      <button 
                        class="inline-flex items-center justify-center px-2 py-1.5 text-xs font-medium rounded-lg transition-all duration-200 bg-red-500 text-white shadow-sm hover:bg-red-600 hover:-translate-y-0.5"
                        @click.stop="deleteItem(item.id)"
                        title="削除"
                      >
                        <i class="fas fa-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6">
            <div class="text-gray-500 text-sm">
              全{{ filteredData.length }}件中 {{ paginationInfo.start }}-{{ paginationInfo.end }} 件を表示
            </div>
            <div class="flex items-center gap-1">
              <button 
                class="px-3 py-2 text-sm text-gray-500 bg-white border border-gray-200 rounded-md transition-all duration-200 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                @click="changePage(pagination.currentPage - 1)"
                :disabled="pagination.currentPage === 1"
              >
                <i class="fas fa-chevron-left"></i>
              </button>
              <button 
                v-for="page in visiblePages" 
                :key="page"
                :class="[
                  'px-3 py-2 text-sm border rounded-md transition-all duration-200',
                  page === pagination.currentPage 
                    ? 'bg-indigo-500 text-white border-indigo-500' 
                    : 'text-gray-500 bg-white border-gray-200 hover:bg-gray-100'
                ]"
                @click="changePage(page)"
              >
                {{ page }}
              </button>
              <span v-if="showEllipsis" class="px-2 text-gray-500">...</span>
              <button 
                class="px-3 py-2 text-sm text-gray-500 bg-white border border-gray-200 rounded-md transition-all duration-200 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                @click="changePage(pagination.currentPage + 1)"
                :disabled="pagination.currentPage === totalPages"
              >
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- Bulk Actions -->
        <div class="bg-white rounded-2xl shadow-lg border border-gray-200 p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">
            <i class="fas fa-tasks mr-2"></i>
            一括操作
          </h3>
          <div class="flex flex-wrap gap-3">
            <button 
              class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-emerald-500 shadow-md hover:bg-emerald-600 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="bulkActivate"
              :disabled="selectedItems.length === 0"
            >
              <i class="fas fa-check mr-2"></i>
              選択項目を有効化
            </button>
            <button 
              class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-amber-500 shadow-md hover:bg-amber-600 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="bulkDeactivate"
              :disabled="selectedItems.length === 0"
            >
              <i class="fas fa-pause mr-2"></i>
              選択項目を無効化
            </button>
            <button 
              class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-red-500 shadow-md hover:bg-red-600 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="bulkDelete"
              :disabled="selectedItems.length === 0"
            >
              <i class="fas fa-trash mr-2"></i>
              選択項目を削除
            </button>
            <button 
              class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50 hover:border-gray-300 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="bulkExport"
              :disabled="selectedItems.length === 0"
            >
              <i class="fas fa-download mr-2"></i>
              選択項目をエクスポート
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import router from '@/router'

interface MenuItem {
  id: string
  label: string
  icon: string
}

interface User {
  name: string
  role: string
}

interface Stat {
  id: string
  label: string
  value: number
  gradient: string
}

interface DataItem {
  id: string
  businessId: string
  businessName: string
  office: string
  status: 'active' | 'inactive'
}

interface Filters {
  search: string
  office: string
  status: string
  type: string
}

interface Pagination {
  currentPage: number
  perPage: number
}

interface Stat {
  id: string
  label: string
  value: number
  gradient: string
}

const sidebarVisible = ref(true)
const activeMenu = ref('dashboard')
const selectedItems = ref<string[]>([])

const filters = reactive({
  search: '',
  office: '',
  sendMode: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  perPage: 10
})

const stats = ref<Stat[]>([
  { id: 'total', label: '総登録数', value: 156, gradient: 'bg-gradient-to-br from-[#667eea] to-[#764ba2]' },
  { id: 'active', label: 'アクティブ', value: 142, gradient: 'bg-gradient-to-br from-[#10b981] to-[#059669]' },
  { id: 'inactive', label: '非アクティブ', value: 14, gradient: 'bg-gradient-to-br from-[#f59e0b] to-[#d97706]' },
  { id: 'updated', label: '今月更新', value: 23, gradient: 'bg-gradient-to-br from-[#8b5cf6] to-[#7c3aed]' }
])


const menuItems = ref([
  { id: 'dashboard', label: 'ダッシュボード', icon: 'fas fa-home' },
  { id: 'email-master', label: 'メール宛先マスター', icon: 'fas fa-envelope' },
  { id: 'list', label: '宛先一覧', icon: 'fas fa-list' },
  { id: 'new', label: '新規登録', icon: 'fas fa-plus-circle' },
  { id: 'search', label: '検索', icon: 'fas fa-search' }
])

const settingsItems = ref([
  { id: 'settings', label: 'システム設定', icon: 'fas fa-cog' },
  { id: 'users', label: 'ユーザー管理', icon: 'fas fa-users' },
  { id: 'reports', label: 'レポート', icon: 'fas fa-chart-bar' }
])

// Updated email data to match the original HTML exactly
const emailData = ref([
  {
    id: '1',
    businessId: 'psurm22a',
    businessName: '請求合計表 日酒販',
    officeName: '広域卸営業部',
    clientName: '千葉県酒類販売',
    sendMode: 'MPDF',
    isActive: true,
    lastUpdated: new Date('2024-01-15')
  },
  {
    id: '2',
    businessId: 'psurm23b',
    businessName: '請求書送付 東京支店',
    officeName: '東京営業部',
    clientName: '東京酒類卸売',
    sendMode: 'ZIP',
    isActive: true,
    lastUpdated: new Date('2024-01-14')
  },
  {
    id: '3',
    businessId: 'psurm24c',
    businessName: '月次レポート 大阪',
    officeName: '大阪営業部',
    clientName: '関西酒類販売',
    sendMode: 'TXT',
    isActive: false,
    lastUpdated: new Date('2024-01-13')
  },
  {
    id: '4',
    businessId: 'psurm25d',
    businessName: '在庫報告書 九州',
    officeName: '九州営業部',
    clientName: '九州酒類流通',
    sendMode: 'MPDF',
    isActive: true,
    lastUpdated: new Date('2024-01-12')
  },
  {
    id: '5',
    businessId: 'psurm26e',
    businessName: '売上実績 北海道',
    officeName: '北海道営業部',
    clientName: '北海道酒類商社',
    sendMode: 'ZIP',
    isActive: true,
    lastUpdated: new Date('2024-01-11')
  }
])

const filteredData = computed(() => {
  let data = emailData.value

  if (filters.search) {
    data = data.filter(item => 
      item.businessId.toLowerCase().includes(filters.search.toLowerCase()) ||
      item.businessName.toLowerCase().includes(filters.search.toLowerCase())
    )
  }

  if (filters.office) {
    data = data.filter(item => item.officeName.includes(filters.office))
  }

  if (filters.sendMode) {
    data = data.filter(item => item.sendMode === filters.sendMode)
  }

  if (filters.status) {
    const isActive = filters.status === 'active'
    data = data.filter(item => item.isActive === isActive)
  }

  return data
})

const totalPages = computed(() => Math.ceil(filteredData.value.length / pagination.perPage))

const paginatedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.perPage
  const end = start + pagination.perPage
  return filteredData.value.slice(start, end)
})

const paginationInfo = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.perPage + 1
  const end = Math.min(start + pagination.perPage - 1, filteredData.value.length)
  return { start, end }
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, pagination.currentPage - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const showEllipsis = computed(() => {
  return totalPages.value > 5 && pagination.currentPage < totalPages.value - 2
})

const allSelected = computed(() => {
  return paginatedData.value.length > 0 && selectedItems.value.length === paginatedData.value.length
})

const toggleSidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
}

const setActiveMenu = (menuId) => {
  activeMenu.value = menuId
  console.log(`Navigate to: ${menuId}`)
}

const createNew = () => {
  console.log('Create new record')
  alert('新規登録画面に移動します')
}

const logout = () => {
  if (confirm('ログアウトしますか？')) {
    console.log('Logout')
    alert('ログアウトしました')
  }
}

const exportCSV = () => {
  console.log('Export CSV')
  alert('CSV出力を開始します')
}

const printList = () => {
  window.print()
}

const applyFilters = () => {
  pagination.currentPage = 1
}

const updatePagination = () => {
  pagination.currentPage = 1
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    pagination.currentPage = page
  }
}

const viewItem = (id: string) => {
  console.log(`View item: ${id}`)
  alert(`詳細表示: ${id}`)
}

const editItem = (id: string) => {
  console.log(`Edit item: ${id}`)
  alert(`編集: ${id}`)
}

const deleteItem = (id: string) => {
  if (confirm('本当に削除しますか？')) {
    console.log(`Delete item: ${id}`)
    alert(`削除しました: ${id}`)
  }
}

const bulkActivate = () => {
  if (confirm(`選択した${selectedItems.value.length}件を有効化しますか？`)) {
    console.log('Bulk activate:', selectedItems.value)
    alert('選択項目を有効化しました')
    selectedItems.value = []
  }
}

const bulkDeactivate = () => {
  if (confirm(`選択した${selectedItems.value.length}件を無効化しますか？`)) {
    console.log('Bulk deactivate:', selectedItems.value)
    alert('選択項目を無効化しました')
    selectedItems.value = []
  }
}

const bulkDelete = () => {
  if (confirm(`選択した${selectedItems.value.length}件を削除しますか？`)) {
    console.log('Bulk delete:', selectedItems.value)
    alert('選択項目を削除しました')
    selectedItems.value = []
  }
}

const bulkExport = () => {
  console.log('Bulk export:', selectedItems.value)
  alert('選択項目をエクスポートしました')
}

const handleClickOutside = (event: Event) => {
  const sidebar = document.querySelector('[class*="fixed"][class*="z-40"]')
  const hamburgerBtn = document.querySelector('button[class*="lg:hidden"]')
  
  if (window.innerWidth <= 1024 && 
      sidebar && !sidebar.contains(event.target as Node) && 
      hamburgerBtn && !hamburgerBtn.contains(event.target as Node) &&
      sidebarVisible.value) {
    sidebarVisible.value = false
  }
}

const getSendModeClass = (mode) => {
  const baseClass = 'inline-flex items-center px-2 py-1 text-xs font-medium rounded-full'
  switch (mode) {
    case 'MPDF':
      return `${baseClass} bg-red-100 text-red-800`
    case 'ZIP':
      return `${baseClass} bg-blue-100 text-blue-800`
    case 'TXT':
      return `${baseClass} bg-green-100 text-green-800`
    default:
      return `${baseClass} bg-gray-100 text-gray-800`
  }
}

const getSendModeIcon = (mode) => {
  switch (mode) {
    case 'MPDF':
      return 'fas fa-file-pdf'
    case 'ZIP':
      return 'fas fa-file-zip'
    case 'TXT':
      return 'fas fa-file-alt'
    default:
      return 'fas fa-file'
  }
}

const formatDate = (date) => {
  return date.toLocaleDateString('ja-JP', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  
  // Set initial sidebar visibility based on screen size
  if (window.innerWidth <= 1024) {
    sidebarVisible.value = false
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.font-inter {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>