<template>
  <div class="">
    <div class="">

      <form @submit.prevent="handleSubmit" class="space-y-8">
        
        <!-- 1. 基本情報カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-info-circle text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">基本情報</h3>
              <p class="text-sm text-gray-500 mt-1">業務の基本的な識別情報を入力してください</p>
            </div>
          </div>
          
          <div class="grid gap-6 md:grid-cols-2">
            <div class="mb-6">
              <InputField
                id="businessId"
                label="業務ID"
                v-model="formData.businessId"
                type="text"
                icon="fas fa-id-badge"
                placeholder=""
                :required="false"
                :error="businessIdError"
              />
            </div>
            <div class="mb-6">
              <InputField
                id="businessName"
                label="業務名"
                v-model="formData.businessName"
                type="text"
                icon="fas fa-briefcase"
                placeholder=""
                :required="false"
                :error="businessNameError"
              />
            </div>
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
              <InputField
                id="officeCode"
                label="事業所コード"
                v-model="formData.officeCode"
                type="text"
                icon="fas fa-hashtag"
                placeholder=""
                :required="false"
                :error="officeCodeError"
              />
            </div>
            <div class="mb-6">
              <InputField
                id="officeName"
                label="事業所名"
                v-model="formData.officeName"
                type="text"
                icon="fas fa-building-user"
                placeholder=""
                :required="false"
                :error="officeNameError"
              />
            </div>
            <div class="mb-6">
              <InputField
                id="clientCode"
                label="得意先コード"
                v-model="formData.clientCode"
                type="text"
                icon="fas fa-user-tag"
                placeholder=""
                :required="false"
                :error="clientCodeError"
              />
            </div>
            <div class="mb-6">
              <InputField
                id="clientName"
                label="得意先名"
                v-model="formData.clientName"
                type="text"
                icon="fas fa-user-tie"
                placeholder=""
                :required="false"
                :error="clientNameError"
              />
            </div>
          </div>
        </div>

        <!-- 3. 送信設定カード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex items-center mb-6 pb-4 border-b-2 border-slate-100">
            <div class="w-12 h-12 bg-gradient-to-br from-[#667eea] to-[#764ba2] to-purple-600 rounded-xl flex items-center justify-center mr-4 shadow-lg shadow-indigo-500/30">
              <i class="fas fa-paper-plane text-white text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-800 m-0">送信設定</h3>
              <p class="text-sm text-gray-500 mt-1">メール送信に関する基本設定</p>
            </div>
          </div>
          
          <div class="grid gap-6 md:grid-cols-2">
            <div class="mb-6">
              <InputField
                id="destinationName"
                label="送信先名称"
                v-model="formData.destinationName"
                type="text"
                icon="fas fa-tag"
                placeholder=""
                :required="false"
                :error="destinationNameError"
              />
            </div>
            <div class="mb-6">
              <SelectBox
                v-model="formData.sendMode"
                label="送信モード"
                :options="SendModes"
                placeholder="送信モード"
                icon="fas fa-cog"
                :required="false"
              />
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
            <div>
              <FileInput
                v-model="formData.searchFileName"
                accept=""
                label="検索ファイル名"
                icon="fas fa-search"
                :required="false"
                :error="searchFileNameError"
              />
            </div>
            <div>
              <FileInput
                v-model="formData.searchDirectory"
                accept=""
                label="検索ディレクトリ"
                icon="fas fa-folder"
                :required="false"
                :error="searchDirectoryError"
              />
            </div>
            <div class="mb-6">
              <FileInput
                v-model="formData.sendDirectory"
                accept=""
                label="送信ディレクトリ"
                icon="fas fa-folder-plus"
                :required="false"
                :error="sendDirectoryError"
              />
            </div>
            <div class="mb-6">
              <FileInput
                v-model="formData.mailTitle"
                accept=""
                label="メールタイトル"
                icon="fas fa-envelope"
                :required="false"
                :error="mailTitleError"
              />
            </div>
          </div>
          <div class="mb-6">
            <FileInput
              v-model="formData.bodyFilePath"
              accept=""
              label="本文ファイルパス"
              icon="fas fa-file-alt"
              :required="false"
              :error="bodyFilePathError"
            />
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
            <InputField
              id="mailingList"
              label="メーリングリスト名"
              v-model="formData.mailingList"
              type="text"
              icon="fas fa-list"
              placeholder=""
              :required="false"
              :error="mailingListError"
            />
          </div>
          
          <div class="grid gap-6 md:grid-cols-2 mb-6">
            <div 
              v-for="(email, index) in formData.emailAddresses" 
              :key="index" 
              class=""
            >
              <label class="flex items-center text-sm font-medium text-gray-700 mb-2">
                <i class="fas fa-at mr-2 text-gray-500 w-4"></i>
                送信宛先{{ index + 1 }}
              </label>
              <div class="flex gap-2">
                <input 
                  type="email" 
                  v-model="formData.emailAddresses[index]" 
                  :placeholder="index === 0 ? '必須' : '宛先を入力してください'" 
                  class="flex-1 px-4 py-3 text-sm text-gray-900 bg-white border-2 border-gray-200 rounded-xl transition-all duration-200 shadow-sm focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100" 
                  :required="false"
                />
                <button 
                  v-if="index > 0" 
                  type="button" 
                  @click="removeEmailAddress(index)"
                  class="inline-flex items-center justify-center px-3 py-2 text-sm font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>
            </div>
          </div>
          
          <button 
            type="button" 
            @click="addEmailAddress" 
            class="w-full border-2 border-dashed border-gray-300 bg-gray-50 text-gray-500 px-4 py-4 rounded-xl transition-all duration-200 hover:border-indigo-500 hover:bg-blue-50 hover:text-indigo-500"
          >
            <i class="fas fa-plus mr-2"></i>
            宛先を追加する
          </button>
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
              <SelectBox
                v-model="formData.autoUpdateLock"
                label="自動更新ロック区分"
                :options="autoUpdateLocks"
                placeholder=""
                icon="fas fa-lock"
                :required="false"
              />
              <div class="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <div class="flex items-start">
                  <i class="fas fa-info-circle text-blue-500 mr-2 mt-0.5"></i>
                  <p class="text-sm text-blue-700">
                    有効にすると、システムによる自動更新が無効になります。手動での更新が必要になります。
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- アクションボタンカード -->
        <div class="bg-white rounded-2xl shadow-lg shadow-black/8 border border-gray-200 p-8 mb-6 transition-all duration-300 hover:shadow-xl hover:shadow-black/12 hover:-translate-y-0.5">
          <div class="flex flex-col sm:flex-row justify-end gap-4">
            <button 
              type="button" 
              @click="deleteRecord" 
              class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50 hover:border-gray-300 hover:-translate-y-0.5"
            >
              <i class="fas fa-trash mr-2"></i>
              削除
            </button>
            <button 
              type="button" 
              @click="restoreRecord" 
              class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-gray-500 bg-white border-2 border-gray-200 shadow-sm hover:bg-gray-50 hover:border-gray-300 hover:-translate-y-0.5"
            >
              <i class="fas fa-undo mr-2"></i>
              復活
            </button>
            <button 
              type="button" 
              @click="clearForm" 
              class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-amber-500 shadow-lg shadow-amber-500/40 hover:bg-amber-600 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-amber-500/50"
            >
              <i class="fas fa-eraser mr-2"></i>
              クリア
            </button>
            <button 
              type="submit" 
              class="inline-flex items-center justify-center px-6 py-3 text-sm font-medium rounded-lg transition-all duration-200 text-white bg-gradient-to-br from-[#667eea] to-[#764ba2] shadow-lg shadow-indigo-500/40 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-indigo-500/50"
            >
              <i class="fas fa-save mr-2"></i>
              登録
            </button>
          </div>
        </div>

      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import router from '@/router'
import InputField from '@/components/atoms/InputField.vue'
import Button from '@/components/atoms/Button.vue'
import SelectBox from '@/components/atoms/SelectBox.vue';
import FileInput from '@/components/atoms/FileInput.vue'

interface FormData {
  businessId: string
  businessName: string
  officeCode: string
  officeName: string
  clientCode: string
  clientName: string
  destinationName: string
  sendMode: string
  searchFileName: string
  searchDirectory: string
  sendDirectory: string
  mailTitle: string
  bodyFilePath: string
  mailingList: string
  emailAddresses: string[]
  autoUpdateLock: string
}

const formData = reactive<FormData>({
  businessId: '',
  businessName: '',
  officeCode: '',
  officeName: '',
  clientCode: '',
  clientName: '',
  destinationName: '',
  sendMode: '',
  searchFileName: '',
  searchDirectory: '',
  sendDirectory: '',
  mailTitle: '',
  bodyFilePath: '',
  mailingList: '',
  emailAddresses: ['', '', '', ''],
  autoUpdateLock: ''
})

// Error refs (you can implement validation logic)
const businessIdError = ref('')
const businessNameError = ref('')
const officeCodeError = ref('')
const officeNameError = ref('')
const clientCodeError = ref('')
const clientNameError = ref('')
const destinationNameError = ref('')
const searchFileNameError = ref('')
const searchDirectoryError = ref('')
const sendDirectoryError = ref('')
const mailTitleError = ref('')
const bodyFilePathError = ref('')
const mailingListError = ref('')

const addEmailAddress = () => {
  formData.emailAddresses.push('')
}

const removeEmailAddress = (index: number) => {
  if (index > 0) {
    formData.emailAddresses.splice(index, 1)
  }
}

const handleSubmit = () => {
  console.log('Form submitted:', formData)
  router.push('/register/confirmation');
}

const deleteRecord = () => {
  if (confirm('本当に削除しますか？')) {
    console.log('Record deleted')
    alert('削除しました')
  }
}

const restoreRecord = () => {
  console.log('Record restored')
  alert('復活しました')
}

const navigateToList = () => {
  router.push('/')
}

const clearForm = () => {
  if (confirm('フォームをクリアしますか？')) {
    Object.keys(formData).forEach(key => {
      if (key === 'emailAddresses') {
        formData[key] = ['', '', '', '']
      } else if (key === 'sendMode') {
        formData[key] = 'MPDF'
      } else if (key === 'autoUpdateLock') {
        formData[key] = '0'
      } else {
        formData[key as keyof FormData] = '' as any
      }
    })
    alert('フォームをクリアしました')
  }
}

const SendModes = [
  { value: 'MPDF', label: 'MPDF（PDFファイル）' },
  { value: 'TXT', label: 'TXT（テキストファイル）' },
  { value: 'ZIP', label: 'ZIP（圧縮ファイル）' },
];

const autoUpdateLocks = [
  { value: '0', label: '0: 無効（自動更新を許可）' },
  { value: '1', label: '1: 有効（自動更新を無効化）' },
];

// Try scrolling the window
window.scrollTo({ top: 0, behavior: 'auto' });
// Also try scrolling the document element and body
document.documentElement.scrollTop = 0;
document.body.scrollTop = 0;
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.font-inter {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>
