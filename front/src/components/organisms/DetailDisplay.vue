<template>
  <div class="">
    <div class="">
      
      <!-- Breadcrumb -->
      <nav class="flex items-center mb-6 text-sm text-gray-500">
        <a href="#" @click.prevent="navigateHome" class="text-[#667eea] hover:text-indigo-400 transition-colors duration-200 no-underline">
          <i class="fas fa-home mr-2"></i>ホーム
        </a>
        <span class="mx-3 text-gray-300">
          <i class="fas fa-chevron-right"></i>
        </span>
        <a href="#" @click.prevent="navigateToList" class="text-[#667eea] hover:text-indigo-400 transition-colors duration-200 no-underline">メール宛先マスター</a>
        <span class="mx-3">
          <i class="fas fa-chevron-right"></i>
        </span>
        <span class="font-medium">詳細表示</span>
      </nav>

      <!-- 詳細情報表示 -->
      <div class="space-y-8">
        
        <!-- 1. 基本情報カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-info-circle text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">基本情報</h3>
              <p class="text-sm text-gray-500 mt-1">業務の基本的な識別情報</p>
            </div>
          </div>
          
          <div class="grid gap-6 md:grid-cols-2">
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-id-badge mr-2 text-gray-500 w-4"></i>
                業務ID
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.businessId }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-briefcase mr-2 text-gray-500 w-4"></i>
                業務名
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.businessName }}
              </div>
            </div>
          </div>
          
          <div class="flex items-center text-xs text-gray-500 mt-4 pt-4 border-t border-gray-200">
            <i class="fas fa-clock mr-2"></i>
            最終更新: {{ formatDate(detailData.lastUpdated) }}
          </div>
        </div>

        <!-- 2. 事業所・取引先情報カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-building text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">事業所・取引先情報</h3>
              <p class="text-sm text-gray-500 mt-1">関連する事業所と取引先の詳細情報</p>
            </div>
          </div>
          
          <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-hashtag mr-2 text-gray-500 w-4"></i>
                事業所コード
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.officeCode }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-building-user mr-2 text-gray-500 w-4"></i>
                事業所名
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.officeName }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-user-tag mr-2 text-gray-500 w-4"></i>
                得意先コード
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.clientCode }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-user-tie mr-2 text-gray-500 w-4"></i>
                得意先名
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.clientName }}
              </div>
            </div>
          </div>
        </div>

        <!-- 3. 送信設定カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-paper-plane text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">送信設定</h3>
              <p class="text-sm text-gray-500 mt-1">メール送信に関する基本設定</p>
            </div>
          </div>
          
          <div class="grid gap-6 md:grid-cols-2">
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-tag mr-2 text-gray-500 w-4"></i>
                送信先名称
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.destinationName }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-cog mr-2 text-gray-500 w-4"></i>
                送信モード
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                <span class="inline-flex items-center">
                  <i :class="getSendModeIcon(detailData.sendMode)" class="mr-2"></i>
                  {{ getSendModeText(detailData.sendMode) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 4. ファイル・ディレクトリ情報カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-folder-open text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">ファイル・ディレクトリ情報</h3>
              <p class="text-sm text-gray-500 mt-1">ファイルの場所とメール内容の設定</p>
            </div>
          </div>
          
          <div class="grid gap-6 md:grid-cols-2">
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-search mr-2 text-gray-500 w-4"></i>
                検索ファイル名
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.searchFileName }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-folder mr-2 text-gray-500 w-4"></i>
                検索ディレクトリ
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.searchDirectory }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-folder-plus mr-2 text-gray-500 w-4"></i>
                送信ディレクトリ
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.sendDirectory }}
              </div>
            </div>
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-envelope mr-2 text-gray-500 w-4"></i>
                メールタイトル
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                {{ detailData.mailTitle }}
              </div>
            </div>
          </div>
          <div class="mb-6">
            <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
              <i class="fas fa-file-alt mr-2 text-gray-500 w-4"></i>
              本文ファイルパス
            </label>
            <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
              {{ detailData.bodyFilePath }}
            </div>
          </div>
        </div>

        <!-- 5. 宛先情報カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-address-book text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">宛先情報</h3>
              <p class="text-sm text-gray-500 mt-1">メール送信先の管理</p>
            </div>
          </div>
          
          <div class="mb-6">
            <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
              <i class="fas fa-list mr-2 text-gray-500 w-4"></i>
              メーリングリスト名
            </label>
            <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
              {{ detailData.mailingList }}
            </div>
          </div>
          
          <div class="mb-6">
            <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
              <i class="fas fa-at mr-2 text-gray-500 w-4"></i>
              送信宛先一覧
            </label>
            <ul class="list-none p-0 m-0">
              <li 
                v-for="(email, index) in detailData.emailAddresses" 
                :key="index" 
                class="flex items-center p-2 px-3 bg-slate-50 border border-slate-200 rounded-lg mb-2"
              >
                <i class="fas fa-envelope text-slate-500 mr-2"></i>
                <span class="font-medium">送信宛先{{ index + 1 }}:</span>
                <span v-if="email" class="ml-2">{{ email }}</span>
                <span v-else class="ml-2 text-gray-500 italic">未設定</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- 6. システム設定カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-sliders-h text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">システム設定</h3>
              <p class="text-sm text-gray-500 mt-1">システムの動作に関する設定</p>
            </div>
          </div>
          
          <div class="max-w-md">
            <div class="mb-6">
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-lock mr-2 text-gray-500 w-4"></i>
                自動更新ロック区分
              </label>
              <div class="p-3 px-4 text-sm text-gray-900 bg-gray-50 border-2 border-gray-100 rounded-lg font-medium break-all">
                <span class="inline-flex items-center">
                  <i :class="detailData.autoUpdateLock === '0' ? 'fas fa-unlock text-green-500' : 'fas fa-lock text-red-500'" class="mr-2"></i>
                  {{ getAutoUpdateLockText(detailData.autoUpdateLock) }}
                </span>
              </div>
              <div :class="[
                'mt-3 p-3 border rounded-lg',
                detailData.autoUpdateLock === '0' 
                  ? 'bg-green-50 border-green-200' 
                  : 'bg-red-50 border-red-200'
              ]">
                <div class="flex items-start">
                  <i :class="detailData.autoUpdateLock === '0' ? 'fas fa-info-circle text-green-500' : 'fas fa-exclamation-triangle text-red-500'" class="mr-2 mt-0.5"></i>
                  <p :class="detailData.autoUpdateLock === '0' ? 'text-sm text-green-700' : 'text-sm text-red-700'">
                    {{ detailData.autoUpdateLock === '0' ? '現在、システムによる自動更新が有効になっています。' : '現在、システムによる自動更新が無効になっています。手動での更新が必要です。' }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- アクションボタンカード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex flex-col sm:flex-row justify-between gap-4">
            <div class="flex gap-4">
              <button 
                type="button" 
                @click="router.back()" 
                class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50 hover:border-gray-300 hover:-translate-y-0.5 cursor-pointer"
              >
                <i class="fas fa-arrow-left mr-2"></i>
                一覧に戻る
              </button>
              <button 
                type="button" 
                @click="printPage" 
                class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50 hover:border-gray-300 hover:-translate-y-0.5 cursor-pointer"
              >
                <i class="fas fa-print mr-2"></i>
                印刷
              </button>
            </div>
            <div class="flex gap-4">
              <button 
                type="button" 
                @click="router.push(`/detail/edit/${idDetail}`)" 
                class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-emerald-500 shadow-lg shadow-emerald-500/40 hover:bg-emerald-600 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-emerald-500/50 cursor-pointer"
              >
                <i class="fas fa-edit mr-2"></i>
                編集
              </button>
              <button 
                type="button" 
                @click="duplicateRecord" 
                class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-gradient-to-br from-[#667eea] to-[#764ba2] shadow-lg shadow-indigo-500/40 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-indigo-500/50 cursor-pointer"
              >
                <i class="fas fa-copy mr-2"></i>
                複製
              </button>
              <button 
                type="button" 
                @click="openDialog(idDetail)" 
                class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-red-500 shadow-lg shadow-red-500/40 hover:bg-red-600 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-red-500/50 cursor-pointer"
              >
                <i class="fas fa-trash mr-2"></i>
                削除
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
  
  <UseDialog
    :show="dialogVisible"
    title="メールの削除"
    message="このメールを削除してもよろしいですか？"
    @close="dialogVisible = false"
    @confirm="confirmDelete"
  />
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import UseDialog from '@/components/atoms/UseDialog.vue'

const route = useRoute()
const idDetail = route.params.id

const detailData = reactive({
  businessId: 'psurm22a',
  businessName: '請求合計表 日酒販',
  officeCode: '0020',
  officeName: '広域卸営業部',
  clientCode: '8527',
  clientName: '千葉県酒類販売',
  destinationName: '広域卸）千葉県酒類販売',
  sendMode: 'MPDF',
  searchFileName: 'psurm22a_00208527_*.PDF',
  searchDirectory: 'Z:\\HOSTON\\BSP',
  sendDirectory: 'D:\\Mail_Sender\\SEND',
  mailTitle: '請求合計表【日本酒類販売株式会社】',
  bodyFilePath: 'D:\\Mail_Sender\\MAIL\\file\\MPDF.TXT',
  mailingList: 'psurm22a_00208527@nishuhan.co.jp',
  emailAddresses: ['aaa@nishuhan.co.jp', '', ''],
  autoUpdateLock: '0',
  isActive: true,
  lastUpdated: new Date('2024-01-15T14:30:00')
})

const formatDate = (date) => {
  return date.toLocaleDateString('ja-JP', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getSendModeIcon = (mode) => {
  switch (mode) {
    case 'MPDF':
      return 'fas fa-file-pdf text-red-500'
    case 'TXT':
      return 'fas fa-file-alt text-blue-500'
    case 'ZIP':
      return 'fas fa-file-archive text-yellow-500'
    default:
      return 'fas fa-file'
  }
}

const getSendModeText = (mode) => {
  switch (mode) {
    case 'MPDF':
      return 'MPDF（PDFファイル）'
    case 'TXT':
      return 'TXT（テキストファイル）'
    case 'ZIP':
      return 'ZIP（圧縮ファイル）'
    default:
      return mode
  }
}

const getAutoUpdateLockText = (lock) => {
  return lock === '0' ? '0: 無効（自動更新を許可）' : '1: 有効（自動更新を無効化）'
}

const navigateHome = () => {
  console.log('Navigate to home')
}

const navigateToList = () => {
  console.log('Navigate to list')
}

const printPage = () => {
  window.print()
}

const duplicateRecord = () => {
  console.log('Duplicate record')
  alert('レコードを複製します')
}

const deleteRecord = () => {
  if (confirm('本当に削除しますか？')) {
    console.log('Delete record')
    alert('レコードを削除しました')
  }
}

const dialogVisible = ref(false)
const selectedUser = ref(null)

const openDialog = (user) => {
  selectedUser.value = user
  dialogVisible.value = true
}

const confirmDelete = () => {
  console.log('Delete confirmed')
  alert('レコードを削除しました')
  dialogVisible.value = false
}

// Try scrolling the window
window.scrollTo({ top: 0, behavior: 'auto' })
// Also try scrolling the document element and body
document.documentElement.scrollTop = 0
document.body.scrollTop = 0
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.font-inter {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>