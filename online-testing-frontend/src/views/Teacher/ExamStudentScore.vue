<template>
  <div class="student-scores-page">
    <div class="page-actions-bar">
      <button class="btn secondary-outline-btn back-btn" @click="goBack">
        <i class="icon-back-arrow"></i> {{ isedit ? '返回考试详情' : '返回上一页' }}
      </button>
      <button class="btn primary-btn upload-scores-btn" v-if="isedit" @click="uploadScores">
        <i class="icon-upload"></i> 上传成绩
      </button>
    </div>

    <h1>学生成绩排名</h1>

    <div class="ranking-list-container">
      <div class="ranking-list">
        <div class="list-header">
          <div class="header-item rank-col">排名</div>
          <div class="header-item user-col">用户ID</div>
          <div class="header-item name-col">学生姓名</div>
          <div class="header-item score-col">分数</div>
          <div class="header-item time-col">开始时间</div>
          <div class="header-item time-col">结束时间</div>
          <div class="header-item actions-col">操作</div>
        </div>

        <div v-if="isLoading" class="loading-indicator">
          <p>正在加载成绩数据...</p>
        </div>
        <div v-else-if="computedstudents.length === 0" class="no-results">
          <p>暂无学生成绩数据。</p>
        </div>
        <div v-else>
          <div v-for="(student) in computedstudents(examresultswithname)" :key="student.studentId" class="user-row-item">
            <div class="data-cell rank-col">{{ student.rank }}</div>
            <div class="data-cell user-col user-info">
              {{ student.studentId }}
            </div>
            <div class="data-cell name-col">{{ student.studentName }}</div>
            <div class="data-cell score-col">{{ student.score }}</div>
            <div class="data-cell time-col">{{ student.startTime || 'N/A' }}</div>
            <div class="data-cell time-col">{{ student.finishTime || 'N/A' }}</div>
            <div class="data-cell actions-col">


              <button class="btn edit-action-btn" @click="gotoedit(student.studentId)" title="修改/查看学生成绩详情">
                <i class="icon-edit"></i> 修改/查看
              </button>
            </div>
          </div>
          <div v-if="notattended.length > 0" class="no-results">
            <p>以下学生未参加考试：</p>
            <div v-for="(student1) in notattended" :key="student1.studentId" :value="student1.studentId"
              class="user-row-item">
              <div class="data-cell rank-col">{{ student1.studentId }}</div>
              <div class="data-cell user-col user-info">{{ student1.studentName }}</div>
              <div class="data-cell score-col"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const url_front = 'http://localhost:8080/';

const isedit = ref(false);
const isLoading = ref(true); // Added loading state
const allexamresults = ref([]); // Initialize as empty, will be filled from API
const allstudents = ref([]); // Placeholder for all students, if needed
const paperName = ref(''); // Placeholder for paper name, if needed
const paperId=ref(null);
const courseId=ref(null);
onMounted(async () => {
  paperId.value = parseInt(route.params.paperId);
  courseId.value = parseInt(route.params.courseId);
   // Get paper name from route params
  if (route.query.mode && route.query.mode === 'edit') {
    isedit.value = true;
    paperName.value = route.params.paperName || '';
  }
  await fetchAllStudents(courseId.value); // Fetch all students if needed
  await fetchexamresults(paperId.value, courseId.value);
  await fetchPaperName(paperId.value,courseId.value); // Fetch paper name if needed
});
const fetchPaperName= async (paperId, courseId) => {
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId) });
    const url = url_front+`api/paper-questions/query-paper-and-questions?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取试卷信息失败 (${res.status})`);
    const data = await res.json();
    paperName.value = data.paperName || '未知试卷';
  } catch (error) {
    console.error('fetchPaperQuestions error:', error);
    paperInfo.value = { paperName: '加载失败', paperQuestions: [] };
  }
};
const fetchAllStudents = async (courseId) => {
  try {
    const url = url_front + `api/courses/${courseId}/students`;
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }
    const data = await res.json();
    allstudents.value = Array.isArray(data) ? data : []; // Ensure it's an array
    console.log("allstudents:", allstudents.value);
  } catch (error) {
    // alert('加载学生信息失败，请检查网络或服务状态: ' + error.message);
    console.error(error);
    allstudents.value = []; // Set to empty on error
  }
}
const notattended = computed(() => {
  if (!Array.isArray(allstudents.value)) return [];
  const students = allstudents.value;

  const temp = students.filter(student => {
    const studentId = student.studentId;
    return !allexamresults.value.some(examResult => examResult.studentId === studentId);
  }).map(student => ({
    studentId: student.studentId,
    studentName: student.studentName,
    department: student.department
  }));
  console.log("notattended:", temp);
  return temp;
});
const fetchexamresults = async (paperId, courseId) => {
  isLoading.value = true;
  try {
    const params = new URLSearchParams({
      courseId: String(courseId), // Ensure params are strings
      paperId: String(paperId),
    });
    const url = url_front + `api/exam/search-examResult-for-all?${params}`;
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }
    const data = await res.json();
    console.log("examresults:", data);
    allexamresults.value = Array.isArray(data) ? data : []; // Ensure it's an array
  } catch (error) {
    alert('加载学生成绩失败，请检查网络或服务状态: ' + error.message);
    console.error(error);
    allexamresults.value = []; // Set to empty on error
  } finally {
    isLoading.value = false;
  }
};
const examresultswithname = computed(() => {
  if (!Array.isArray(allexamresults.value)) return [];
  const temp2 = allexamresults.value.map(result => {
    const student = allstudents.value.find(s => s.studentId === result.studentId);
    return {
      ...result,
      studentName: student ? student.studentName : '未知学生',
    };
  });
  console.log("examresultswithname:", temp2);
  return temp2;
}
);

const computedstudents = (allexamresults) => {
  if (!Array.isArray(allexamresults)) return []; // Guard against non-array
  const sortedResults = [...allexamresults].sort((a, b) => (b.totalScore || 0) - (a.totalScore || 0));
  const temp3= sortedResults.map((result, index) => ({
    ...result,
    rank: index + 1,
    score: result.totalScore !== undefined ? result.totalScore : 'N/A',
    studentId: result.studentId !== undefined ? result.studentId : 'N/A',
    // Assuming startTime and endTime might come with result, otherwise they'll be N/A from template
    startTime: result.startTime ? formatDate(result.startTime) : null,
    finishTime: result.finishTime ? formatDate(result.finishTime) : null,
  }));
  console.log("computedstudents:", temp3);
  return temp3;
};

// 日期格式化 (如果需要显示在列表中的开始/结束时间)
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
};

const goBack = () => {
  // Preserve original logic, but ensure route.params are defined if used
  if (isedit.value) {
    // This route seems to imply it's coming from an ended exam detail, adjust if needed
    router.push('/teacher/exam-management'); // Fallback or more specific route
  } else if (route.params.courseId && route.params.paperId) {
    router.push(`/teacher/exams_detail/${route.params.courseId}/${route.params.paperId}`);
  } else {
    router.push('/teacher/exam-management'); // Default fallback
  }
};

const gotoedit = (studentId) => {
  if (route.params.courseId && route.params.paperId && studentId !== undefined) {
    router.push(`/teacher/exam-detail/student-exam-detail/${route.params.courseId}/${route.params.paperId}/${studentId}`);
  } else {
    alert("无法获取足够信息以修改学生详情。");
  }
};

//Placeholder for uploadScores function
const uploadScores = async() => {
  try {
    const proportionInput = prompt("请输入该考试成绩的占比（小数，0-1之间）：");
    const proportion = parseFloat(proportionInput);

    if ( proportion < 0 || proportion > 1) {
      alert("请输入有效的比例值（0~1之间）");
      return;
    }

    const studentscoremap=examresultswithname.value.map(item => ({
      studentId: item.studentId,
      score: item.totalScore,
      proportion: proportion,
      name:paperName.value // Assuming paperName is defined in the scope
    }));
    // 正确方式：直接发送数组
    const res = await fetch(`${url_front}api/courses/section/${courseId.value}/paper/${paperId.value}/students/exam`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body:JSON.stringify(
         studentscoremap // [{studentId: 1, score: 85}, ...]
      )
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }
  } catch (error) {
    // alert('加载学生信息失败，请检查网络或服务状态: ' + error.message);
    console.error(error);
  }
};

// async function uploadScores(sectionId, paperId, studentData) {
//   const proportionInput = prompt("请输入该考试成绩的占比（例如0.3）：");
//   const proportion = parseFloat(proportionInput);
//
//   if (isNaN(proportion) || proportion < 0 || proportion > 1) {
//     alert("请输入有效的比例值（0~1之间）");
//     return;
//   }
//
//   const requestBody = studentData.map(student => ({
//     studentId: student.studentId,
//     score: student.score,
//     name: student.name,//卷名
//     proportion: proportion
//   }));
//
//   const res = await fetch(`/api/courses/section/${sectionId}/paper/${paperId}/students/exam`, {
//     method: 'POST',
//     headers: {
//       'Content-Type': 'application/json'
//     },
//     body: JSON.stringify(requestBody)
//   });
//
//   if (res.ok) {
//     alert("成绩上传成功！");
//   } else {
//     alert("成绩上传失败，请重试。");
//   }
// }
</script>

<style scoped>
/* --- 全局与页面布局 --- */
.student-scores-page {
  /* Renamed from .exam-details and .actions */
  padding: 25px 35px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa;
  min-height: 100vh;
  color: #333;
}

.page-actions-bar {
  /* Was .actions */
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.page-actions-bar .btn {
  font-size: 0.95em;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.2em;
  font-weight: 600;
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1em;
  font-weight: 500;
  transition: all 0.2s ease-in-out;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-decoration: none;
  line-height: 1.5;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.08);
}

.btn:hover:not(:disabled) {
  opacity: 0.85;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.08);
}

.btn:disabled {
  background-color: #adb5bd;
  color: #6c757d;
  cursor: not-allowed;
  box-shadow: none;
  opacity: 0.7;
}

.btn i[class^="icon-"] {
  font-size: 1.1em;
}

.primary-btn {
  background-color: #007bff;
  color: white;
}

.primary-btn:hover:not(:disabled) {
  background-color: #0069d9;
}

.secondary-outline-btn {
  background-color: transparent;
  color: #6c757d;
  border: 1px solid #6c757d;
}

.secondary-outline-btn:hover:not(:disabled) {
  background-color: #6c757d;
  color: white;
}

.edit-action-btn {
  /* For modify button in row */
  background-color: #ffc107;
  /* Yellow/Orange for edit */
  color: #212529;
  padding: 6px 12px;
  /* Smaller button in table row */
  font-size: 0.85em;
}

.edit-action-btn:hover:not(:disabled) {
  background-color: #e0a800;
}


/* --- 排名列表 --- */
.ranking-list-container {
  background: white;
  padding: 25px 30px;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.07);
}

.ranking-list {
  width: 100%;
  border-collapse: collapse;
  /* Behaves more like a table */
}

.list-header,
.user-row-item {
  display: flex;
  align-items: center;
  /* Vertically center content in rows/header */
  padding: 12px 15px;
  /* Padding for rows/header */
  border-bottom: 1px solid #e9ecef;
  /* Separator line */
}

.list-header {
  font-weight: 600;
  /* Bold header text */
  color: #495057;
  background-color: #f8f9fa;
  /* Light background for header */
  border-top-left-radius: 8px;
  /* Rounded corners for header */
  border-top-right-radius: 8px;
  font-size: 0.95em;
}

.user-row-item:last-child {
  border-bottom: none;
  /* No border for the last item */
}

.user-row-item:nth-child(even) {
  /* Zebra striping for rows */
  background-color: #fdfdff;
}

.user-row-item:hover {
  background-color: #f0f8ff;
  /* Light blue hover for rows */
}


/* Column styling */
.header-item,
.data-cell {
  display: flex;
  flex-direction: row;
  padding: 8px 10px;
  /* Cell padding */
  text-align: left;
  flex-shrink: 0;
  /* Prevent shrinking by default */
}

.rank-col {
  flex-basis: 8%;
  min-width: 60px;
  text-align: center;
}

.user-col {
  flex-basis: 22%;
  min-width: 60px;
  max-width: 120px;
}

.score-col {
  flex-basis: 10%;
  min-width: 80px;
  text-align: center;
  font-weight: 500;
}
.name-col {
  flex-basis: 5%;
  width: 80px;
  min-width: 120px;

}

.time-col {
  flex-basis: 20%;
  min-width: 150px;
  font-size: 0.9em;
  color: #555;
}

.actions-col {
  flex-basis: 15%;
  min-width: 100px;
  text-align: center;
}


.user-info {
  display: flex;
  align-items: center;
}

/* .current-user-badge styling would go here if used */

/* --- 空状态与加载状态 --- */
.loading-indicator,
.no-results {
  text-align: center;
  padding: 40px 20px;
  color: rgba(248, 0, 0, 0.68);
  background-color: rgba(131, 234, 234, 0.27);
  border-radius: 16px;
  font-size: 1em;
  margin: 20px 0;
  border: 2px dashed #d0d9e0;
}

.loading-indicator p,
.no-results p {
  margin: 0;
}


/* --- 响应式调整 --- */
@media (max-width: 992px) {
  .time-col {
    display: none;
  }

  /* Hide time columns on medium screens */
  .user-col {
    flex-basis: 30%;
  }

  .actions-col {
    flex-basis: 20%;
  }
}

@media (max-width: 768px) {
  .student-scores-page {
    padding: 20px 15px;
  }

  .page-actions-bar {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }

  .page-actions-bar .btn {
    width: 100%;
  }

  h1 {
    font-size: 1.8em;
  }

  .ranking-list-container {
    padding: 15px;
  }

  .list-header {
    display: none;
  }

  /* Hide header on small screens, rows become cards */
  .user-row-item {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    /* Align items to start in column */
    padding: 15px;
    margin-bottom: 10px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }

  .data-cell {
    padding: 5px 0;
    width: 100%;
    text-align: left !important;
    /* Override centered text for some columns */
    display: flex;
    /* For label-value pairs */
    flex-basis: auto !important;
    /* Reset flex basis */
    min-width: unset !important;
    /* Reset min width */
  }

  .data-cell::before {
    /* Add labels for data cells */
    content: attr(data-label);
    font-weight: 600;
    width: 90px;
    /* Fixed width for labels */
    flex-shrink: 0;
    margin-right: 10px;
    color: #495057;
  }

  .rank-col::before {
    content: "排名：";
  }

  .user-col::before {
    content: "用户ID：";
  }

  .score-col::before {
    content: "分数：";
  }

  /* Time columns are hidden, but if shown, would need labels too */
  /* .time-col::before { content: "时间："; } */
  .actions-col {
    padding-top: 10px;
    border-top: 1px solid #eee;
    margin-top: 10px;
  }

  .actions-col::before {
    display: none;
  }

  /* No label for actions column */
  .actions-col .btn {
    width: 100%;
  }
}

/* Placeholder Icons */
.icon-back-arrow::before {
  content: "←";
  margin-right: 6px;
  font-weight: bold;
}

.icon-upload::before {
  content: "↑";
  margin-right: 6px;
}

.icon-edit::before {
  content: "✏️";
  margin-right: 6px;
}
</style>