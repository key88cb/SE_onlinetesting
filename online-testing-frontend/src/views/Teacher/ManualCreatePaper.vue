<template>
  <div class="manual-create-paper">
    <h1>手动组卷</h1>

    <div class="paper-form">
      <div class="form-group paper-info-group">
        <label>试卷名称：</label>
        <input type="text" v-model="paper.title" placeholder="请输入试卷名称" />
        <label>课程信息：</label>
        <input type="number" v-model="paper.courseId" placeholder="请输入班级名称" />
        <label>创建者：</label>
        <input type="text" v-model="paper.creator" placeholder="请输入出卷人名称" />
      </div>
    <!-- 右上角按钮 -->
<!--      <button-->
<!--    class="view-library-btn"-->
<!--    :class="{ 'close-mode': showLibrary }"-->
<!--    @click="showLibrary = !showLibrary"-->
<!--  >-->
<!--    {{ showLibrary ? '关闭题库' : '查看题库' }}-->
<!--    </button>-->
      <div class="form-group">
        <label>题目列表：</label>
        <div class="question-list">
          <div v-for="(question, index) in paper.questions" :key="index" class="question-row">
            <input type="text" :value="question.questionText || question.questionId" disabled />
            <span>题目分数:</span>
            <input type="number" v-model.number="question.score" placeholder="分数" />
            <button class="btn remove-btn" @click="removeQuestion(index)">-</button>
          </div>
        </div>
<!--        <button class="btn add-btn" @click="addQuestionRow">+ 下一行</button>-->
      </div>

      <div class="form-actions">
        <button
            class="view-library-btn"
            :class="{ 'close-mode': showLibrary }"
            @click="showLibrary = !showLibrary"
        >
          {{ showLibrary ? '关闭题库' : '查看题库' }}
        </button>
        <button class="btn preview-btn" @click="previewPaper">预览试卷</button>
      </div>
    </div>

    <!-- 试卷预览模态框 -->
    <div v-if="showPreviewModal" class="modal">
      <div class="modal-content">
        <h2>试卷预览 - {{ paper.title }}</h2>

        <div class="preview-questions">
          <div v-for="(item, index) in previewQuestions" :key="item.id" class="question-preview">
            <div class="question-header">
              <span class="type-badge">{{ item.questionType }}</span>
              <span class="question-number">{{ index + 1 }}. {{ item.questionText }}</span>
              <span class="score">{{ item.score }}分</span>
            </div>
            <div class="options">
              <div
                  v-for="(option, optionIndex) in item.options"
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
          <button class="btn publish-btn" @click="goToPublish">去发布</button>
        </div>
      </div>
    </div>

    <!-- 题库模态框 -->
    <div v-if="showLibrary" class="modal2">
        <div>题库查看</div>

        <div class="toolbar">
          <div class="search-filter">
            <input
                v-model="searchQuery"
                placeholder="输入科目或关键词搜索题目"
                class="search-input"
            />
            <select v-model="selectedsubject" class="subject-filter">
              <option value="">全部科目</option>
              <option v-for="subject in uniquesubjects" :key="subject" :value="subject">{{ subject }}</option>
            </select>
          </div>
        </div>
        <div class="questions-grid">
          <div
              v-for="question in filteredQuestions"
              :key="question.questionId"
              class="question-card"
          >
            <button class="add-question-btn" @click="openAddScoreModal(question)">
                +
            </button>
            <div class="question-header">
              <span class="type-badge">{{ question.questionType }}</span>
            </div>
            <div class="question-text">{{ question.questionText }}</div>
            <div class="question-tag">备注：{{ question.tags }}  </div>
            <div class="options">
              <div
                  v-for="(option, index) in question.options"
                  :key="option.optionIdValue"
                  :class="['option', { correct: question.correctAnswer.includes(option.optionIdValue) }]">
                {{ String.fromCharCode(65 + index) }}. {{ option.optionText }}
              </div>
            </div>
          </div>
          <div v-if="filteredQuestions.length === 0" class="no-results">
            没有找到相关题目
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

  <!-- 设置分数弹窗提醒 -->
  <div v-if="showAddScoreModal" class="modal">
    <div class="modal-content" style="max-width: 400px;">
      <h3>设置分数</h3>
      <div style="margin-bottom: 20px;">
        <div>题目：{{ currentAddQuestion?.text }}</div>
        <input type="number" v-model.number="addScoreValue" min="1" placeholder="请输入分数" style="width: 100%; margin-top: 10px;" />
      </div>
      <div class="modal-buttons">
        <button class="btn cancel-btn" @click="showAddScoreModal = false">取消</button>
        <button class="btn confirm-btn" @click="confirmAddQuestion">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed,onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()

// 当前试卷数据
const paper = ref({
  title: '',
  courseId: null,
  creator: '',
  questions: []
})

// 考试设置
const examSettings = ref({
  startTime: '',
  endTime: '',
  fullScore: 0,
})

// 模拟题库数据
const questionBank = ref([])

// 控制模态框显示
const showPreviewModal = ref(false)
const showPublishModal = ref(false)
const showLibrary = ref(false)

// 计算预览问题
const previewQuestions = computed(() => {
  return paper.value.questions
      .map(item => {
        const question = questionBank.value.find(q => q.questionId == item.id) // 这里使用了q.id,是错的
        return question ? { ...question, score: item.score } : null
      })
      .filter(Boolean)
})

// 计算总分
const totalScore = computed(() => {
  return previewQuestions.value.reduce((sum, q) => sum + q.score, 0)
})

// 移除题目行
const removeQuestion = (index) => {
  if (paper.value.questions.length > 1) {
    paper.value.questions.splice(index, 1)
  }
}

// 预览试卷
const previewPaper = () => {
  if (previewQuestions.value.length === 0) {
    alert('请先添加有效题目')
    return
  }
  showPreviewModal.value = true
}
onMounted(async () => {
  try {
     // const res =Search_for_all_questions()
     // questions.value = res.data
  } catch (error) {
    alert('加载题目失败，请检查网络或服务状态')
    console.error(error)
  }
})
// 关闭预览模态框
const closePreviewModal = () => {
  showPreviewModal.value = false
}

// 去发布
const goToPublish = () => {
  closePreviewModal()
  examSettings.value.fullScore = totalScore.value
  showPublishModal.value = true
}

const Search_for_all_questions = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/questions',{
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      query: {
        method:'search',
        subject: currentsubject.value,
      }
    })
    if (!res.ok) {
      throw new Error('网络错误')
    }
//此处需要修改
    console.log(res)
    const data = await res.json()
    questions.value = data
  } catch (error) {
    alert('加载题目失败，请检查网络或服务状态')
    console.error(error)
  }
}
const Create_Exam_Paper = async (requestData) => {
  try {
    const res = await fetch('http://localhost:8080/api/paper-questions/manual-create-paper', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })
    if (!res.ok) {
      throw new Error('网络错误')
    }

    alert('试卷发布成功！')
    router.push('/teacher/exam-management')
  } catch (error) {
    console.error('创建试卷失败:', error)
    alert('创建试卷失败，请稍后再试')
  }
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

  // 构建请求数据
  const manualPaperRequest = {
    courseId: paper.value.courseId,
    creator: paper.value.creator,
    singleChoiceNum: 0, // 这些字段可以由后端自动统计
    multipleChoiceNum: 0,
    trueFalseNum: 0,
    openTime: new Date(examSettings.value.startTime).toISOString().slice(0, 19),
    closeTime: new Date(examSettings.value.endTime).toISOString().slice(0, 19),
    totalScores: totalScore.value,
    highestScoresForSingleChoice: 0, // 可选，留空让后端处理
    highestScoresForMultipleChoice: 0,
    highestScoresForTrueFalse: 0,
    questions: paper.value.questions.map(q => ({
      questionId: q.id,
      points: q.score
    })),
    paperName: paper.value.title
  }

  Create_Exam_Paper(manualPaperRequest)
}
// 查看题库
// 模拟从后端获取题目数据  @search
const searchQuery = ref('')
const selectedsubject = ref('')

// 获取所有唯一科目
const uniquesubjects = computed(() => {
  return [...new Set(questionBank.value.map(q => q.subjectCategory))].filter(Boolean)
})

// 过滤后的题目列表
// const filteredQuestions = computed(() => {
//   let result = [...questionBank.value]
//
//   if (searchQuery.value) {
//     const query = searchQuery.value.toLowerCase()
//     result = result.filter(q =>
//         q.text.toLowerCase().includes(query) ||
//         q.subject.toLowerCase().includes(query)
//     )
//   }
//
//   if (selectedsubject.value) {
//     result = result.filter(q => q.subject === selectedsubject.value)
//   }
//
//   return result
// })

const fetchQuestions = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/questions')
    if (!res.ok) throw new Error('网络错误')
    const data = await res.json()

    questionBank.value = data.map(q => {
      if (q.questionType === 'True/False' && q.options && q.options.length > 2) {
        q.options = q.options.slice(0, 2)
      }
      return q
    })
  } catch (error) {
    alert('加载题目失败，请检查网络或服务状态')
    console.error(error)
  }
}

onMounted(fetchQuestions)

const filteredQuestions = computed(() => {
  return questionBank.value.filter(question => {
    // 1. 科目筛选条件
    const subjectMatch = !selectedsubject.value ||
        question.subjectCategory === selectedsubject.value

    // 2. 搜索词筛选条件（同时搜索题干、标签和选项文本）
    const searchTerm = searchQuery.value.toLowerCase()
    const searchMatch = !searchQuery.value ||
        question.questionText.toLowerCase().includes(searchTerm) ||
        (question.tags && question.tags.toLowerCase().includes(searchTerm)) ||
        (question.options && question.options.some(
            opt => opt.optionText.toLowerCase().includes(searchTerm)
        ))

    return subjectMatch && searchMatch
  })
})

// 判断是否为多选题
const isMultipleChoice = computed(() => {
  return currentQuestion.value.type === '多选'
})

// 判断是否为判断题
const isJudgmentQuestion = computed(() => {
  return currentQuestion.value.type === '判断'
})


//添加题目的方法
const showAddScoreModal = ref(false)
const currentAddQuestion = ref(null)
const addScoreValue = ref(1)

const openAddScoreModal = (question) => {
  currentAddQuestion.value = question
  addScoreValue.value = 1
  showAddScoreModal.value = true
}

const confirmAddQuestion = () => {
  if (!addScoreValue.value || addScoreValue.value <= 0) {
    alert('请输入有效分数')
    return
  }
  // 检查是否已添加
  if (paper.value.questions.some(q => q.id == currentAddQuestion.value.questionId)) {
    alert('该题已在题目列表中')
    showAddScoreModal.value = false
    return
  }
  // 添加到题目列表 - 使用一致的数据结构
  paper.value.questions.push({
    id: currentAddQuestion.value.questionId, // 使用 questionId 而不是 id
    score: addScoreValue.value,
    questionText: currentAddQuestion.value.questionText // 保存题干
  })
  showAddScoreModal.value = false
}
</script>

<style scoped>
h1 {
  color: #000000;
}
.manual-create-paper {
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
  color: black;
}

.paper-info-group {
  border: 2px solid #0d47a1;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  background: #f5f8ff;
}

.view-library-btn {
  background-color: #2e7d32;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
}

.view-library-btn:hover {
  background-color: #45a049;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-group input[type="text"],
.form-group input[type="number"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1em;
}

.question-list {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 6px;
}

.question-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.question-row input {
  flex: 1;
}

.question-row input:first-child {
  flex: 3;
}

.question-row input:last-child {
  flex: 1;
}

.remove-btn {
  background-color: #c62828;
  color: white;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 1.2em;
  line-height: 1;
}

.add-btn {
  background-color: #0d47a1;
  color: white;
  padding: 8px 16px;
}

.add-btn:hover {
  background-color: #1565c0;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 0 0 10px 10px;
  padding: 0 10px 20px 10px;
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
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 弹出层 */
.modal2 {
  background: #fff;
  border-radius: 8px;
  border-color: #ccc;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-height: 90%;
  overflow: hidden;
  position: relative;
  margin-top: 40px; /* 新增：与上方间隔 */
}

/* 弹出层头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
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
  color:black;
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
}

.score {
  font-weight: bold;
  color: #0d47a1;
}

.options {
  margin-left: 20px;
  margin-bottom: 10px;
  color:black
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

.publish-btn {
  background-color: #2e7d32;
  color: white;
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
/* 来自questionbank的 */
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
.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.question-card {
  background-color: #282c34;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}
.add-question-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #0d47a1;
  color: #f0f0f0;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  font-size: 20px;
  cursor: pointer;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: normal;
  padding: 0;
}
.question-text {
  margin-bottom: 15px;
  font-weight: bold;
}
.view-library-btn {
  background-color: #4caf50; /* 默认绿色 */
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.view-library-btn.close-mode {
  background-color: #f44336; /* 红色 */
}
/* 此处为题库 */
</style>
