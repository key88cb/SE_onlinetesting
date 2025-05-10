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

// 组件卸载时清除定时器，防止内存泄漏
onUnmounted(() => {
  clearInterval(intervalId)
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
/* 整个布局占满浏览器 */
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 侧边栏固定宽度 */
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #0d47a1, #1565c0);
  color: white;
  padding: 20px;
  box-sizing: border-box;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.sidebar h2 {
  margin-bottom: 30px;
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 1px;
}

.sidebar nav a {
  display: block;
  color: white;
  text-decoration: none;
  margin-bottom: 15px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.sidebar nav a:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.sidebar nav a.router-link-active {
  font-weight: bold;
  border-left: 4px solid white;
  padding-left: 8px;
}

/* 内容区域 */
.content {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  background-color: #f5f5f5;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* 顶部栏 */
.top-bar {
  width: 100%;
  height: 60px;
  background-color: #ffffff;
  display: flex;
  justify-content: space-between; /* 修改为 space-between */
  align-items: center;
  padding: 0 30px;
  box-sizing: border-box;
  border-bottom: 1px solid #ddd;
  position: relative;
}

.time-display {
  font-size: 14px;
  color: #333;
}

/* 用户信息区域 */
.user-area {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 10px;
}

.username {
  font-size: 15px;
  color: #333;
  margin-right: 5px;
}

.arrow {
  transform: rotate(0deg);
  transition: transform 0.2s;
}

.user-area:hover .arrow {
  transform: rotate(180deg);
}

/* 下拉菜单 */
.dropdown-menu {
  position: absolute;
  top: 60px;
  right: 30px;
  background: white;
  border: 1px solid #ddd;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  overflow: hidden;
  z-index: 100;
}

.dropdown-item {
  padding: 10px 20px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.3s;
}

.dropdown-item:hover {
  background-color: #f0f0f0;
}

/* 页面内容容器 */
.page-container {
  width: 100%;
  flex-grow: 1;
  padding: 20px;
  box-sizing: border-box;
}

/* 其他美化 */
h1 {
  font-size: 28px;
  color: #0d47a1;
  border-bottom: 2px solid #0d47a1;
  padding-bottom: 8px;
  margin-bottom: 20px;
}

button, .el-button {
  background-color: #0d47a1 !important;
  color: white !important;
  border: none !important;
  border-radius: 6px !important;
  padding: 10px 20px !important;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(13, 71, 161, 0.3);
  transition: background-color 0.3s, transform 0.2s;
}

button:hover, .el-button:hover {
  background-color: #1565c0 !important;
  transform: translateY(-2px);
}

.el-table {
  border-radius: 10px;
  overflow: hidden;
}

.el-table th {
  background-color: #0d47a1;
  color: white;
  font-weight: bold;
}

.submenu-group {
  margin-bottom: 20px;
}

.submenu-title {
  font-weight: bold;
  margin: 10px 0 5px;
  padding-left: 4px;
  font-size: 15px;
  color: #f0f0f0;
}

.submenu-group a {
  padding-left: 20px;
  font-size: 14px;
}

.menu-group {
  margin-bottom: 15px;
}

.menu-title {
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.menu-title:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.submenu {
  padding-left: 16px;
  display: flex;
  flex-direction: column;
}

.submenu a {
  font-size: 14px;
  margin: 4px 0;
  padding: 6px 10px;
  border-radius: 4px;
  text-decoration: none;
  color: white;
}

.submenu a.router-link-active {
  font-weight: bold;
  border-left: 3px solid white;
  padding-left: 7px;
}

.arrow-icon {
  margin-left: auto;
  font-size: 12px;
  transform: rotate(0deg);
  transition: transform 0.3s ease;
}

.arrow-icon.rotated {
  transform: rotate(180deg);
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  height: 0;
}
</style>