<template>
  <div class="auto-create-paper">
    <h1>自动组卷</h1>

    <div class="paper-form">
      <div class="form-group">
        <label>试卷名称：</label>
        <input type="text" v-model="paper.title" placeholder="请输入试卷名称" />
      </div>

      <div class="form-group">
        <label>考试科目：</label>
        <select v-model="paper.subject">
          <option value="">请选择科目</option>
          <option v-for="subject in subjects" :key="subject" :value="subject">{{ subject }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>题型分布：</label>
        <div class="checkbox-group">
          <label><input type="checkbox" v-model="config.types" value="单选" /> 单选题</label>
          <label><input type="checkbox" v-model="config.types" value="多选" /> 多选题</label>
          <label><input type="checkbox" v-model="config.types" value="判断" /> 判断题</label>
        </div>
      </div>

      <div class="form-group">
        <label>每种题型数量：</label>
        <div class="quantity-inputs">
          <div class="input-group">
            <span>单选题：</span>
            <input type="number" min="0" max="50" v-model.number="config.singleChoiceCount" />
          </div>
          <div class="input-group">
            <span>多选题：</span>
            <input type="number" min="0" max="50" v-model.number="config.multiChoiceCount" />
          </div>
          <div class="input-group">
            <span>判断题：</span>
            <input type="number" min="0" max="50" v-model.number="config.judgmentCount" />
          </div>
        </div>
      </div>

      <div class="form-group">
        <label>考点范围：</label>
        <input type="text" v-model="config.topics" placeholder="输入考点关键词，用逗号分隔" />
      </div>

      <div class="form-actions">
        <button class="btn preview-btn" @click="previewPaper">预览试卷</button>
      </div>
    </div>

    <!-- 试卷预览模态框 -->
    <div v-if="showPreviewModal" class="modal">
      <div class="modal-content">
        <h2>试卷预览 - {{ paper.title }}</h2>

        <div class="preview-questions">
          <div v-for="(question, index) in previewQuestions" :key="question.id" class="question-preview">
            <div class="question-header">
              <span class="type-badge">{{ question.type }}</span>
              <span class="question-number">{{ index + 1 }}. {{ question.text }}</span>
              <span class="score">{{ question.score }}分</span>
            </div>
            <div class="options">
              <div
                  v-for="(option, optionIndex) in question.options"
                  :key="option.value"
                  :class="['option', { correct: option.isCorrect }]">
                {{ String.fromCharCode(65 + optionIndex) }}. {{ option.label }}
              </div>
            </div>
          </div>

          <div v-if="previewQuestions.length === 0" class="no-questions">
            没有找到相关题目
          </div>
        </div>

        <div class="total-score">
          总分：{{ totalScore }}分
        </div>

        <div class="modal-buttons">
          <button class="btn close-btn" @click="closePreviewModal">关闭</button>
          <button class="btn generate-btn" @click="generateNewPaper">重新生成</button>
          <button class="btn publish-btn" @click="goToPublish">去发布</button>
        </div>
      </div>
    </div>

    <!-- 发布表单模态框 -->
    <div v-if="showPublishModal" class="modal">
      <div class="modal-content">
        <h2>发布考试</h2>

        <div class="form-group">
          <label>考试时间：</label>
          <div class="datetime-pickers">
            <input type="datetime-local" v-model="examSettings.startTime" />
            <span>至</span>
            <input type="datetime-local" v-model="examSettings.endTime" />
          </div>
        </div>

        <div class="form-group">
          <label>考试时长：</label>
          <div class="duration-picker">
            <input type="number" min="10" v-model.number="examSettings.duration" />
            <span>分钟</span>
          </div>
        </div>

        <div class="form-group">
          <label>试卷总分：</label>
          <input type="number" min="1" v-model.number="examSettings.fullScore" disabled />
        </div>

        <div class="form-group">
          <label>及格分数：</label>
          <input type="number" min="0" :max="examSettings.fullScore" v-model.number="examSettings.passingScore" />
        </div>

        <div class="form-group">
          <label>是否允许查看答案：</label>
          <select v-model="examSettings.showAnswersAfter">
            <option value="immediately">立即显示</option>
            <option value="afterEnd">考试结束后显示</option>
            <option value="never">不显示</option>
          </select>
        </div>

        <div class="modal-buttons">
          <button class="btn cancel-btn" @click="cancelPublish">取消</button>
          <button class="btn confirm-btn" @click="confirmPublish">确认发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 当前试卷数据
const paper = ref({
  title: '',
  subject: '',
  questions: []
})

// 考试设置
const examSettings = ref({
  startTime: '',
  endTime: '',
  duration: 60,
  fullScore: 0,
  passingScore: 0,
  showAnswersAfter: 'afterEnd'
})

// 题库数据
const questionBank = ref([
  {
    id: 1,
    type: '单选',
    subject: '操作系统原理',
    text: '进程和线程的主要区别是什么？',
    options: [
      { value: 'A', label: '资源分配的基本单位', isCorrect: false },
      { value: 'B', label: 'CPU调度的基本单位', isCorrect: true },
      { value: 'C', label: '程序运行环境描述', isCorrect: false }
    ]
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
    ]
  },
  {
    id: 3,
    type: '判断',
    subject: '计算机网络',
    text: 'HTTP协议是无状态的协议。',
    options: [
      { value: 'A', label: '正确', isCorrect: true },
      { value: 'B', label: '错误', isCorrect: false }
    ]
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
    ]
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
    ]
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
    ]
  }
])

// 组卷配置
const config = ref({
  types: ['单选'],
  singleChoiceCount: 2,
  multiChoiceCount: 1,
  judgmentCount: 1,
  topics: ''
})

// 控制模态框显示
const showPreviewModal = ref(false)
const showPublishModal = ref(false)

// 所有科目
const subjects = ref(['操作系统原理', '数据库基础', '计算机网络'])

// 计算预览问题
const filteredQuestions = computed(() => {
  let result = [...questionBank.value]

  // 按科目过滤
  if (paper.value.subject) {
    result = result.filter(q => q.subject === paper.value.subject)
  }

  // 按考点过滤
  if (config.value.topics) {
    const topicKeywords = config.value.topics.toLowerCase().split(',').map(t => t.trim())
    result = result.filter(q =>
        topicKeywords.some(topic => q.text.toLowerCase().includes(topic))
    )
  }

  return result
})

// 根据当前配置生成的试卷题目
const previewQuestions = computed(() => {
  let selectedQuestions = []

  // 如果没有选择任何题型，直接返回空数组
  if (config.value.types.length === 0) {
    return []
  }

  // 获取符合要求的题目
  let filtered = filteredQuestions.value

  // 如果没有匹配的题目，直接返回空数组
  if (!filtered.length) {
    return []
  }

  // 按照题型分别随机抽取题目
  config.value.types.forEach(type => {
    const count = getCountByType(type)
    const availableQuestions = filtered.filter(q => q.type === type)

    if (availableQuestions.length >= count) {
      // 如果有足够的题目，随机选取
      const randomQuestions = getRandomQuestions(availableQuestions, count)
      selectedQuestions = [...selectedQuestions, ...randomQuestions]
    } else {
      // 如果题目不足，全部加入
      selectedQuestions = [...selectedQuestions, ...availableQuestions]
    }
  })

  // 添加分数配置
  return selectedQuestions.map(question => ({
    ...question,
    score: getQuestionScore(question)
  }))
})

// 计算总分
const totalScore = computed(() => {
  return previewQuestions.value.reduce((sum, q) => sum + q.score, 0)
})

// 获取各题型数量
const getCountByType = (type) => {
  switch (type) {
    case '单选': return config.value.singleChoiceCount
    case '多选': return config.value.multiChoiceCount
    case '判断': return config.value.judgmentCount
    default: return 0
  }
}

// 根据题型动态计算每题分数
const getQuestionScore = (question) => {
  switch (question.type) {
    case '单选': return 2
    case '多选': return 3
    case '判断': return 1
    default: return 0
  }
}

// 预览试卷
const previewPaper = () => {
  if (!paper.value.title.trim()) {
    alert('请填写试卷名称')
    return
  }

  if (!paper.value.subject) {
    alert('请选择考试科目')
    return
  }

  // 检查是否有题型被选中
  if (config.value.types.length === 0) {
    alert('请选择至少一种题型')
    return
  }

  // 检查题目数量是否足够
  config.value.types.forEach(type => {
    const requiredCount = getCountByType(type)
    const availableCount = filteredQuestions.value.filter(q => q.type === type).length

    if (availableCount < requiredCount) {
      alert(`对于${type}题型，只有${availableCount}道可用题目，少于您要求的${requiredCount}道`)
    }
  })

  // 检查总题数是否为0
  const totalQuestions = previewQuestions.value.length
  if (totalQuestions === 0) {
    alert('根据您的设置，无法生成包含任何题目的试卷')
    return
  }

  showPreviewModal.value = true
}

// 关闭预览模态框
const closePreviewModal = () => {
  showPreviewModal.value = false
}

// 重新生成试卷
const generateNewPaper = () => {
  closePreviewModal()
  previewPaper()
}

// 去发布
const goToPublish = () => {
  closePreviewModal()
  examSettings.value.fullScore = totalScore.value
  showPublishModal.value = true
}

// 取消发布
const cancelPublish = () => {
  showPublishModal.value = false
}

// 确认发布
const confirmPublish = () => {
  if (!examSettings.value.startTime || !examSettings.value.endTime) {
    alert('请填写完整的考试时间')
    return
  }

  if (new Date(examSettings.value.startTime) >= new Date(examSettings.value.endTime)) {
    alert('结束时间必须大于开始时间')
    return
  }

  alert('试卷发布成功！')
  router.push('/teacher/exam-management')
}

// 获取随机题目
const getRandomQuestions = (questionsList, count) => {
  const shuffled = [...questionsList].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, count)
}

// 获取指定类型的题目数量
const getTypeQuestionCount = (type) => {
  return filteredQuestions.value.filter(q => q.type === type).length
}
</script>

<style scoped>
h1{
  color: black;
}
.auto-create-paper {
  padding: 20px;
}

.paper-form {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 20px;
  color: #000000;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1em;
}

.checkbox-group {
  display: flex;
  gap: 15px;
  margin-top: 8px;
}

.checkbox-group label {
  font-weight: normal;
}

.quantity-inputs {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-group span {
  white-space: nowrap;
}

.input-group input[type="number"] {
  width: 60px;
  height: 30px;
  text-align: center;
}

.preview-btn {
  background-color: #2e7d32;
  color: white;
  padding: 10px 20px;
}

.preview-btn:hover {
  background-color: #388e3c;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 25px;
  border-radius: 10px;
  width: 100%;
  max-width: 800px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #0d47a1;
}

.preview-questions {
  margin-top: 20px;
}

.question-preview {
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

.score {
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

.total-score {
  font-size: 1.2em;
  font-weight: bold;
  margin-top: 20px;
  color: #0d47a1;
  text-align: right;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.close-btn {
  background-color: #666;
  color: white;
}

.close-btn:hover {
  background-color: #444;
}

.generate-btn {
  background-color: #0d47a1;
  color: white;
  padding: 10px 20px;
}

.generate-btn:hover {
  background-color: #1565c0;
}

.publish-btn {
  background-color: #2e7d32;
  color: white;
  padding: 10px 20px;
}

.publish-btn:hover {
  background-color: #388e3c;
}

.datetime-pickers {
  display: flex;
  gap: 10px;
  align-items: center;
}

.datetime-pickers span {
  color: #666;
}

.duration-picker {
  display: flex;
  gap: 10px;
  align-items: center;
}

.duration-picker input {
  width: 60px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.confirm-btn {
  background-color: #0d47a1;
  color: white;
  padding: 10px 20px;
}

.confirm-btn:hover {
  background-color: #1565c0;
}

.cancel-btn {
  background-color: #c62828;
  color: white;
  padding: 10px 20px;
}

.cancel-btn:hover {
  background-color: #d32f2f;
}

.no-questions {
  text-align: center;
  color: #666;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 10px;
}
</style>
