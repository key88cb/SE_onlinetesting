<template>
  <div class="past-papers">
    <h1>历年真题试卷</h1>

    <div class="search-bar">
      <input
          v-model="searchQuery"
          placeholder="输入科目或关键词搜索试卷"
          class="search-input"
      />
      <button @click="clearSearch" class="clear-btn">清除</button>
    </div>

    <div class="filter-options">
      <select v-model="selectedSubject" class="subject-filter">
        <option value="">全部科目</option>
        <option v-for="subject in uniqueSubjects" :key="subject" :value="subject">{{ subject }}</option>
      </select>

      <select v-model="selectedYear" class="year-filter">
        <option value="">全部年份</option>
        <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}</option>
      </select>
    </div>

    <div class="papers-grid">
      <div
          v-for="paper in filteredPapers"
          :key="paper.id"
          class="paper-card"
          @click="viewPaperDetails(paper)"
      >
        <h3>{{ paper.title }}</h3>
        <div class="paper-info">
          <p>科目：{{ paper.subject }}</p>
          <p>年份：{{ paper.year }}</p>
          <p>总题数：{{ paper.totalQuestions }}</p>
          <p>总分：{{ paper.fullScore }}</p>
        </div>
      </div>

      <div v-if="filteredPapers.length === 0" class="no-results">
        没有找到相关试卷
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 模拟历年真题数据，实际应从API获取
const pastPapers = ref([
  {
    id: 1,
    title: '2023年操作系统原理期末考试',
    subject: '操作系统原理',
    year: 2023,
    totalQuestions: 20,
    fullScore: 100
  },
  {
    id: 2,
    title: '2023年需求分析与设计期中考试',
    subject: '需求分析与设计',
    year: 2023,
    totalQuestions: 15,
    fullScore: 75
  },
  {
    id: 3,
    title: '2022年数据库基础期末考试',
    subject: '数据库基础',
    year: 2022,
    totalQuestions: 25,
    fullScore: 100
  },
  {
    id: 4,
    title: '2022年操作系统原理期中考试',
    subject: '操作系统原理',
    year: 2022,
    totalQuestions: 18,
    fullScore: 90
  }
])

const searchQuery = ref('')
const selectedSubject = ref('')
const selectedYear = ref('')

// 获取所有唯一科目
const uniqueSubjects = computed(() => {
  return [...new Set(pastPapers.value.map(paper => paper.subject))]
})

// 获取年份选项
const yearOptions = computed(() => {
  const years = new Set(pastPapers.value.map(paper => paper.year))
  const sortedYears = Array.from(years).sort((a, b) => b - a)
  return sortedYears
})

// 过滤后的试卷列表
const filteredPapers = computed(() => {
  let results = [...pastPapers.value]

  // 应用搜索查询
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    results = results.filter(paper =>
        paper.title.toLowerCase().includes(query) ||
        paper.subject.toLowerCase().includes(query)
    )
  }

  // 应用科目过滤
  if (selectedSubject.value) {
    results = results.filter(paper => paper.subject === selectedSubject.value)
  }

  // 应用年份过滤
  if (selectedYear.value) {
    results = results.filter(paper => paper.year.toString() === selectedYear.value.toString())
  }

  return results
})

const clearSearch = () => {
  searchQuery.value = ''
  selectedSubject.value = ''
  selectedYear.value = ''
}

const viewPaperDetails = (paper) => {
  router.push(`/student/past-paper/${paper.id}/details`)
}
</script>

<style scoped>
h1 {
  color: #000000;
}
.past-papers {
  padding: 20px;
}

.search-bar {
  display: flex;
  margin-bottom: 15px;
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

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 25px;
}

.subject-filter,
.year-filter {
  flex: 1;
  min-width: 180px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1em;
}

.papers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.paper-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.paper-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.paper-card h3 {
  margin-top: 0;
  color: #0d47a1;
  font-size: 1.2em;
}

.paper-info {
  margin-top: 10px;
  font-size: 0.95em;
  color: #555;
}

.paper-info p {
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
