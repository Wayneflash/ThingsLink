<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="text-center">
      <div class="text-9xl font-bold text-gray-300 mb-4">404</div>
      <h1 class="text-3xl font-bold text-gray-900 mb-4">页面不存在</h1>
      <p class="text-gray-600 mb-8">抱歉，您访问的页面不存在或已被移除</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>
