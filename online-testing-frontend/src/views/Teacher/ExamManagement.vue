<template>
  <div class="paperInfo-management">
    <h1>考试情况管理</h1>

    <div class="tabs">
      <button
          :class="['tab-btn', { active: currentTab === 'notStarted' }]"
          @click="setActiveTab('notStarted')"
      >
        未开始考试
      </button>
      <button
          :class="['tab-btn', { active: currentTab === 'ongoing' }]"
          @click="setActiveTab('ongoing')"
      >
        进行中考试
      </button>
      <button
          :class="['tab-btn', { active: currentTab === 'ended' }]"
          @click="setActiveTab('ended')"
      >
        已结束考试
      </button>
    </div>

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

    <div class="paperInfos-list" v-if="currentTab === 'notStarted'">
      <div
          v-for="paperInfo in filteredNotStartedpaperInfos"
          :key="paperInfo.paperId"
          class="paperInfo-card"
      >
        <h3>考试名称:{{ paperInfo.paperName }}</h3>
        <p>计划时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>

        <div class="card-actions">
          <button class="btn edit-btn" @click.stop="gotoedit(paperInfo)">修改考试设置</button>
          <button class="btn delete-btn" @click.stop="deletepaperInfo(paperInfo)">删除考试</button>
        </div>
      </div>
    </div>

    <div class="paperInfos-list" v-if="currentTab === 'ongoing'">
      <div
          v-for="paperInfo in filteredOngoingpaperInfos"
          :key="paperInfo.paperId"
          class="paperInfo-card"
      >
        <h3>考试名称:{{ paperInfo.paperName }}</h3>
        <p>剩余时间：{{ formatTime(paperInfo.remainingTime) }}</p>
      </div>
    </div>

    <div class="paperInfos-list" v-if="currentTab === 'ended'">
      <div
          v-for="paperInfo in filteredEndedpaperInfos"
          :key="paperInfo.paperId"
          class="paperInfo-card ended-paperInfo"
      >
        <h3>考试名称:{{ paperInfo.paperName }}</h3>
        <p>考试时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>
        <div class="paperInfo-actions">
          <button class="delete-button" @click="searchforinfo(paperInfo.paperId,paperInfo.courseId)">查看考试详情</button>
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
  fetchPaperInfos();
})

const constId = ref(1)
const creator = ref("")
const showSettingsModal = ref(false)
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
const currentPaperInfo = ref(null)
const selectedpaperInfo = ref(null)
const currentTab = ref('notStarted')

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

// 计算不同状态的考试（已过滤）
const notStartedpaperInfos = computed(() => {
  const now = new Date()
  return paperInfos.value.filter(e => new Date(e.openTime) > now)
})

const ongoingpaperInfos = computed(() => {
  const now = new Date()
  return paperInfos.value.filter(e =>
      new Date(e.openTime) <= now && new Date(e.closeTime) > now
  ).map(paperInfo => ({
    ...paperInfo,
    remainingTime: Math.floor((new Date(paperInfo.closeTime) - now) / 1000),
  }))
})

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
const filteredNotStartedpaperInfos = computed(() => filterPaperInfos(notStartedpaperInfos.value))
const filteredOngoingpaperInfos = computed(() => filterPaperInfos(ongoingpaperInfos.value))
const filteredEndedpaperInfos = computed(() => filterPaperInfos(endedpaperInfos.value))

const nopaperInfos = computed(() => {
  return (
      (currentTab.value === 'notStarted' && filteredNotStartedpaperInfos.value.length === 0) ||
      (currentTab.value === 'ongoing' && filteredOngoingpaperInfos.value.length === 0) ||
      (currentTab.value === 'ended' && filteredEndedpaperInfos.value.length === 0)
  )
})

const gotoedit = (paperInfo) => {
  router.push(`/teacher/create-paper/manual-edit/${paperInfo.courseId}/${paperInfo.paperId}?mode=edit`)
}

const searchforinfo=(paperId,courseId)=>{
  router.push(`/teacher/exams_detail/${courseId}/${paperId}`)
}


const deletepaperInfo = async (paperInfo) => {
  try{
    const res=await fetch('http://localhost:8080/api/paper-questions/delete-paper',{
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        paperId: paperInfo.paperId,
        courseId: paperInfo.courseId,
      })
    })
    fetchPaperInfos();
    if (!res.ok) {
      throw new Error('网络错误')
    }
  } catch (error) {
    console.error('删除考试失败:', error)
    alert('删除考试失败，请稍后再试')
  }
}

const setActiveTab = (tab) => {
  currentTab.value = tab
  selectedpaperInfo.value = null
  showSettingsModal.value = false
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
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