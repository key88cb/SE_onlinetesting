<template>
  <div class="exam-results">
    <h1>我的考试记录</h1>

    <div class="search-filter">
      <div class="search-group">
        <input
            v-model="searchQuery"
            placeholder="按科目或日期搜索"
            class="search-input"
        />
        <button @click="clearSearch" class="clear-btn">清除</button>
      </div>

      <div class="filter-group">
        <select v-model="selectedDateFilter" class="date-filter">
          <option value="">所有时间</option>
          <option value="today">今天</option>
          <option value="thisWeek">本周</option>
          <option value="thisMonth">本月</option>
        </select>
      </div>
    </div>

    <div class="results-list">
      <div
          v-for="result in filteredResults"
          :key="result.id"
          class="result-card"
          @click="viewDetails(result)"
      >
        <div class="result-header">
          <h3>{{ result.subject }}</h3>
          <div class="score-badge" :class="getScoreBadgeClass(result.score)">
            {{ result.score }}分
          </div>
        </div>

        <div class="result-info">
          <p>考试时间：{{ formatDate(result.date) }}</p>
          <p>课程：{{ result.course }}</p>
          <p>用时：{{ formatDuration(result.duration) }}</p>
        </div>
      </div>

      <div v-if="filteredResults.length === 0" class="no-results">
        没有找到相关的考试记录
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 模拟考试结果数据，实际应从API获取
const examResults = ref([
  {
    id: 1,
    subject: '操作系统原理',
    course: '计算机基础',
    score: 85,
    date: new Date(Date.now() - 86400000).toISOString(), // 昨天
    duration: 3200, // 用时53分钟20秒
    fullScore: 100
  },
  {
    id: 2,
    subject: '需求分析与设计',
    course: '软件工程',
    score: 67,
    date: new Date(Date.now() - 86400000 * 3).toISOString(), // 三天前
    duration: 3400, // 用时56分钟40秒
    fullScore: 75
  },
  {
    id: 3,
    subject: '数据库基础',
    course: '计算机基础',
    score: 92,
    date: new Date(Date.now() - 86400000 * 7).toISOString(), // 一周前
    duration: 3300, // 用时55分钟
    fullScore: 100
  }
])

// 排序以确保最新的在最上面
examResults.value.sort((a, b) => new Date(b.date) - new Date(a.date))

const searchQuery = ref('')
const selectedDateFilter = ref('')

const filteredResults = computed(() => {
  let results = [...examResults.value]

  // 应用搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    results = results.filter(result =>
        result.subject.toLowerCase().includes(query) ||
        result.course.toLowerCase().includes(query) ||
        formatDate(result.date).toLowerCase().includes(query)
    )
  }

  // 应用日期过滤
  if (selectedDateFilter.value) {
    const now = new Date()
    switch(selectedDateFilter.value) {
      case 'today':
        results = results.filter(result => {
          const date = new Date(result.date)
          return date.getDate() === now.getDate() &&
              date.getMonth() === now.getMonth() &&
              date.getFullYear() === now.getFullYear()
        })
        break
      case 'thisWeek':
        results = results.filter(result => {
          const date = new Date(result.date)
          const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          return date >= oneWeekAgo
        })
        break
      case 'thisMonth':
        results = results.filter(result => {
          const date = new Date(result.date)
          return date.getMonth() === now.getMonth() &&
              date.getFullYear() === now.getFullYear()
        })
        break
    }
  }

  return results
})

const getScoreBadgeClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 60) return 'pass'
  return 'fail'
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatDuration = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}分钟${String(remainingSeconds).padStart(2, '0')}秒`
}

const clearSearch = () => {
  searchQuery.value = ''
  selectedDateFilter.value = ''
}

const viewDetails = (result) => {
  router.push(`/student/result/${result.id}/details`)
}
</script>

<style scoped>
h1{
  color: black;
}
.exam-results {
  padding: 20px;
}

.search-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 25px;
}

.search-group {
  flex: 1;
  min-width: 250px;
  display: flex;
}

.search-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px 0 0 6px;
  font-size: 1em;
}

.clear-btn {
  padding: 10px 20px;
  background-color: #0d47a1;
  color: white;
  border: none;
  border-radius: 0 6px 6px 0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.clear-btn:hover {
  background-color: #1565c0;
}

.filter-group {
  min-width: 150px;
}

.date-filter {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1em;
}

.results-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.result-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.result-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.result-header h3 {
  margin: 0;
  color: #0d47a1;
  font-size: 1.2em;
}

.score-badge {
  padding: 5px 10px;
  border-radius: 6px;
  font-weight: bold;
}

.excellent {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.good {
  background-color: #e0f2f1;
  color: #00796b;
}

.pass {
  background-color: #fff9c4;
  color: #fbc02d;
}

.fail {
  background-color: #ffebee;
  color: #c62828;
}

.result-info {
  margin-top: 10px;
  font-size: 0.95em;
  color: #555;
}

.result-info p {
  margin: 5px 0;
}

.no-results {
  text-align: center;
  color: #666;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 10px;
}
</style>
