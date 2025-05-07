<template>
  <div class="home">
    <h1>在线测试系统</h1>
    <div class="welcome-message">
      <p>欢迎使用在线测试系统</p>
      <div class="action-buttons">
        <router-link to="/test" class="btn">开始测试</router-link>
        <router-link to="/result" class="btn">查看结果</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
// 组件逻辑
</script>

<style scoped>
.home {
  padding: 20px;
}

.welcome-message {
  text-align: center;
  margin-top: 50px;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn {
  padding: 12px 24px;
  background-color: #0d47a1;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.btn:hover {
  background-color: #1565c0;
}
</style> 