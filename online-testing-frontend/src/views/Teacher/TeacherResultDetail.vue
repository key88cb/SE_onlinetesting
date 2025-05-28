<template>
  <div class="actions">
    <button class="btn back-btn" @click="goBack">返回列表</button>
  </div>
<div class="exam-details">
  <h1>考试详情 - {{ paperInfo.paperName }}</h1>

  <div class="exam-info">
    <div class="info-item">
      <strong>考试时间：</strong>{{ formatDate(paperInfo.openTime) }} 至 {{ formatDate(paperInfo.closeTime) }}
    </div>
    <div class="info-item">
      <strong>试卷得分：</strong>{{examresult.totalScore}}/{{ paperInfo.totalScores }}
    </div>
  </div>

  <div class="question-list">
    <h2>考试题目</h2>
    <div class="edit-button">
          <button class="btn edit-btn" @click="showSettingsModal = !showSettingsModal">{{ showSettingsModal ? '取消修改' : '编辑分数' }}</button>
        </div>
    <div class="questions-container">
      <div
          v-for="(record, index) in EditedRecord()"
          :key="record.questionId"
          class="question-card"
      >
        <div class="question-header">
          <span class="type-badge">{{ record.questionType }}</span>
          <span class="question-number">{{ index + 1 }}. {{ record.questionText }}</span>
          <span class="question-score2">{{ record.getpoints }}/</span>
          <span class="question-score">{{ record.points }}</span>
          
        </div>
        <div class="options">
          <div
              v-for="(option, optionIndex) in getoptions(record)"
              :key="option.value"
              :class="['option', 
              { selected: record.studentAnswer.includes(option.value)},
              { correct: option.isCorrect },
              { wrong: record.studentAnswer.includes(option.value) && !option.isCorrect }
              ]"
          >
          <div class="option-label">
            {{ option.value }}. {{ option.label }}
          </div>
          <div class="option-result">
            {{ option.isCorrect && record.questionType === 'Multiple Choice'&&!record.studentAnswer.includes(option.value) ? '未选择正确选项' : '' }}
            {{!option.isCorrect&&record.questionType === 'Multiple Choice'&&record.studentAnswer.includes(option.value) ? '选择错误选项' : '' }}
          </div> 
          </div>
        </div>
        <div v-if="showSettingsModal" class="modal">
        <div class="modal-content">
          <h3>修改分数</h3>
          <label for="newScore">新分数:</label>
          <input type="number" id="newScore" v-model="record.editscore" />
          <button @click="saveNewScore(record.editscore,record.courseId,record.paperId,record.questionId)">保存</button>
        </div>
      </div>
      </div>
      <div v-if="paperInfo.paperQuestions.length === 0" class="no-questions">
        暂无考试题目
      </div>
    </div>
  </div>

</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const showSettingsModal = ref(false)
const route = useRoute()
const router = useRouter()
const paperInfo = ref({
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
paperQuestions:[
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
})
const examresult=ref({
  studentId: 123,
  totalScore: 85
})
const records = ref({
  studentId: 123,
  courseId: 201,
  paperId: 101,
  detailedResults:[{
    paperId: 101,
    questionId: 1,
    courseId: 201,
    studentId: 123,
    correctAnswer: 'B',
    studentAnswer: 'B',
    points: 5,
  }]
})
onMounted(() => {
  console.log(route.params)
  const paperId = parseInt(route.params.paperId)
  const courseId = parseInt(route.params.courseId)
  const studentId = parseInt(route.params.studentId)
  fetchPaperQuestions(paperId, courseId);
  fetchExamResult(paperId, courseId, studentId);
  fetchrecords(paperId, courseId, studentId);
})
const fetchrecords = async (paperId, courseId, studentId) => {
  try {
    const params = new URLSearchParams({
      courseId: courseId,
      paperId: paperId,
      studentId: studentId
    });
    const url = `http://localhost:8080/api/exam/search-exam-for-one?${params}`;
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    if (!res.ok) {
      throw new Error('网络错误')
    }
    const data = await res.json()
    records.value = data
    console.log("record",records.value)
  } catch (error) {
    alert('加载考试记录失败，请检查网络或服务状态')
    console.error(error)
  }
}
const fetchExamResult = async (paperId, courseId, studentId) => {
  try {
    const params = new URLSearchParams({
      courseId: courseId,
      paperId: paperId,
      studentId: studentId
    });
    const url = `http://localhost:8080/api/exam/search-examResult-for-one?${params}`;
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    if (!res.ok) {
      throw new Error('网络错误')
    }
    const data = await res.json()
    examresult.value = data
  } catch (error) {
    alert('加载考试结果失败，请检查网络或服务状态')
    console.error(error)
  }
}
const EditedRecord=()=>{
  const recordsq= records.value.detailedResults
  console.log(recordsq)
  const questions=paperInfo.value.paperQuestions
  return questions.map(question => {
    const recordq = recordsq.find(r => r.questionId === question.questionId);
    // console.log(question)
    return {
      ...question,
      studentAnswer: recordq ? recordq.studentAnswer : '',
      getpoints: recordq ? recordq.points : 0,
      editscore: recordq ? recordq.points : 0, // 编辑分数
    };
  });
}
const getoptions = (question) => {
  if(question.questionType ==="True/False"){
    const letters = ['A', 'B']
  return letters.map((letter,index) => ({
    value: letter,
    label: question[`option${letter}`],
    isCorrect: question.correctAnswer.includes(letter),
  }))
  }{
  const letters = ['A', 'B', 'C', 'D']
  return letters.map((letter,index) => ({
    value: letter,
    label: question[`option${letter}`],
    isCorrect: question.correctAnswer.includes(letter),
  }))
  }
}
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
console.log(paperInfo.value)
// console.log('hey')
  // paperInfos.value = data
} catch (error) {
  alert('加载考试状态失败，请检查网络或服务状态')
  console.error(error)
}
}
const saveNewScore=async(newScore,courseId,paperId,questionId)=>{
  const studentId = parseInt(route.params.studentId)
  console.log("修改分数",newScore,courseId,paperId,studentId,questionId)
  try {
    const url = `http://localhost:8080/api/exam/edit-score`;
      const res=await fetch(url,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        courseId: courseId,
        paperId: paperId,
        studentId: studentId,
        questionId:questionId,
        score: newScore
      }
    )
    })
    if (!res.ok) {
      throw new Error('网络错误')
    }
    console.log("修改分数成功")
  }  catch (error) {
    alert('修改分数失败，请检查网络或服务状态')
    console.error(error)
  }
  fetchrecords(paperId, courseId, studentId);
  fetchExamResult(paperId, courseId, studentId)
}
// 日期格式化
const formatDate = (dateString) => {
const date = new Date(dateString)
return date.toLocaleString('zh-CN')
}


// 返回考试列表
const goBack = () => {
router.push('/teacher/exam-management')
}
</script>

<style scoped>
h1 {
color: #000000;
}
.exam-details {
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
}

.question-list {
background: white;
padding: 20px;
border-radius: 10px;
box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.questions-container {
margin-top: 15px;
}

.question-card {
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

.question-score {
font-weight: bold;
}
.question-score2 {
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
.option.selected {
  background-color: #e3f2fd;
}
.option.wrong {
  background-color: red;
}

.btn {
padding: 8px 16px;
border: none;
border-radius: 6px;
cursor: pointer;
transition: background-color 0.3s;
}

.edit-btn {
background-color: #2e7d32;
color: white;
}

.edit-btn:hover {
background-color: #388e3c;
}

.back-btn {
background-color: #666;
color: white;
}

.back-btn:hover {
background-color: #444;
}
</style>
