<template>
  <div class="min-h-screen w-full flex">
    <!-- Left side - Form content -->
    <div class="w-full lg:w-1/2 flex items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
      <div class="w-full max-w-md space-y-8">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">Sign Up</h1>
          <p class="mt-2 text-sm text-gray-600">
            Enter your email and password to sign up!
          </p>
        </div>
        <SignUpForm />
      </div>
    </div>
    
    <!-- Right side - Brand/Logo -->
    <div class="hidden lg:block lg:w-1/2 bg-[#1f2b5b]">
      <div class="h-full flex flex-col items-center justify-center text-white">
        <div class="text-6xl font-bold mb-4">NAIS</div>
        <div class="text-4xl font-medium">メールマスター</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import SignUpForm from '../components/organisms/SignUpForm.vue'
</script>
