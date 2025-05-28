<template>
  <div :key="paperInfo.paperId" class="paperInfo-card">
    <p><strong>考试名称：</strong>{{ paperInfo.paperName }}</p>
    <p><strong>剩余时间：</strong>{{ formatTime(remainingTime) }}</p>

    <div v-for="(question, index) in paperInfo.paperQuestions" :key="question.questionId" class="question-card">
      <h3>第 {{ index + 1 }} 题：{{ question.questionText }}</h3>
      <div class="options">
        <div
            v-for="(option, optionIndex) in getoptions(question)"
            :key="option.value"
            class="option"
        >
          <!-- 根据题型渲染不同的控件 -->
          <template v-if="question.questionType === 'Single Choice' || question.questionType === 'True/False'">
            <input
                type="radio"
                :name="'question-' + question.questionId"
                :value="String.fromCharCode(65 + optionIndex)"
                v-model="studentAnswers[question.questionId]"
            />
          </template>
          <template v-if="question.questionType === 'Multiple Choice'">
            <input
                type="checkbox"
                :value="String.fromCharCode(65 + optionIndex)"
                v-model="studentAnswers[question.questionId]"
                @change="selectOption(question, String.fromCharCode(65 + optionIndex))"online_test_db
            />
          </template>

          {{ String.fromCharCode(65 + optionIndex) }}. {{ option.label }}
        </div>
      </div>
    </div>

    <button class="submit-btn" @click="submitExam">提交答卷</button>
  </div>
</template>



<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'

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


const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

const remainingTime = ref(0)
const timer = ref(null)


const startCountdown = () => {
  const rawCloseTime = paperInfo.value.closeTime
  const closeTimeDate = new Date(rawCloseTime)

  console.log('rawCloseTime:', rawCloseTime)
  console.log('closeTimeDate:', closeTimeDate)

  if (isNaN(closeTimeDate.getTime())) {
    console.error('closeTime 时间无效', rawCloseTime)
    return
  }

  const now = Date.now()

  const totalTime = Math.floor((closeTimeDate - now) / 1000)

  // 新增：判断考试是否已结束
  if (totalTime <= 0) {
    alert("考试已结束")
    // 可以选择跳转到仪表盘或其他页面
    router.push('/student/dashboard')
    return
  }
  console.log('totalTime:', totalTime)
  console.log('typeof totalTime:', typeof totalTime)

  remainingTime.value = Math.max(0, totalTime)

  if (timer.value) clearInterval(timer.value)

  timer.value = setInterval(() => {
    if (remainingTime.value <= 0) {
      clearInterval(timer.value)
      alert('考试时间已到，自动提交中...')
      submitExam()
    } else {
      remainingTime.value--
    }
  }, 1000)
}


onMounted(async () => {
  const paperId = parseInt(route.params.paperId)
  const courseId = parseInt(route.params.courseId)

  await fetchPaperQuestions(paperId, courseId)

  const initAnswers = {}
  paperInfo.value.paperQuestions.forEach(q => {
    if (q.questionType === 'Multiple Choice') {
      initAnswers[q.questionId] = []
    } else {
      initAnswers[q.questionId] = ''
    }
  })
  studentAnswers.value = initAnswers

  startCountdown()
})



const getoptions = (question) => {
  const letters = ['A', 'B', 'C', 'D']
  let validLetters = letters

  // 如果是判断题，只显示 A 和 B（即“正确”和“错误”）
  if (question.questionType === 'True/False') {
    validLetters = ['A', 'B']
  }
  console.log(question)
  return validLetters.map((letter, index) => ({
    value: letter,
    label: question[`option${letter}`],
    isCorrect: question.correctAnswer.includes(letter)
  }))
}

const fetchPaperQuestions = async (paperId, courseId) => {
  try {
    const params = new URLSearchParams({
      courseId: courseId,
      paperId: paperId,
    });
    const url = `http://localhost:8080/api/paper-questions/query-paper-and-questions?${params}`;
    const response=await fetch(url,{
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })

    if (!response.ok) {
      throw new Error('获取试卷失败')
    }

    const data = await response.json()
    paperInfo.value = data
    console.log('成功加载真实试卷:', data)
  } catch (error) {
    alert('无法加载试卷，请检查网络或服务状态')
    console.error(error)
  }
}
    /*console.log('使用模拟数据加载成功')
  } catch (error) {
    alert('加载考试状态失败，请检查网络或服务状态')
    console.error(error)
  }
}*/

const studentAnswers = ref({})


const selectOption = (question, optionValue) => {
  const questionId = question.questionId
  const questionType = question.questionType
  // console.log(studentAnswers.value[questionId])
}

const submitExam = async () => {
  const studentId = 123 // TODO: 从登录状态中获取真实学生ID

  const dto = {
    paperId: paperInfo.value.paperId,
    courseId: paperInfo.value.courseId,
    studentId: studentId,
    answers: Object.entries(studentAnswers.value).map(([questionId, answer]) => {
      const question = paperInfo.value.paperQuestions.find(
          q => q.questionId === parseInt(questionId)
      )

      var selectedOption=studentAnswers.value[questionId]
      if (question.questionType === 'Multiple Choice') {
        // 多选题：数组转为字符串拼接，如 ['A', 'C'] → 'AC'
        selectedOption = Array.isArray(answer) ? answer.join('') : ''
      } else {
        // 单选题/判断题：保持原样
        selectedOption = answer || ''
      }

      return {
        questionId: parseInt(questionId),
        answer:selectedOption
      }
    })
  }
  console.log(dto)
  try {
    const res = await fetch('http://localhost:8080/api/exam/submit', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(dto)
    })

    if (!res.ok) {
      throw new Error('提交失败')
    }

    alert('提交成功！')
    await router.push('/student/dashboard')
  } catch (error) {
    alert('提交失败，请重试')
    console.error(error)
  }
}


</script>

<style scoped>
.exam-questions {
  padding: 20px;
  color:black;
}

.exam-info-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.question-item {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.submit-btn {
  margin-top: 30px;
  padding: 12px 24px;
  background-color: #2e7d32;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: #388e3c;
}



</style>