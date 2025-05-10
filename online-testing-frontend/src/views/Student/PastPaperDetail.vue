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
            {{ option.value }}.
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
console.log(paperId)
const pastPapers = ref([
  {
    id: 1,
    title: '2023年操作系统原理期末考试',
    subject: '操作系统原理',
    year: 2023,
    totalQuestions: 20,
    fullScore: 100
  },
  {
    id: 2,
    title: '2023年需求分析与设计期中考试',
    subject: '需求分析与设计',
    year: 2023,
    totalQuestions: 15,
    fullScore: 75
  },
  {
    id: 3,
    title: '2022年数据库基础期末考试',
    subject: '数据库基础',
    year: 2022,
    totalQuestions: 25,
    fullScore: 100
  },
  {
    id: 4,
    title: '2022年操作系统原理期中考试',
    subject: '操作系统原理',
    year: 2022,
    totalQuestions: 18,
    fullScore: 90
  }
])
// 过滤试卷数据
const paper = ref(pastPapers.value.find(p => p.id === paperId))
// 模拟题目数据
const raw_questions = ref([
  {
    id: 1,
    paper_id: 1,
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
    paper_id: 1,
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
    paper_id: 1,
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
    paper_id: 1,
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
    paper_id: 1,
    text: '以下哪个调度算法可能导致某些进程长期得不到执行？',
    options: [
      { value: 'A', label: '先来先服务(FCFS)' },
      { value: 'B', label: '短作业优先(SJF)' },
      { value: 'C', label: '轮转(RR)' },
      { value: 'D', label: '多级反馈队列' }
    ],
    correctAnswer: 'B',
    explanation: '短作业优先(SJF)可能导致长作业长期等待，产生饥饿现象。'
  },
  {
  id: 6,
  paper_id: 2,
  text: '在软件工程中，UML 是指什么？',
  options: [
    { value: 'A', label: '统一建模语言' },
    { value: 'B', label: '用户模型语言' },
    { value: 'C', label: '通用标记语言' },
    { value: 'D', label: '用户界面建模工具' }
  ],
  correctAnswer: 'A',
  explanation: 'UML 是 Unified Modeling Language 的缩写，用于软件系统建模。'
},
{
  id: 7,
  paper_id: 2,
  text: '用例图的主要作用是什么？',
  options: [
    { value: 'A', label: '展示类之间的关系' },
    { value: 'B', label: '描述系统的功能需求和参与者交互' },
    { value: 'C', label: '表示对象的状态变化' },
    { value: 'D', label: '展示程序执行流程' }
  ],
  correctAnswer: 'B',
  explanation: '用例图用于描述系统提供的功能及其与外部用户的交互。'
},
{
  id: 8,
  paper_id: 2,
  text: '以下哪项是面向对象分析的核心思想？',
  options: [
    { value: 'A', label: '以过程为中心' },
    { value: 'B', label: '以函数为中心' },
    { value: 'C', label: '以对象为中心' },
    { value: 'D', label: '以模块为中心' }
  ],
  correctAnswer: 'C',
  explanation: '面向对象分析强调以对象为中心组织系统结构。'
},
{
  id: 9,
  paper_id: 2,
  text: '下列哪个不是需求规格说明书的作用？',
  options: [
    { value: 'A', label: '作为开发依据' },
    { value: 'B', label: '作为测试依据' },
    { value: 'C', label: '记录项目预算' },
    { value: 'D', label: '作为客户和开发者之间的协议' }
  ],
  correctAnswer: 'C',
  explanation: '需求规格说明书主要描述系统功能与行为，不涉及预算。'
},
{
  id: 10,
  paper_id: 2,
  text: '在系统分析阶段，通常使用哪种图来描述数据流动？',
  options: [
    { value: 'A', label: '活动图' },
    { value: 'B', label: '数据流图' },
    { value: 'C', label: '状态图' },
    { value: 'D', label: '部署图' }
  ],
  correctAnswer: 'B',
  explanation: '数据流图(DFD)用于描述系统中数据的流动和处理过程。'
},
{
  id: 11,
  paper_id: 3,
  text: 'SQL 语言中，用于更新表中记录的命令是？',
  options: [
    { value: 'A', label: 'INSERT' },
    { value: 'B', label: 'SELECT' },
    { value: 'C', label: 'UPDATE' },
    { value: 'D', label: 'DELETE' }
  ],
  correctAnswer: 'C',
  explanation: '`UPDATE` 命令用于修改已有记录的内容。'
},
{
  id: 12,
  paper_id: 3,
  text: '数据库中，主键的作用是什么？',
  options: [
    { value: 'A', label: '唯一标识每一条记录' },
    { value: 'B', label: '加快查询速度' },
    { value: 'C', label: '限制字段长度' },
    { value: 'D', label: '设置默认值' }
  ],
  correctAnswer: 'A',
  explanation: '主键用于确保每条记录的唯一性。'
},
{
  id: 13,
  paper_id: 3,
  text: '下列关于索引的说法中，哪一项是正确的？',
  options: [
    { value: 'A', label: '索引可以提高查询效率' },
    { value: 'B', label: '一个表只能有一个索引' },
    { value: 'C', label: '索引不会占用存储空间' },
    { value: 'D', label: '索引只适用于数值类型' }
  ],
  correctAnswer: 'A',
  explanation: '索引通过创建查找结构来提升查询性能，但会增加存储开销。'
},
{
  id: 14,
  paper_id: 3,
  text: 'SQL 中，`JOIN` 的作用是什么？',
  options: [
    { value: 'A', label: '排序结果' },
    { value: 'B', label: '连接多个表' },
    { value: 'C', label: '分组统计' },
    { value: 'D', label: '删除重复记录' }
  ],
  correctAnswer: 'B',
  explanation: '`JOIN` 用于将两个或多个表的数据进行关联查询。'
},
{
  id: 15,
  paper_id: 3,
  text: '事务的 ACID 特性中，“一致性”指的是？',
  options: [
    { value: 'A', label: '事务要么全部完成，要么全部失败' },
    { value: 'B', label: '事务执行前后数据库的完整性约束未被破坏' },
    { value: 'C', label: '多个事务并发执行时互不干扰' },
    { value: 'D', label: '事务对数据库的修改是永久的' }
  ],
  correctAnswer: 'B',
  explanation: '一致性保证事务执行前后数据处于一致状态。'
},
{
  id: 16,
  paper_id: 4,
  text: '下列哪种调度算法属于抢占式调度？',
  options: [
    { value: 'A', label: '先来先服务(FCFS)' },
    { value: 'B', label: '短作业优先(SJF)' },
    { value: 'C', label: '时间片轮转(RR)' },
    { value: 'D', label: '多级队列调度' }
  ],
  correctAnswer: 'C',
  explanation: '时间片轮转是一种典型的抢占式调度算法。'
},
{
  id: 17,
  paper_id: 4,
  text: '操作系统中，死锁的必要条件不包括？',
  options: [
    { value: 'A', label: '互斥' },
    { value: 'B', label: '请求与保持' },
    { value: 'C', label: '不可抢占资源' },
    { value: 'D', label: '线程同步' }
  ],
  correctAnswer: 'D',
  explanation: '死锁的四个必要条件是互斥、请求与保持、不可抢占资源和循环等待。'
},
{
  id: 18,
  paper_id: 4,
  text: '下列哪项是进程控制块(PCB)中不包含的信息？',
  options: [
    { value: 'A', label: '进程状态' },
    { value: 'B', label: '寄存器快照' },
    { value: 'C', label: '程序源代码' },
    { value: 'D', label: '进程优先级' }
  ],
  correctAnswer: 'C',
  explanation: 'PCB 不保存源代码，而是保存运行时相关状态信息。'
},
{
  id: 19,
  paper_id: 4,
  text: '页表的主要作用是？',
  options: [
    { value: 'A', label: '存储文件数据' },
    { value: 'B', label: '映射虚拟地址到物理地址' },
    { value: 'C', label: '管理进程状态' },
    { value: 'D', label: '分配内存分区' }
  ],
  correctAnswer: 'B',
  explanation: '页表负责将程序使用的虚拟地址转换为实际物理地址。'
},
{
  id: 20,
  paper_id: 4,
  text: '下列哪种存储管理方式会产生外碎片？',
  options: [
    { value: 'A', label: '分页' },
    { value: 'B', label: '分段' },
    { value: 'C', label: '段页式' },
    { value: 'D', label: '固定分区' }
  ],
  correctAnswer: 'B',
  explanation: '分段存储管理容易产生外部碎片，而分页则避免了这一问题。'
}
]
)
const questions = ref([])
// 过滤题目数据
questions.value = raw_questions.value.filter(q => q.paper_id === paperId)
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
  color:black;
}

.paper-info-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  color:black;
}

.info-row {
  margin-bottom: 10px;
  font-size: 1em;
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

.question-options {
  margin-top: 10px;
  margin-left: 20px;
  color:black;
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
  background-color: #f1f8e9;
  color:red;
  border-radius: 6px;
}
</style>
