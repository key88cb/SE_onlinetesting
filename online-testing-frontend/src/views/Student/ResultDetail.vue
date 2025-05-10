<template>
  <div class="result-detail">
    <h1>考试详细结果</h1>

    <div class="exam-info-card">
      <div class="info-row"><strong>课程：</strong>{{ exam.course }}</div>
      <div class="info-row"><strong>科目：</strong>{{ exam.subject }}</div>
      <div class="info-row"><strong>考试时间：</strong>{{ formatDate(exam.date) }}</div>
      <div class="info-row"><strong>用时：</strong>{{ formatDuration(exam.duration) }}</div>
      <div class="info-row total-score">
        <span><strong>总分：</strong>{{ exam.fullScore }}</span>
        <span :class="['score-badge', getScoreBadgeClass(exam.score)]">{{ exam.score }}分</span>
      </div>
    </div>

    <div class="questions-list">
      <div
          v-for="(question, index) in questions"
          :key="question.id"
          class="question-card"
          :class="{ 'correct': question.isCorrect, 'incorrect': !question.isCorrect }"
      >
        <h3>第 {{ index + 1 }} 题：{{ question.text }}</h3>

        <div class="question-options">
          <label
              v-for="option in question.options"
              :key="option.value"
              :class="[
              'option-label',
              {
                'selected': option.value === question.userAnswer,
                'correct': option.value === question.correctAnswer && option.value === question.userAnswer,
                'wrong-answer': option.value === question.userAnswer && option.value !== question.correctAnswer,
                'correct-answer': option.value === question.correctAnswer
              }
            ]"
          >
            <input
                type="radio"
                :name="'q' + index"
                :value="option.value"
                :checked="option.value === question.userAnswer"
                disabled
            />
            {{ option.label }}
          </label>
        </div>

        <div class="question-result">
          <p>本题得分：{{ question.questionScore }}/{{ question.fullScore }}</p>
          <p v-if="question.explanation">解析：{{ question.explanation }}</p>
        </div>
      </div>
    </div>

    <div class="total-score-section">
      <h2>总成绩：{{ exam.score }}/{{ exam.fullScore }}</h2>
      <p>获得等级：{{ getScoreLevel(exam.score) }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 模拟考试数据
const examId = parseInt(route.params.id)

const exam = ref({
  id: examId,
  course: '计算机基础',
  subject: '操作系统原理',
  date: new Date().toISOString(),
  duration: 3600,
  fullScore: 100,
  score: 85
})

// 模拟题目数据
const questions = ref([
  {
    id: 1,
    text: '进程和线程的主要区别是什么？',
    options: [
      { value: 'A', label: '资源分配的基本单位' },
      { value: 'B', label: 'CPU调度的基本单位' },
      { value: 'C', label: '程序运行环境描述' },
      { value: 'D', label: '内存管理的基本单位' }
    ],
    userAnswer: 'B',
    correctAnswer: 'B',
    fullScore: 5,
    questionScore: 5,
    explanation: '进程是资源分配的基本单位，线程是CPU调度的基本单位。'
  },
  {
    id: 2,
    text: '下列哪项不是线程的优点？',
    options: [
      { value: 'A', label: '轻量级' },
      { value: 'B', label: '共享内存' },
      { value: 'C', label: '独立地址空间' },
      { value: 'D', label: '通信容易' }
    ],
    userAnswer: 'C',
    correctAnswer: 'C',
    fullScore: 5,
    questionScore: 5,
    explanation: '线程共享同一地址空间，这是它们与进程的主要区别之一。'
  },
  {
    id: 3,
    text: '在操作系统中，进程状态不包括以下哪种状态？',
    options: [
      { value: 'A', label: '就绪' },
      { value: 'B', label: '阻塞' },
      { value: 'C', label: '执行' },
      { value: 'D', label: '销毁' }
    ],
    userAnswer: 'D',
    correctAnswer: 'D',
    fullScore: 5,
    questionScore: 5,
    explanation: '进程的三个基本状态是就绪、执行和阻塞。'
  },
  {
    id: 4,
    text: '下面关于分页存储管理系统的描述正确的是？',
    options: [
      { value: 'A', label: '允许程序员使用连续的地址空间' },
      { value: 'B', label: '将主存分成与页大小相同的块' },
      { value: 'C', label: '产生内部碎片' },
      { value: 'D', label: '产生外部碎片' }
    ],
    userAnswer: 'B',
    correctAnswer: 'B',
    fullScore: 5,
    questionScore: 5,
    explanation: '分页存储管理系统将主存分成与页大小相同的块，从而避免了外部碎片的产生，但可能产生内部碎片。'
  },
  {
    id: 5,
    text: '以下哪个调度算法可能导致某些进程长期得不到执行？',
    options: [
      { value: 'A', label: '先来先服务(FCFS)' },
      { value: 'B', label: '短作业优先(SJF)' },
      { value: 'C', label: '轮转(RR)' },
      { value: 'D', label: '多级反馈队列' }
    ],
    userAnswer: 'B',
    correctAnswer: 'B',
    fullScore: 5,
    questionScore: 5,
    explanation: '短作业优先(SJF)可能导致长作业长期等待，产生饥饿现象。'
  }
])

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

// 格式化持续时间
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分钟${pad(secs)}秒`
}

// 补零函数
const pad = (num) => String(num).padStart(2, '0')

// 获取分数等级
const getScoreLevel = (score) => {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  if (score >= 60) return '及格'
  return '不及格'
}

// 获取分数徽章类
const getScoreBadgeClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 60) return 'pass'
  return 'fail'
}
</script>

<style scoped>
.result-detail {
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
}

.total-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
}

.score-badge {
  padding: 5px 10px;
  border-radius: 6px;
  font-weight: bold;
}

.excellent {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.good {
  background-color: #e0f2f1;
  color: #00796b;
}

.pass {
  background-color: #fff9c4;
  color: #fbc02d;
}

.fail {
  background-color: #ffebee;
  color: #c62828;
}

.questions-list {
  margin-bottom: 30px;
  color:black;
}

.question-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.question-card.correct {
  border-left: 4px solid #2e7d32;
}

.question-card.incorrect {
  border-left: 4px solid #c62828;
}

.question-options {
  margin-top: 10px;
  margin-left: 20px;
}

.option-label {
  display: block;
  margin: 8px 0;
  padding: 8px;
  border-radius: 4px;
}

.option-label.selected {
  background-color: #e3f2fd;
}

.option-label.correct-answer {
  background-color: #e8f5e9;
  font-weight: bold;
}

.option-label.wrong-answer {
  background-color: #ffebee;
}

.question-result {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f5f5;
  color:red;
  border-radius: 6px;
}

.total-score-section {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}
</style>
