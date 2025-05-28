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
          <button class="btn time-btn" @click.stop="openTimeEditModal(paperInfo)">修改考试时间</button>
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
    <div v-if="showTimeModal" class="modal-overlay">
      <div class="modal-content">
        <h3>修改考试时间</h3>
        <div class="datetime-pickers">
          <input type="datetime-local" v-model="selectedPaperForTime.openTime" />
          <span>至</span>
          <input type="datetime-local" v-model="selectedPaperForTime.closeTime" />
        </div>


        <div class="modal-buttons">
          <button @click="showTimeModal = false">取消</button>
          <button @click="submitUpdateTime">确认</button>
        </div>
      </div>

    </div>

    <!-- 提示模态框 -->
    <div v-if="showAlertModal" class="modal-overlay">
      <div class="modal-content alert-modal">
        <p>{{ alertMessage }}</p>
        <button @click="showAlertModal = false">确定</button>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, watch,onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
onMounted(() => {
  fetchPaperInfos();
})

const creator = ref("")
const showSettingsModal = ref(false)
const filterCourseId = ref('')
const filterCreator = ref('')
const showTimeModal = ref(false)
const showAlertModal = ref(false)
const alertMessage = ref('')
const selectedPaperForTime = ref(null)
const paperInfos = ref([]) // 存储全量数据

// watch([filterCourseId, filterCreator], async () => {
//   await fetchPaperInfos(); // 当筛选条件变化时重新拉取数据
// });
watch([filterCourseId, filterCreator], () => {
  // 不需要重新请求，只需重新计算 computed 即可
})

const uniqueCourseIds = computed(() => {
  return [...new Set(paperInfos.value.map(i => i.courseId).filter(Boolean))]
})

const uniqueCreators = computed(() => {
  return [...new Set(paperInfos.value.map(i => i.creator).filter(Boolean))]
})

const openTimeEditModal = (paperInfo) => {
  selectedPaperForTime.value = { ...paperInfo }
  showTimeModal.value = true
}

const submitUpdateTime = async () => {
  const paper = selectedPaperForTime.value

  // 校验时间顺序
  const openTime = new Date(paper.openTime)
  const closeTime = new Date(paper.closeTime)

  if (openTime >= closeTime) {
    alertMessage.value = '开始时间不能晚于结束时间'
    showAlertModal.value = true
    return
  }

  try {
    const res = await fetch('http://localhost:8080/api/paper-questions/update-paper-time', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        paperId: paper.paperId,
        courseId: paper.courseId,
        openTime: paper.openTime,
        closeTime: paper.closeTime,
        paperName: paper.paperName
      })
    })

    if (!res.ok) {
      throw new Error('网络错误')
    }

    alertMessage.value = '试卷时间修改成功'
    showAlertModal.value = true
    showTimeModal.value = false
    await fetchPaperInfos() // 刷新数据
  } catch (error) {
    alertMessage.value = '更新失败，请重试'
    showAlertModal.value = true
    console.error(error)
  }
}

const fetchPaperInfos = async () => {
  try {
    const url = 'http://localhost:8080/api/paper-questions/query-all-papers'
    const res = await fetch(url, { method: 'GET' })

    if (!res.ok) throw new Error('网络错误')

    const data = await res.json()
    paperInfos.value = data // 保存全量数据
    console.log('成功拉取全部考试数据:', data)
  } catch (error) {
    alert('加载考试失败，请检查网络或服务状态')
    console.error(error)
  }
}


const currentPaperInfo = ref(null)
const selectedpaperInfo = ref(null)
const currentTab = ref('notStarted')


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
  )
})

// 考试数据过滤函数
const filterPaperInfos = (list) => {
  return list.filter(paperInfo => {
    const courseMatch = !filterCourseId.value || paperInfo.courseId == filterCourseId.value
    const creatorMatch = !filterCreator.value || paperInfo.creator === filterCreator.value
    return courseMatch && creatorMatch
  })
}

// 各状态+筛选后的列表
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  position: relative;
}

.datetime-pickers {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 20px;
}

.datetime-pickers input {
  padding: 8px;
  font-size: 1em;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-buttons button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-buttons button:first-child {
  background-color: #ccc;
  color: #333;
}

.modal-buttons button:last-child {
  background-color: #2e7d32;
  color: white;
}

.alert-modal {
  text-align: center;
}

.alert-modal p {
  margin-bottom: 20px;
  font-size: 16px;
}

.alert-modal button {
  padding: 8px 20px;
  background-color: #2e7d32;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

</style>