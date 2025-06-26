<template>
  <div>
    <div class="px-6 space-y-6">
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
      <hr class="border-blue-100" />

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
      <hr class="border-green-200" />

      <div>
        <label class="block mb-2 font-bold">送信宛先</label>
        <div class="grid grid-cols-5 gap-4">
          <div
            v-for="(email, index) in emails"
            :key="index"
            class="text-sm"
          >
            {{ index + 1 }}. {{ email }}
          </div>
        </div>
      </div>

      <div class="pt-6 border-t border-purple-200">
        <div class="flex items-center">
          <div class="w-[12rem] font-bold">自動更新ロック区分:</div>
          <div>{{ autoUpdateLock }}</div>
        </div>
      </div>
    </div>
    <hr class="border-gray-100 mt-6" />
    <!-- Confirm Buttons -->
    <div class="flex justify-center gap-4 mt-8 px-6">
      <button class="bg-gray-300 px-4 py-2 rounded" @click="router.back()">
        戻る
      </button>
      <button class="bg-blue-600 text-white px-4 py-2 rounded" @click="submitConfirmed">
        確定
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import router from '@/router'
import { useRoute } from 'vue-router'

const showConfirmation = ref(false);

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

function scrollToTop() {
  // Try scrolling the window
  window.scrollTo({ top: 0, behavior: 'auto' });
  // Also try scrolling the document element and body
  document.documentElement.scrollTop = 0;
  document.body.scrollTop = 0;
}
const submitConfirmed = () => {
  scrollToTop();
  router.push('/register/edit/complete');
}
</script>

