<template>
  <div class="past-papers-page">
    <h1>历年真题试卷</h1>

    <div class="controls-container">
      <div class="search-bar">
        <input
            v-model="searchQuery"
            placeholder="输入科目或关键词搜索试卷"
            class="form-control search-input"
        />
        <button @click="clearSearch" class="btn secondary-btn clear-btn" title="清除筛选">
          <i class="icon-clear"></i> 清除
        </button>
      </div>

      <div class="filter-options">
        <select v-model="selectedSubject" class="form-control subject-filter">
          <option value="">全部科目</option>
          <option v-for="subjectItem in uniqueSubjects" :key="subjectItem" :value="subjectItem">{{ subjectItem }}</option>
        </select>

        <select v-model="selectedYear" class="form-control year-filter">
          <option value="">全部年份</option>
          <option v-for="yearItem in yearOptions" :key="yearItem" :value="yearItem">{{ yearItem }}</option>
        </select>
      </div>
    </div>

    <div class="papers-grid">
      <div v-if="isLoading" class="loading-indicator">
        <p>正在加载历年试卷...</p>
      </div>
      <div
          v-else-if="filteredPapers.length > 0"
          v-for="paper in filteredPapers"
          :key="paper.paperId"
          class="paper-card"
          @click="viewPaperDetails(paper)"
          tabindex="0"
          role="button"
          :aria-label="`查看试卷详情: ${paper.title}`"
      >
        <div class="card-header">
          <h3 class="paper-title">{{ paper.title }}</h3>
          <span class="paper-year-badge" v-if="paper.year">{{ paper.year }}</span>
        </div>
        <div class="paper-info">
          <p><span class="info-label">科目：</span>{{ paper.subject || 'N/A' }}</p>
          <p v-if="paper.totalQuestions !== undefined"><span class="info-label">总题数：</span>{{ paper.totalQuestions }} 题</p>
          <p v-if="paper.fullScore !== undefined"><span class="info-label">总分：</span>{{ paper.fullScore }} 分</p>
        </div>
        <div class="card-footer">
          <span class="view-details-link">查看详情 &rarr;</span>
        </div>
      </div>
      <div v-if="!isLoading && filteredPapers.length === 0" class="no-results">
        <p>没有找到符合条件的历年试卷。</p>
        <p class="sub-text">请尝试调整搜索关键词或筛选条件。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isLoading = ref(true); // Initialize with true to show loading on mount
const pastPapers = ref([]);

// --- 题目类型中英文映射 (如果需要显示题目类型) ---
// const QUESTION_TYPE_MAP_TO_CHINESE = { ... };
// const getDisplayQuestionType = (backendType) => { ... };
// ---

// --- 科目ID到名称的映射 (示例，您应根据实际情况调整或从API获取) ---
const COURSE_ID_TO_NAME_MAP = {
  1: '操作系统原理',
  2: '数据库基础',
  3: '计算机网络',
  4: '数据结构与算法',
  5: '软件工程导论',
  // 添加更多映射
};

const getCourseNameById = (courseId) => {
  return COURSE_ID_TO_NAME_MAP[courseId] || `科目ID ${courseId}`; // Fallback
};
// ---

onMounted(async () => {
  isLoading.value = true;
  try {
    const res = await fetch('http://localhost:8080/api/paper-questions/exams?status=ended', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => null);
      throw new Error(errorData?.message || `网络错误 ${res.status}`);
    }
    const data = await res.json();
    pastPapers.value = data.map(paper => ({
      paperId: paper.paperId,
      courseId: paper.courseId,
      title: paper.paperName || '未命名试卷',
      subject: getCourseNameById(paper.courseId), // 使用映射获取科目名称
      year: paper.openTime ? new Date(paper.openTime).getFullYear() : null,
      totalQuestions: paper.paperQuestions ? paper.paperQuestions.length : undefined,
      fullScore: paper.totalScores,
      // Preserving original fields if needed by other parts or detail view
      paperName: paper.paperName,
      openTime: paper.openTime,
      closeTime: paper.closeTime,
      creator: paper.creator
    }));
  } catch (error) {
    alert(`加载历年试卷失败: ${error.message}`);
    console.error("Failed to load past papers:", error);
    pastPapers.value = []; // Ensure it's an empty array on error
  } finally {
    isLoading.value = false;
  }
});

const searchQuery = ref('');
const selectedSubject = ref('');
const selectedYear = ref('');

const uniqueSubjects = computed(() => {
  return [...new Set(pastPapers.value.map(paper => paper.subject).filter(Boolean))].sort();
});

const yearOptions = computed(() => {
  const years = new Set(pastPapers.value.map(paper => paper.year).filter(Boolean));
  return Array.from(years).sort((a, b) => b - a);
});

const filteredPapers = computed(() => {
  let results = [...pastPapers.value];
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase().trim();
    results = results.filter(paper =>
        (paper.title && paper.title.toLowerCase().includes(query)) ||
        (paper.subject && paper.subject.toLowerCase().includes(query))
    );
  }
  if (selectedSubject.value) {
    results = results.filter(paper => paper.subject === selectedSubject.value);
  }
  if (selectedYear.value) {
    results = results.filter(paper => String(paper.year) === String(selectedYear.value));
  }
  return results;
});

const clearSearch = () => {
  searchQuery.value = '';
  selectedSubject.value = '';
  selectedYear.value = '';
};

const viewPaperDetails = (paper) => {
  router.push(`/teacher/past-paper/${paper.paperId}/details?courseId=${paper.courseId}`);
};
</script>

<style scoped>
/* --- 全局与页面布局 --- */
.past-papers-page {
  padding: 25px 35px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa;
  min-height: 100vh;
  color: #333;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 35px;
  font-size: 2.4em;
  font-weight: 600;
}

/* --- 控制区域容器 --- */
.controls-container {
  background-color: #fff;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-bar {
  display: flex;
  width: 100%;
}

/* --- 表单控件统一样式 --- */
.form-control {
  display: block;
  width: 100%;
  padding: 12px 18px;
  font-size: 1em;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  appearance: none;
  border-radius: 8px;
  transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  box-sizing: border-box;
}
.form-control:focus {
  color: #495057;
  background-color: #fff;
  border-color: #86b7fe;
  outline: 0;
  box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);
}
select.form-control {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m2 5 6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.9rem center;
  background-size: 16px 12px;
  cursor: pointer;
}

.search-input.form-control { /* Applied to search input */
  flex-grow: 1;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-right: none;
}
.search-input.form-control:focus {
  z-index: 1;
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer;
  font-size: 1em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.btn:hover { opacity: 0.85; transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.btn:active { transform: translateY(0); box-shadow: 0 2px 4px rgba(0,0,0,0.08); }
.btn i[class^="icon-"] { font-size: 1.1em; }

.secondary-btn { background-color: #6c757d; color: white; }
.secondary-btn:hover { background-color: #5a6268; }

.clear-btn { /* Applied to clear button */
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  padding: 12px 20px; /* Ensure consistent height with input */
  white-space: nowrap;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  width: 100%;
}

.subject-filter, /* Applied to select */
.year-filter {   /* Applied to select */
  flex: 1;
  min-width: 200px;
}

/* --- 试卷卡片网格 --- */
.papers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
}

.paper-card {
  background: white;
  padding: 22px 28px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.07);
  cursor: pointer;
  transition: transform 0.25s ease-out, box-shadow 0.25s ease-out, border-color 0.25s ease-out;
  display: flex;
  flex-direction: column;
  border-left: 5px solid #007bff; /* Default theme accent */
}
.paper-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-left-color: #0056b3; /* Darken accent on hover */
}
.paper-card:focus-visible {
  outline: 2px solid #007bff;
  outline-offset: 2px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 10px; /* Space between title and badge */
}
.paper-title {
  color: #2c3e50;
  font-size: 1.25em;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
  word-break: break-word; /* Prevent long titles from breaking layout */
}
.paper-year-badge {
  background-color: #6c757d;
  color: white;
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 0.8em;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0; /* Prevent badge from shrinking */
}

.paper-info {
  margin-top: 8px;
  font-size: 0.9em;
  color: #555;
  line-height: 1.7; /* Increased line height for readability */
  flex-grow: 1;
}
.paper-info p {
  margin: 8px 0; /* Increased paragraph spacing */
  display: flex;
}
.paper-info .info-label {
  font-weight: 500;
  color: #333;
  min-width: 65px; /* Adjusted for alignment */
  display: inline-block;
  margin-right: 5px;
}

.card-footer {
  margin-top: 15px;
  padding-top: 12px; /* Increased padding */
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
.view-details-link {
  color: #007bff;
  font-weight: 500;
  font-size: 0.9em;
  text-decoration: none;
}
.view-details-link:hover {
  text-decoration: underline;
  color: #0056b3;
}

/* --- 空状态与加载状态 --- */
.loading-indicator,
.no-results {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px 20px;
  margin-top: 20px;
  background-color: #f0f3f5;
  border-radius: 8px;
  border: 1px dashed #d0d9e0;
}
.loading-indicator p,
.no-results p {
  margin: 0 0 8px 0;
  font-size: 1.15em;
  color: #5a6268;
}
.no-results .sub-text {
  font-size: 0.95em;
  color: #868e96;
}

/* --- 响应式调整 --- */
@media (max-width: 768px) {
  .past-papers-page { padding: 20px 15px; } /* Adjusted padding for smaller screens */
  h1 { font-size: 2em; margin-bottom: 25px; }
  .controls-container { padding: 20px; flex-direction: column; }
  .search-bar { flex-direction: column; gap: 10px; }
  .search-input.form-control { border-radius: 8px; border-right: 1px solid #ced4da; }
  .clear-btn { border-radius: 8px; width: 100%; }

  .filter-options { flex-direction: column; gap: 15px; }
  .subject-filter, .year-filter { min-width: 100%; }

  .papers-grid { grid-template-columns: 1fr; gap: 18px; }
  .paper-card { padding: 18px 22px; }
  .paper-title { font-size: 1.15em; }
  .paper-info { font-size: 0.85em; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 8px; }
  .paper-year-badge { margin-left: 0; }
}

/* Placeholder icons */
.icon-clear::before { content: "✗"; margin-right: 5px; font-weight: bold;}
</style>