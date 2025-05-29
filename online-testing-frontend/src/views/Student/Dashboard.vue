<template>
  <div class="student-dashboard">
    <h1>考试信息</h1>

    <div class="search-bar">
      <input
          v-model="searchQuery"
          placeholder="输入课程Id或者考试名称进行搜索"
          class="search-input"
      />
      <button @click="clearSearch" class="clear-btn">清除</button>
    </div>

    <div class="tabs">
      <button
          :class="['tab-btn', { active: currentTab === 'ongoing' }]"
          @click="setActiveTab('ongoing')"
      >
        进行中考试
      </button>
      <button
          :class="['tab-btn', { active: currentTab === 'notStarted' }]"
          @click="setActiveTab('notStarted')"
      >
        未开始考试
      </button>
    </div>

    <div class="exam-list">
      <div
          v-for="exam in filteredExams"
          :key="exam.paperId"
          class="exam-card"
          @click="viewExamInfo(exam)"
      >
        <h3>考试名称：{{ exam.paperName }}</h3>
        <p>课程ID：{{ exam.courseId }}</p>
        <p>出卷人：{{ exam.creator }}</p>
        <p v-if="currentTab === 'ongoing'">
          结束时间：{{ formatDate(exam.closeTime) }}
        </p>
        <p v-if="currentTab === 'notStarted'">
          开始时间：{{ formatDate(exam.openTime) }}
        </p>
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

    <!-- 弹窗展示考试详情 -->
    <div v-if="showDialog" class="exam-dialog-overlay" @click.self="closeDialog">
      <div class="exam-dialog">
        <h2>{{ selectedExam.paperName }}</h2>
        <p>课程ID：{{ selectedExam.courseId }}</p>
        <p>出卷人：{{ selectedExam.creator }}</p>
        <p>开始时间：{{ formatDate(selectedExam.openTime) }}</p>
        <p>结束时间：{{ formatDate(selectedExam.closeTime) }}</p>
        <p>单选题数：{{ selectedExam.singleChoiceNum ?? '未知' }}</p>
        <p>多选题数：{{ selectedExam.multipleChoiceNum ?? '未知' }}</p>
        <p>判断题数：{{ selectedExam.trueFalseNum ?? '未知' }}</p>
        <p>总分：{{ selectedExam.totalScores ?? '未知' }}</p>
        <button @click="closeDialog" class="dialog-close-btn">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const currentTab = ref('ongoing')
const notStartedExams = ref([])
const ongoingExams = ref([])
const searchQuery = ref('')

const showDialog = ref(false)
const selectedExam = ref(null)

// 拉取考试数据
const fetchExams = async () => {
  try {
    // console.log("fetchExams")
    const [notStartedRes, ongoingRes] = await Promise.all([
      axios.get('http://localhost:8080/api/paper-questions/exams', { params: { status: 'notStarted' } }),
      axios.get('http://localhost:8080/api/paper-questions/exams', { params: { status: 'ongoing' } })
    ])
    notStartedExams.value = notStartedRes.data || []
    ongoingExams.value = ongoingRes.data || []
  } catch (e) {
    console.error('获取考试信息失败', e)
  }
}

onMounted(() => {
  fetchExams()
})

// 切换 tab
const setActiveTab = (tab) => {
  currentTab.value = tab
}

// 判断考试是否进行中
const isExamOngoing = (exam) => {
  const now = new Date()
  return new Date(exam.openTime) <= now && now <= new Date(exam.closeTime)
}

// 跳转至考试详情页
const viewExamDetails = (exam) => {
  if(isExamOngoing(exam)){
    return router.push(`/student/exam/${exam.courseId}/${exam.paperId}`)
  }
}

// 弹窗展示考试详情
const viewExamInfo = (exam) => {
  console.log(exam)
  selectedExam.value = exam
  showDialog.value = true
}

// 关闭弹窗
const closeDialog = () => {
  showDialog.value = false
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}
const pad = (num) => String(num).padStart(2, '0')

// 搜索和筛选
const filteredExams = computed(() => {
  let exams = currentTab.value === 'ongoing' ? ongoingExams.value : notStartedExams.value
  if (searchQuery.value) {
    const query = searchQuery.value.trim().toLowerCase()
    exams = exams.filter(exam =>
        String(exam.courseId).includes(query) ||
        (exam.paperName && exam.paperName.toLowerCase().includes(query))
    )
  }
  return exams
})

// 清除搜索
const clearSearch = () => {
  searchQuery.value = ''
}
</script>

<style scoped>
h1{
  color: black;
}
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
  color: black;
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

.exam-dialog-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.exam-dialog {
  background: #fff;
  padding: 32px 28px;
  border-radius: 10px;
  min-width: 320px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  text-align: left;
}
.dialog-close-btn {
  margin-top: 18px;
  padding: 8px 24px;
  background: #0d47a1;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.dialog-close-btn:hover {
  background: #1565c0;
}
</style>