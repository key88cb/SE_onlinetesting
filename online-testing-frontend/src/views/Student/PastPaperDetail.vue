<template>
  <div class="past-paper-detail">
    <h1>历年真题试卷详情</h1>

    <div class="paper-info-card">
      <div class="info-row"><strong>试卷名称：</strong>{{ paper.title }}</div>
      <div class="info-row"><strong>科目：</strong>{{ paper.subject }}</div>
      <div class="info-row"><strong>年份：</strong>{{ paper.year }}</div>
      <div class="info-row"><strong>题量：</strong>{{ paper.totalQuestions }} 题</div>
      <div class="info-row"><strong>满分：</strong>{{ paper.fullScore }}</div>
    </div>

    <div class="questions-list">
      <div
          v-for="(question, index) in questions"
          :key="question.id"
          class="question-card"
      >
        <h3>第 {{ index + 1 }} 题：{{ question.text }}</h3>

        <div class="question-options">
          <label
              v-for="option in question.options"
              :key="option.value"
              :class="[
              'option-label',
              {
                'selected': option.value === question.correctAnswer
              }
            ]"
          >
            <input
                type="radio"
                :name="'q' + index"
                :value="option.value"
                :checked="option.value === question.correctAnswer"
                disabled
            />
            {{ option.label }}
          </label>
        </div>

        <div class="question-explanation">
          <p>答案：{{ getOptionLabel(question.correctAnswer) }}</p>
          <p v-if="question.explanation">解析：{{ question.explanation }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 模拟试卷信息
const paperId = parseInt(route.params.id)

const paper = ref({
  id: paperId,
  title: '2023年操作系统原理期末考试',
  subject: '操作系统原理',
  year: 2023,
  totalQuestions: 5,
  fullScore: 100
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
    correctAnswer: 'B',
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
    correctAnswer: 'C',
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
    correctAnswer: 'D',
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
    correctAnswer: 'B',
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
    correctAnswer: 'B',
    explanation: '短作业优先(SJF)可能导致长作业长期等待，产生饥饿现象。'
  }
])

// 获取选项标签
const getOptionLabel = (value) => {
  const question = questions.value.find(q => q.correctAnswer === value)
  const option = question?.options.find(o => o.value === value)
  return option?.label || ''
}
</script>

<style scoped>
.past-paper-detail {
  padding: 20px;
}

.paper-info-card {
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

.questions-list {
  margin-bottom: 30px;
}

.question-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
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
  background-color: #e8f5e9;
  font-weight: bold;
}

.question-explanation {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 6px;
}
</style>
