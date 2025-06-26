<template>
    <div class="overflow-hidden bg-white">
        <div class="flex flex-col gap-5 px-6 mb-4 sm:flex-row sm:items-center sm:justify-end">
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
            <form>
                <div class="relative">
                <span class="absolute -translate-y-1/2 pointer-events-none top-1/2 left-4">
                    <svg class="fill-gray-500" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path fill-rule="evenodd" clip-rule="evenodd" d="M3.04199 9.37381C3.04199 5.87712 5.87735 3.04218 9.37533 3.04218C12.8733 3.04218 15.7087 5.87712 15.7087 9.37381C15.7087 12.8705 12.8733 15.7055 9.37533 15.7055C5.87735 15.7055 3.04199 12.8705 3.04199 9.37381ZM9.37533 1.54218C5.04926 1.54218 1.54199 5.04835 1.54199 9.37381C1.54199 13.6993 5.04926 17.2055 9.37533 17.2055C11.2676 17.2055 13.0032 16.5346 14.3572 15.4178L17.1773 18.2381C17.4702 18.531 17.945 18.5311 18.2379 18.2382C18.5308 17.9453 18.5309 17.4704 18.238 17.1775L15.4182 14.3575C16.5367 13.0035 17.2087 11.2671 17.2087 9.37381C17.2087 5.04835 13.7014 1.54218 9.37533 1.54218Z" fill=""></path>
                    </svg>
                </span>
                <input type="text" placeholder="Search..." class="shadow-theme-xs focus:border-brand-300 focus:ring-brand-500/10 h-10 w-full rounded-lg border border-gray-300 bg-transparent py-2.5 pr-4 pl-[42px] text-sm text-gray-800 placeholder:text-gray-400 focus:ring-3 focus:outline-hidden xl:w-[300px]">
                </div>
            </form>
            <div>
                <button class="text-theme-sm shadow-theme-xs inline-flex h-10 items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2.5 font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-800">
                <svg class="stroke-current fill-white" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M2.29004 5.90393H17.7067" stroke="" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                    <path d="M17.7075 14.0961H2.29085" stroke="" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                    <path d="M12.0826 3.33331C13.5024 3.33331 14.6534 4.48431 14.6534 5.90414C14.6534 7.32398 13.5024 8.47498 12.0826 8.47498C10.6627 8.47498 9.51172 7.32398 9.51172 5.90415C9.51172 4.48432 10.6627 3.33331 12.0826 3.33331Z" fill="" stroke="" stroke-width="1.5"></path>
                    <path d="M7.91745 11.525C6.49762 11.525 5.34662 12.676 5.34662 14.0959C5.34661 15.5157 6.49762 16.6667 7.91745 16.6667C9.33728 16.6667 10.4883 15.5157 10.4883 14.0959C10.4883 12.676 9.33728 11.525 7.91745 11.525Z" fill="" stroke="" stroke-width="1.5"></path>
                </svg>

                Filter
                </button>
            </div>
            <div>
                <button @click="router.push('register')" class="inline-flex items-center gap-2 rounded-lg bg-blue-500 px-4 py-2.5 text-sm font-medium text-white shadow-theme-xs hover:bg-blue-600">
                    新規登録
                    <svg class="fill-current" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path fill-rule="evenodd" clip-rule="evenodd" d="M9.2502 4.99951C9.2502 4.5853 9.58599 4.24951 10.0002 4.24951C10.4144 4.24951 10.7502 4.5853 10.7502 4.99951V9.24971H15.0006C15.4148 9.24971 15.7506 9.5855 15.7506 9.99971C15.7506 10.4139 15.4148 10.7497 15.0006 10.7497H10.7502V15.0001C10.7502 15.4143 10.4144 15.7501 10.0002 15.7501C9.58599 15.7501 9.2502 15.4143 9.2502 15.0001V10.7497H5C4.58579 10.7497 4.25 10.4139 4.25 9.99971C4.25 9.5855 4.58579 9.24971 5 9.24971H9.2502V4.99951Z" fill=""></path>
                </svg>
            </button>
            </div>
            </div>
        </div>

        <div class="max-w-full overflow-x-auto custom-scrollbar">
            <table class="min-w-full">
                <!-- table header start -->
                <thead class="border-gray-100 border-y bg-gray-50">
                    <tr>
                    <th class="px-6 py-3 whitespace-nowrap">
                        <div class="flex items-center">
                        <div x-data="{checked: false}" class="flex items-center gap-3">
                            <div @click="checked = !checked" class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px] bg-white " :class="checked ? 'border-brand-500 bg-brand-500' : 'bg-white border-gray-300' ">
                            <svg :class="checked ? 'block' : 'hidden'" width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg" class="hidden">
                                <path d="M11.6668 3.5L5.25016 9.91667L2.3335 7" stroke="white" stroke-width="1.94437" stroke-linecap="round" stroke-linejoin="round"></path>
                            </svg>
                            </div>
                            <div>
                            <span class="block font-medium text-gray-500 text-theme-xs">
                                業務ID
                            </span>
                            </div>
                        </div>
                        </div>
                    </th>
                    <th class="px-6 py-3 whitespace-nowrap">
                        <div class="flex items-center">
                        <p class="font-medium text-gray-500 text-theme-xs">
                            送信先名称
                        </p>
                        </div>
                    </th>
                    <th class="px-6 py-3 whitespace-nowrap">
                        <div class="flex items-center">
                        <p class="font-medium text-gray-500 text-theme-xs">
                            作成日時
                        </p>
                        </div>
                    </th>
                    <th v-if="showActions" class="px-6 py-3 whitespace-nowrap">
                        <div class="flex items-center justify-center">
                        <p class="font-medium text-gray-500 text-theme-xs">
                            Action
                        </p>
                        </div>
                    </th>
                    </tr>
                </thead>
                <!-- table header end -->
                <tbody class="divide-y divide-gray-100">
                    <tr v-for="user in users" :key="user.id" @click="router.push(`/detail/${user.id}`)" class="cursor-pointer hover:bg-gray-50"><!--{ name: 'detail', params: { id: user.id } }-->
                        <!-- Checkbox Column -->
                        <td class="px-6 py-3 whitespace-nowrap">
                            <div class="flex items-center">
                            <div class="flex items-center gap-3">
                                <div 
                                @click="user.checked = !user.checked" 
                                class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px]"
                                :class="user.checked ? 'border-brand-500 bg-brand-500' : 'bg-white border-gray-300'"
                                >
                                <svg 
                                    v-show="user.checked" 
                                    width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path d="M11.6668 3.5L5.25016 9.91667L2.3335 7" stroke="white" stroke-width="1.94437" stroke-linecap="round" stroke-linejoin="round"></path>
                                </svg>
                                </div>
                                <span class="block font-medium text-gray-700 text-theme-sm">{{ user.id }}</span>
                            </div>
                            </div>
                        </td>

                        <!-- User Info Column -->
                        <td class="px-6 py-3 whitespace-nowrap">
                            <div class="flex items-center">
                            <div class="flex items-center gap-3">
                                <div 
                                class="flex h-10 w-10 items-center justify-center rounded-full"
                                :style="{ backgroundColor: user.avatarBg }"
                                >
                                <span class="text-xs font-semibold" :style="{ color: user.avatarColor }">
                                    {{ user.initials }}
                                </span>
                                </div>
                                <div>
                                <span class="text-theme-sm mb-0.5 block font-medium text-gray-700">{{ user.name }}</span>
                                <span class="text-gray-500 text-theme-sm">{{ user.email }}</span>
                                </div>
                            </div>
                            </div>
                        </td>

                        <!-- Date Column -->
                        <td class="px-6 py-3 whitespace-nowrap">
                            <div class="flex items-center">
                            <p class="text-gray-700 text-theme-sm">{{ user.date }}</p>
                            </div>
                        </td>

                        <!-- Actions Column -->
                        <td v-if="showActions" class="px-6 py-3 whitespace-nowrap">
                            <div class="flex items-center justify-center">
                            <button @click.stop="openDialog(user.id)" class="text-gray-500 hover:text-red-500 mr-2">
                                <svg class="fill-current" width="21" height="21" viewBox="0 0 21 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path fill-rule="evenodd" clip-rule="evenodd" d="M7.04142 4.29199C7.04142 3.04935 8.04878 2.04199 9.29142 2.04199H11.7081C12.9507 2.04199 13.9581 3.04935 13.9581 4.29199V4.54199H16.1252H17.166C17.5802 4.54199 17.916 4.87778 17.916 5.29199C17.916 5.70621 17.5802 6.04199 17.166 6.04199H16.8752V8.74687V13.7469V16.7087C16.8752 17.9513 15.8678 18.9587 14.6252 18.9587H6.37516C5.13252 18.9587 4.12516 17.9513 4.12516 16.7087V13.7469V8.74687V6.04199H3.8335C3.41928 6.04199 3.0835 5.70621 3.0835 5.29199C3.0835 4.87778 3.41928 4.54199 3.8335 4.54199H4.87516H7.04142V4.29199ZM15.3752 13.7469V8.74687V6.04199H13.9581H13.2081H7.79142H7.04142H5.62516V8.74687V13.7469V16.7087C5.62516 17.1229 5.96095 17.4587 6.37516 17.4587H14.6252C15.0394 17.4587 15.3752 17.1229 15.3752 16.7087V13.7469ZM8.54142 4.54199H12.4581V4.29199C12.4581 3.87778 12.1223 3.54199 11.7081 3.54199H9.29142C8.87721 3.54199 8.54142 3.87778 8.54142 4.29199V4.54199ZM8.8335 8.50033C9.24771 8.50033 9.5835 8.83611 9.5835 9.25033V14.2503C9.5835 14.6645 9.24771 15.0003 8.8335 15.0003C8.41928 15.0003 8.0835 14.6645 8.0835 14.2503V9.25033C8.0835 8.83611 8.41928 8.50033 8.8335 8.50033ZM12.9168 9.25033C12.9168 8.83611 12.581 8.50033 12.1668 8.50033C11.7526 8.50033 11.4168 8.83611 11.4168 9.25033V14.2503C11.4168 14.6645 11.7526 15.0003 12.1668 15.0003C12.581 15.0003 12.9168 14.6645 12.9168 14.2503V9.25033Z" fill=""></path>
                                </svg>
                            </button>
                            <button @click.stop="editUser(user.id)" class="text-gray-500 hover:text-gray-800" type="button">
                                <svg class="fill-current" width="21" height="21" viewBox="0 0 21 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path fill-rule="evenodd" clip-rule="evenodd" d="M17.0911 3.53206C16.2124 2.65338 14.7878 2.65338 13.9091 3.53206L5.6074 11.8337C5.29899 12.1421 5.08687 12.5335 4.99684 12.9603L4.26177 16.445C4.20943 16.6931 4.286 16.9508 4.46529 17.1301C4.64458 17.3094 4.90232 17.3859 5.15042 17.3336L8.63507 16.5985C9.06184 16.5085 9.45324 16.2964 9.76165 15.988L18.0633 7.68631C18.942 6.80763 18.942 5.38301 18.0633 4.50433L17.0911 3.53206ZM14.9697 4.59272C15.2626 4.29982 15.7375 4.29982 16.0304 4.59272L17.0027 5.56499C17.2956 5.85788 17.2956 6.33276 17.0027 6.62565L16.1043 7.52402L14.0714 5.49109L14.9697 4.59272ZM13.0107 6.55175L6.66806 12.8944C6.56526 12.9972 6.49455 13.1277 6.46454 13.2699L5.96704 15.6283L8.32547 15.1308C8.46772 15.1008 8.59819 15.0301 8.70099 14.9273L15.0436 8.58468L13.0107 6.55175Z" fill=""></path>
                                </svg>
                            </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
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
import { ref } from 'vue'
import router from '@/router'
import UseDialog from '@/components/atoms/UseDialog.vue';

const users = ref([
  {
    id: 'DE124321',
    name: 'John Doe',
    email: 'johndeo@gmail.com',
    initials: 'JD',
    avatarBg: '#f0f9ff',
    avatarColor: '#0086c9',
    date: '2024-06-15',
    status: 'Complete',
    checked: false
  },
  {
    id: 'DE124321',
    name: 'Kierra Franci',
    email: 'kierra@gmail.com',
    initials: 'KF',
    avatarBg: '#fdf2fa',
    avatarColor: '#dd2590',
    date: '2024-06-15',
    status: 'Complete',
    checked: false
  },
  {
    id: 'DE124321',
    name: 'Emerson Workman',
    email: 'emerson@gmail.com',
    initials: 'EW',
    avatarBg: '#f0f9ff',
    avatarColor: '#0086c9',
    date: '2024-06-15',
    status: 'Pending',
    checked: false
  },
  {
    id: 'DE124321',
    name: 'Chance Philips',
    email: 'chance@gmail.com',
    initials: 'CP',
    avatarBg: '#fff6ed',
    avatarColor: '#ec4a0a',
    date: '2024-06-15',
    status: 'Complete',
    checked: false
  },
  {
    id: 'DE124321',
    name: 'Terry Geidt',
    email: 'terry@gmail.com',
    initials: 'TG',
    avatarBg: '#f0fdf4',
    avatarColor: '#16a34a',
    date: '2024-06-15',
    status: 'Complete',
    checked: false
  }
])

const showActions = ref(false);

const dialogVisible = ref(false)
const selectedUser = ref(null)

const openDialog = (user) => {
  selectedUser.value = user
  dialogVisible.value = true
}

const confirmDelete = () => {
  users.value = users.value.filter(u => u.id !== selectedUser.value.id)
  dialogVisible.value = false
}

const editUser = (userId) => {
  router.push(`/register/edit/${userId}`);
  console.log('Edit user:', userId);
};

</script>