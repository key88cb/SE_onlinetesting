<template>
  <div class="exam-details">
    <h1>考试详情 - {{ currentExam.title }}</h1>

    <div class="exam-info">
      <div class="info-item">
        <strong>考试科目：</strong>{{ currentExam.subject }}
      </div>
      <div class="info-item">
        <strong>考试时间：</strong>{{ formatDate(currentExam.startTime) }} 至 {{ formatDate(currentExam.endTime) }}
      </div>
      <div class="info-item">
        <strong>考试时长：</strong>{{ currentExam.duration }}分钟
      </div>
      <div class="info-item">
        <strong>试卷总分：</strong>{{ currentExam.fullScore }}
      </div>
      <div class="info-item">
        <strong>及格分数：</strong>{{ currentExam.passingScore }}
      </div>
      <div class="info-item">
        <strong>试题总数：</strong>{{ currentExam.questions.length }}
      </div>
    </div>

    <div class="question-list">
      <h2>考试题目</h2>

      <div class="questions-container">
        <div
            v-for="(question, index) in currentExam.questions"
            :key="question.id"
            class="question-card"
        >
          <div class="question-header">
            <span class="type-badge">{{ question.type }}</span>
            <span class="question-number">{{ index + 1 }}. {{ question.text }}</span>
            <span class="question-score">{{ question.score }}分</span>
          </div>
          <div class="options">
            <div
                v-for="(option, optionIndex) in question.options"
                :key="option.value"
                :class="['option', { correct: option.isCorrect }]"
            >
              {{ String.fromCharCode(65 + optionIndex) }}. {{ option.label }}
            </div>
          </div>
        </div>

        <div v-if="currentExam.questions.length === 0" class="no-questions">
          暂无考试题目
        </div>
      </div>
    </div>

    <div class="actions">
      <button class="btn edit-btn" @click="editExam">编辑考试</button>
      <button class="btn back-btn" @click="goBack">返回列表</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 模拟考试数据
const exams = ref([
  {
    id: 1,
    title: '操作系统原理期中考试',
    subject: '操作系统原理',
    startTime: new Date(Date.now() + 86400000).toISOString(),
    endTime: new Date(Date.now() + 86400000 + 3600000).toISOString(),
    duration: 60,
    fullScore: 100,
    passingScore: 60,
    questions: [1, 2, 3, 4, 5],
    studentsCount: 0
  },
  {
    id: 2,
    title: '数据库基础期末考试',
    subject: '数据库基础',
    startTime: new Date().toISOString(),
    endTime: new Date(Date.now() + 7200000).toISOString(),
    duration: 90,
    fullScore: 100,
    passingScore: 60,
    questions: [2, 6, 1, 5, 3],
    studentsCount: 32
  },
  {
    id: 3,
    title: '计算机网络期中考试',
    subject: '计算机网络',
    startTime: new Date(Date.now() - 3600000).toISOString(),
    endTime: new Date(Date.now() + 3600000).toISOString(),
    duration: 60,
    fullScore: 75,
    passingScore: 45,
    questions: [3, 7, 1, 2, 5],
    studentsCount: 18
  }
])

// 模拟题目数据
const questions = ref([
  {
    id: 1,
    type: '单选',
    subject: '操作系统原理',
    text: '进程和线程的主要区别是什么？',
    options: [
      { value: 'A', label: '资源分配的基本单位', isCorrect: false },
      { value: 'B', label: 'CPU调度的基本单位', isCorrect: true },
      { value: 'C', label: '程序运行环境描述', isCorrect: false }
    ],
    score: 2
  },
  {
    id: 2,
    type: '多选',
    subject: '数据库基础',
    text: '下列哪些是关系型数据库？',
    options: [
      { value: 'A', label: 'MySQL', isCorrect: true },
      { value: 'B', label: 'MongoDB', isCorrect: false },
      { value: 'C', label: 'PostgreSQL', isCorrect: true },
      { value: 'D', label: 'Oracle', isCorrect: true }
    ],
    score: 3
  },
  {
    id: 3,
    type: '判断',
    subject: '计算机网络',
    text: 'HTTP协议是无状态的协议。',
    options: [
      { value: 'A', label: '正确', isCorrect: true },
      { value: 'B', label: '错误', isCorrect: false }
    ],
    score: 1
  },
  {
    id: 4,
    type: '单选',
    subject: '操作系统原理',
    text: '以下哪个调度算法可能导致某些进程长期得不到执行？',
    options: [
      { value: 'A', label: '先来先服务(FCFS)', isCorrect: false },
      { value: 'B', label: '短作业优先(SJF)', isCorrect: true },
      { value: 'C', label: '轮转(RR)', isCorrect: false },
      { value: 'D', label: '多级反馈队列', isCorrect: false }
    ],
    score: 2
  },
  {
    id: 5,
    type: '单选',
    subject: '数据库基础',
    text: '在数据库设计中，规范化的主要目的是什么？',
    options: [
      { value: 'A', label: '提高查询速度', isCorrect: false },
      { value: 'B', label: '减少冗余和更新异常', isCorrect: true },
      { value: 'C', label: '增加数据量', isCorrect: false },
      { value: 'D', label: '提升并发性能', isCorrect: false }
    ],
    score: 2
  },
  {
    id: 6,
    type: '多选',
    subject: '计算机网络',
    text: '下列关于TCP/IP协议的说法哪些是正确的？',
    options: [
      { value: 'A', label: 'TCP是面向连接的协议', isCorrect: true },
      { value: 'B', label: 'IP负责路由寻址', isCorrect: true },
      { value: 'C', label: 'TCP保证传输可靠性', isCorrect: true },
      { value: 'D', label: 'IP提供可靠传输', isCorrect: false }
    ],
    score: 3
  }
])

// 当前考试
const currentExam = ref(null)

onMounted(() => {
  const examId = parseInt(route.params.id)
  currentExam.value = exams.value.find(exam => exam.id == examId)

  // 填充完整考试信息
  if (currentExam.value) {
    currentExam.value.questions = currentExam.value.questions.map(id => {
      const question = questions.value.find(q => q.id == id)
      return question ? { ...question } : null
    }).filter(Boolean)
  }
})

// 日期格式化
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 编辑考试
const editExam = () => {
  alert('进入编辑模式')
  // 这里可以跳转到编辑页面或打开编辑模态框
}

// 返回考试列表
const goBack = () => {
  router.push('/teacher/exam-management')
}
</script>

<style scoped>
h1 {
  color: #000000;
}
.exam-details {
  padding: 20px;
}

.exam-info {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  flex: 1;
  min-width: 250px;
  margin-bottom: 10px;
  font-size: 1em;
  color: #333;
}

.question-list {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.questions-container {
  margin-top: 15px;
}

.question-card {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  border-left: 4px solid #0d47a1;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.type-badge {
  background-color: #0d47a1;
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.9em;
}

.question-number {
  flex: 1;
  margin: 0 10px;
  font-weight: bold;
}

.question-score {
  font-weight: bold;
  color: #0d47a1;
}

.options {
  margin-left: 20px;
  margin-bottom: 10px;
}

.option {
  display: block;
  margin: 5px 0;
  padding: 5px 8px;
  border-radius: 4px;
}

.option.correct {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 15px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.edit-btn {
  background-color: #2e7d32;
  color: white;
}

.edit-btn:hover {
  background-color: #388e3c;
}

.back-btn {
  background-color: #666;
  color: white;
}

.back-btn:hover {
  background-color: #444;
}
</style>
