<template>
  <div class="create-paper">
    <h1>编辑发布试卷</h1>

    <div class="paper-preview">
      <div class="paper-header">
        <h2>试卷模板</h2>
        <button class="btn preview-btn">预览试卷</button>
      </div>

      <div class="paper-info">
        <div class="form-group">
          <label>考试名称：</label>
          <input type="text" v-model="paper.title" placeholder="请输入考试名称" />
        </div>

        <div class="form-group">
          <label>考试科目：</label>
          <select v-model="paper.subject">
            <option value="">请选择科目</option>
            <option v-for="subject in subjects" :key="subject" :value="subject">{{ subject }}</option>
          </select>
        </div>
      </div>

      <div class="question-types">
        <div class="type-card" @click="showQuestionType('manual')">
          <h3>手动组卷</h3>
          <p>通过输入题目ID手动选择题目组成试卷</p>
          <div class="question-inputs">
            <input
                type="text"
                v-for="n in manualQuestionsCount"
                :key="n"
                v-model="manualQuestionIds[n-1]"
                placeholder="请输入题目ID"
                @blur="loadManualQuestion(n-1)"
            />
          </div>
        </div>

        <div class="type-card" @click="showQuestionType('auto')">
          <h3>自动组卷</h3>
          <p>系统根据参数自动生成试卷</p>

          <div class="auto-options">
            <div class="form-group">
              <label>题型分布：</label>
              <div class="checkbox-group">
                <label><input type="checkbox" v-model="autoConfig.types" value="单选" /> 单选题</label>
                <label><input type="checkbox" v-model="autoConfig.types" value="多选" /> 多选题</label>
                <label><input type="checkbox" v-model="autoConfig.types" value="判断" /> 判断题</label>
                <label><input type="checkbox" v-model="autoConfig.types" value="简答" /> 简答题</label>
              </div>
            </div>

            <div class="form-group">
              <label>每种题型数量：</label>
              <div class="quantity-inputs">
                <input type="number" min="0" max="50" v-model.number="autoConfig.singleChoiceCount" placeholder="单选题数量" />
                <input type="number" min="0" max="50" v-model.number="autoConfig.multipleChoiceCount" placeholder="多选题数量" />
                <input type="number" min="0" max="50" v-model.number="autoConfig.judgmentCount" placeholder="判断题数量" />
                <input type="number" min="0" max="50" v-model.number="autoConfig.shortAnswerCount" placeholder="简答题数量" />
              </div>
            </div>

            <div class="form-group">
              <label>难度比例：</label>
              <div class="slider-container">
                <div class="slider-row">
                  <span>易</span>
                  <input type="range" min="0" max="100" v-model.number="autoConfig.easyPercentage" />
                  <span>难</span>
                </div>
                <div class="percentage-display">
                  当前设置：{{ autoConfig.easyPercentage }}% 易，{{ 100 - autoConfig.easyPercentage }}% 难
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>考点范围：</label>
              <input type="text" v-model="autoConfig.topics" placeholder="输入考点关键词，用逗号分隔" />
            </div>

            <div class="form-group">
              <label>试卷总题数：</label>
              <span class="total-questions">{{ totalAutoQuestions }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="actions">
        <button class="btn generate-btn" @click="generatePaper">重新生成试卷</button>
        <button class="btn confirm-btn" @click="confirmExamSettings">确认出卷</button>
      </div>
    </div>

    <!-- 题目预览模态框 -->
    <div v-if="showPreviewModal" class="modal">
      <div class="modal-content">
        <h2>试卷预览</h2>

        <div class="exam-settings">
          <div class="setting-item">
            <strong>考试名称：</strong>{{ paper.title || '未命名试卷' }}
          </div>
          <div class="setting-item">
            <strong>考试科目：</strong>{{ paper.subject || '未选择' }}
          </div>
          <div class="setting-item">
            <strong>出卷方式：</strong>{{ isAutoMode ? '自动组卷' : '手动组卷' }}
          </div>
        </div>

        <div class="preview-questions">
          <div
              v-for="(question, index) in currentQuestions"
              :key="question.id"
              class="question-preview"
          >
            <div class="question-header">
              <span class="type-badge">{{ question.type }}</span>
              <span class="difficulty">{{ question.difficulty }}</span>
            </div>
            <div class="question-text">{{ index + 1 }}. {{ question.text }}</div>

            <div class="options" v-if="!isEssayQuestion(question)">
              <div
                  v-for="(option, optionIndex) in question.options"
                  :key="option.value"
                  :class="['option', { correct: option.isCorrect }]">
                {{ String.fromCharCode(65 + optionIndex) }}. {{ option.label }}
              </div>
            </div>

            <div class="essay-answer" v-else>
              <strong>参考答案：</strong>
              <p>{{ question.correctAnswer }}</p>
            </div>

            <div class="question-explanation">
              <strong>解析：</strong>
              <p>{{ question.explanation }}</p>
            </div>
          </div>
        </div>

        <div class="modal-buttons">
          <button class="btn close-btn" @click="closePreviewModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 考试设置模态框 -->
    <div v-if="showExamSettingsModal" class="modal">
      <div class="modal-content exam-settings">
        <h2>考试设置</h2>

        <div class="form-group">
          <label>考试时间：</label>
          <div class="datetime-pickers">
            <input type="datetime-local" v-model="paper.startTime" />
            <span>至</span>
            <input type="datetime-local" v-model="paper.endTime" />
          </div>
        </div>

        <div class="form-group">
          <label>考试时长：</label>
          <div class="duration-picker">
            <input type="number" min="10" v-model.number="paper.duration" />
            <span>分钟</span>
          </div>
        </div>

        <div class="form-group">
          <label>试卷总分：</label>
          <input type="number" min="1" v-model.number="paper.fullScore" />
        </div>

        <div class="form-group">
          <label>及格分数：</label>
          <input type="number" min="0" :max="paper.fullScore" v-model.number="paper.passingScore" />
        </div>

        <div class="form-group">
          <label>是否允许查看答案：</label>
          <select v-model="paper.showAnswersAfter">
            <option value="immediately">立即显示</option>
            <option value="afterEnd">考试结束后显示</option>
            <option value="never">不显示</option>
          </select>
        </div>

        <div class="modal-buttons">
          <button class="btn save-btn" @click="saveExam">保存并发布</button>
          <button class="btn cancel-btn" @click="cancelExamSettings">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 所有模拟数据
const questions = ref([
  {
    id: 1,
    type: '单选',
    subject: '操作系统原理',
    text: '进程和线程的主要区别是什么？',
    difficulty: '中',
    options: [
      { value: 'A', label: '资源分配的基本单位', isCorrect: false },
      { value: 'B', label: 'CPU调度的基本单位', isCorrect: true },
      { value: 'C', label: '程序运行环境描述', isCorrect: false }
    ],
    explanation: '进程是资源分配的基本单位，线程是CPU调度的基本单位。'
  },
  {
    id: 2,
    type: '多选',
    subject: '数据库基础',
    text: '下列哪些是关系型数据库？',
    difficulty: '中',
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
    difficulty: '易',
    options: [
      { value: 'A', label: '正确', isCorrect: true },
      { value: 'B', label: '错误', isCorrect: false }
    ]
  },
  {
    id: 4,
    type: '简答',
    subject: '数据结构',
    text: '请解释什么是哈希冲突以及解决方法有哪些？',
    difficulty: '难',
    correctAnswer: '当两个不同的键值对应到相同的存储位置时就发生了哈希冲突...解决方法包括开放定址法、链地址法等',
    explanation: '哈希冲突是哈希函数计算出相同位置的情况，常见解决方案包括开放定址法、链地址法等'
  },
  {
    id: 5,
    type: '单选',
    subject: '操作系统原理',
    text: '以下哪个调度算法可能导致某些进程长期得不到执行？',
    difficulty: '中',
    options: [
      { value: 'A', label: '先来先服务(FCFS)', isCorrect: false },
      { value: 'B', label: '短作业优先(SJF)', isCorrect: true },
      { value: 'C', label: '轮转(RR)', isCorrect: false },
      { value: 'D', label: '多级反馈队列', isCorrect: false }
    ],
    explanation: '短作业优先(SJF)可能导致长作业长期等待，产生饥饿现象。'
  },
  {
    id: 6,
    type: '单选',
    subject: '数据库基础',
    text: '在数据库设计中，规范化的主要目的是什么？',
    difficulty: '中',
    options: [
      { value: 'A', label: '提高查询速度', isCorrect: false },
      { value: 'B', label: '减少冗余和更新异常', isCorrect: true },
      { value: 'C', label: '增加数据量', isCorrect: false },
      { value: 'D', label: '提升并发性能', isCorrect: false }
    ],
    explanation: '数据库规范化的目的是减少数据冗余和更新异常。'
  },
  {
    id: 7,
    type: '多选',
    subject: '计算机网络',
    text: '下列关于TCP/IP协议的说法哪些是正确的？',
    difficulty: '难',
    options: [
      { value: 'A', label: 'TCP是面向连接的协议', isCorrect: true },
      { value: 'B', label: 'IP负责路由寻址', isCorrect: true },
      { value: 'C', label: 'TCP保证传输可靠性', isCorrect: true },
      { value: 'D', label: 'IP提供可靠传输', isCorrect: false }
    ]
  }
])

// 当前试卷信息
const paper = ref({
  id: null,
  title: '',
  subject: '',
  startTime: '',
  endTime: '',
  duration: 60,
  fullScore: 100,
  passingScore: 60,
  showAnswersAfter: 'afterEnd'
})

// 自动组卷配置
const autoConfig = ref({
  types: ['单选', '多选'],
  singleChoiceCount: 5,
  multipleChoiceCount: 3,
  judgmentCount: 2,
  shortAnswerCount: 1,
  easyPercentage: 50,
  topics: ''
})

// 当前试卷题目
const currentQuestions = ref([])
const isAutoMode = ref(false)
const mode = ref(null) // 'manual' or 'auto'

// 手动组卷相关
const manualQuestionsCount = ref(10)
const manualQuestionIds = ref(Array(10).fill(''))

// 控制模态框显示
const showPreviewModal = ref(false)
const showExamSettingsModal = ref(false)

// 主题列表
const subjects = ref(['操作系统原理', '数据库基础', '计算机网络', '数据结构'])

// 获取所有唯一科目
const uniqueSubjects = computed(() => {
  return [...new Set(questions.value.map(q => q.subject))]
})

// 计算自动组卷总题数
const totalAutoQuestions = computed(() => {
  return autoConfig.value.singleChoiceCount +
      autoConfig.value.multipleChoiceCount +
      autoConfig.value.judgmentCount +
      autoConfig.value.shortAnswerCount
})

// 根据模式显示对应的题目
const displayQuestions = computed(() => {
  return currentQuestions.value.filter(Boolean)
})

// 判断是否为简答题
const isEssayQuestion = (question) => {
  return question.type === '简答'
}

// 生成试卷
const generatePaper = () => {
  if (mode.value === 'manual') {
    const ids = manualQuestionIds.value.map(Number)
    currentQuestions.value = ids.map(id => {
      if (id && !isNaN(id)) {
        return questions.value.find(q => q.id === id) || null
      }
      return null
    })
  } else if (mode.value === 'auto') {
    let selectedQuestions = []

    // 根据类型筛选
    let filtered = questions.value.filter(q =>
        autoConfig.value.types.includes(q.type) &&
        (autoConfig.value.topics ? q.text.toLowerCase().includes(autoConfig.value.topics.toLowerCase()) : true)
    )

    // 根据难度筛选
    const easyQuestions = filtered.filter(q => q.difficulty === '易')
    const mediumQuestions = filtered.filter(q => q.difficulty === '中')
    const hardQuestions = filtered.filter(q => q.difficulty === '难')

    // 计算各难度数量
    const easyCount = Math.floor(totalAutoQuestions.value * (autoConfig.value.easyPercentage / 100))
    const hardCount = totalAutoQuestions.value - easyCount

    // 随机抽取题目
    selectedQuestions = getRandomQuestions(filtered, totalAutoQuestions.value)

    currentQuestions.value = selectedQuestions
  }

  showPreviewModal.value = true
}

// 获取随机题目
const getRandomQuestions = (questionsList, count) => {
  const shuffled = [...questionsList].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, count)
}

// 加载手动添加的题目
const loadManualQuestion = (index) => {
  const id = Number(manualQuestionIds.value[index])
  if (!isNaN(id)) {
    const question = questions.value.find(q => q.id === id)
    if (question) {
      currentQuestions.value[index] = question
    } else {
      alert(`题目ID ${id} 不存在`)
    }
  }
}

// 显示指定类型的组卷方式
const showQuestionType = (type) => {
  mode.value = type
  if (type === 'manual') {
    manualQuestionsCount.value = totalAutoQuestions.value || 10
    manualQuestionIds.value = Array(manualQuestionsCount.value).fill('')
    currentQuestions.value = Array(manualQuestionsCount.value).fill(null)
  } else {
    autoConfig.value = {
      types: ['单选', '多选'],
      singleChoiceCount: 5,
      multipleChoiceCount: 3,
      judgmentCount: 2,
      shortAnswerCount: 1,
      easyPercentage: 50,
      topics: ''
    }
    currentQuestions.value = []
  }
}

// 确认出卷
const confirmExamSettings = () => {
  if (displayQuestions.value.length === 0) {
    alert('请先生成或选择题目')
    return
  }

  showPreviewModal.value = false
  showExamSettingsModal.value = true
}

// 保存考试设置
const saveExam = () => {
  if (!paper.value.title.trim()) {
    alert('考试名称不能为空')
    return
  }

  if (!paper.value.subject) {
    alert('请选择考试科目')
    return
  }

  if (!paper.value.startTime || !paper.value.endTime) {
    alert('请填写完整的考试时间')
    return
  }

  if (new Date(paper.value.startTime) >= new Date(paper.value.endTime)) {
    alert('结束时间必须大于开始时间')
    return
  }

  alert('试卷发布成功！')
  router.push('/teacher/dashboard')
}

// 取消考试设置
const cancelExamSettings = () => {
  showExamSettingsModal.value = false
  showPreviewModal.value = true
}

// 关闭预览模态框
const closePreviewModal = () => {
  showPreviewModal.value = false
}
</script>

<style scoped>
.create-paper {
  padding: 20px;
}

.paper-preview {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.preview-btn {
  background-color: #0d47a1;
  color: white;
}

.preview-btn:hover {
  background-color: #1565c0;
}

.paper-info {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-group input[type="text"],
.form-group select,
.form-group input[type="number"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1em;
}

.question-types {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.type-card {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.type-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.type-card h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #0d47a1;
}

.type-card p {
  margin: 0;
  color: #666;
}

.question-inputs {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.question-inputs input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  width: 100%;
}

.auto-options .form-group {
  margin-bottom: 20px;
}

.checkbox-group {
  display: flex;
  gap: 15px;
  margin-top: 8px;
}

.checkbox-group label {
  font-weight: normal;
}

.slider-container {
  margin-top: 10px;
}

.slider-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.slider-row input[type="range"] {
  flex: 1;
  margin: 0 10px;
}

.percentage-display {
  margin-top: 5px;
  font-size: 0.9em;
  color: #666;
}

.total-questions {
  font-weight: bold;
  color: #0d47a1;
  font-size: 1.1em;
}

.actions {
  display: flex;
  gap: 15px;
  margin-top: 20px;
  justify-content: flex-end;
}

.generate-btn {
  background-color: #0d47a1;
  color: white;
  padding: 10px 20px;
}

.generate-btn:hover {
  background-color: #1565c0;
}

.confirm-btn {
  background-color: #2e7d32;
  color: white;
  padding: 10px 20px;
}

.confirm-btn:hover {
  background-color: #388e3c;
}

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

.exam-settings {
  min-width: 600px;
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.setting-item {
  margin-bottom: 10px;
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
  justify-content: space-between;
  margin-bottom: 10px;
}

.type-badge {
  background-color: #0d47a1;
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.9em;
}

.difficulty {
  color: #666;
  font-size: 0.9em;
}

.question-text {
  margin-bottom: 15px;
  font-weight: bold;
}

.options {
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

.essay-answer {
  margin-bottom: 10px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 6px;
}

.question-explanation {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 6px;
  font-size: 0.95em;
  color: #666;
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

.save-btn {
  background-color: #0d47a1;
  color: white;
}

.save-btn:hover {
  background-color: #1565c0;
}

.cancel-btn {
  background-color: #c62828;
  color: white;
}

.cancel-btn:hover {
  background-color: #d32f2f;
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

.no-questions {
  text-align: center;
  color: #666;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 10px;
}

/* 新增样式 */
.paper-info {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.paper-info .form-group {
  flex: 1;
  min-width: 250px;
}

.question-inputs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.question-inputs input {
  flex: 1;
  min-width: 150px;
  height: 34px;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>
