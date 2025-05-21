<template>
  <div class="analysis-container">
    <h1>成绩分析</h1>
    
    <div class="overview-cards">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="overview-card">
            <template #header>
              <div class="card-header">
                <span>平均分</span>
              </div>
            </template>
            <div class="card-content">
              <span class="score">{{ averageScore }}</span>
              <span class="unit">分</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="overview-card">
            <template #header>
              <div class="card-header">
                <span>最高分</span>
              </div>
            </template>
            <div class="card-content">
              <span class="score">{{ highestScore }}</span>
              <span class="unit">分</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="overview-card">
            <template #header>
              <div class="card-header">
                <span>考试次数</span>
              </div>
            </template>
            <div class="card-content">
              <span class="score">{{ examCount }}</span>
              <span class="unit">次</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>成绩趋势</span>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里需要集成图表库，如 ECharts -->
              <div class="chart-placeholder">成绩趋势图表</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>成绩分布</span>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里需要集成图表库，如 ECharts -->
              <div class="chart-placeholder">成绩分布图表</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="detail-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>详细成绩</span>
          </div>
        </template>
        <el-table :data="scoreDetails" style="width: 100%" border>
          <el-table-column prop="examName" label="考试名称" />
          <el-table-column prop="examTime" label="考试时间" width="180" />
          <el-table-column prop="score" label="得分" width="100">
            <template #default="scope">
              <span :class="{ 'high-score': scope.row.score >= 90, 'low-score': scope.row.score < 60 }">
                {{ scope.row.score }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="rank" label="排名" width="100" />
          <el-table-column prop="averageScore" label="平均分" width="100" />
          <el-table-column prop="highestScore" label="最高分" width="100" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟数据
const averageScore = ref(85.5)
const highestScore = ref(98)
const examCount = ref(10)

const scoreDetails = ref([
  {
    examName: '2024年春季学期期中考试',
    examTime: '2024-03-20',
    score: 85,
    rank: 15,
    averageScore: 82.5,
    highestScore: 98
  },
  {
    examName: 'Java程序设计基础测试',
    examTime: '2024-03-15',
    score: 92,
    rank: 5,
    averageScore: 85.0,
    highestScore: 95
  }
])
</script>

<style scoped>
.analysis-container {
  padding: 20px;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  padding: 20px 0;
}

.score {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}

.unit {
  font-size: 16px;
  color: #909399;
  margin-left: 5px;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-container {
  height: 320px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-placeholder {
  color: #909399;
  font-size: 16px;
}

.high-score {
  color: #67c23a;
  font-weight: bold;
}

.low-score {
  color: #f56c6c;
  font-weight: bold;
}

.detail-section {
  margin-top: 20px;
}
</style> 