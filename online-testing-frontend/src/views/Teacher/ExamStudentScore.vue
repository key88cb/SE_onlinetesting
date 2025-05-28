<template>
    <div class="actions">
      <button class="btn back-btn" @click="goBack">返回列表</button>
      <button class="btn print-btn" v-if="isedit">上传成绩</button>
    </div>
  <!-- <div class="exam-details">
    <h2>分数详情 - {{ paperInfo.paperName }}</h2>

    <div class="exam-info">
      <div class="info-item">
        <strong>考试时间：</strong>{{ formatDate(paperInfo.openTime) }} 至 {{ formatDate(paperInfo.closeTime) }}
      </div>
      <div class="info-item">
        <strong>试卷总分：</strong>{{ paperInfo.totalScores }}
      </div>
    </div> -->
    <div class="ranking-list">
    <!-- 表头 -->
    <div class="header">
      <span>排名</span>
      <span>用户</span>
      <span>分数</span>
      <span>开始时间</span>
      <span>结束时间</span>
      <span>修改成绩</span>

    </div>
    

    <!-- 用户列表 -->
    <div v-for="(student, index) in computedstudents" :key="index" class="user-row">
      <span>{{ student.rank }}</span>
      <span class="user-info">
        <!-- <span
          v-if="user.studentId === currentStudentId"
          class="current-user-badge"
        >我</span> -->
        {{ student.studentId }}
      </span>
      <span>{{ student.score }}</span>
      <span>0</span>
      <span>0</span>
      <button class="btn" @click="gotoedit(student.studentId)">修改</button>
    </div>
  </div>

  <!-- </div> -->
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isedit=ref(false)
const allexamresults=ref([{
  studentId:1,
  totalScore: 65,},
  {
  studentId:2,
  totalScore: 70,
  }
]
)
onMounted(() => {
  console.log(route.params)
  const paperId = parseInt(route.params.paperId)
  const courseId = parseInt(route.params.courseId)
  if(route.query.mode&&route.query.mode=='edit')
  isedit.value=true
  // fetchPaperQuestions(paperId, courseId);
  fetchexamresults(paperId, courseId);
})
const fetchexamresults = async (paperId, courseId) => {
  // console.log(constId.value,creator.value)
  // 模拟网络延迟
    try {
      const params = new URLSearchParams({
      courseId: courseId,
      paperId: paperId,
    });
    const url = `http://localhost:8080/api/exam/search-examResult-for-all?${params}`;
      const res=await fetch(url,{
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    if (!res.ok) {
      throw new Error('网络错误')
    }
    const data =await res.json()
    console.log("examresults:",data)
    allexamresults.value = data
  }  catch (error) {
    alert('加载examresults失败，请检查网络或服务状态')
    console.error(error)
  }
}
const computedstudents=computed(() => {
  // 计算排名
  const sortedResults = [...allexamresults.value].sort((a, b) => b.totalScore - a.totalScore)
  return sortedResults.map((result, index) => ({
    ...result,
    rank: index + 1,
    score: result.totalScore,
    studentId: result.studentId
  }))
})
const fetchPaperQuestions = async (paperId, courseId) => {
  // console.log(constId.value,creator.value)
  // 模拟网络延迟
    try {
      const params = new URLSearchParams({
      courseId: courseId,
      paperId: paperId,
    });
    const url = `http://localhost:8080/api/paper-questions/query-paper-and-questions?${params}`;
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
  const data =await res.json()
  paperInfo.value = data
  console.log("paperInfo:",paperInfo.value)
  // console.log('hey')
    // paperInfos.value = data
  } catch (error) {
    alert('加载考试状态失败，请检查网络或服务状态')
    console.error(error)
  }
}

// 日期格式化
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 返回考试列表
const goBack = () => {
  if (isedit.value==true)
    router.push('/teacher/endedexam-detail')
  else
    router.push('/teacher/exams_detail/'+ route.params.courseId+ '/'+ route.params.paperId)
}
const gotoedit = (studentId) => {
  router.push('/teacher/exam-detail/student-exam-detail/' + route.params.courseId + '/' + route.params.paperId + '/' + studentId)
}
</script>

<style scoped>
h1 {
  color: #000000;
}
/* .exam-details {
  padding: 20px;
}

.exam-info {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  justify-content: space-between;
  gap: 20px;
}

.info-item {
  flex: 1;
  min-width: 250px;
  margin-bottom: 10px;
  font-size: 1em;
  color: #333;
} */
.back-btn {
  background-color: #666;
  color: white;
}

.back-btn:hover {
  background-color: #444;
}
.ranking-list {
  font-family: Arial, sans-serif;
  width: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-weight: bold;
  border-bottom: 3px solid #eee;
}

.user-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  border-bottom: 3px solid #eee; /* 关键：添加下划线 */
}

.user-info {
  display: flex;
  align-items: center;
}

.current-user-badge {
  background-color: #2ecc71; /* 绿色背景 */
  color: white;
  padding: 2px 5px;
  border-radius: 4px;
  margin-right: 5px;
}
.actions{
  display: flex;
  justify-content: space-between;
}
</style>
