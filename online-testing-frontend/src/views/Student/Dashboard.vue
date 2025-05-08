<template>
  <div class="student-dashboard">
    <h1>考试信息</h1>

    <div class="search-bar">
      <input
          v-model="searchQuery"
          placeholder="输入课程名称进行搜索"
          class="search-input"
      />
      <button @click="clearSearch" class="clear-btn">清除</button>
    </div>

    <div class="tabs">
      <button
          :class="['tab-btn', { active: activeTab === 'ongoing' }]"
          @click="setActiveTab('ongoing')"
      >
        进行中考试
      </button>
      <button
          :class="['tab-btn', { active: activeTab === 'upcoming' }]"
          @click="setActiveTab('upcoming')"
      >
        未开始考试
      </button>
    </div>

    <div class="exam-list">
      <div
          v-for="exam in filteredExams"
          :key="exam.id"
          class="exam-card"
          @click="viewExamDetails(exam)"
      >
        <h3>{{ exam.course }}</h3>
        <p>科目：{{ exam.subject }}</p>
        <p v-if="activeTab === 'ongoing'">剩余时间：{{ formatTime(exam.remainingTime) }}</p>
        <p v-if="activeTab === 'upcoming'">开始时间：{{ formatDate(exam.startTime) }}</p>
        <p>出卷人：{{ exam.teacher }}</p>

        <!-- 根据考试状态显示不同按钮 -->
        <button
            class="start-btn"
            :disabled="!isExamOngoing(exam)"
            @click.stop="viewExamDetails(exam)"
        >
          {{ isExamOngoing(exam) ? '去考试' : '考试未开始' }}
        </button>
      </div>

      <div v-if="filteredExams.length === 0" class="no-results">
        没有找到相关考试
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 示例数据 - 实际应从API获取
const exams = ref([
  {
    id: 1,
    course: '计算机基础',
    subject: '操作系统原理',
    teacher: '张老师',
    startTime: new Date().toISOString(),
    endTime: new Date(Date.now() + 3600 * 1000).toISOString(), // 1小时后结束
    remainingTime: 3600,
    totalQuestions: 20,
    fullScore: 100
  },
  {
    id: 2,
    course: '软件工程',
    subject: '需求分析与设计',
    teacher: '李老师',
    startTime: new Date(Date.now() + 86400 * 1000).toISOString(), // 一天后开始
    endTime: new Date(Date.now() + 86400 * 1000 + 3600 * 1000).toISOString(),
    remainingTime: 3600,
    totalQuestions: 15,
    fullScore: 75
  }
])

const searchQuery = ref('')
const activeTab = ref('ongoing')

// 判断考试是否进行中
const isExamOngoing = (exam) => {
  const now = new Date()
  return new Date(exam.startTime) <= now && now <= new Date(exam.endTime)
}

// 跳转至考试详情页
const viewExamDetails = (exam) => {
  router.push(`/student/exam/${exam.id}/detail`)
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

// 格式化剩余时间
const formatTime = (seconds) => {
  const hrs = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${pad(hrs)}:${pad(mins)}:${pad(secs)}`
}

// 辅助函数：补零
const pad = (num) => String(num).padStart(2, '0')

// 过滤逻辑
const filteredExams = computed(() => {
  let result = [...exams.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(exam =>
        exam.course.toLowerCase().includes(query) ||
        exam.subject.toLowerCase().includes(query)
    )
  }

  const now = new Date()
  if (activeTab.value === 'ongoing') {
    result = result.filter(exam => new Date(exam.startTime) <= now && now <= new Date(exam.endTime))
  } else if (activeTab.value === 'upcoming') {
    result = result.filter(exam => new Date(exam.startTime) > now)
  }

  return result
})

// 切换 tab
const setActiveTab = (tab) => {
  activeTab.value = tab
}

// 清除搜索
const clearSearch = () => {
  searchQuery.value = ''
}
</script>

<style scoped>
.student-dashboard {
  padding: 20px;
}

.search-bar {
  display: flex;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px 0 0 6px;
  font-size: 1em;
}

.clear-btn {
  padding: 10px 20px;
  background-color: #0d47a1;
  color: white;
  border: none;
  border-radius: 0 6px 6px 0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.clear-btn:hover {
  background-color: #1565c0;
}

.tabs {
  display: flex;
  margin-bottom: 20px;
}

.tab-btn {
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

.tab-btn.active {
  background-color: #0d47a1;
  color: white;
}

.tab-btn:last-child {
  margin-right: 0;
}

.exam-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.exam-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.exam-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.exam-card h3 {
  margin-top: 0;
  color: #0d47a1;
}

.start-btn {
  margin-top: 15px;
  padding: 10px;
  background-color: #0d47a1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.start-btn:hover:not([disabled]) {
  background-color: #1565c0;
}

.start-btn[disabled] {
  background-color: #ccc;
  color: #666;
  cursor: not-allowed;
}

.no-results {
  text-align: center;
  color: #666;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 10px;
}
</style>
