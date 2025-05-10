<template>
  <div class="login">
    <h1>在线测试系统</h1>
    <div class="login-container">
      <div class="user-type">
        <button
            :class="['type-btn', { active: userType === 'student' }]"
            @click="setUserType('student')"
        >
          学生登录
        </button>
        <button
            :class="['type-btn', { active: userType === 'teacher' }]"
            @click="setUserType('teacher')"
        >
          教师登录
        </button>
      </div>

      <form @submit.prevent="login" class="login-form">
        <div class="form-group">
          <label v-if="userType === 'student'">学号</label>
          <label v-else>工号</label>
          <input
              type="text"
              v-model="credentials.id"
              :placeholder="userType === 'student' ? '请输入学号' : '请输入工号'"
              required
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
              type="password"
              v-model="credentials.password"
              placeholder="请输入密码"
              required
          />
        </div>

        <button type="submit" class="submit-btn">
          登录
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userType = ref('student')
const credentials = ref({
  id: '',
  password: ''
})

const setUserType = (type) => {
  userType.value = type
  credentials.value = { id: '', password: '' }
}

const login = () => {
  // 这里添加实际的登录验证逻辑
  if (userType.value === 'student') {
    router.push('/student/dashboard')
  } else {
    router.push('/teacher/dashboard')
  }
}
</script>

<style scoped>
h1{
  color: black;
}
.login {
  padding: 20px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.user-type {
  display: flex;
  margin-bottom: 25px;
}

.type-btn {
  flex: 1;
  padding: 12px;
  background-color: #e0e0e0;
  color: #333;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  margin-right: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.type-btn.active {
  background-color: #0d47a1;
  color: white;
}

.type-btn:last-child {
  margin-right: 0;
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1em;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #0d47a1;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover {
  background-color: #1565c0;
}
</style>
