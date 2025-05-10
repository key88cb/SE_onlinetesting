<template>
  <div class="create-paper-container">
    <h2>请选择出卷方式</h2>
    <div class="option-blocks">
      <!-- 左侧：手动出卷 -->
      <div class="option-block" @click="goToManualPaper">
        <h3>手动出卷</h3>
        <p>自主选择题目，灵活组卷</p>
      </div>

      <!-- 右侧：自动出卷 -->
      <div class="option-block" @click="goToAutoPaper">
        <h3>自动出卷</h3>
        <p>系统智能推荐题目，快速生成试卷</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    goToManualPaper() {
      this.$router.push('/teacher/create-paper/manual');
    },
    goToAutoPaper() {
      this.$router.push('/teacher/create-paper/auto');
    }
  }
};
</script>

<style scoped>
h1 {
  color: #000000;
}
.create-paper-container {
  background-color: #f5f9ff;
  min-height: 100vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center; /* 内容垂直居中 */
}

h2 {
  color: #0d47a1;
  margin-bottom: 30px;
  text-align: center;
}

.option-blocks {
  display: flex;
  flex-direction: row;
  gap: 20px;
  justify-content: center; /* 水平居中 */
  width: 100%;
  max-width: 800px;
}

.option-block {
  background-color: white;
  border-left: 6px solid #0d47a1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 25px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 250px; /* 固定高度，更小巧 */
  width: 350px; /* 固定宽度，适中大小 */
  border-radius: 10px;
}

.option-block:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background-color: #f9f9f9;
}

.option-block h3 {
  margin: 0 0 10px;
  font-size: 1.4em;
  color: #0d47a1;
}

.option-block p {
  margin: 0;
  font-size: 1em;
  color: #555;
}
</style>
