<template>
  <div class="container mx-auto flex flex-col items-center">
    <!-- <Stepper :name="'complete'"/> -->
    <div class="flex flex-col items-center w-full">
      <div class="flex justify-center my-8">
        登録が正常に完了しました。追加されたデータの検索や一覧表示が可能です
      </div>
      <div>リストを表示するにはボタンをクリックしてください。</div>
      <div class="flex justify-center items-center">
        <Button @click="$router.push('/')" variant="primary" class="mt-6 rounded-full bg-blue-900 text-[#fff] w-[140px]" type="button">ホームページ</Button>
      </div>
    </div>
  </div>
</template>

<script setup lang='ts'>
import router from '@/router'
// import Stepper from "@/components/Stepper.vue";

</script>
