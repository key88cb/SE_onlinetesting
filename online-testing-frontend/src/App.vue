// 网页UI使用框架 https://chhhhhzh.github.io/SE_project_front/
<template>
  <div class="layout">
    <!-- 条件渲染侧边栏 -->
    <aside v-if="isLoggedIn" class="sidebar">
      <h2>在线测试子系统</h2>
      <nav>
        <!-- 根据角色动态显示菜单 -->
        <router-link to="/student/dashboard" v-if="isStudent">考试信息</router-link>
        <router-link to="/student/results" v-if="isStudent">答题记录</router-link>
        <router-link to="/student/past-papers" v-if="isStudent">历年真题</router-link>

        <router-link to="/teacher/dashboard" v-if="isTeacher">教师首页</router-link>
        <router-link to="/teacher/question-bank" v-if="isTeacher">题库管理</router-link>
        <router-link to="/teacher/create-paper" v-if="isTeacher">编辑发布试卷</router-link>
        <router-link to="/teacher/past-papers" v-if="isTeacher">查看历年卷</router-link>
        <router-link to="/teacher/exam-management" v-if="isTeacher">考试情况管理</router-link>
        <router-link to="/teacher/endedexam-detail" v-if="isTeacher">考试成绩管理</router-link>
      </nav>
    </aside>

    <main class="content">
      <!-- ⭐ 顶栏，右上角用户模块 -->
      <div class="top-bar" v-if="isLoggedIn">
        <div class="time-display">
          {{ currentTime }}
        </div>
        <div class="user-area" @click="toggleDropdown">
          <img src="./assets/default_teacher.png" alt="头像" class="avatar" />
          <span class="username">{{ username }}</span>
          <svg class="arrow" viewBox="0 0 1024 1024" width="12" height="12">
            <path d="M512 672L192 352h640z" fill="#333" />
          </svg>
        </div>

        <div v-if="dropdownVisible" class="dropdown-menu">
          <div class="dropdown-item" @click="logout">退出登录</div>
        </div>
      </div>

      <div class="page-container">
        <router-view />
      </div>
    </main>
  </div>
</template>


<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 用户信息
const username = ref('user')
const role = ref(null) // 'student' or 'teacher'
const dropdownVisible = ref(false)

// 时间显示相关
const currentTime = ref('')
let intervalId = null

// 登录状态判断
const isLoggedIn = computed(() => {
  return route.path.startsWith('/student') || route.path.startsWith('/teacher')
})


// 角色判断
const isStudent = computed(() => { return route.path.startsWith('/student') })
const isTeacher = computed(() => { return route.path.startsWith('/teacher') })

// 格式化时间
const formatTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 页面加载时启动定时器
onMounted(() => {
  currentTime.value = formatTime()
  intervalId = setInterval(() => {
    currentTime.value = formatTime()
  }, 1000) // 每秒更新一次

  // 模拟从 localStorage 获取用户信息（真实项目中应调用接口）
  const user = JSON.parse(localStorage.getItem('user'))
  if (user && user.username && user.role) {
    username.value = user.username
    role.value = user.role
  }
})


// 切换下拉菜单
const toggleDropdown = () => {
  dropdownVisible.value = !dropdownVisible.value
}

// 退出登录
const logout = () => {
  localStorage.removeItem('user') // 清除登录信息
  role.value = null
  username.value = 'user'
  dropdownVisible.value = false
  router.push('/login')
}
</script>


<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', system-ui, -apple-system, sans-serif;
  background-color: var(--background-light);
}

.sidebar {
  width: 260px;
  background: linear-gradient(135deg, #1a237e, #0d47a1);
  color: white;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: var(--shadow-lg);
  transition: var(--transition-base);
  z-index: 100;
}

.sidebar h2 {
  margin-bottom: 32px;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.5px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar nav a {
  display: block;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  margin-bottom: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  transition: var(--transition-base);
  font-size: 15px;
}

.sidebar nav a:hover {
  background-color: rgba(255, 255, 255, 0.15);
  transform: translateX(4px);
}

.sidebar nav a.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
  font-weight: 500;
  color: white;
  box-shadow: var(--shadow-sm);
}

.content {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  background-color: var(--background-light);
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.top-bar {
  width: 100%;
  height: 64px;
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  box-sizing: border-box;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  box-shadow: var(--shadow-sm);
}

.time-display {
  font-size: 14px;
  color: var(--text-light);
  font-weight: 500;
}

.user-area {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  padding: 8px 12px;
  border-radius: 8px;
  transition: var(--transition-base);
}

.user-area:hover {
  background-color: var(--background-light);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
  border: 2px solid var(--border-color);
  transition: var(--transition-base);
}

.user-area:hover .avatar {
  border-color: var(--primary-color);
}

.username {
  font-size: 15px;
  color: var(--text-color);
  margin-right: 8px;
  font-weight: 500;
}

.arrow {
  transform: rotate(0deg);
  transition: var(--transition-base);
}

.user-area:hover .arrow {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 72px;
  right: 32px;
  background: white;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
  border-radius: 8px;
  overflow: hidden;
  z-index: 100;
  min-width: 160px;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  padding: 12px 20px;
  font-size: 14px;
  color: var(--text-color);
  cursor: pointer;
  transition: var(--transition-base);
}

.dropdown-item:hover {
  background-color: var(--background-light);
  color: var(--primary-color);
}

.page-container {
  flex: 1;
  padding: 24px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .sidebar {
    width: 200px;
    padding: 16px;
  }
  
  .top-bar {
    padding: 0 16px;
  }
  
  .page-container {
    padding: 16px;
  }
}
</style>