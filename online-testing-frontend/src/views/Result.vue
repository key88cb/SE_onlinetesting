<template>
  <div class="result">
    <h1>测试结果</h1>
    <div class="result-container">
      <div class="score-card">
        <h2>得分</h2>
        <div class="score">85</div>
        <div class="score-details">
          <p>总题数：10</p>
          <p>正确：8</p>
          <p>错误：2</p>
        </div>
      </div>
      <div class="answer-review">
        <h2>答题回顾</h2>
        <div class="question-review">
          <div class="question-item">
            <h3>问题 1</h3>
            <p>你的答案：A</p>
            <p>正确答案：A</p>
            <p class="correct">✓ 正确</p>
          </div>
          <div class="question-item">
            <h3>问题 2</h3>
            <p>你的答案：B</p>
            <p>正确答案：C</p>
            <p class="wrong">✗ 错误</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 组件逻辑
</script>

<style scoped>
.result {
  padding: 20px;
}

.result-container {
  max-width: 800px;
  margin: 0 auto;
}

.score-card {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  margin-bottom: 30px;
}

.score {
  font-size: 48px;
  font-weight: bold;
  color: #0d47a1;
  margin: 20px 0;
}

.score-details {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.answer-review {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.question-review {
  margin-top: 20px;
}

.question-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.question-item:last-child {
  border-bottom: none;
}

.correct {
  color: #2e7d32;
  font-weight: bold;
}

.wrong {
  color: #c62828;
  font-weight: bold;
}
</style> 