<template>
  <div class="page-wrapper">
    <h1 class="page-title">考试信息</h1>

    <div class="page-card">
      <div class="search-bar">
        <input
            v-model="searchQuery"
            placeholder="输入课程名称进行搜索"
            class="form-input"
        />
        <button @click="clearSearch" class="btn btn-primary">清除</button>
      </div>

      <div class="tabs">
        <button
            :class="['btn', { 'btn-primary': activeTab === 'ongoing' }]"
            @click="setActiveTab('ongoing')"
        >
          进行中考试
        </button>
        <button
            :class="['btn', { 'btn-primary': activeTab === 'upcoming' }]"
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
          <h3 class="section-title">{{ exam.course }}</h3>
          <div class="exam-info">
            <p><span class="tag tag-primary">科目</span> {{ exam.subject }}</p>
            <p v-if="activeTab === 'ongoing'">
              <span class="tag tag-warning">剩余时间</span> {{ formatTime(exam.remainingTime) }}
            </p>
            <p v-if="activeTab === 'upcoming'">
              <span class="tag tag-info">开始时间</span> {{ formatDate(exam.startTime) }}
            </p>
            <p><span class="tag tag-secondary">出卷人</span> {{ exam.teacher }}</p>
          </div>

          <button
              class="btn btn-primary"
              :disabled="!isExamOngoing(exam)"
              @click.stop="viewExamDetails(exam)"
          >
            {{ isExamOngoing(exam) ? '去考试' : '考试未开始' }}
          </button>
        </div>

        <div v-if="filteredExams.length === 0" class="empty-state">
          没有找到相关考试
        </div>
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
.search-bar {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.search-bar .form-input {
  flex: 1;
  border-radius: var(--border-radius-md) 0 0 var(--border-radius-md);
}

.search-bar .btn {
  border-radius: 0 var(--border-radius-md) var(--border-radius-md) 0;
}

.tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.tabs .btn {
  flex: 1;
  background-color: var(--background-light);
  color: var(--text-color);
}

.tabs .btn.btn-primary {
  color: white;
}

.exam-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.exam-card {
  background: white;
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: var(--transition-base);
  border: 1px solid var(--border-color);
}

.exam-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.exam-info {
  margin: var(--spacing-md) 0;
}

.exam-info p {
  margin: var(--spacing-sm) 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.exam-card .btn {
  width: 100%;
  margin-top: var(--spacing-md);
}

.exam-card .btn:disabled {
  background-color: var(--text-light);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 768px) {
  .exam-list {
    grid-template-columns: 1fr;
  }
  
  .tabs {
    flex-direction: column;
  }
}
</style>
