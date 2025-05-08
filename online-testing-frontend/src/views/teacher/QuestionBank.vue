<template>
  <div class="question-bank-container">
    <h1>题库编辑</h1>

    <div class="toolbar">
      <el-button type="primary" @click="showAddQuestionDialog">添加题目</el-button>
      <el-input
        v-model="searchQuery"
        placeholder="搜索题目"
        style="width: 200px; margin-left: 16px"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <el-table :data="questions" style="width: 100%" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="type" label="题型" width="100">
        <template #default="scope">
          <el-tag :type="getQuestionTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
      <el-table-column prop="difficulty" label="难度" width="100">
        <template #default="scope">
          <el-rate
            v-model="scope.row.difficulty"
            disabled
            show-score
            text-color="#ff9900"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="editQuestion(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="deleteQuestion(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑题目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题目' : '添加题目'"
      width="70%"
    >
      <el-form :model="questionForm" label-width="100px">
        <el-form-item label="题型">
          <el-select v-model="questionForm.type" placeholder="请选择题型">
            <el-option label="单选题" value="单选题" />
            <el-option label="多选题" value="多选题" />
            <el-option label="判断题" value="判断题" />
            <el-option label="简答题" value="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
          />
        </el-form-item>
        <el-form-item label="难度">
          <el-rate v-model="questionForm.difficulty" />
        </el-form-item>
        <el-form-item label="选项" v-if="['单选题', '多选题'].includes(questionForm.type)">
          <div v-for="(option, index) in questionForm.options" :key="index" class="option-item">
            <el-input v-model="option.content" placeholder="请输入选项内容">
              <template #prepend>{{ String.fromCharCode(65 + index) }}</template>
            </el-input>
            <el-checkbox v-model="option.isCorrect">正确答案</el-checkbox>
            <el-button type="danger" link @click="removeOption(index)">删除</el-button>
          </div>
          <el-button type="primary" link @click="addOption">添加选项</el-button>
        </el-form-item>
        <el-form-item label="答案" v-if="questionForm.type === '判断题'">
          <el-radio-group v-model="questionForm.answer">
            <el-radio label="true">正确</el-radio>
            <el-radio label="false">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="参考答案" v-if="questionForm.type === '简答题'">
          <el-input
            v-model="questionForm.answer"
            type="textarea"
            :rows="4"
            placeholder="请输入参考答案"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuestion">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Search } from '@element-plus/icons-vue'

// 模拟题目数据
const questions = ref([
  {
    id: 1,
    type: '单选题',
    content: '以下哪个不是Java的基本数据类型？',
    difficulty: 3,
    createTime: '2024-03-20 10:00:00'
  },
  {
    id: 2,
    type: '多选题',
    content: '以下哪些是Java的访问修饰符？',
    difficulty: 4,
    createTime: '2024-03-20 11:00:00'
  }
])

const searchQuery = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

const questionForm = reactive({
  type: '',
  content: '',
  difficulty: 3,
  options: [],
  answer: ''
})

const getQuestionTypeTag = (type) => {
  const typeMap = {
    '单选题': 'primary',
    '多选题': 'success',
    '判断题': 'warning',
    '简答题': 'info'
  }
  return typeMap[type] || 'info'
}

const showAddQuestionDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editQuestion = (row) => {
  isEdit.value = true
  Object.assign(questionForm, row)
  dialogVisible.value = true
}

const deleteQuestion = (row) => {
  // TODO: 实现删除题目的逻辑
  console.log('删除题目：', row)
}

const addOption = () => {
  questionForm.options.push({
    content: '',
    isCorrect: false
  })
}

const removeOption = (index) => {
  questionForm.options.splice(index, 1)
}

const resetForm = () => {
  questionForm.type = ''
  questionForm.content = ''
  questionForm.difficulty = 3
  questionForm.options = []
  questionForm.answer = ''
}

const saveQuestion = () => {
  // TODO: 实现保存题目的逻辑
  console.log('保存题目：', questionForm)
  dialogVisible.value = false
}
</script>

<style scoped>
.question-bank-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 