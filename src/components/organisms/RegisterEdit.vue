<template>
    <form @submit.prevent="handleSubmit">
        <!-- Loading State -->
        <div v-if="loading" class="flex justify-center items-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span class="ml-3 text-gray-600">処理中...</span>
        </div>

        <!-- Form Fields -->
        <div v-else class="space-y-8">
                <!-- Business ID Section -->
            <div class="px-6 space-y-6">
                <div class="space-x-4 gap-6">
                    <div class="flex items-center">
                        <div class="w-[21%]">
                            <InputField
                                id="businessId"
                                label="業務ID"
                                v-model="businessId"
                                type="text"
                                placeholder="Enter your 業務ID"
                                :required="false"
                                :error="idError"
                            />
                        </div>
                        <label class="text-xs font-medium text-gray-700 ml-4">
                            請求合計表　日酒販
                        </label>
                    </div>
                </div>
                <hr class="border-orange-100">
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-3">
                    <div class="flex items-center">
                        <div class="w-[66%]">
                            <InputField
                                id="businessOfficeCode"
                                label="事業所"
                                v-model="businessOfficeCode"
                                type="text"
                                placeholder="Enter your 事業所"
                                :required="false"
                                :error="businessOfficeCodeError"
                            />
                        </div>
                        <label class="text-xs font-medium text-gray-700 ml-4">
                            {{ businessOfficeName }}
                        </label>
                    </div>
                    <div class="flex items-center">
                        <div class="w-[66%]">
                            <InputField
                                id="customerCode"
                                label="得意先"
                                v-model="customerCode"
                                type="text"
                                placeholder="Enter your 得意先"
                                :required="false"
                                :error="customerCodeError"
                            />
                        </div>
                        <label class="text-xs font-medium text-gray-700 ml-4">
                            千葉県酒類販売
                        </label>
                    </div>
                    <div class="flex items-center">
                        <div class="w-[69%]">
                            <InputField
                                id="chainStoreName"
                                label="チェーン店"
                                v-model="chainStoreName"
                                type="text"
                                placeholder="Enter your チェーン店"
                                :required="false"
                                :error="chainStoreNameError"
                            />
                        </div>
                    </div>
                </div>
                <hr class="border-orange-100">
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    <!-- Supplier -->
                    <div class="space-y-3 w-[92%]">
                        <TextArea
                            v-model="supplier"
                            label="仕入先"
                            placeholder="仕入先情報を入力してください"
                            :rows="4"
                            :error="supplierError"
                        />
                    </div>
                    <!-- Order Destination -->
                    <div class="space-y-3 w-[92%]">
                        <TextArea
                            v-model="orderDestination"
                            label="仕入先"
                            placeholder="発注先設者情報を入力してください"
                            :rows="4"
                            :error="orderDestinationError"
                        />
                    </div>
                </div>
                <hr class="border-orange-100">
                <div class="space-y-3">
                    <div class="flex items-center w-full">
                        <div class="w-[30%]">
                            <InputField
                                id="extendedDistributionCode"
                                label="拡張振分コード"
                                v-model="extendedDistributionCode"
                                type="text"
                                placeholder="拡張振分コードを入力してください"
                                :required="false"
                                :error="extendedDistributionCodeError"
                            />
                        </div>
                    </div>
                </div>
                <hr class="border-blue-100">
            </div>
                <!-- Email Configuration -->
            <div class="px-6 space-y-6">
                <!-- First Row: Destination Name and Transmission Mode -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <InputField
                                id="destinationName"
                                label="送信先名称"
                                v-model="destinationName"
                                type="text"
                                placeholder="Enter your 事業所"
                                :required="false"
                                :error="destinationNameError"
                            />
                        </div>
                    </div>
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <SelectBox
                                v-model="transmissionMode"
                                label="送信モード"
                                :options="transmissionModes"
                                placeholder="送信モード"
                                required
                            />
                        </div>
                    </div>
                </div>
                <hr class="border-blue-200">
                <!-- Second Row: Search File Name and Search Directory -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <InputField
                                id="searchFileName"
                                label="検索ファイル名"
                                v-model="searchFileName"
                                type="text"
                                placeholder="検索ファイル名を入力してください"
                                :required="false"
                                :error="searchFileNameError"
                            />
                        </div>
                    </div>
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <InputField
                                id="searchDirectory"
                                label="検索ディレクトリ"
                                v-model="searchDirectory"
                                type="text"
                                placeholder="検索ディレクトリを入力してください"
                                :required="false"
                                :error="searchDirectoryError"
                            />
                        </div>
                    </div>
                </div>
                <hr class="border-blue-200">
                <!-- Third Row: Transmission Directory and Mail Title -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <InputField
                                id="transmissionDirectory"
                                label="送信ディレクトリ"
                                v-model="transmissionDirectory"
                                type="text"
                                placeholder="送信ディレクトリを入力してください"
                                :required="false"
                                :error="transmissionDirectoryError"
                            />
                        </div>
                    </div>
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <InputField
                                id="mailTitle"
                                label="メールタイトル"
                                v-model="mailTitle"
                                type="text"
                                placeholder="メールタイトルを入力してください"
                                :required="false"
                                :error="mailTitleError"
                            />
                        </div>
                    </div>
                </div>
                <hr class="border-blue-200">
                <!-- Fourth Row: Body File Path and Attachment File Path -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <InputField
                                id="bodyFilePath"
                                label="本文ファイルパス"
                                v-model="bodyFilePath"
                                type="text"
                                placeholder="本文ファイルパスを入力してください"
                                :required="false"
                                :error="bodyFilePathError"
                            />
                        </div>
                    </div>
                    <div class="flex items-center">
                        <div class="w-3/4">
                            <InputField
                                id="attachmentFilePath"
                                label="添付ファイルパス"
                                v-model="attachmentFilePath"
                                type="text"
                                placeholder="添付ファイルパスを入力してください"
                                :required="false"
                                :error="attachmentFilePathError"
                            />
                        </div>
                    </div>
                </div>
                <hr class="border-blue-200">
                <!-- Fifth Row: Mailing List Name -->
                <div class="space-y-2">
                    <div class="flex items-center">
                        <div class="w-[37%]">
                            <InputField
                                id="mailingListName"
                                label="メーリングリスト名"
                                v-model="mailingListName"
                                type="text"
                                placeholder="メーリングリスト名を入力してください"
                                :required="false"
                                :error="mailingListNameError"
                            />
                        </div>
                    </div>
                </div>
                <hr class="border-green-200">
            </div>

            <div class="px-6">
                <div>
                    <label class="block mb-2 text-gray-700 font-medium">送信宛先</label>
                    <div class="grid grid-cols-5 gap-4">
                        <div v-for="n in 10" :key="n" class="flex items-center space-x-2">
                            <span class="text-sm text-gray-600 w-4">{{ n }}</span>
                            <InputField
                                :id="`email-${n}`"
                                label=""
                                v-model="emails[n - 1]"
                                type="text"
                                placeholder="送信宛先を入力してください"
                                :required="false"
                            />
                        </div>
                    </div>
                </div>
            </div>
            <div class="px-6">
                <div class="pt-6 border-t border-purple-200">
                    <div class="flex items-center">
                        <div class=" w-[24%]">
                            <SelectBox
                                v-model="autoUpdateLock"
                                label="自動更新ロック区分"
                                :options="autoUpdateLockOpts"
                                placeholder="自動更新ロック区分"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Action Buttons -->
        <div class="my-6 px-6">
            <div class="flex h-full flex-col gap-6 sm:gap-5 xl:flex-row">
                <div class="bg-white p-4 xl:w-2/5 items-center flex justify-center">
                    <div class="flex">
                        <button
                        type="button"
                        @click="handleDelete"
                        class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors"
                        style="border-radius: 6px 0 0 6px; border-right: none;"
                        >
                        削除
                        </button>
                        <button
                        type="button"
                        @click="handleRestore"
                        class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors"
                        style="border-radius: 0 6px 6px 0;"
                        >
                        復活
                        </button>
                    </div>
                </div>
            
                <div class="p-4 xl:w-8/12 items-center flex justify-center">
                    <div class="flex space-x-4">
                        <!-- Center: Clear button -->
                        <button
                            type="button"
                            @click="handleClear"
                            class="px-6 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors"
                        >
                            クリア
                        </button>

                        <!-- Right: Register button -->
                        <button
                            type="submit"
                            class="px-6 py-2 text-sm font-medium text-white bg-blue-600 border border-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
                        >
                            登録
                        </button>
                    </div>
                </div>
            </div>
        </div> 
    </form>
</template>
<script setup>
    import { ref, reactive, onMounted } from 'vue'
    import InputField from '@/components/atoms/InputField.vue'
    import TextArea from '@/components/atoms/TextArea.vue'
    import Button from '@/components/atoms/Button.vue'
    import SelectBox from '@/components/atoms/SelectBox.vue';
    import { useRouter } from "vue-router";

    const selectedMode = ref('');
    const router = useRouter();

    const transmissionModes = [
    { value: 'MPDF', label: 'MPDF' },
    { value: 'PDF', label: 'PDF' },
    { value: 'TEXT', label: 'TEXT' },
    { value: 'HTML', label: 'HTML' },
    ];

    const autoUpdateLockOpts = [
    { value: '0', label: '0:無効' },
    { value: '1', label: '1:有効' },
    { value: '2', label: '2:部分ロック' },
    { value: '3', label: '3:完全ロック' },
    ];


    // Form data
    const businessId = ref('psurm22a')
    const businessOfficeCode = ref('0020')
    const businessOfficeName = ref('広域卸営業部')
    const customerCode = ref('8527')
    const chainStoreName = ref('')
    const supplier = ref('')
    const orderDestination = ref('')
    const extendedDistributionCode = ref('')
    const destinationName = ref('(広域卸) 千葉県酒類販売')
    const transmissionMode = ref('MPDF')
    const searchFileName = ref('psurm22a_00208527_*.PDF')
    const searchDirectory = ref('Z:\\HOSTON\\BSP')
    const transmissionDirectory = ref('D:\\Mail_Sender\\SEND')
    const mailTitle = ref('請求合計表【日本酒類販売株式会社】')
    const bodyFilePath = ref('D:\\Mail_Sender\\MAILfile\\MPDF.TXT')
    const attachmentFilePath = ref('')
    const mailingListName = ref('psurm22a_00208527@nishuhan.co.jp')

    const emails = ref(Array(10).fill('aaa@minhuhan.co.jp'));
    const emailError = false;

    function scrollToTop() {
    // Try scrolling the window
    window.scrollTo({ top: 0, behavior: 'auto' });
    // Also try scrolling the document element and body
    document.documentElement.scrollTop = 0;
    document.body.scrollTop = 0;
    }

    const handleSubmit = () => {
        scrollToTop();
        router.push('/register/edit/confirmation');
    };
</script>