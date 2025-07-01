<template>
  <div class="container mx-auto flex flex-col items-center">
    <!-- <Stepper :name="'complete'"/> -->
    <div class="flex flex-col items-center w-full">
      <div class="flex justify-center my-8">
        登録が正常に完了しました。追加されたデータの検索や一覧表示が可能です
      </div>
      <div>リストを表示するにはボタンをクリックしてください。</div>
      <!-- Action Button -->
      <div class="flex justify-center items-center my-8">
        <button 
          @click="$router.push('/')" 
          type="button"
          class="inline-flex items-center justify-center px-8 py-4 text-base font-semibold rounded-xl transition-all duration-200 text-white bg-gradient-to-br from-[#667eea] to-[#764ba2] shadow-lg shadow-indigo-500/40 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-indigo-500/50 focus:outline-none focus:ring-4 focus:ring-indigo-200"
        >
          <i class="fas fa-home mr-3"></i>
          ホームページ
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang='ts'>
// import Stepper from "@/components/Stepper.vue";

</script>
<style scoped>

</style>