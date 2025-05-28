<template>
  <div class="paperInfo-management">
    <h1>考试情况管理</h1>
    <div class="filter-bar">
      <label>班级ID：</label>
      <select v-model="filterCourseId">
        <option value="">全部</option>
        <option v-for="id in uniqueCourseIds" :key="id" :value="id">{{ id }}</option>
      </select>
      <label>创建者：</label>
      <select v-model="filterCreator">
        <option value="">全部</option>
        <option v-for="c in uniqueCreators" :key="c" :value="c">{{ c }}</option>
      </select>
    </div>

    <div class="paperInfos-list">
      <div
          v-for="paperInfo in filteredEndedpaperInfos"
          :key="paperInfo.paperId"
          class="paperInfo-card ended-paperInfo"
      >
        <h3>考试名称:{{ paperInfo.paperName }}</h3>
        <p>考试时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>
        <div class="paperInfo-actions">
          <button class="delete-button" @click="searchforinfo(paperInfo.paperId,paperInfo.courseId)">查看考试详情</button>
          <button class="button" @click="gotostudentinfo(paperInfo)">查看学生成绩</button>
        </div>
      </div>
    </div>

    <div v-if="nopaperInfos" class="no-results">
      没有找到相关考试
    </div>
    <!-- 编辑考试设置模态框 -->

  </div>
</template>

<script setup>
import { ref, computed, watch,onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
onMounted(() => {
  // console.log("cicjhu")
  fetchPaperInfos();
})

const constId = ref(3)
const creator = ref("")
const filterCourseId = ref('')
const filterCreator = ref('')

const fetchPaperInfos = async() => {
  try {
    var params = new URLSearchParams({
      courseId: constId.value,
      creator: creator.value
    });
    if(creator.value===""){
      params=new URLSearchParams({
      courseId: constId.value,
    });
    }

    console.log('请求参数:', params.toString())
    const url = `http://localhost:8080/api/paper-questions/query-papers-for-all?${params}`;
    const res=await fetch(url,{
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    if (!res.ok) {
      console.error('网络错误:', res.status, res.statusText)
      throw new Error('网络错误')
    }
    var data =await res.json()
    paperInfos.value = data
    console.log(paperInfos.value)
  } catch (error) {
    alert('加载考试状态失败，请检查网络或服务状态')
    console.error(error)
  }
}

const paperInfos = ref([])
const currentTab = ref('ended')

// 获取唯一的班级ID和创建者列表
const uniqueCourseIds = computed(() => {
  return [...new Set(paperInfos.value.map(item => item.courseId))]
})

const uniqueCreators = computed(() => {
  return [...new Set(paperInfos.value.map(item => item.creator))]
})

// 考试数据过滤函数
const filterPaperInfos = (paperInfosList) => {
  return paperInfosList.filter(paperInfo => {
    const courseMatch = !filterCourseId.value || paperInfo.courseId == filterCourseId.value
    const creatorMatch = !filterCreator.value || paperInfo.creator === filterCreator.value
    return courseMatch && creatorMatch
  })
}


const endedpaperInfos = computed(() => {
  const now = new Date()
  return paperInfos.value.filter(e =>
      new Date(e.closeTime) <= now
  ).map(paperInfo => {
    return {
      ...paperInfo
    }
  })
})

// 应用过滤后的考试列表
const filteredEndedpaperInfos = computed(() => filterPaperInfos(endedpaperInfos.value))

const nopaperInfos = computed(() => {
  return (
      (currentTab.value === 'ended' && filteredEndedpaperInfos.value.length === 0)
  )
})


const searchforinfo=(paperId,courseId)=>{
  router.push(`/teacher/exams_detail/${courseId}/${paperId}`)
}
const gotostudentinfo=(paperInfo)=>{
  router.push('/teacher/exam-details-student-score/' + paperInfo.courseId + '/' + paperInfo.paperId+'?mode=edit')
}
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
h1 {
  color: #000000;
}
.paperInfo-management {
  padding: 20px;
  background-color: #f0f4ff;
  color:black;
}

.filter-bar {
  margin: 15px 0;
  padding: 10px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.filter-bar label {
  margin-right: 10px;
}

.filter-bar select {
  margin-right: 20px;
  padding: 5px 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.edit-btn {
  background-color: #2196F3;
  color: white;
}

.edit-btn:hover {
  background-color: #0b7dda;
}

.delete-btn {
  background-color: #f44336;
  color: white;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.paperInfo-card {
  margin: 10px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.card-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}


.form-group {
  margin-bottom: 15px;
}

.datetime-pickers {
  display: flex;
  align-items: center;
  gap: 10px;
}


.save-btn {
  background-color: #4CAF50;
  color: white;
}

.cancel-btn {
  background-color: #9E9E9E;
  color: white;
}

.no-results {
  text-align: center;
  padding: 20px;
  color: #666;
}
</style>