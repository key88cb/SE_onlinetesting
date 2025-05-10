<template>
  <div class="exam-detail">
    <h1>考试详情</h1>

    <div class="exam-info-card">
      <div class="info-row"><strong>课程：</strong>{{ exam.course }}</div>
      <div class="info-row"><strong>科目：</strong>{{ exam.subject }}</div>
      <div class="info-row"><strong>出卷人：</strong>{{ exam.teacher }}</div>
      <div class="info-row"><strong>开放时间：</strong>{{ formatDate(exam.startTime) }}</div>
      <div class="info-row"><strong>结束时间：</strong>{{ formatDate(exam.endTime) }}</div>
      <div class="info-row"><strong>题量：</strong>{{ exam.totalQuestions }} 题</div>
      <div class="info-row"><strong>满分：</strong>{{ exam.fullScore }}</div>
    </div>

    <!-- 动态按钮 -->
    <button
        class="start-btn"
        :disabled="!isExamOngoing"
        @click="startTheExam"
    >
      {{ isExamOngoing ? '开始答题' : '考试未开始' }}
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 示例数据 - 实际应从 API 获取
const exams = [
  {
    id: 1,
    course: '计算机基础',
    subject: '操作系统原理',
    teacher: '张老师',
    startTime: new Date().toISOString(),
    endTime: new Date(Date.now() + 3600 * 1000).toISOString(), // 1小时后结束
    fullScore: 100,
    totalQuestions: 20
  },
  {
    id: 2,
    course: '软件工程',
    subject: '需求分析与设计',
    teacher: '李老师',
    startTime: new Date(Date.now() + 86400 * 1000).toISOString(), // 一天后开始
    endTime: new Date(Date.now() + 86400 * 1000 + 3600 * 1000).toISOString(),
    fullScore: 75,
    totalQuestions: 15
  }
]

// 根据路由参数获取考试 ID
const examId = parseInt(route.params.id)
const exam = ref(exams.find(e => e.id === examId))

if (!exam.value) {
  alert('考试不存在')
  router.push('/student/dashboard')
}

// 判断考试是否进行中
const isExamOngoing = computed(() => {
  const now = new Date()
  return now >= new Date(exam.value.startTime) && now <= new Date(exam.value.endTime)
})

// 跳转答题页
const startTheExam = () => {
  if (isExamOngoing.value) {
    router.push(`/student/exam/${exam.value.id}/questions`)
  }
}

// 时间格式化函数
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}
const pad = (num) => String(num).padStart(2, '0')

</script>

<style scoped>
.exam-detail {
  padding: 20px;
  color:black;
}

.exam-info-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.info-row {
  margin-bottom: 10px;
  font-size: 1em;
  font-weight: 500;
}

.start-btn {
  padding: 12px 24px;
  background-color: #0d47a1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1em;
  transition: background-color 0.3s;
}

.start-btn:hover:not([disabled]) {
  background-color: #1565c0;
}

.start-btn[disabled] {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>
