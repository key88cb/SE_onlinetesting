<template>
  <div class="paperInfo-management">
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

    <div class="paperInfos-list" v-if="currentTab === 'notStarted'">
      <div
          v-for="paperInfo in notStartedpaperInfos"
          :key="paperInfo.paperId"
          class="paperInfo-card"
      >
      <h3>考试名称:{{ paperInfo.paperName }}</h3>
        <p>计划时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>

        <div class="card-actions">
          <button class="btn edit-btn" @click.stop="openSettingsModal(paperInfo)">修改考试设置</button>
          <button class="btn delete-btn" @click.stop="deletepaperInfo(paperInfo)">删除考试</button>
        </div>
      </div>
    </div>

    <div class="paperInfos-list" v-if="currentTab === 'ongoing'">
      <div
          v-for="paperInfo in ongoingpaperInfos"
          :key="paperInfo.paperId"
          class="paperInfo-card"
      >
      <h3>考试名称:{{ paperInfo.paperName }}</h3>
        <!-- <p>当前时间：进行中（{{ paperInfo.studentsCount }}人参考）</p> -->
        <p>剩余时间：{{ formatTime(paperInfo.remainingTime) }}</p>
      </div>
    </div>

    <div class="paperInfos-list" v-if="currentTab === 'ended'">
      <div
          v-for="paperInfo in endedpaperInfos"
          :key="paperInfo.paperId"
          class="paperInfo-card ended-paperInfo"
      >
        <h3>考试名称:{{ paperInfo.paperName }}</h3>
        <p>考试时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>
        <div class="paperInfo-actions">
          <button class="delete-button" @click="searchforinfo(paperInfo.paperId,paperInfo.courseId)">查看考试详情</button>
        </div>
      </div>
    </div>

    <div v-if="nopaperInfos" class="no-results">
      没有找到相关考试
    </div>
    <!-- 编辑考试设置模态框 -->
    <div v-if="showSettingsModal" class="modal">
      <div class="modal-content">
        <h2>修改考试设置：{{ selectedpaperInfo.title }}</h2>

        <div class="form-group">
          <label>考试时间：</label>
          <div class="datetime-pickers">
            <input type="datetime-local" v-model="selectedpaperInfo.openTime" />
            <span>至</span>
            <input type="datetime-local" v-model="selectedpaperInfo.closeTime" />
          </div>
        </div>
        <div class="form-group">
        <div class="update-name">
          <label>考试名称：</label>
          <input type="text" v-model="selectedpaperInfo.paperName" placeholder="请输入考试名称" />
        </div>
        </div>  

        <div class="modal-buttons">
          <button class="btn save-btn" @click="savepaperInfoSettings">保存修改</button>
          <button class="btn cancel-btn" @click="closeSettingsModal">取消</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch,onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
onMounted(() => {
  fetchPaperInfos();
})
const constId = ref(4)
const creator = ref('TeacherD')
const showSettingsModal = ref(false)
const fetchPaperInfos = async() => {
  // console.log(constId.value,creator.value)
  // 模拟网络延迟
    try {
      const params = new URLSearchParams({
      courseId: constId.value,
      creator: creator.value
    });
    const url = `http://localhost:8080/api/paper-questions/query-papers?${params}`;
      const res=await fetch(url,{
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    if (!res.ok) {
      throw new Error('网络错误')
    }
//此处需要修改
    var data =await res.json()
  paperInfos.value = data
  console.log(paperInfos.value)
    // paperInfos.value = data
  } catch (error) {
    alert('加载考试状态失败，请检查网络或服务状态')
    console.error(error)
  }
}
// 模拟考试数据
const paperInfos = ref([
{
  paperId: 101,
  courseId: 201,
  creator: '张老师',
  singleChoiceNum: 5,
  multipleChoiceNum: 3,
  trueFalseNum: 2,
  openTime: new Date().toISOString(), // 当前时间
  closeTime: new Date(Date.now() + 86400000).toISOString(), // 当前时间 + 1 天
  highestScoresForSingleChoice: 25,
  highestScoresForMultipleChoice: 30,
  highestScoresForTrueFalse: 10,
  totalScores: 65,
  paperName: '操作系统原理期中考试',

  paperQuestions: [
    {
      paperId: 101,
      courseId: 201,
      questionId: 1,
      points: 5,
      knowledgePoints: 'Java基础语法',
      questionText: '下列哪个是合法的标识符？',
      questionType: '单选题',
      correctAnswer: 'B',
      options: ['2variable', '_variable', '@variable', 'variable#'],
    },
    {
      paperId: 101,
      courseId: 201,
      questionId: 2,
      points: 5,
      knowledgePoints: '面向对象编程',
      questionText: '下列哪些是面向对象的特性？',
      questionType: '多选题',
      correctAnswer: 'A,B,C',
      options: ['封装', '继承', '多态', '函数式'],
    },
    {
      paperId: 101,
      courseId: 201,
      questionId: 3,
      points: 5,
      knowledgePoints: 'Java异常处理',
      questionText: 'finally块一定会被执行。',
      questionType: '判断题',
      correctAnswer: 'B',
      options: ['正确', '错误'],
    }
  ]
}])
const currentPaperInfo = ref(null
)
// 模拟学生答题数据
const openSettingsModal=(paperInfo)=>{
  showSettingsModal.value = true
  currentPaperInfo.value = paperInfo
  selectedpaperInfo.value = paperInfo
}
const searchforinfo=(paperId,courseId)=>{
  router.push(`/teacher/exams_detail/${courseId}/${paperId}`)
}
const savepaperInfoSettings = async () => {
  try{
  const res=await fetch('http://localhost:8080/api/paper-questions/update-paper-time',{
    method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        paperId: selectedpaperInfo.value.paperId,
        courseId: selectedpaperInfo.value.courseId,
        openTime: selectedpaperInfo.value.openTime,
        closeTime: selectedpaperInfo.value.closeTime,
        paperName: selectedpaperInfo.value.paperName,
      })
    })
    fetchPaperInfos();
    if (!res.ok) {
      throw new Error('网络错误')
    }
  }  
  catch (error) {
    console.error('修改考试时间失败:', error)
    alert('修改考试时间失败，请稍后再试')
}  
  }
// 当前选项卡
const currentTab = ref('notStarted')
const selectedpaperInfo = ref(null)
// 搜索过滤
const studentSearchQuery = ref('')
const searchQuery = ref('')

// 设置当前考试
const setActiveTab = (tab) => {
  currentTab.value = tab
  selectedpaperInfo.value = null
  showSettingsModal.value = false
}


// 计算不同状态的考试
const notStartedpaperInfos = computed(() => {
  const now = new Date()
  return paperInfos.value.filter(e => new Date(e.openTime) > now)
})

const ongoingpaperInfos = computed(() => {
  const now = new Date()
  return paperInfos.value.filter(e =>
      new Date(e.openTime) <= now && new Date(e.closeTime) > now
  ).map(paperInfo => ({
    ...paperInfo,
    remainingTime: Math.floor((new Date(paperInfo.closeTime) - now) / 1000),
  }))
})

const endedpaperInfos = computed(() => {
  const now = new Date()
  console.log(paperInfos.value)
  return paperInfos.value.filter(e =>
      new Date(e.closeTime) <= now
  ).map(paperInfo => {
    return {
      ...paperInfo
    }
  })
})
const  deletepaperInfo = async (paperInfo) => {
  try{
  const res=await fetch('http://localhost:8080/api/paper-questions/delete-paper',{
    method: 'Delete',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        paperId: paperInfo.paperId,
        courseId: paperInfo.courseId,
      })
    })
    fetchPaperInfos();
    if (!res.ok) {
      throw new Error('网络错误')
    }
  }  
  catch (error) {
    console.error('修改考试时间失败:', error)
    alert('修改考试时间失败，请稍后再试')
}  
  }
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}
const nopaperInfos = computed(() => {
  // console.log(endedpaperInfos.value)
  return (
      (currentTab.value === 'notStarted' && notStartedpaperInfos.value.length === 0) ||
      (currentTab.value === 'ongoing' && ongoingpaperInfos.value.length === 0) ||
      (currentTab.value === 'ended' && endedpaperInfos.value.length === 0)
  )
})


</script>



<style scoped>
h1 {
  color: #000000;
}
.paperInfo-management {
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
.paperInfo-card{
  margin: 10px;
  padding: 10px;
  border: 1px solid black;
}
</style>
