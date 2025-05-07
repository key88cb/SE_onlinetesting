<template>
  <div class="test">
    <h1>在线测试</h1>
    <div class="test-container">
      <div class="question">
        <h2>问题 1</h2>
        <p>这是一个示例问题</p>
        <div class="options">
          <label>
            <input type="radio" name="q1" value="A"> 选项 A
          </label>
          <label>
            <input type="radio" name="q1" value="B"> 选项 B
          </label>
          <label>
            <input type="radio" name="q1" value="C"> 选项 C
          </label>
        </div>
      </div>
      <div class="navigation">
        <button class="btn">上一题</button>
        <button class="btn">下一题</button>
        <button class="btn submit">提交</button>
      </div>
    </div>
  </div>
</template>

<script setup>
// 组件逻辑
</script>

<style scoped>
.test {
  padding: 20px;
}

.test-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.question {
  margin-bottom: 30px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.options label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

.btn {
  padding: 10px 20px;
  background-color: #0d47a1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn:hover {
  background-color: #1565c0;
}

.submit {
  background-color: #2e7d32;
}

.submit:hover {
  background-color: #388e3c;
}
</style> 