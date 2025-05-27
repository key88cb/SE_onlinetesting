<template>
  <div class="auto-create-paper">
    <h1>自动组卷</h1>

    <div class="paper-form">
      <div class="form-group">
        <label>试卷名称：</label>
        <input type="text" v-model="paper.title" placeholder="请输入试卷名称" />
      </div>
      <div class="form-group">
        <label>考试课程班级：</label>
        <select v-model="paper.course">
          <option value="">请选择考试课程班级</option>
          <option v-for="course in courses" :key="course" :value="course">{{ course }}</option>
        </select>
      </div>
      <div class="form-group">
        <label>出卷人：</label>
        <input type="text" v-model="paper.creator" placeholder="请输入出卷人" />
      </div>

      <div class="form-group">
        <label>题型分布：</label>
        <div class="checkbox-group">
          <label><input type="checkbox" v-model="config.types" value="单选" /> 单选题</label>
          <label><input type="checkbox" v-model="config.types" value="多选" /> 多选题</label>
          <label><input type="checkbox" v-model="config.types" value="判断" /> 判断题</label>
        </div>
      </div>

      <div class="form-group input-group-container">
        <label>每种题型数量：</label>
        <div class="quantity-inputs full-width">
          <div class="input-unit-group">
            <span class="input-label">单选题：</span>
            <input type="number" min="0" max="50" v-model.number="config.singleChoiceCount"
                   :disabled="!config.types.includes('单选')" />
            <span class="unit">道</span>
          </div>
          <div class="input-unit-group">
            <span class="input-label">多选题：</span>
            <input type="number" min="0" max="50" v-model.number="config.multiChoiceCount"
                   :disabled="!config.types.includes('多选')" />
            <span class="unit">道</span>
          </div>
          <div class="input-unit-group">
            <span class="input-label">判断题：</span>
            <input type="number" min="0" max="50" v-model.number="config.judgmentCount"
                   :disabled="!config.types.includes('判断')" />
            <span class="unit">道</span>
          </div>
        </div>
      </div>

      <div class="form-group input-group-container">
        <label>每种题型单题分数：</label>
        <div class="quantity-inputs full-width">
          <div class="input-unit-group">
            <span class="input-label">单选题：</span>
            <input type="number" min="1" max="10" v-model.number="config.singleChoiceScore"
                   :disabled="!config.types.includes('单选')" />
            <span class="unit">分</span>
          </div>
          <div class="input-unit-group">
            <span class="input-label">多选题：</span>
            <input type="number" min="1" max="10" v-model.number="config.multiChoiceScore"
                   :disabled="!config.types.includes('多选')" />
            <span class="unit">分</span>
          </div>
          <div class="input-unit-group">
            <span class="input-label">判断题：</span>
            <input type="number" min="1" max="10" v-model.number="config.judgmentScore"
                   :disabled="!config.types.includes('判断')" />
            <span class="unit">分</span>
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
              <span class="type-badge">{{ question.questionType }}</span>
              <span class="question-number">{{ index + 1 }}. {{ question.questionText }}</span>
              <span class="score">{{ question.points }}分</span>
            </div>
            <div class="options">
              <div
                  v-for="(option, optionIndex) in question.options"
                  :key="option.value"
                  :class="['option', { correct: option.isCorrect }]">
                {{ String.fromCharCode(65 + optionIndex) }}. {{ option.optionText }}
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
          <label>试卷总分：</label>
          <input type="number" min="1" v-model.number="examSettings.fullScore" disabled />
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'


const router = useRouter()

// 当前试卷数据
const paper = ref({
  title: '',
  course: '',
  creator: '',
  questions: []
})

// 考试设置
const examSettings = ref({
  startTime: '',
  endTime: '',
  fullScore: 0,
})

// 题库数据
const confirmedQuestions = ref([])

// 组卷配置
const config = ref({
  types: ['单选'],
  singleChoiceCount: 2,
  multiChoiceCount: 1,
  judgmentCount: 1,
  topics: '',
  singleChoiceScore: 2,
  multiChoiceScore: 3,
  judgmentScore: 1
})

// 控制模态框显示
const showPreviewModal = ref(false)
const showPublishModal = ref(false)

// 所有科目
const courses = ref(['操作系统原理', '数据库基础', '计算机网络'])

// 计算预览问题
const previewQuestions = computed(() => {
  return paper.value.questions.map(question => ({
    ...question,
    points: getQuestionScore(question)
  }))
})

// 计算总分
const totalScore = computed(() => {
  return previewQuestions.value.reduce((sum, q) => sum + q.points, 0)
})

// 根据题型动态计算每题分数
function getQuestionScore(question) {
  switch (question.questionType) {
    case 'Single Choice': return config.value.singleChoiceScore
    case 'Multiple Choice': return config.value.multiChoiceScore
    case 'True/False': return config.value.judgmentScore
    default: return 0
  }
}

async function fetchQuestionsFromAPI(params = {}) {
  try {
    const url = new URL('http://localhost:8080/api/questions')
    if (params.questionType) url.searchParams.append('questionType', params.questionType)
    if (params.tags) url.searchParams.append('tags', params.tags)

    const res = await fetch(url, {method: 'GET'})
    if (!res.ok) throw new Error('加载题库失败')
    const data = await res.json()
    return data
  } catch (error) {
    alert(error.message)
    return []
  }
}

// 预览试卷
async function previewPaper() {
  if (!paper.value.title) {
    alert('请填写试卷名称')
    return
  }
  if (!paper.value.course) {
    alert('请选择考试课程')
    return
  }
  if (!paper.value.creator) {
    alert('请填写出卷人')
    return
  }

  const availableQuestions = {}
  // 检查题目数量是否超过题库总量
  for (const type of config.value.types) {
    let questionTypeBackend = ''
    if (type === '单选') questionTypeBackend = 'Single Choice'
    else if (type === '多选') questionTypeBackend = 'Multiple Choice'
    else if (type === '判断') questionTypeBackend = 'True/False'

    // 获取该题型的总数量
    const topics = config.value.topics?.trim()
    const tags = topics ? topics.split(',').map(t => t.trim()).filter(Boolean) : []

    const questions = await fetchQuestionsFromAPI({
      questionType: questionTypeBackend,
      tags: tags.join(',')  // 传递给后端 API
    })

    availableQuestions[type] = questions.length
  }

  // 检查用户输入的题数是否超过题库数量
  const errors = []

  if (config.value.types.includes('单选') && config.value.singleChoiceCount > availableQuestions['单选']) {
    errors.push(`单选题请求 ${config.value.singleChoiceCount} 道，题库仅提供 ${availableQuestions['单选']} 道`)
  }
  if (config.value.types.includes('多选') && config.value.multiChoiceCount > availableQuestions['多选']) {
    errors.push(`多选题请求 ${config.value.multiChoiceCount} 道，题库仅提供 ${availableQuestions['多选']} 道`)
  }
  if (config.value.types.includes('判断') && config.value.judgmentCount > availableQuestions['判断']) {
    errors.push(`判断题请求 ${config.value.judgmentCount} 道，题库仅提供 ${availableQuestions['判断']} 道`)
  }

  if (errors.length > 0) {
    alert('题数超出题库限制：\n' + errors.join('\n'))
    return
  }

  let selectedQuestions = []
  // 获取所有配置的题型
  for (const type of config.value.types) {
    let questionTypeBackend = ''
    if (type === '单选') questionTypeBackend = 'Single Choice'
    else if (type === '多选') questionTypeBackend = 'Multiple Choice'
    else if (type === '判断') questionTypeBackend = 'True/False'
    // 获取题目数量
    let count = 0
    if (type === '单选') count = config.value.singleChoiceCount
    else if (type === '多选') count = config.value.multiChoiceCount
    else if (type === '判断') count = config.value.judgmentCount
    if (count <= 0) continue

    // 调用 API 查询题目
    const questions = await fetchQuestionsFromAPI({
      questionType: questionTypeBackend,
      tags: config.value.topics
    })

    // 随机选取题目
    const shuffled = [...questions].sort(() => 0.5 - Math.random())
    selectedQuestions.push(...shuffled.slice(0, count))
  }
  // 对每个问题进行过滤，判断题只显示 A/B
  const filteredQuestions = selectedQuestions.map(question => ({
    ...question,
    options: question.questionType === 'True/False'
        ? question.options.slice(0, 2)
        : question.options
  }))

  // 更新试卷预览
  paper.value.questions = filteredQuestions
  confirmedQuestions.value = [...filteredQuestions]
  examSettings.value.fullScore = totalScore.value
  showPreviewModal.value = true
}

// 关闭预览模态框
function closePreviewModal() {
  showPreviewModal.value = false
}

// 重新生成试卷
function generateNewPaper() {
  closePreviewModal()
  previewPaper()
}

// 去发布
function goToPublish() {
  closePreviewModal()
  examSettings.value.fullScore = totalScore.value
  showPublishModal.value = true
}

// 发布试卷
async function confirmPublish() {
  if (!examSettings.value.startTime || !examSettings.value.endTime) {
    alert('请填写完整的考试时间')
    return
  }

  if (new Date(examSettings.value.startTime) >= new Date(examSettings.value.endTime)) {
    alert('结束时间必须大于开始时间')
    return
  }

  try {
    const payload = {
      courseId: getCourseIdFromName(paper.value.course),
      creator: paper.value.creator,
      openTime: new Date(examSettings.value.startTime).toISOString().slice(0, 19),
      closeTime: new Date(examSettings.value.endTime).toISOString().slice(0, 19),
      paperName: paper.value.title,
      questionTypeConfigs: buildQuestionTypeConfigs(),
      questions: confirmedQuestions.value.map(q => ({
        questionId: q.questionId,
        points: getQuestionScore(q)
      }))
    }

    const res = await fetch('http://localhost:8080/api/paper-questions/auto-create-paper', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        })

    if (res.status === 200) {
      alert('试卷发布成功！')
      showPublishModal.value = false
      await router.push('/teacher/exam-management')
    }
  } catch (error) {
    console.error(error)
    alert('发布试卷失败')
  }
}

function getCourseIdFromName(courseName) {
  // 假设这里根据 courseName 映射为实际 courseId，比如：
  switch (courseName) {
    case '操作系统原理': return 1
    case '数据库基础': return 2
    case '计算机网络': return 3
    default: return -1
  }
}

function buildQuestionTypeConfigs() {
  const types = []
  const topics = config.value.topics?.trim()

  // 判断是否为空或空白字符串
  const tags = topics ? topics.split(',').map(t => t.trim()).filter(Boolean) : null

  if (config.value.types.includes('单选')) {
    types.push({
      type: 'Single Choice',
      numberOfQuestions: config.value.singleChoiceCount,
      pointsPerQuestion: config.value.singleChoiceScore,
      tags: tags
    })
  }
  if (config.value.types.includes('多选')) {
    types.push({
      type: 'Multiple Choice',
      numberOfQuestions: config.value.multiChoiceCount,
      pointsPerQuestion: config.value.multiChoiceScore,
      tags: tags
    })
  }
  if (config.value.types.includes('判断')) {
    types.push({
      type: 'True/False',
      numberOfQuestions: config.value.judgmentCount,
      pointsPerQuestion: config.value.judgmentScore,
      tags: tags
    })
  }
  return types
}


// 取消发布
async function cancelPublish() {
  showPublishModal.value = false
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

.datetime-pickers input[type="datetime-local"] {
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 1em;
}

.datetime-pickers span {
  margin: 0 10px;
  color: #555;
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

.input-group-container {
  display: flex;
  flex-direction: column;
}

.quantity-inputs.full-width {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.input-unit-group {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
}
.input-label {
  white-space: nowrap;
  margin-right: 8px;
  font-weight: normal;
  color: #333;
}

.input-unit-group input[type="number"] {
  width: 100%;
  padding-right: 20px; /* 为单位留出空间 */
  text-align: center;
}

.unit {
  position: absolute;
  right: 8px;
  color: #666;
  font-size: 0.9em;
  pointer-events: none;
}
</style>
