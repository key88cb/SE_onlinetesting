<template>
  <div class="teacher-dashboard">
    <h1>教师控制台</h1>
    <p>欢迎回来，{{ username }}，Do you have anything to tell me?</p>

    <div class="actions">
      <div class="action-card" @click="navigateTo('/teacher/question-bank')">
        <h3>题库管理</h3>
        <p>添加、编辑和删除试题</p>
      </div>

      <div class="action-card" @click="navigateTo('/teacher/create-paper')">
        <h3>编辑发布试卷</h3>
        <p>手动或自动组卷并发布考试</p>
      </div>

      <div class="action-card" @click="navigateTo('/teacher/past-papers')">
        <h3>查看历年卷</h3>
        <p>查看所有历史试卷及答案</p>
      </div>

      <div class="action-card" @click="navigateTo('/teacher/exam-management')">
        <h3>考试情况管理</h3>
        <p>查看已发布考试及学生答题情况</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('王老师')

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
h1{
  color: black;
}
p{
  color: black;
}
.teacher-dashboard {
  padding: 20px;
}

.actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.action-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.action-card h3 {
  margin-top: 0;
  color: #0d47a1;
  font-size: 1.2em;
}

.action-card p {
  margin: 8px 0 0;
  color: #666;
}
</style>
