<template>
  <div class="exam-management">
    <h1>考试情况管理</h1>

    <div class="tabs">
      <button
          :class="['tab-btn', { active: currentTab === 'notStarted' }]"
          @click="setActiveTab('notStarted')"
      >
        未开始考试
      </button>
      <button
          :class="['tab-btn', { active: currentTab === 'ongoing' }]"
          @click="setActiveTab('ongoing')"
      >
        进行中考试
      </button>
      <button
          :class="['tab-btn', { active: currentTab === 'ended' }]"
          @click="setActiveTab('ended')"
      >
        已结束考试
      </button>
    </div>

    <div class="exams-list" v-if="currentTab === 'notStarted'">
      <div
          v-for="exam in notStartedExams"
          :key="exam.id"
          class="exam-card"
          @click="viewExamDetails(exam)"
      >
        <h3>{{ exam.title }}</h3>
        <p>科目：{{ exam.subject }}</p>
        <p>计划时间：{{ formatDate(exam.startTime) }} - {{ formatDate(exam.endTime) }}</p>

        <div class="card-actions">
          <button class="btn edit-btn" @click.stop="editExam(exam)">编辑设置</button>
          <button class="btn delete-btn" @click.stop="deleteExam(exam)">删除考试</button>
        </div>
      </div>
    </div>

    <div class="exams-list" v-if="currentTab === 'ongoing'">
      <div
          v-for="exam in ongoingExams"
          :key="exam.id"
          class="exam-card"
          @click="viewExamDetails(exam)"
      >
        <h3>{{ exam.title }}</h3>
        <p>科目：{{ exam.subject }}</p>
        <p>当前时间：进行中（{{ exam.studentsCount }}人参考）</p>
        <p>剩余时间：{{ formatTime(exam.remainingTime) }}</p>
      </div>
    </div>

    <div class="exams-list" v-if="currentTab === 'ended'">
      <div
          v-for="exam in endedExams"
          :key="exam.id"
          class="exam-card ended-exam"
          @click="viewExamResults(exam)"
      >
        <h3>{{ exam.title }}</h3>
        <p>科目：{{ exam.subject }}</p>
        <p>考试时间：{{ formatDate(exam.startTime) }} - {{ formatDate(exam.endTime) }}</p>
        <p>参考人数：{{ exam.studentsCount }} 人，平均分：{{ exam.avgScore }} 分</p>
      </div>
    </div>

    <div v-if="noExams" class="no-results">
      没有找到相关考试
    </div>

    <!-- 考试详情模态框 -->
    <div v-if="showDetailModal" class="modal">
      <div class="modal-content">
        <h2>考试详情：{{ selectedExam.title }}</h2>

        <div class="exam-info">
          <div class="info-row">
            <strong>考试名称：</strong> {{ selectedExam.title }}
          </div>
          <div class="info-row">
            <strong>所属科目：</strong> {{ selectedExam.subject }}
          </div>
          <div class="info-row">
            <strong>考试时间：</strong> {{ formatDate(selectedExam.startTime) }} - {{ formatDate(selectedExam.endTime) }}
          </div>
          <div class="info-row">
            <strong>考试时长：</strong> {{ formatTime(selectedExam.duration * 60) }}
          </div>
          <div class="info-row">
            <strong>总题数：</strong> {{ selectedquestions.length }}
          </div>
          <div class="info-row">
            <strong>试卷总分：</strong> {{ selectedExam.fullScore }}
          </div>
          <div class="info-row">
            <strong>及格分数：</strong> {{ selectedExam.passingScore }}
          </div>
        </div>

        <div class="questions-list">
          <h3>考试题目</h3>
          <div
              v-for="(question, index) in selectedquestions"
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
          </div>
        </div>

        <div class="modal-buttons">
          <button class="btn close-btn" @click="closeDetailModal">关闭</button>
          <button class="btn stats-btn" @click="viewStatistics(selectedExam)" v-if="currentTab === 'ended'">查看统计信息</button>
        </div>
      </div>
    </div>

    <!-- 编辑考试设置模态框 -->
    <div v-if="showSettingsModal" class="modal">
      <div class="modal-content">
        <h2>修改考试设置：{{ selectedExam.title }}</h2>

        <div class="form-group">
          <label>考试时间：</label>
          <div class="datetime-pickers">
            <input type="datetime-local" v-model="selectedExam.startTime" />
            <span>至</span>
            <input type="datetime-local" v-model="selectedExam.endTime" />
          </div>
        </div>

        <div class="form-group">
          <label>考试时长：</label>
          <div class="duration-picker">
            <input type="number" min="10" v-model.number="selectedExam.duration" />
            <span>分钟</span>
          </div>
        </div>

        <div class="form-group">
          <label>试卷总分：</label>
          <input type="number" min="1" v-model.number="selectedExam.fullScore" />
        </div>

        <div class="form-group">
          <label>及格分数：</label>
          <input type="number" min="0" :max="selectedExam.fullScore" v-model.number="selectedExam.passingScore" />
        </div>

        <div class="form-group">
          <label>是否允许查看答案：</label>
          <select v-model="selectedExam.showAnswersAfter">
            <option value="immediately">立即显示</option>
            <option value="afterEnd">考试结束后显示</option>
            <option value="never">不显示</option>
          </select>
        </div>

        <div class="modal-buttons">
          <button class="btn save-btn" @click="saveExamSettings">保存修改</button>
          <button class="btn cancel-btn" @click="closeSettingsModal">取消</button>
        </div>
      </div>
    </div>

    <!-- 统计信息模态框 -->
    <div v-if="showStatsModal" class="modal">
      <div class="modal-content stats-modal">
        <h2>考试统计信息：{{ selectedExam.title }}</h2>

        <div class="stats-section">
          <h3>整体数据</h3>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ selectedExam.studentsCount }}</div>
              <div class="stat-label">参考人数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ selectedExam.avgScore }}</div>
              <div class="stat-label">平均分</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ selectedExam.passRate }}</div>
              <div class="stat-label">及格率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ selectedExam.highestScore }}</div>
              <div class="stat-label">最高分</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ selectedExam.lowestScore }}</div>
              <div class="stat-label">最低分</div>
            </div>
          </div>

          <div class="charts-container">
            <div class="chart">
              <h4>分数分布</h4>
                <div class="score-distribution">
                  <div
                    v-for="range in scoreDistribution"
                    :key="range.label"
                    class="distribution-bar"
                    :style="{
                      height: range.percentage + '%',
                      width: '40px', // 柱子宽度
                      margin: '0 15px', // 柱子间距
                    }"
                  >
                    <div class="bar-label">
                    <div class="bar-label">{{ range.label }}</div>
                    <div class="bar-value">{{ range.count }}人 ({{ range.percentage }}%)</div>
                    </div>
                  </div>
                </div>
            </div>

            <div class="chart">
              <h4>题目正确率</h4>
              <div class="question-stats">
                <div
                    v-for="(stat, index) in questionStatistics"
                    :key="index"
                    class="question-stat"
                >
                  <div class="question-number">第{{ index + 1 }}题</div>
                  <div class="correct-rate">{{ stat.correctRate }}% 正确</div>
                  <div class="topic">考点：{{ stat.topic }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="student-results">
          <h3>学生答题情况</h3>
          <div class="search-filter">
            <input
                v-model="studentSearchQuery"
                placeholder="按姓名或学号搜索"
                class="search-input"
            />
            <button class="clear-btn" @click="clearStudentSearch">清除</button>
          </div>

          <div class="results-table">
            <table>
              <thead>
              <tr>
                <th>姓名</th>
                <th>学号</th>
                <th>得分</th>
                <th>用时</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr
                  v-for="result in filteredStudentResults"
                  :key="result.id"
              >
                <td>{{ result.studentName }}</td>
                <td>{{ result.studentId }}</td>
                <td :class="{
                    'high-score': result.score >= selectedExam.fullScore * 0.8,
                    'pass': result.score >= selectedExam.passingScore && result.score < selectedExam.fullScore * 0.8,
                    'fail': result.score < selectedExam.passingScore
                  }">
                  {{ result.score }}/{{ selectedExam.fullScore }}
                </td>
                <td>{{ formatDuration(result.duration) }}</td>
                <td>
                  <button class="btn detail-btn" @click.stop="viewStudentAnswers(result)">查看详情</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="modal-buttons">
          <button class="btn close-btn" @click="closeStatsModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 学生答卷详情模态框 -->
    <div v-if="showAnswerDetailModal" class="modal">
      <div class="modal-content answer-detail">
        <h2>学生答卷详情：{{ selectedStudentResult.studentName }}</h2>

        <div class="answer-header">
          <div><strong>考试：</strong> {{ selectedExam.title }}</div>
          <div><strong>学生：</strong> {{ selectedStudentResult.studentName }} ({{ selectedStudentResult.studentId }})</div>
          <div><strong>得分：</strong> {{ selectedStudentResult.score }}/{{ selectedExam.fullScore }}</div>
          <div><strong>用时：</strong> {{ formatDuration(selectedStudentResult.duration) }}</div>
        </div>

        <div class="answers-list">
          <div
              v-for="(question, index) in selectedquestions"
              :key="question.id"
              class="answer-item"
          >
            <div class="question-header">
              <span class="type-badge">{{ question.type }}</span>
              <span class="question-title">{{ index + 1 }}. {{ question.text }}</span>
              <span class="score">{{ getQuestionScore(index) }}/{{ question.fullScore }}</span>
            </div>

            <div class="options" v-if="!isEssayQuestion(question)">
              <div
                  v-for="(option, optionIndex) in question.options"
                  :key="option.value"
                  :class="[
                  'option',
                  { selected: option.value === selectedStudentResult.answers[index] },
                  { correct: option.isCorrect },
                  { wrong: option.value === selectedStudentResult.answers[index] && !option.isCorrect }
                ]"
              >
                <div class="option-label">
                  {{ String.fromCharCode(65 + optionIndex) }}. {{ option.label }}
                </div>
                <div class="option-result">
                  {{ option.isCorrect && question.type === '多选'&&!selectedStudentResult.answers[index].includes(option.value) ? '未选择正确选项' : '' }}
                  {{!option.isCorrect&&question.type === '多选'&&selectedStudentResult.answers[index].includes(option.value) ? '选择错误选项' : '' }}
                </div>
              </div>
            </div>

            <div class="essay-answer" v-else>
              <strong>学生作答：</strong>
              <p>{{ selectedStudentResult.answers[index] }}</p>
              <!-- <div class="essay-evaluation">
                <label>评分：<input type="number" v-model.number="selectedStudentResult.score" /></label>
                <button class="btn save-evaluation-btn" @click.stop="saveEvaluation">保存评分</button> -->
              <!-- </div> -->
            </div>

            <div class="question-explanation" v-if="question.explanation">
              <strong>解析：</strong>
              <p>{{ question.explanation }}</p>
            </div>
          </div>
        </div>

        <div class="modal-buttons">
          <button class="btn close-btn" @click="closeAnswerDetailModal">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 模拟考试数据
const exams = ref([
  {
    id: 1,
    title: '操作系统原理期中考试',
    subject: '操作系统原理',
    startTime: new Date(Date.now() + 86400000).toISOString(), // 明天开始
    endTime: new Date(Date.now() + 86400000 + 3600000).toISOString(), // 明天+1小时
    duration: 60,
    fullScore: 100,
    passingScore: 60,
    showAnswersAfter: 'afterEnd',
    questions: [1, 2, 3, 4, 5],
    studentsCount: 0,
    avgScore: 0,
    passRate: '0%'
  },
  {
    id: 2,
    title: '数据库基础期末考试',
    subject: '数据库基础',
    startTime: new Date().toISOString(),
    endTime: new Date(Date.now() + 7200000).toISOString(), // 2小时后
    duration: 90,
    fullScore: 100,
    passingScore: 60,
    showAnswersAfter: 'afterEnd',
    questions: [2, 6, 1, 5, 3],
    studentsCount: 32,
    avgScore: 76,
    passRate: '78%',
    highestScore: 95,
    lowestScore: 48
  },
  {
    id: 3,
    title: '计算机网络期中考试',
    subject: '计算机网络',
    startTime: new Date(Date.now() - 3600000).toISOString(), // 已开始
    endTime: new Date(Date.now() + 3600000).toISOString(), // 1小时后结束
    duration: 60,
    fullScore: 75,
    passingScore: 45,
    showAnswersAfter: 'afterEnd',
    questions: [3, 7, 1, 2, 5],
    studentsCount: 18
  },
  {
    id: 4,
    title: '软件工程期末考试',
    subject: '软件工程',
    startTime: new Date(Date.now() - 86400000).toISOString(), // 已开始
    endTime: new Date(Date.now() - 3600000).toISOString(), // 已结束
    duration: 120,
    fullScore: 100,
    passingScore: 60,
    showAnswersAfter: 'afterEnd',
    questions: [4, 3, 6, 1, 2],
    studentsCount: 25,
    avgScore: 82,
    passRate: '88%',
    highestScore: 98,
    lowestScore: 53
  }
])

// 模拟题目数据
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
])

// 模拟学生答题数据
const studentAnswers = ref({
  1: [
    { studentId: '20210101', studentName: '张三', answers: ['B', 'ACD', 'A', '分页存储管理系统将主存分成与页大小相同的块，从而避免了外部碎片的产生，但可能产生内部碎片。', 'B'] },
    { studentId: '20210102', studentName: '李四', answers: ['B', 'ACD', 'A', '分页系统', 'B'] },
    { studentId: '20210103', studentName: '王五', answers: ['B', 'ACD', 'A', '分页存储', 'B'] }
  ],
  2: [
    { studentId: '20210101', studentName: '张三', score: 82, duration: 4800, answers: ['A', 'C', 'B', 'B', 'A'] },
    { studentId: '20210102', studentName: '李四', score: 68, duration: 5100, answers: ['A', 'CD', 'A', 'A', 'A'] },
    { studentId: '20210103', studentName: '王五', score: 75, duration: 4500, answers: ['B', 'ACD', 'A', 'B', 'B'] }
  ],
  3: [
    { studentId: '20210101', studentName: '张三', answers: ['A', 'C', 'B', 'B', 'A'] },
    { studentId: '20210102', studentName: '李四', answers: ['A', 'C', 'A', 'A', 'A'] },
    { studentId: '20210103', studentName: '王五', answers: ['B', 'C', 'A', 'B', 'B'] }
  ],
  4: [
    { studentId: '20210101', studentName: '张三', score: 92, duration: 6600, answers: ['B', 'B', 'B', '分页存储管理系统将主存分成与页大小相同的块，从而避免了外部碎片的产生，但可能产生内部碎片。', 'B'] },
    { studentId: '20210102', studentName: '李四', score: 58, duration: 7200, answers: ['B', 'C', 'A', '分页系统', 'A'] },
    { studentId: '20210103', studentName: '王五', score: 79, duration: 6300, answers: ['B', 'ACD', 'A', '分页存储', 'B'] }
  ]
})

// 当前选项卡
const currentTab = ref('notStarted')
const selectedExam = ref(null)
const showDetailModal = ref(false)
const showSettingsModal = ref(false)
const showStatsModal = ref(false)
const showAnswerDetailModal = ref(false)
const selectedStudentResult = ref(null)
const selectedquestions =ref(questions)
// 搜索过滤
const studentSearchQuery = ref('')
const searchQuery = ref('')
const selectedSubject = ref('')

// 设置当前考试
const setActiveTab = (tab) => {
  currentTab.value = tab
  selectedExam.value = null
  showDetailModal.value = false
  showSettingsModal.value = false
  showStatsModal.value = false
}

// 获取所有唯一科目
const uniqueSubjects = computed(() => {
  return [...new Set(exams.value.map(e => e.subject))]
})

// 计算不同状态的考试
const notStartedExams = computed(() => {
  const now = new Date()
  return exams.value.filter(e => new Date(e.startTime) > now)
})

const ongoingExams = computed(() => {
  const now = new Date()
  return exams.value.filter(e =>
      new Date(e.startTime) <= now && new Date(e.endTime) > now
  ).map(exam => ({
    ...exam,
    remainingTime: Math.floor((new Date(exam.endTime) - now) / 1000),
    studentsCount: studentAnswers.value[exam.id]?.length || 0
  }))
})

const endedExams = computed(() => {
  const now = new Date()
  return exams.value.filter(e =>
      new Date(e.endTime) <= now
  ).map(exam => {
    const results = studentAnswers.value[exam.id] || []
    const totalStudents = results.length
    if (totalStudents === 0) {
      return {
        ...exam,
        studentsCount: 0,
        avgScore: 0,
        passRate: '0%'
      }
    }

    const totalScore = results.reduce((sum, r) => sum + r.score, 0)
    const avgScore = Math.round(totalScore / totalStudents)
    const passedCount = results.filter(r => r.score >= exam.passingScore).length
    const passRate = `${Math.round(passedCount / totalStudents * 100)}%`
    const highestScore = Math.max(...results.map(r => r.score))
    const lowestScore = Math.min(...results.map(r => r.score))

    return {
      ...exam,
      studentsCount: totalStudents,
      avgScore,
      passRate,
      highestScore,
      lowestScore
    }
  })
})

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}
const viewExamResults = (exam) => {
  selectedExam.value = exam
  showDetailModal.value = true
  showStatsModal.value = true
  currentTab.value = null
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

const isEssayQuestion = (question) => {
  return question.type === '简答'
}

const getQuestionScore = (index) => {
  return selectedStudentResult.value?.questionScores?.[index] || 0
}

const filteredStudentResults = computed(() => {
  const results = studentAnswers.value[selectedExam.value?.id] || []
  if (!studentSearchQuery.value.trim()) return results

  const query = studentSearchQuery.value.toLowerCase().trim()
  return results.filter(r =>
      r.studentName.toLowerCase().includes(query) ||
      r.studentId.includes(query)
  )
})

const scoreDistribution = computed(() => {
  const ranges = [
    { min: 90, max: 100, label: '90-100' },
    { min: 80, max: 89, label: '80-89' },
    { min: 70, max: 79, label: '70-79' },
    { min: 60, max: 69, label: '60-69' },
    { min: 0, max: 59, label: '0-59' }
  ]

  const results = studentAnswers.value[selectedExam.value?.id] || []
  const counts = Array(5).fill(0)

  results.forEach(r => {
    for (let i = 0; i < ranges.length; i++) {
      if (r.score >= ranges[i].min && r.score <= ranges[i].max) {
        counts[i]++
        break
      }
    }
  })

  const total = results.length
  return ranges.map((range, index) => ({
    ...range,
    count: counts[index],
    percentage: total ? ((counts[index] / total) * 100).toFixed(1) : 0
  }))
})
const closeStatsModal = () => {
  showStatsModal.value = false
}
const closeDetailModal = () => {
  showDetailModal.value = false
}
const closeSettingsModal = () => {
  showSettingsModal.value = false
}
const closeAnswerDetailModal = () => {
  showAnswerDetailModal.value = false
}
const questionStatistics = computed(() => {
  const results = studentAnswers.value[selectedExam.value?.id] || []
  const exam = selectedExam.value
  if (!exam || !results.length) return []

  return exam.questions.map((qId, index) => {
    const question = questions.value.find(q => q.id === qId)
    if (!question) return { correctRate: 0, topic: '未知' }

    let correctCount = 0
    results.forEach(result => {
      const answer = result.answers[index]
      if (question.type === '单选' || question.type === '判断') {
        const correctOption = question.options.find(o => o.isCorrect)
        if (answer === correctOption?.value) correctCount++
      } else if (question.type === '多选') {
        const correctOptions = question.options.filter(o => o.isCorrect).map(o => o.value)
        const studentAnswer = Array.isArray(answer) ? answer : answer.split('')
        if (JSON.stringify(studentAnswer.sort()) === JSON.stringify(correctOptions.sort())) {
          correctCount++
        }
      } else if (question.type === '简答') {
        // 简答题需人工评分，默认视为得分
        correctCount += result.questionScores?.[index] || 0
      }
    })

    var correctRate = question.type === '简答'
        ? Math.round(correctCount / results.length / question.fullScore * 100)
        : Math.round(correctCount / results.length * 100)
    if (isNaN(correctRate)) correctRate = 0
    return {
      correctRate,
      topic: question.topic || '暂无考点'
    }
  })
})

const viewStatistics = (exam) => {
  selectedExam.value = exam
  showStatsModal.value = true
}

const viewStudentAnswers = (result) => {
  selectedStudentResult.value = result
  showAnswerDetailModal.value = true
}

const clearStudentSearch = () => {
  studentSearchQuery.value = ''
}

const noExams = computed(() => {
  return (
      (currentTab.value === 'notStarted' && notStartedExams.value.length === 0) ||
      (currentTab.value === 'ongoing' && ongoingExams.value.length === 0) ||
      (currentTab.value === 'ended' && endedExams.value.length === 0)
  )
})


</script>



<style scoped>
h1 {
  color: #000000;
}
.exam-management {
  padding: 20px;
  background-color: #f0f4ff;
  color:black;
}
.delete-btn:hover {
  background-color: #d32f2f;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.stats-section {
  margin-top: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-item {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 1.5em;
  font-weight: bold;
  color: #0d47a1;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
}

.charts-container {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  margin-top: 30px;
}

.chart {
  flex: 1;
  min-width: 300px;
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.score-distribution {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  margin-top: 20px;
  position: relative;
  border-bottom: 1px solid #ccc;
  padding-bottom: 10px;
}

.distribution-bar {
  width: 30px;
  background-color: #0d47a1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  position: relative;
  transition: all 0.3s ease;
}

.bar-label {
  font-size: 0.8em;
  margin-bottom: 5px;
  text-align: center;
  position: absolute;
  bottom: 0;
  transform: translateY(25px);
}

.bar-value {
  font-size: 0.8em;
  margin-top: 5px;
  text-align: center;
}

.question-stats {
  margin-top: 20px;
}

.question-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 6px;
}

.question-number {
  font-weight: bold;
  color: #0d47a1;
}

.correct-rate {
  color: #2e7d32;
  font-weight: bold;
}

.topic {
  color: #666;
  font-size: 0.9em;
}

.results-table {
  margin-top: 20px;
}

.results-table table {
  width: 100%;
  border-collapse: collapse;
}

.results-table th, .results-table td {
  padding: 12px;
  border: 1px solid #ddd;
  text-align: left;
}

.results-table th {
  background-color: #0d47a1;
  color: white;
  font-weight: bold;
}

.high-score {
  background-color: #e8f5e9;
  color: #2e7d32;
  font-weight: bold;
}

.pass {
  background-color: #fff9c4;
  color: #fbc02d;
  font-weight: bold;
}

.fail {
  background-color: #ffebee;
  color: #c62828;
  font-weight: bold;
}

.answers-list {
  margin-top: 30px;
}

.answer-item {
  margin-bottom: 20px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.question-title {
  flex: 1;
  margin-right: 10px;
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

.option.selected {
  background-color: #e3f2fd;
}

.option.correct {
  background-color: #e8f5e9;
  font-weight: bold;
}

.option.wrong {
  background-color: #ffebee;
}

.question-explanation {
  margin-top: 10px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 6px;
}

.detail-btn {
  background-color: #0d47a1;
  color: white;
  padding: 6px 12px;
  font-size: 0.9em;
}

.detail-btn:hover {
  background-color: #1565c0;
}

.answer-detail {
  max-width: 900px;
  margin: 0 auto;
}

.answer-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ddd;
}

.answer-header div {
  margin-bottom: 10px;
}

.save-evaluation-btn {
  background-color: #2e7d32;
  color: white;
  padding: 6px 12px;
  font-size: 0.9em;
  margin-left: 10px;
}

.save-evaluation-btn:hover {
  background-color: #388e3c;
}
.exam-card{
  margin: 10px;
  padding: 10px;
  border: 1px solid black;
}
</style>
