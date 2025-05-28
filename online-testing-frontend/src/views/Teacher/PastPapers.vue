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
          :key="paper.paperId"
          class="paper-card"
          @click="viewPaperDetails(paper)"
      >
        <h3>试卷名称：{{ paper.title }}</h3>
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
import { ref, computed,onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const pastPapers = ref([])

onMounted(async () => {
  try {
    // 拉取所有已结束考试
    const res = await fetch('http://localhost:8080/api/paper-questions/exams?status=ended', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
    if (!res.ok) throw new Error('网络错误')
    const data = await res.json()
    // 适配后端数据结构
    pastPapers.value = data.map(paper => ({
      paperId: paper.paperId,
      courseId: paper.courseId,
      title: paper.paperName || '',
      subject: paper.courseId ? String(paper.courseId) : '', // 可根据实际后端返回的科目字段调整
      year: paper.openTime ? new Date(paper.openTime).getFullYear() : '',
      totalQuestions: paper.paperQuestions ? paper.paperQuestions.length : undefined,
      fullScore: paper.totalScores,
      paperName: paper.paperName,
      openTime: paper.openTime,
      closeTime: paper.closeTime,
      creator: paper.creator
    }))
  } catch (error) {
    alert('加载试卷失败，请检查网络或服务状态')
    console.error(error)
  }
})

const searchQuery = ref('')
const selectedSubject = ref('')
const selectedYear = ref('')

// 获取所有唯一科目（这里用 courseId 代替，实际可根据后端返回的科目字段调整）
const uniqueSubjects = computed(() => {
  return [...new Set(pastPapers.value.map(paper => paper.subject).filter(Boolean))]
})

// 获取年份选项
const yearOptions = computed(() => {
  const years = new Set(pastPapers.value.map(paper => paper.year).filter(Boolean))
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
      (paper.title && paper.title.toLowerCase().includes(query)) ||
      (paper.subject && paper.subject.toLowerCase().includes(query))
    )
  }

  // 应用科目过滤
  if (selectedSubject.value) {
    results = results.filter(paper => paper.subject === selectedSubject.value)
  }

  // 应用年份过滤
  if (selectedYear.value) {
    results = results.filter(paper => String(paper.year) === String(selectedYear.value))
  }

  return results
})

const clearSearch = () => {
  searchQuery.value = ''
  selectedSubject.value = ''
  selectedYear.value = ''
}

const viewPaperDetails = (paper) => {
  router.push(`/teacher/past-paper/${paper.paperId}/details?courseId=${paper.courseId}`)
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
