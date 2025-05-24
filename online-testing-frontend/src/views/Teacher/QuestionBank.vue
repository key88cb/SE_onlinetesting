<!--已基本实现完成的题库管理 三件套 HTML-->
<!--网页的骨架就不说了把-->
<template>
  <div class="question-bank-page">
    <header class="page-header">
      <h1>题库管理</h1>
    </header>
<!--控制部分 页面上方的控制条-->
    <section class="controls-section">
<!--搜索条-->
      <div class="search-filter-group">
        <input
            v-model="searchQuery"
            placeholder="按题目内容、科目或标签搜索..."
            class="control-input search-input"/>
<!--选择条-->
        <select v-model="selectedSubjectFilter" class="control-input subject-select">
          <option value="">全部分类</option>
          <option v-for="subject in uniqueSubjects" :key="subject" :value="subject">{{ subject }}</option>
        </select>
      </div>
      <button class="btn primary-btn add-question-btn" @click="showAddQuestionDialog">
        <i class="icon-add"></i> 添加新题目
      </button>
    </section>
<!--题目内容-->
    <section class="content-section">
      <div v-if="isLoading" class="loading-indicator">
        <p>题目加载中...</p>
      </div>
      <div v-else-if="!filteredQuestions.length" class="no-results-indicator">
        <p>未找到符合条件的题目，或题库当前为空。</p>
      </div>
<!--      题目卡-->
      <div v-else class="questions-grid">
        <div
            v-for="question in filteredQuestions"
            :key="question.id"
            class="question-card"
            @click="viewQuestionDetails(question)"
            tabindex="0"
            role="button"
            :aria-label="`查看题目详情: ${question.text}`"
        >
          <div class="card-header">
            <span class="type-badge" :class="`type-${question.type.toLowerCase()}`">{{ question.type }}</span>
            <div class="card-meta">
              <span>科目: {{ question.subject }}</span>
              <span v-if="question.tag">标签: {{ question.tag }}</span>
            </div>
          </div>
          <h3 class="card-title">{{ question.text }}</h3>
          <div class="card-options">
            <div
                v-for="(option) in question.options"
                :key="option.value"
                :class="['option-item', { 'correct-answer': option.isCorrect }]"
            >
              <span class="option-letter">{{ option.value }}.</span> {{ option.label }}
            </div>
          </div>
          <div class="card-actions">
            <button class="btn icon-btn edit-action-btn" @click.stop="editQuestion(question)" title="编辑题目">
              <i class="icon-edit"></i> 编辑
            </button>
            <button class="btn icon-btn delete-action-btn" @click.stop="deleteQuestion(question)" title="删除题目">
              <i class="icon-delete"></i> 删除
            </button>
          </div>
        </div>
      </div>
    </section>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-dialog" role="dialog" aria-labelledby="modalTitle" aria-modal="true">
        <header class="modal-header">
          <h2 id="modalTitle" class="modal-title">{{ isEditing ? '编辑题目' : '添加新题目' }}</h2>
          <button class="modal-close-btn" @click="closeModal" aria-label="关闭模态框">&times;</button>
        </header>
        <div class="modal-body">
          <form @submit.prevent="saveQuestion">
            <div class="form-row">
              <div class="form-group half-width">
                <label for="q-type">题目类型：</label>
                <select v-model="currentQuestion.type" @change="handleQuestionTypeChange" id="q-type" class="form-control">
                  <option value="单选">单选</option>
                  <option value="多选">多选</option>
                  <option value="判断">判断</option>
                </select>
              </div>
              <div class="form-group half-width">
                <label for="q-subject">所属科目：</label>
                <input type="text" v-model="currentQuestion.subject" placeholder="例如：计算机网络" id="q-subject" class="form-control" required />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group half-width">
                <label for="q-tags">标签 (逗号分隔)：</label>
                <input type="text" v-model="currentQuestion.tag" placeholder="例如：HTTP, TCP/IP" id="q-tags" class="form-control" />
              </div>
              <div class="form-group half-width">
                <label for="q-creator">创建者：</label>
                <input type="text" v-model="currentQuestion.creator" placeholder="您的名称或代号" id="q-creator" class="form-control" required />
              </div>
            </div>

            <div class="form-group">
              <label for="q-text">题目描述：</label>
              <textarea v-model="currentQuestion.text" rows="4" placeholder="在此输入完整的题目内容..." id="q-text" class="form-control" required></textarea>
            </div>

            <fieldset class="options-fieldset">
              <legend>选项与答案设置</legend>
              <div v-if="isJudgmentQuestion" class="judgment-options-group">
                <div v-for="option in currentQuestion.options" :key="option.value" class="option-input-group judgment">
                  <input
                      type="radio"
                      :id="'option-judge-' + option.value"
                      :value="option.value"
                      v-model="judgmentCorrectAnswerValue"
                      name="judgmentCorrectAnswerGroup"
                      class="form-radio"
                  />
                  <label :for="'option-judge-' + option.value" class="option-input-label">{{ option.label }}</label>
                </div>
                <p class="form-hint">判断题：请选择一个作为正确答案。</p>
              </div>

              <div v-else class="standard-options-group">
                <div v-for="(option, index) in currentQuestion.options" :key="index" class="option-input-group">
                  <span class="option-prefix">{{ option.value }}.</span>
                  <input type="text" v-model="option.label" :placeholder="`选项 ${option.value} 内容`" class="form-control option-text-input" required />
                  <div class="correct-marker">
                    <input
                        :type="isSingleChoice ? 'radio' : 'checkbox'"
                        :id="'correct-opt-' + option.value"
                        :name="isSingleChoice ? 'single-choice-correct-answer' : 'multi-choice-correct-answer-' + option.value"
                        :value="option.value"
                        v-model="option.isCorrect"
                        @change="isSingleChoice && setSingleCorrectOption(option.value)"
                        class="form-check-input"
                    />
                    <label :for="'correct-opt-' + option.value">正确</label>
                  </div>
                </div>
                <div class="option-management-buttons">
                  <button v-if="!isJudgmentQuestion && currentQuestion.options.length < 4" @click="addOption" type="button" class="btn secondary-btn add-option-btn">
                    <i class="icon-add-circle"></i> 添加选项
                  </button>
                  <button v-if="!isJudgmentQuestion && currentQuestion.options.length > 2" @click="removeLastOption" type="button" class="btn danger-outline-btn remove-option-btn">
                    <i class="icon-remove-circle"></i> 删除末尾选项
                  </button>
                </div>
                <p v-if="isSingleChoice" class="form-hint">单选题：请选择一个正确答案。</p>
                <p v-if="isMultipleChoice" class="form-hint">多选题：可勾选多个正确答案。</p>
              </div>
            </fieldset>

            <footer class="modal-footer">
              <button class="btn secondary-btn" type="button" @click="closeModal">取消</button>
              <button class="btn primary-btn" type="submit">
                <i class="icon-save"></i> {{ isEditing ? '更新题目' : '保存题目' }}
              </button>
            </footer>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
<!--行为-->
<script setup>
import { ref, computed, onMounted, watch } from 'vue';
// --- 类型映射 --- 因为不太想要数据库的英文 就把中英文名字绑定在这里
const QUESTION_TYPES_MAP = {
  SINGLE_CHOICE: { backend: 'Single Choice', frontend: '单选' },
  MULTIPLE_CHOICE: { backend: 'Multiple Choice', frontend: '多选' },
  TRUE_FALSE: { backend: 'True/False', frontend: '判断' }
};
// 下面两个函数是从前后端识别内容
const getFrontendType = (backendType) => {
  for (const key in QUESTION_TYPES_MAP) {
    if (QUESTION_TYPES_MAP[key].backend === backendType) {
      return QUESTION_TYPES_MAP[key].frontend;
    }
  }
  return backendType;
};
const getBackendType = (frontendType) => {
  for (const key in QUESTION_TYPES_MAP) {
    if (QUESTION_TYPES_MAP[key].frontend === frontendType) {
      return QUESTION_TYPES_MAP[key].backend;
    }
  }
  return frontendType;
};

// --- 响应式状态 ---
const questions = ref([]);
const isLoading = ref(false);
const showModal = ref(false);
const isEditing = ref(false);
const judgmentCorrectAnswerValue = ref(null);
const searchQuery = ref('');
const selectedSubjectFilter = ref('');
// 下面这句是打印的调试语句 如果加了可以进入前端页面的开发者模式 （F12） 切换到控制台 刷新后可以看到输出
// console.log("Vue Component: Initializing state refs.");

// --- 默认选项生成函数 ---
const defaultSingleMultiOptions = (count = 4) => {
  const opts = [];
  for (let i = 0; i < count; i++) {
    opts.push({ value: String.fromCharCode(65 + i), label: '', isCorrect: false });
  }
  return opts;
};

const defaultJudgmentOptionsFE = () => [
  { value: 'A', label: '正确', isCorrect: false }, // 在模板中可以显示 "正确 (True)"
  { value: 'B', label: '错误', isCorrect: false }  // 在模板中可以显示 "错误 (False)"
];

// --- 当前题目对象 (前端内部状态) --- 对于这些关键的数据 需要和dto稍微匹配一下
const currentQuestion = ref({
  id: null,
  questionId: null,
  type: QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend,
  subject: '',
  text: '',
  tag: '',
  creator: '出题老师',
  options: defaultSingleMultiOptions(),
  subjectCategory: '',
  tags: '',
  questionText: '',
  questionType: QUESTION_TYPES_MAP.SINGLE_CHOICE.backend,
  correctAnswer: ''
});
// --- 工具函数：选项字母与后端 OptionDto.optionIdValue (数字) 转换 --- ABCD 的解析 1234数字的解析
const optionLetterToIdValue = (letter) => {
  if (!letter || typeof letter !== 'string' || letter.length !== 1) return null;
  const val = letter.charCodeAt(0) - 64;
  return val > 0 ? val : null;
};
const optionIdValueToLetter = (idValue) => {
  if (idValue === null || typeof idValue !== 'number' || idValue < 1) return '';
  return String.fromCharCode(64 + idValue);
};

// --- 数据转换函数 --- 前端的内容转成后端可用
const convertFrontendToBackendDto = (frontendQuestion) => {
  console.log("convertFrontendToBackendDto: Input (frontendQuestion)", JSON.parse(JSON.stringify(frontendQuestion)));
  const backendDto = {
    questionId: frontendQuestion.questionId,
    subjectCategory: frontendQuestion.subject,
    tags: frontendQuestion.tag,
    questionText: frontendQuestion.text,
    questionType: getBackendType(frontendQuestion.type), // 中文转英文
    creator: frontendQuestion.creator,
    correctAnswer: '',
    options: []
  };

  let correctAnswersLetters = [];
  if (frontendQuestion.options && frontendQuestion.options.length > 0) {
    frontendQuestion.options.forEach((opt) => {
      const optionIdVal = optionLetterToIdValue(opt.value);
      if (optionIdVal !== null) {
        backendDto.options.push({
          optionIdValue: optionIdVal,
          optionText: opt.label
        });
        if (opt.isCorrect) {
          correctAnswersLetters.push(opt.value);
        }
      }
    });
  }

  // 根据题目类型格式化 correctAnswer 字符串
  if (frontendQuestion.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) { // 判断 (中文)
    backendDto.correctAnswer = correctAnswersLetters.length > 0 ? correctAnswersLetters[0] : ''; // "A" 或 "B"
  } else if (frontendQuestion.type === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.frontend) { // 多选 (中文)
    // 多选题答案格式为 "ABC"，需要排序后连接
    backendDto.correctAnswer = correctAnswersLetters.sort().join(''); // 例如 "ABC"
  } else { // 单选
    backendDto.correctAnswer = correctAnswersLetters.length > 0 ? correctAnswersLetters[0] : ''; // 例如 "B"
  }
  console.log("convertFrontendToBackendDto: Output (backendDto)", JSON.parse(JSON.stringify(backendDto)));
  return backendDto;
};

const convertBackendToFrontendItem = (backendDto) => {
  console.log("convertBackendToFrontendItem: Input (backendDto)", JSON.parse(JSON.stringify(backendDto)));
  const frontendKey = backendDto.questionId || `temp-${Date.now()}-${Math.random()}`;
  const frontendTypeDisplay = getFrontendType(backendDto.questionType); // 英文转中文

  const frontendItem = {
    id: frontendKey,
    questionId: backendDto.questionId,
    type: frontendTypeDisplay, // 前端使用的中文类型
    subject: backendDto.subjectCategory,
    text: backendDto.questionText,
    tag: backendDto.tags,
    creator: backendDto.creator,
    correctAnswer: backendDto.correctAnswer, // 保存后端原始的 correctAnswer
    options: [],
    subjectCategory: backendDto.subjectCategory,
    tags: backendDto.tags,
    questionText: backendDto.questionText,
    questionTypeOriginal: backendDto.questionType, // 保存原始后端英文类型
  };

  if (frontendItem.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) { // 判断 (中文)
    const defaultOpts = defaultJudgmentOptionsFE().map(opt => ({...opt}));
    defaultOpts[0].isCorrect = (frontendItem.correctAnswer === 'A');
    defaultOpts[1].isCorrect = (frontendItem.correctAnswer === 'B');
    frontendItem.options = defaultOpts;
  } else if (backendDto.options && backendDto.options.length > 0) {
    // **修改点：多选题 correctAnswer 解析**
    const correctChars = backendDto.questionType === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.backend && backendDto.correctAnswer
        ? backendDto.correctAnswer.split('') // "ABC" -> ["A", "B", "C"]
        : [backendDto.correctAnswer]; // 单选或无效多选按单个处理

    frontendItem.options = backendDto.options.map(optDto => {
      const optionValLetter = optionIdValueToLetter(optDto.optionIdValue);
      return {
        value: optionValLetter,
        label: optDto.optionText,
        isCorrect: correctChars.includes(optionValLetter)
      };
    }).sort((a,b) => (a.value || '').localeCompare(b.value || ''));
  }
  console.log("convertBackendToFrontendItem: Output (frontendItem)", JSON.parse(JSON.stringify(frontendItem)));
  return frontendItem;
};


// --- API 调用函数 ---
// (fetchQuestionsFromAPI, createQuestionAPI, updateQuestionAPI, deleteQuestionAPI 保持不变，因为它们依赖于上面已修改的转换函数)
// 同样提前给出URL
const API_BASE_URL = 'http://localhost:8080/api/questions';
const fetchQuestionsFromAPI = async (params = {}) => {
  isLoading.value = true;
  // console.log("fetchQuestionsFromAPI: Fetching with params", params);
  try {
    const url = new URL(API_BASE_URL);
    if (params.subject) url.searchParams.append('subjectCategory', params.subject);
    // 当从UI的筛选器获取类型时，它已经是中文了，需要转换为后端期望的英文
    if (params.type) url.searchParams.append('questionType', getBackendType(params.type));
    if (params.tags) url.searchParams.append('tags', params.tags);

    // console.log("fetchQuestionsFromAPI: Requesting URL", url.toString());
    const res = await fetch(url.toString());
    // console.log("fetchQuestionsFromAPI: Response status", res.status);
    if (!res.ok) {
      const errorText = await res.text();
      // console.error("fetchQuestionsFromAPI: HTTP error response text:", errorText);
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    const data = await res.json();
    // console.log('fetchQuestionsFromAPI: Raw data from API:', JSON.parse(JSON.stringify(data)));
    if (Array.isArray(data)) {
      questions.value = data.map(dto => convertBackendToFrontendItem(dto));
    } else {
      // console.error('fetchQuestionsFromAPI: Data from API is not an array!', data);
      questions.value = [];
    }
    // console.log('fetchQuestionsFromAPI: Updated questions.value:', JSON.parse(JSON.stringify(questions.value)));
  } catch (error) {
    alert('加载题目数据失败: ' + error.message);
    // console.error('加载题目数据失败:', error);
    questions.value = [];
  } finally {
    isLoading.value = false;
    // console.log("fetchQuestionsFromAPI: Fetch complete, isLoading set to false.");
  }
};

const createQuestionAPI = async (frontendQuestionData) => {
  const payload = convertFrontendToBackendDto(frontendQuestionData);
  // console.log("createQuestionAPI: Sending payload", JSON.parse(JSON.stringify(payload)));
  try {
    const res = await fetch(API_BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    // console.log("createQuestionAPI: Response status", res.status);
    if (!res.ok) {
      const errorText = await res.text();
      // console.error("createQuestionAPI: HTTP error response text:", errorText);
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    const responseData = await res.json();
    // console.log("createQuestionAPI: Response data", JSON.parse(JSON.stringify(responseData)));
    return responseData;
  } catch (error) {
    alert('添加题目失败: ' + error.message);
    // console.error('添加题目失败:', error);
    return null;
  }
};

const updateQuestionAPI = async (questionId, frontendQuestionData) => {
  if (!questionId) {
    alert('错误：题目ID缺失，无法更新。');
    // console.error("updateQuestionAPI: questionId is missing.");
    return null;
  }
  const payload = convertFrontendToBackendDto(frontendQuestionData);
  // console.log(`updateQuestionAPI: Updating question ID ${questionId} with payload`, JSON.parse(JSON.stringify(payload)));
  try {
    const res = await fetch(`${API_BASE_URL}/${questionId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    // console.log("updateQuestionAPI: Response status", res.status);
    if (!res.ok) {
      const errorText = await res.text();
      // console.error("updateQuestionAPI: HTTP error response text:", errorText);
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    const responseData = await res.json();
    // console.log("updateQuestionAPI: Response data", JSON.parse(JSON.stringify(responseData)));
    return responseData;
  } catch (error) {
    alert('修改题目失败: ' + error.message);
    // console.error('修改题目失败:', error);
    return null;
  }
};

const deleteQuestionAPI = async (questionId) => {
  if (!questionId) {
    alert('错误：题目ID缺失，无法删除。');
    // console.error("deleteQuestionAPI: questionId is missing.");
    return false;
  }
  // console.log(`deleteQuestionAPI: Deleting question ID ${questionId}`);
  try {
    const res = await fetch(`${API_BASE_URL}/${questionId}`, {
      method: 'DELETE'
    });
    // console.log("deleteQuestionAPI: Response status", res.status);
    if (!res.ok && res.status !== 204) {
      const errorText = await res.text();
      // console.error("deleteQuestionAPI: HTTP error response text:", errorText);
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    // console.log("deleteQuestionAPI: Deletion successful for ID", questionId);
    return true;
  } catch (error) {
    alert('删除题目失败: ' + error.message);
    console.error('删除题目失败:', error);
    return false;
  }
};

// --- 组件生命周期与UI交互逻辑 ---
onMounted(() => {
  // console.log("Vue Component: Mounted. Calling fetchQuestionsFromAPI.");
  fetchQuestionsFromAPI();
});

const handleQuestionTypeChange = () => {
  const type = currentQuestion.value.type; // 前端选择的中文类型
  // console.log("handleQuestionTypeChange: Frontend type changed to", type);
  if (type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    currentQuestion.value.options = defaultJudgmentOptionsFE().map(opt => ({...opt}));
    judgmentCorrectAnswerValue.value = null;
  } else {
    const currentOptionsAreJudgmentFormat = currentQuestion.value.options.length === 2 &&
        currentQuestion.value.options[0].label.includes('正确'); // 更宽松的检查
    if (currentOptionsAreJudgmentFormat || currentQuestion.value.options.length !== 4) {
      currentQuestion.value.options = defaultSingleMultiOptions().map(opt => ({...opt}));
    }
  }
};

watch(() => currentQuestion.value.type, (newType, oldType) => {
  if (newType !== oldType) {
    // console.log(`watch currentQuestion.type: Changed from ${oldType} to ${newType}`);
    handleQuestionTypeChange();
  }
});

watch([() => currentQuestion.value.correctAnswer, () => currentQuestion.value.type],
    ([newCorrectAnswer, newType]) => {
      if (newType === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
        // newCorrectAnswer 对判断题是 'A' 或 'B'
        if (judgmentCorrectAnswerValue.value !== newCorrectAnswer) {
          // console.log(`watch complex (Judgment): correctAnswer changed to ${newCorrectAnswer}, updating judgmentCorrectAnswerValue.`);
          judgmentCorrectAnswerValue.value = newCorrectAnswer;
        }
      }
    }, { deep: true } // options 变化也会触发，但我们主要关心 correctAnswer 对判断题的影响
);


const saveQuestion = async () => {
  // console.log("saveQuestion: Initiated. isEditing:", isEditing.value);
  if (!currentQuestion.value.text.trim()) { alert('题目内容不能为空'); return; }
  if (!currentQuestion.value.subject.trim()) { alert('所属科目不能为空'); return; }
  if (!currentQuestion.value.creator.trim()) { alert('创建者不能为空'); return; }
  if (currentQuestion.value.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    if (judgmentCorrectAnswerValue.value === null) { alert('判断题请选择一个答案'); return; }
    currentQuestion.value.options.forEach(option => {
      option.isCorrect = (option.value === judgmentCorrectAnswerValue.value);
    });
  } else {
    if (!currentQuestion.value.options.some(o => o.isCorrect && o.label.trim() !== '')) {
      alert('单选题或多选题至少选择一个有效答案'); return;
    }
    if (currentQuestion.value.options.some(o => o.isCorrect && !o.label.trim())) {
      alert('被选为正确答案的选项，其内容不能为空。'); return;
    }
  }

  let resultDto;
  if (isEditing.value) {
    resultDto = await updateQuestionAPI(currentQuestion.value.questionId, currentQuestion.value);
    if (resultDto) {
      const index = questions.value.findIndex(q => q.questionId === resultDto.questionId);
      if (index > -1) {
        questions.value[index] = convertBackendToFrontendItem(resultDto);
      }
      alert('题目修改成功！');
    }
  } else {
    resultDto = await createQuestionAPI(currentQuestion.value);
    if (resultDto) {
      questions.value.push(convertBackendToFrontendItem(resultDto));
      alert('题目添加成功！');
    }
  }
  if (resultDto) closeModal();
};

const showAddQuestionDialog = () => {
  isEditing.value = false;
  currentQuestion.value = {
    id: null, questionId: null,
    type: QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend, // 默认中文单选
    subject: '', text: '', tag: '', creator: '默认出题老师',
    options: defaultSingleMultiOptions().map(opt => ({...opt})),
    subjectCategory: '', tags: '', questionText: '',
    questionType: QUESTION_TYPES_MAP.SINGLE_CHOICE.backend, // 对应的后端英文类型
    correctAnswer: ''
  };
  judgmentCorrectAnswerValue.value = null;
  handleQuestionTypeChange();
  showModal.value = true;
};

const editQuestion = (questionToEdit) => {
  isEditing.value = true;
  const qCopy = JSON.parse(JSON.stringify(questionToEdit)); // qCopy 是前端格式

  currentQuestion.value = {
    id: qCopy.id,
    questionId: qCopy.questionId,
    type: qCopy.type, // 这是前端的中文类型，例如 "单选"
    subject: qCopy.subject,
    text: qCopy.text,
    tag: qCopy.tag,
    creator: qCopy.creator,
    options: qCopy.options.map(opt => ({ ...opt })),
    // 下面这些字段在 convertFrontendToBackendDto 中会用到
    // subjectCategory: qCopy.subject, // 将在 convertFrontendToBackendDto 中从 qCopy.subject 设置
    // tags: qCopy.tag,                // 将在 convertFrontendToBackendDto 中从 qCopy.tag 设置
    // questionText: qCopy.text,
    questionType: getBackendType(qCopy.type), // 存储对应的后端英文类型
    correctAnswer: qCopy.correctAnswer // 保留从后端（或上次保存）来的 correctAnswer 字符串
  };

  handleQuestionTypeChange(); // 根据 currentQuestion.type (中文) 重置选项结构

  if (currentQuestion.value.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    // qCopy.correctAnswer 对于判断题应该是 'A' 或 'B'
    judgmentCorrectAnswerValue.value = qCopy.correctAnswer; // 直接使用
    currentQuestion.value.options.forEach(opt => {
      opt.isCorrect = (opt.value === judgmentCorrectAnswerValue.value);
    });
  } else { // 单选或多选
    const correctVals = (qCopy.correctAnswer || '').split(currentQuestion.value.type === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.frontend ? '' : ',');
    if(currentQuestion.value.type === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.frontend && qCopy.correctAnswer.length > 1 && !qCopy.correctAnswer.includes(',')){
      // 处理后端返回的 "ABC" 格式多选题答案
      currentQuestion.value.options.forEach(opt => {
        opt.isCorrect = qCopy.correctAnswer.includes(opt.value);
      });
    } else {
      currentQuestion.value.options.forEach(opt => {
        opt.isCorrect = correctVals.includes(opt.value);
      });
    }
    judgmentCorrectAnswerValue.value = null;
  }
  showModal.value = true;
};

const setSingleCorrectOption = (optionValue) => {
  if (currentQuestion.value.type === QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend) {
    currentQuestion.value.options.forEach(opt => {
      opt.isCorrect = (opt.value === optionValue);
    });
  }
};

const deleteQuestion = async (question) => {
  console.log("deleteQuestion: Attempting to delete question", JSON.parse(JSON.stringify(question)));
  if (confirm(`确定要删除题目 "${question.text}" (ID: ${question.questionId}) 吗？`)) {
    const success = await deleteQuestionAPI(question.questionId);
    if (success) {
      questions.value = questions.value.filter(q => q.questionId !== question.questionId);
      alert('题目删除成功！');
    }
  }
};

const viewQuestionDetails = (question) => {
  console.log("viewQuestionDetails: Viewing details for", JSON.parse(JSON.stringify(question)));
  const optionsString = question.options.map(opt => `${opt.value}: ${opt.label} (${opt.isCorrect ? '正确答案' : '错误/未选'})`).join('\n  ');
  // 这里就不多写了
  // alert( /* ... */ );
};

const closeModal = () => { showModal.value = false; };

const addOption = () => {
  if (currentQuestion.value.type !== QUESTION_TYPES_MAP.TRUE_FALSE.frontend && currentQuestion.value.options.length < 4) {
    const nextLetter = String.fromCharCode(65 + currentQuestion.value.options.length);
    currentQuestion.value.options.push({ value: nextLetter, label: '', isCorrect: false });
  } else if (currentQuestion.value.type !== QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    alert('单选题或多选题最多添加4个选项');
  }
};

const removeLastOption = () => {
  if (currentQuestion.value.type !== QUESTION_TYPES_MAP.TRUE_FALSE.frontend && currentQuestion.value.options.length > 2) {
    currentQuestion.value.options.pop();
  } else if (currentQuestion.value.type !== QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    alert('题目至少需要2个选项');
  }
};


// --- 计算属性 ---
const uniqueSubjects = computed(() => {
  return [...new Set(questions.value.map(q => q.subject).filter(s => s))].sort();
});

const filteredQuestions = computed(() => {
  let result = questions.value;
  const query = searchQuery.value.toLowerCase().trim();
  const subject = selectedSubjectFilter.value;

  if (query) {
    result = result.filter(q =>
        (q.text && q.text.toLowerCase().includes(query)) ||
        (q.subject && q.subject.toLowerCase().includes(query)) ||
        (q.tag && q.tag.toLowerCase().includes(query))
    );
  }
  if (subject) {
    result = result.filter(q => q.subject === subject);
  }
  return result;
});

// 计算属性现在基于前端使用的中文类型
const isSingleChoice = computed(() => currentQuestion.value.type === QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend);
const isMultipleChoice = computed(() => currentQuestion.value.type === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.frontend);
const isJudgmentQuestion = computed(() => currentQuestion.value.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend);

// ... (调试用 Watchers 保持不变) ...
watch(questions, (newVal) => { /* ... */ }, { deep: true });
watch(currentQuestion, (newVal) => { /* ... */ }, { deep: true });

</script>
<!--样式部分 自己设计就行 注意对比度-->
<style scoped>
/* 全局与页面布局 */
.question-bank-page {
  padding: 20px 30px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa; /* 淡雅的背景色 */
  min-height: 100vh;
  color: #333; /* 默认文字颜色 */
}

.page-header h1 {
  color: #2c3e50; /* 深蓝灰色 */
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.2em;
  font-weight: 600;
}

.controls-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  flex-wrap: wrap; /* 允许换行 */
  gap: 15px; /* 控件之间的间隙 */
}

.search-filter-group {
  display: flex;
  gap: 15px; /* 搜索框和下拉框之间的间隙 */
  flex-grow: 1; /* 占据可用空间 */
  flex-wrap: wrap; /* 允许内部换行 */
}

.control-input {
  padding: 10px 15px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 1em;
  transition: border-color 0.2s, box-shadow 0.2s;
  height: 42px; /* 统一输入框高度 */
  box-sizing: border-box;
}
.control-input:focus {
  border-color: #007bff; /* 主题蓝色 */
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
  outline: none;
}
.search-input {
  min-width: 250px;
  flex-basis: 300px;
  flex-grow: 1;
}
.subject-select {
  min-width: 180px;
  flex-basis: 200px;
  cursor: pointer;
}

/* 按钮通用样式 */
.btn {
  padding: 10px 18px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1em;
  font-weight: 500;
  transition: all 0.2s ease-in-out;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-decoration: none;
  line-height: 1.5; /* 确保文字垂直居中 */
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 3px 6px rgba(0,0,0,0.12);
}
.btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}
.primary-btn {
  background-color: #007bff;
  color: white;
}
.primary-btn:hover { background-color: #0069d9; }

.secondary-btn {
  background-color: #6c757d;
  color: white;
}
.secondary-btn:hover { background-color: #5a6268; }

.danger-btn {
  background-color: #dc3545;
  color: white;
}
.danger-btn:hover { background-color: #c82333; }

.danger-outline-btn {
  background-color: transparent;
  color: #dc3545;
  border: 1px solid #dc3545;
}
.danger-outline-btn:hover {
  background-color: #dc3545;
  color: white;
}

.warning-btn {
  background-color: #ffc107;
  color: #212529;
}
.warning-btn:hover { background-color: #e0a800; }


.icon-btn i { /* 假设您会使用字体图标或SVG */
  line-height: 1; /* 确保图标垂直居中 */
}

/* 题目卡片网格 */
.content-section {
  /* 样式留白等 */
}
.loading-indicator, .no-results-indicator {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
  background-color: #e9ecef;
  border-radius: 8px;
  font-size: 1.1em;
  margin-top: 20px;
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
}

.question-card {
  background-color: #fff;
  border-radius: 10px;
  padding: 20px 25px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.07);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  display: flex;
  flex-direction: column;
  border-top: 4px solid #007bff; /* 主题色上边框 */
}
.question-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 0.9em;
  color: #6c757d;
}
.type-badge {
  padding: 4px 10px;
  border-radius: 15px;
  font-weight: bold;
  color: white;
  font-size: 0.8em;
  text-transform: uppercase;
}
.type-badge.type-单选 { background-color: #17a2b8; }
.type-badge.type-多选 { background-color: #ffc107; color: #333; }
.type-badge.type-判断 { background-color: #28a745; }

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  align-items: flex-start; /* 左对齐 */
  font-size: 0.85em;
}
.card-meta span {
  background-color: #f0f0f0;
  padding: 3px 8px;
  border-radius: 4px;
}

.card-title {
  font-size: 1.15em;
  font-weight: 600;
  color: #343a40;
  margin-bottom: 12px;
  line-height: 1.4;
  flex-grow: 1;
  word-break: break-word; /* 防止长文本溢出 */
}

.card-options {
  margin-bottom: 18px;
  font-size: 0.95em;
  padding-left: 5px;
}
.option-item {
  padding: 6px 0;
  color: #495057;
  word-break: break-word;
}
.option-letter {
  font-weight: bold;
  margin-right: 5px;
  color: #007bff;
}
.option-item.correct-answer {
  color: #0f5132;
  background-color: #d1e7dd;
  padding: 6px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 15px;
  border-top: 1px solid #f1f1f1;
}
.edit-action-btn { background-color: #ffc107; color: #212529; }
.edit-action-btn:hover { background-color: #e0a800; }
.delete-action-btn { background-color: #dc3545; color: white; }
.delete-action-btn:hover { background-color: #c82333; }

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  overflow-y: auto; /* 确保在内容过多时整个遮罩层可滚动 */
}

.modal-dialog {
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.2);
  width: 100%;
  max-width: 700px;
  max-height: 90vh; /* 限制最大高度 */
  display: flex;
  flex-direction: column;
  margin: auto; /* 确保在 overlay 中居中 */
}

.modal-header {
  padding: 18px 25px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0; /* 防止头部被压缩 */
}
.modal-title {
  margin: 0;
  font-size: 1.5em;
  font-weight: 600;
  color: #343a40;
}
.modal-close-btn {
  background: none;
  border: none;
  font-size: 1.8em;
  font-weight: bold;
  color: #6c757d;
  cursor: pointer;
  padding: 0 5px;
  line-height: 1;
}
.modal-close-btn:hover { color: #343a40; }

.modal-body {
  padding: 25px;
  overflow-y: auto; /* 模态框主体内容滚动 */
  flex-grow: 1; /* 占据可用空间 */
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 18px;
}
.form-row:last-child {
  margin-bottom: 0;
}
.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #495057;
  font-size: 0.95em;
}

.form-control {
  width: 100%;
  padding: 10px 12px;
  font-size: 1em;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  border-radius: 0.25rem;
  transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  box-sizing: border-box;
}
.form-control:focus {
  color: #495057;
  background-color: #fff;
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}
textarea.form-control {
  min-height: 80px;
  resize: vertical;
}

.options-fieldset {
  border: 1px solid #dee2e6;
  padding: 20px; /* 增加内边距 */
  border-radius: 6px;
  margin-top: 10px; /* 与上方元素的间距 */
  margin-bottom: 20px;
}
.options-fieldset legend {
  padding: 0 10px;
  font-weight: 600; /* 加粗 */
  color: #343a40; /* 深色 */
  width: auto;
  margin-left: 10px;
  font-size: 1.1em; /* 稍大字体 */
}

.option-input-group {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 10px; /* 增加选项组内边距 */
  border-radius: 4px;
  background-color: #f8f9fa;
}
.option-input-group.judgment {
  background-color: transparent;
  padding: 5px 0;
}

.option-prefix {
  font-weight: bold;
  margin-right: 8px;
  min-width: 25px;
  color: #495057;
}
.option-text-input {
  flex-grow: 1;
}
.correct-marker {
  display: flex;
  align-items: center;
  margin-left: 15px;
  white-space: nowrap;
}
.form-check-input { /* checkbox 和 radio 的通用样式 */
  margin-right: 6px;
  width: 1.2em; /* 增大点击区域 */
  height: 1.2em;
  cursor: pointer;
}
.correct-label, .option-input-label {
  margin-left: 4px;
  color: #333;
  font-size: 1em;
  cursor: pointer; /* 使标签可点击以选择 radio/checkbox */
  user-select: none;
}

.option-management-buttons {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}
.add-option-btn, .remove-option-btn {
  padding: 8px 12px;
  font-size: 0.9em;
}
.add-option-btn {
  background-color: #28a745; /* Green */
  color: white;
}
.add-option-btn:hover {
  background-color: #218838;
}

.form-hint {
  font-size: 0.9em;
  color: #6c757d;
  margin-top: 8px;
  padding-left: 5px;
}

.modal-footer {
  padding: 18px 25px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-shrink: 0; /* 防止底部被压缩 */
}

/* 图标占位符 (您需要替换为真实的图标) */
.icon-add::before { content: "➕"; margin-right: 6px; font-size: 0.9em; }
.icon-edit::before { content: "✎"; margin-right: 6px; }
.icon-delete::before { content: "🗑"; margin-right: 6px; }
.icon-save::before { content: "💾"; margin-right: 6px; }
.icon-add-circle::before { content: "⊕"; margin-right: 6px; }
.icon-remove-circle::before { content: "⊖"; margin-right: 6px; }


/* 响应式调整 */
@media (max-width: 768px) {
  .question-bank-page {
    padding: 15px;
  }
  .page-header h1 {
    font-size: 1.8em;
  }
  .controls-section {
    flex-direction: column;
    align-items: stretch;
  }
  .search-filter-group {
    width: 100%;
    margin-bottom: 15px;
    flex-direction: column; /* 小屏幕上垂直排列 */
  }
  .search-input, .subject-select {
    width: 100%; /* 占满整行 */
    flex-basis: auto;
  }
  .add-question-btn {
    width: 100%;
  }
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  .form-row .form-group {
    margin-bottom: 18px;
  }
  .modal-dialog {
    max-width: 95%;
    margin: 10px;
    max-height: 95vh;
  }
  .modal-title {
    font-size: 1.3em;
  }
  .modal-body {
    padding: 20px;
  }
  .modal-footer {
    padding: 15px 20px;
  }
  .card-actions {
    flex-direction: column; /* 小屏幕上按钮垂直排列 */
    align-items: stretch;
  }
  .card-actions .btn {
    width: 100%;
    margin-bottom: 8px;
  }
  .card-actions .btn:last-child {
    margin-bottom: 0;
  }
}
</style>