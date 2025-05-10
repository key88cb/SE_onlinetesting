<template>
  <div class="exam-questions">
    <h1>开始答题</h1>
    <p><strong>考试名称：</strong>{{ exam.title || exam.subject }}</p>
    <p><strong>剩余时间：</strong>{{ formattedTime }}</p>

    <div v-for="(question, index) in questions" :key="question.id" class="question-item">
      <h3>第 {{ index + 1 }} 题：{{ question.text }}</h3>
      <div class="options">
        <label v-for="option in question.options" :key="option.value">
          <input type="radio" :name="'q' + index" :value="option.value" />
          {{ option.value }}. 
          {{ option.label }}
        </label>
      </div>
    </div>

    <button class="submit-btn" @click="submitExam">提交答卷</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const examId = parseInt(route.params.id)

// 模拟考试题目数据
const questions = ref([
  {
    id: 1,
    text: '什么是进程？',
    options: [
      { value: 'A', label: '资源分配的基本单位' },
      { value: 'B', label: 'CPU调度的基本单位' },
      { value: 'C', label: '程序运行环境描述' }
    ]
  },
  {
    id: 2,
    text: '下列哪项不是线程的优点？',
    options: [
      { value: 'A', label: '轻量级' },
      { value: 'B', label: '共享内存' },
      { value: 'C', label: '独立地址空间' }
    ]
  }
])

// 模拟考试信息
const exam = {
  id: examId,
  title: '操作系统原理期中考试',
  subject: '操作系统原理',
  duration: 3600
}

// 倒计时逻辑
const remainingTime = ref(exam.duration)
const formattedTime = ref('')

const updateFormattedTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  formattedTime.value = `${mins}分钟${pad(secs)}秒`
}

let timer = null
onMounted(() => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
      updateFormattedTime(remainingTime.value)
    } else {
      clearInterval(timer)
      submitExam()
    }
  }, 1000)
})

// 提交试卷
const submitExam = () => {
  alert('提交成功！')

  // 停止计时器
  if (timer) {
    clearInterval(timer)
  }

  // 跳转回考试信息页面
  router.push('/student/dashboard')
}

function pad(n) {
  return n < 10 ? '0' + n : '' + n
}
</script>

<style scoped>
.exam-questions {
  padding: 20px;
  color:black;
}

.exam-info-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.question-item {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.submit-btn {
  margin-top: 30px;
  padding: 12px 24px;
  background-color: #2e7d32;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: #388e3c;
}
</style>
