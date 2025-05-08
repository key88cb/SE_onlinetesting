<template>
  <div class="exam-manage-container">
    <h1>试卷管理</h1>

    <div class="toolbar">
      <el-button type="primary" @click="showCreateExamDialog">创建试卷</el-button>
      <el-input
        v-model="searchQuery"
        placeholder="搜索试卷"
        style="width: 200px; margin-left: 16px"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <el-table :data="exams" style="width: 100%" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="试卷名称" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column prop="duration" label="考试时长" width="120">
        <template #default="scope">
          {{ scope.row.duration }}分钟
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button type="primary" link @click="editExam(scope.row)">编辑</el-button>
          <el-button type="success" link @click="previewExam(scope.row)">预览</el-button>
          <el-button type="danger" link @click="deleteExam(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑试卷对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑试卷' : '创建试卷'"
      width="70%"
    >
      <el-form :model="examForm" label-width="100px">
        <el-form-item label="试卷名称">
          <el-input v-model="examForm.title" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="examForm.duration" :min="1" :max="180" />
          <span class="unit">分钟</span>
        </el-form-item>
        <el-form-item label="考试时间">
          <el-date-picker
            v-model="examForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="examForm.totalScore" :min="1" :max="100" />
          <span class="unit">分</span>
        </el-form-item>
        <el-form-item label="题目列表">
          <div class="question-list">
            <div v-for="(question, index) in examForm.questions" :key="index" class="question-item">
              <div class="question-header">
                <span class="question-index">第{{ index + 1 }}题</span>
                <el-button type="danger" link @click="removeQuestion(index)">删除</el-button>
              </div>
              <el-form-item label="题型">
                <el-select v-model="question.type" placeholder="请选择题型">
                  <el-option label="单选题" value="单选题" />
                  <el-option label="多选题" value="多选题" />
                  <el-option label="判断题" value="判断题" />
                  <el-option label="简答题" value="简答题" />
                </el-select>
              </el-form-item>
              <el-form-item label="题目">
                <el-select
                  v-model="question.id"
                  filterable
                  remote
                  placeholder="请选择题目"
                  :remote-method="searchQuestions"
                  :loading="loading"
                >
                  <el-option
                    v-for="item in questionOptions"
                    :key="item.id"
                    :label="item.content"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="分值">
                <el-input-number v-model="question.score" :min="1" :max="100" />
                <span class="unit">分</span>
              </el-form-item>
            </div>
            <el-button type="primary" link @click="addQuestion">添加题目</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveExam">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Search } from '@element-plus/icons-vue'

// 模拟试卷数据
const exams = ref([
  {
    id: 1,
    title: '2024年春季学期期中考试',
    totalScore: 100,
    duration: 120,
    startTime: '2024-03-20 09:00:00',
    endTime: '2024-03-20 11:00:00',
    status: '未开始'
  },
  {
    id: 2,
    title: 'Java程序设计基础测试',
    totalScore: 100,
    duration: 90,
    startTime: '2024-03-25 14:00:00',
    endTime: '2024-03-25 15:30:00',
    status: '未开始'
  }
])

const searchQuery = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const questionOptions = ref([])

const examForm = reactive({
  title: '',
  duration: 120,
  timeRange: [],
  totalScore: 100,
  questions: []
})

const getStatusType = (status) => {
  const statusMap = {
    '未开始': 'info',
    '进行中': 'success',
    '已结束': 'warning'
  }
  return statusMap[status] || 'info'
}

const showCreateExamDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editExam = (row) => {
  isEdit.value = true
  Object.assign(examForm, {
    ...row,
    timeRange: [row.startTime, row.endTime]
  })
  dialogVisible.value = true
}

const deleteExam = (row) => {
  // TODO: 实现删除试卷的逻辑
  console.log('删除试卷：', row)
}

const previewExam = (row) => {
  // TODO: 实现预览试卷的逻辑
  console.log('预览试卷：', row)
}

const addQuestion = () => {
  examForm.questions.push({
    type: '',
    id: null,
    score: 10
  })
}

const removeQuestion = (index) => {
  examForm.questions.splice(index, 1)
}

const searchQuestions = (query) => {
  // TODO: 实现远程搜索题目的逻辑
  loading.value = true
  setTimeout(() => {
    questionOptions.value = [
      {
        id: 1,
        content: '以下哪个不是Java的基本数据类型？'
      },
      {
        id: 2,
        content: '以下哪些是Java的访问修饰符？'
      }
    ]
    loading.value = false
  }, 1000)
}

const resetForm = () => {
  examForm.title = ''
  examForm.duration = 120
  examForm.timeRange = []
  examForm.totalScore = 100
  examForm.questions = []
}

const saveExam = () => {
  // TODO: 实现保存试卷的逻辑
  console.log('保存试卷：', examForm)
  dialogVisible.value = false
}
</script>

<style scoped>
.exam-manage-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.unit {
  margin-left: 8px;
  color: #909399;
}

.question-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
}

.question-item {
  border-bottom: 1px dashed #dcdfe6;
  padding: 20px 0;
}

.question-item:last-child {
  border-bottom: none;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-index {
  font-weight: bold;
  color: #409EFF;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 