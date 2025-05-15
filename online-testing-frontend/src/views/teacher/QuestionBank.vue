<template>
  <div class="question-bank">
    <h1>题库管理</h1>

    <div class="toolbar">
      <div class="search-filter">
        <input
            v-model="searchQuery"
            placeholder="输入科目或关键词搜索题目"
            class="search-input"
        />
        <select v-model="selectedSubject" class="subject-filter">
          <option value="">全部科目</option>
          <option v-for="subject in uniqueSubjects" :key="subject" :value="subject">{{ subject }}</option>
        </select>
      </div>

      <div class="actions">
        <button class="btn add-btn" @click="showAddQuestionDialog">添加题目</button>
      </div>
    </div>

    <div class="questions-grid">
      <div
          v-for="question in filteredQuestions"
          :key="question.id"
          class="question-card"
          @click="viewQuestionDetails(question)"
      >
        <div class="question-header">
          <span class="type-badge">{{ question.type }}</span>
        </div>
        <div class="question-text">{{ question.text }}</div>

        <div class="options">
          <div
              v-for="(option, index) in question.options"
              :key="option.value"
              :class="['option', { correct: option.isCorrect }]">
            {{ String.fromCharCode(65 + index) }}. {{ option.label }}
          </div>
        </div>

        <div class="question-actions">
          <button class="btn edit-btn" @click.stop="editQuestion(question)">编辑</button>
          <button class="btn delete-btn" @click.stop="deleteQuestion(question)">删除</button>
        </div>
      </div>

      <div v-if="filteredQuestions.length === 0" class="no-results">
        没有找到相关题目
      </div>
    </div>

    <!-- 添加/编辑模态框 -->
    <div v-if="showModal" class="modal">
      <div class="modal-content">
        <h2>{{ isEditing ? '编辑题目' : '添加题目' }}</h2>

        <div class="form-group">
          <label>题目类型：</label>
          <select v-model="currentQuestion.type">
            <option value="单选">单选</option>
            <option value="多选">多选</option>
            <option value="判断">判断</option>
          </select>
        </div>

        <div class="form-group">
          <label>所属科目：</label>
          <input type="text" v-model="currentQuestion.subject" placeholder="请输入科目名称" />
        </div>

        <div class="form-group">
          <label>题目内容：</label>
          <textarea v-model="currentQuestion.text" rows="3" placeholder="请输入题目描述"></textarea>
        </div>

        <div class="form-group">
          <label>选项设置：</label>
          <div v-for="(option, index) in currentQuestion.options" :key="index" class="option-row">
            <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
            <input type="text" v-model="option.label" :placeholder="`选项 ${String.fromCharCode(65 + index)}`" />
            <input type="checkbox" v-if="!isMultipleChoice && !isJudgmentQuestion" v-model="option.isCorrect" /> 正确
          </div>

          <div v-if="isMultipleChoice" class="multiple-choice-hint">
            可选择多个正确答案
          </div>
        </div>

        <div class="modal-buttons">
          <button class="btn save-btn" @click="saveQuestion">保存</button>
          <button class="btn cancel-btn" @click="closeModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

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
  }
])

const showModal = ref(false)
const isEditing = ref(false)
const currentQuestion = ref({
  id: null,
  type: '单选',
  subject: '',
  text: '',
  options: [
    { value: 'A', label: '', isCorrect: false },
    { value: 'B', label: '', isCorrect: false },
    { value: 'C', label: '', isCorrect: false },
    { value: 'D', label: '', isCorrect: false }
  ]
})

const searchQuery = ref('')
const selectedSubject = ref('')

// 获取所有唯一科目
const uniqueSubjects = computed(() => {
  return [...new Set(questions.value.map(q => q.subject))]
})

// 过滤后的题目列表
const filteredQuestions = computed(() => {
  let result = [...questions.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(q =>
        q.text.toLowerCase().includes(query) ||
        q.subject.toLowerCase().includes(query)
    )
  }

  if (selectedSubject.value) {
    result = result.filter(q => q.subject === selectedSubject.value)
  }

  return result
})

// 判断是否为多选题
const isMultipleChoice = computed(() => {
  return currentQuestion.value.type === '多选'
})

// 判断是否为判断题
const isJudgmentQuestion = computed(() => {
  return currentQuestion.value.type === '判断'
})

// 显示添加题目对话框
const showAddQuestionDialog = () => {
  isEditing.value = false
  currentQuestion.value = {
    id: null,
    type: '单选',
    subject: '',
    text: '',
    options: [
      { value: 'A', label: '', isCorrect: false },
      { value: 'B', label: '', isCorrect: false },
      { value: 'C', label: '', isCorrect: false },
      { value: 'D', label: '', isCorrect: false }
    ]
  }
  showModal.value = true
}

// 编辑题目
const editQuestion = (question) => {
  isEditing.value = true
  currentQuestion.value = JSON.parse(JSON.stringify(question))
  showModal.value = true
}

// 删除题目
const deleteQuestion = (question) => {
  if (confirm(`确定要删除题目 "${question.text}" 吗？`)) {
    const index = questions.value.findIndex(q => q.id === question.id)
    if (index > -1) {
      questions.value.splice(index, 1)
    }
  }
}

// 查看题目详情
const viewQuestionDetails = (question) => {
  alert(`查看题目详情：${question.text}`)
}

// 关闭模态框
const closeModal = () => {
  showModal.value = false
}

// 保存题目
const saveQuestion = () => {
  if (!currentQuestion.value.text.trim()) {
    alert('题目内容不能为空')
    return
  }

  if (!isJudgmentQuestion.value && !currentQuestion.value.options.some(o => o.isCorrect)) {
    alert('至少选择一个正确答案')
    return
  }

  if (isEditing.value) {
    // 编辑模式：更新现有题目
    const index = questions.value.findIndex(q => q.id === currentQuestion.value.id)
    if (index > -1) {
      questions.value[index] = {...currentQuestion.value}
    }
  } else {
    // 添加模式：创建新题目
    const newId = Math.max(...questions.value.map(q => q.id)) + 1
    questions.value.push({...currentQuestion.value, id: newId})
  }

  closeModal()
}

// 添加选项
const addOption = () => {
  const nextLetter = String.fromCharCode(65 + currentQuestion.value.options.length)
  currentQuestion.value.options.push({
    value: nextLetter,
    label: '',
    isCorrect: false
  })
}

// 删除选项
const removeOption = (index) => {
  if (currentQuestion.value.options.length > 2) {
    currentQuestion.value.options.splice(index, 1)
  } else {
    alert('每个题目必须至少保留两个选项')
  }
}
</script>

<style scoped>
h1{
  color: black;
}
.question-bank {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.search-input {
  flex: 1 1 200px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1em;
}

.subject-filter {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1em;
}

.actions {
  margin-top: 10px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-btn {
  background-color: #0d47a1;
  color: white;
}

.add-btn:hover {
  background-color: #1565c0;
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.question-card {
  background: #000000;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.question-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
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

.question-text {
  margin-bottom: 15px;
  font-weight: bold;
}

.options {
  margin-bottom: 15px;
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

.question-actions {
  display: flex;
  justify-content: space-between;
}

.edit-btn {
  background-color: #2e7d32;
  color: white;
  padding: 6px 12px;
  font-size: 0.9em;
}

.edit-btn:hover {
  background-color: #388e3c;
}

.delete-btn {
  background-color: #c62828;
  color: white;
  padding: 6px 12px;
  font-size: 0.9em;
}

.delete-btn:hover {
  background-color: #d32f2f;
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
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #0d47a1;
}

.form-group {
  margin-bottom: 20px;
  color: black;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-group select,
.form-group input[type="text"],
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1em;
}

.form-group input[type="text"] {
  height: 30px;
}

.form-group select {
  height: 34px;
}

.form-group textarea {
  resize: vertical;
}

.option-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.option-label {
  width: 30px;
  font-weight: bold;
}

.option-row input[type="text"] {
  flex: 1;
  margin-right: 10px;
}

.option-row input[type="checkbox"] {
  margin-left: 10px;
}

.multiple-choice-hint {
  font-size: 0.9em;
  color: #666;
  margin-top: 5px;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.save-btn {
  background-color: #0d47a1;
  color: white;
  padding: 8px 16px;
}

.save-btn:hover {
  background-color: #1565c0;
}

.cancel-btn {
  background-color: #666;
  color: white;
  padding: 8px 16px;
}

.cancel-btn:hover {
  background-color: #444;
}

.no-results {
  text-align: center;
  color: #666;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 10px;
}
</style>
