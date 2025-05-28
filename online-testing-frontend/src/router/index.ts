import { createRouter, createWebHistory } from 'vue-router'

// 公共页面
import LoginView from '../views/Login.vue'

// 学生页面
import StudentDashboard from '../views/student/Dashboard.vue'
import StudentExamDetail from '../views/student/ExamDetail.vue'
import StudentResults from '../views/student/Results.vue'
import StudentExamQuestions from '../views/student/ExamQuestions.vue'
import StudentPastPapers from '../views/student/PastPapers.vue'
// 新增学生页面
import StudentResultDetail from '../views/student/ResultDetail.vue'
import StudentPastPaperDetail from '../views/student/PastPaperDetail.vue'
import ExamDetail from '../views/Teacher/ExamDetails.vue'
// 教师页面
import TeacherDashboard from '../views/teacher/Dashboard.vue'
import QuestionBank from '../views/teacher/QuestionBank.vue'
import CreatePaper from '../views/teacher/CreatePaper.vue'
import ManualCreatePaper from '../views/teacher/ManualCreatePaper.vue' // 手动出卷页面
import AutoCreatePaper from '../views/teacher/AutoCreatePaper.vue'
import TeacherPastPapers from '../views/teacher/PastPapers.vue'
import ExamManagement from '../views/teacher/ExamManagement.vue'
import StudentsScores from '../views/teacher/ExamStudentScore.vue'
import TeacherResultDetail from '../views/teacher/TeacherResultDetail.vue'
import TeacherEndExam from '../views/teacher/Examfinished.vue'
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/student/dashboard',
    name: 'StudentHome',
    component: StudentDashboard
  },
  {
    path: '/student/results',
    name: 'StudentResultList',
    component: StudentResults
  },
  {
    path: '/student/past-papers',
    name: 'StudentPastPaperList',
    component: StudentPastPapers
  },
  {
    path: '/teacher/dashboard',
    name: 'TeacherHome',
    component: TeacherDashboard
  },
  {
    path: '/student/exam/:id/detail',
    name: 'StudentExamDetail',
    component: StudentExamDetail
  },
  {
    path: '/student/exam/:courseId/:paperId',
    name: 'StudentExamQuestions',
    component: StudentExamQuestions
  },
  // 新增路由
  {
    path: '/student/result/:courseId/:paperId/:studentId',
    name: 'StudentResultDetails',
    component: StudentResultDetail
  },
  {
    path: '/student/past-paper/:id/details',
    name: 'StudentPastPaperDetails',
    component: StudentPastPaperDetail
  },
  // 教师端新增路由
  {
    path: '/teacher/question-bank',
    name: 'QuestionBank',
    component: QuestionBank
  },
  {
    path:'/teacher/exams_detail/:courseId/:paperId',
    name: 'ExamsDetail',
    component: ExamDetail
  },
  {
    path: '/teacher/question-bank',
    name: 'QuestionBank',
    component: QuestionBank
  },
  {
    path: '/teacher/create-paper',
    name: 'CreatePaper',
    component: CreatePaper // 这个页面作为选择入口
  },
  {
    path: '/teacher/create-paper/manual',
    name: 'ManualCreatePaper',
    component: ManualCreatePaper
  },
  {
    path: '/teacher/create-paper/manual-edit/:courseId/:paperId',
    name: 'ManualCreatePaperedit',
    component: ManualCreatePaper
  },
  {
    path: '/teacher/create-paper/auto',
    name: 'AutoCreatePaper',
    component: AutoCreatePaper
  },
  {
    path: '/teacher/past-papers',
    name: 'TeacherPastPapers',
    component: TeacherPastPapers
  },
  {
    path: '/teacher/exam-management',
    name: 'ExamManagement',
    component: ExamManagement
  },
  {
    path: '/teacher/exam-details-student-score/:courseId/:paperId',
    name: 'StudentsScores',
    component: StudentsScores
  },
  {
    path: '/teacher/exam-detail/student-exam-detail/:courseId/:paperId/:studentId',
    name: 'TeacherResultDetail',
    component: TeacherResultDetail
  },
  {
    path: '/teacher/endedexam-detail',
    name: 'EndedExamDetail',
    component: TeacherEndExam
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
