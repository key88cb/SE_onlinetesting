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
  // console.log(`Logging in as ${userType.value} with ID: ${credentials.value.id} and Password: ${credentials.value.password}`)
  const userInfo = {
    role: userType.value,
    userId: credentials.value.id
  }
  localStorage.setItem('user', JSON.stringify(userInfo))
  if (userType.value === 'student') {
    router.push('/student/dashboard')
  } else {
    router.push('/teacher/dashboard')
  }
}
</script>

<style scoped>
.login {
  padding: 20px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--background-light), #e3f2fd);
}

h1 {
  color: var(--text-color);
  font-size: 2.5em;
  margin-bottom: 2rem;
  text-align: center;
  font-weight: 600;
  letter-spacing: 1px;
}

.login-container {
  width: 100%;
  max-width: 420px;
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  transition: var(--transition-base);
}

.login-container:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.user-type {
  display: flex;
  margin-bottom: 2rem;
  gap: 1rem;
}

.type-btn {
  flex: 1;
  padding: 0.8rem;
  background-color: var(--background-light);
  color: var(--text-color);
  font-weight: 500;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition-base);
}

.type-btn:hover {
  background-color: var(--background-light);
  transform: translateY(-1px);
}

.type-btn.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333333;
  font-size: 0.95rem;
}

.form-group input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 1rem;
  transition: var(--transition-base);
  background-color: white;
  color: var(--text-color);
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
  background-color: white;
}

.form-group input::placeholder {
  color: #9e9e9e;
  font-weight: 400;
}

.submit-btn {
  width: 100%;
  padding: 0.8rem;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition-base);
  margin-top: 1rem;
}

.submit-btn:hover {
  background-color: #357abd;
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.submit-btn:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 480px) {
  .login-container {
    padding: 1.5rem;
  }
  
  h1 {
    font-size: 2em;
  }
  
  .type-btn {
    padding: 0.6rem;
  }
}
</style>
