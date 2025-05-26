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
        <strong>试卷总分：</strong>{{ paperInfo.totalScores }}
      </div>
    </div>
    
    <div class="question-list">
      <h2>考试题目</h2>
      <div class="questions-container">
        <div
            v-for="(question, index) in EditedpaperQuestions()"
            :key="question.questionId"
            class="question-card"
        >
          <div class="question-header">
            <span class="type-badge">{{ question.questionType }}</span>
            <span class="question-number">{{ index + 1 }}. {{ question.questionText }}</span>
            <span class="question-score">
              <span class="question-score1">得分率&nbsp;</span>
              <span class="question-score2">{{question.avgScore}}&nbsp;/&nbsp;</span>
              <span class="question-score">{{ question.points }}分</span>
            </span>
          </div>
          <div class="options">
            <div
                v-for="(option, optionIndex) in getoptions(question)"
                :key="option.value"
                :class="['option', { correct: option.isCorrect }]"
            >
            <div class="option-question">
              {{ String.fromCharCode(65 + optionIndex) }}. {{ option.label }}
            </div>
            <div> &nbsp;&nbsp;{{ option.count }}人选择该选项</div>
            </div>
          </div>
        </div>

        <div v-if="paperInfo.paperQuestions.length === 0" class="no-questions">
          暂无考试题目
        </div>

      </div>
    </div>
    <div class="charts-container">
      <h2>分数分布</h2>
      <div class="chart">
        <div class="score-distribution"
        >
        
          <div
            v-for="range in scoreDistribution()"
            :key="range.label"
            class="distribution-bar"
            :style="{
              height: range.percentage + '%',
              width: '80px',
              margin: '0 25px',
              display: 'flex',
              flexDirection: 'column-reverse',
              justifyContent: 'flex-end',
              alignItems: 'center',
              backgroundColor: '#42b883',
              color: '#fff',
              fontSize: '12px'
            }"
          >
            <div class="bar-label" style="text-align: center;">
              {{ range.label }}
            </div>
            <div class="bar-value" style="text-align: center;">
              {{ range.count }}人<br>({{ range.percentage }}%)
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const allexamresults=ref([{
  studentId:1,
  totalScore: 65,},
  {
  studentId:2,
  totalScore: 70,
  }
]
)
const allresults = ref({
  courseId: 201,
  paperId: 101,
  analyses:[{
    avgscore:1.1,
    counts:[3,2,1,1],
    questionId:1
  }
  ]
})
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
onMounted(() => {
  console.log(route.params)
  const paperId = parseInt(route.params.paperId)
  const courseId = parseInt(route.params.courseId)
  fetchPaperQuestions(paperId, courseId);
  fetchresult(paperId, courseId);
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
    const url = `http://localhost:8080/api/exam/search-exam_result-for-all?${params}`;
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
const fetchresult = async (paperId, courseId) => { 
  // console.log(constId.value,creator.value)
  // 模拟网络延迟
    try {
      const params = new URLSearchParams({
      courseId: courseId,
      paperId: paperId,
    });
    const url = `http://localhost:8080/api/exam/search-exam-for-all?${params}`;
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
  allresults.value = data
  console.log(allresults.value)
  console.log('hey')
    // paperInfos.value = data
  } catch (error) {
    alert('加载答题数据失败，请检查网络或服务状态')
    console.error(error)
  }
}
const EditedpaperQuestions = () => {
  const questions = paperInfo.value.paperQuestions;
  console.log(questions)
  return questions.map(question => {
    const analysis = allresults.value.analyses.find(
      item => item.questionId === question.questionId
    );

    return {
      ...question,
      avgScore: analysis?.avgscore.toFixed(2) ?? 0,
      counts: analysis?.counts ?? [0, 0, 0, 0]
    };
  });
};
const scoreDistribution =() => {
      const distribution = {
        '90%以上': 0,
        '80%-90%': 0,
        '70%-80%': 0,
        '60%-70%': 0,
        '60%以下': 0,
      };

      const total = allexamresults.value.length;

      allexamresults.value.forEach(result => {
        const percent = (result.totalScore / paperInfo.totalScores) * 100;
if (percent >= 90) {
          distribution['90%以上']++;
        } else if (percent >= 80) {
          distribution['80%-90%']++;
        } else if (percent >= 70) {
          distribution['70%-80%']++;
        } else if (percent >= 60) {
          distribution['60%-70%']++;
        } else {
          distribution['60%以下']++;
        }
      });
      console.log("分数分布:",distribution)
      // 转换成数组用于渲染
      return Object.keys(distribution).map(label => {
        const count = distribution[label];
        const percentage = total ? ((count / total) * 100).toFixed(1) : 0;
        return {
          label,
          count,
          percentage
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
    count: question.counts[index]
  }))
  }{
  const letters = ['A', 'B', 'C', 'D']
  return letters.map((letter,index) => ({
    value: letter,
    label: question[`option${letter}`],
    isCorrect: question.correctAnswer.includes(letter),
    count: question.counts[index]
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
.question-score1{ 
  font-weight: bold;
  color:red;
}
.question-score2{
  font-weight: bold;
  color:green;
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
.score-distribution {
  width:1000px;
  display: flex;
  align-items: flex-end;
  height: 300px;
  border: 1px solid #ddd;
  margin:0 auto;
  padding: 20px;
  background-color: #f9f9f9;
}

.distribution-bar {
  transition: height 0.3s ease;
  text-align: center;
  padding-top: 5px;
}
</style>
