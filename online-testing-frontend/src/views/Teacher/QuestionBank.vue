<template>
  <div class="question-bank-page">
    <header class="page-header">
      <h1>题库管理</h1>
    </header>
    <section class="controls-section">
      <div class="search-filter-group">
        <input
            v-model="searchQuery"
            placeholder="按题目内容、科目或标签搜索..."
            class="control-input search-input"/>
        <select v-model="selectedSubjectFilter" class="control-input subject-select">
          <option value="">全部分类</option>
          <option v-for="subject_option in uniqueSubjects" :key="subject_option" :value="subject_option">{{ subject_option }}</option>
        </select>
      </div>
      <button class="btn primary-btn add-question-btn" @click="showAddQuestionDialog">
        <i class="icon-add"></i> 添加新题目
      </button>
    </section>
    <section class="content-section">
      <div v-if="isLoading" class="loading-indicator">
        <p>题目加载中...</p>
      </div>
      <div v-else-if="!filteredQuestions.length" class="no-results-indicator">
        <p>未找到符合条件的题目，或题库当前为空。</p>
      </div>
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
                v-for="(option_item) in question.options"
                :key="option_item.value"
                :class="['option-item', { 'correct-answer': option_item.isCorrect }]"
            >
              <span class="option-letter">{{ option_item.value }}.</span> {{ option_item.label }}
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
                      class="visually-hidden-input"
                  />
                  <label :for="'option-judge-' + option.value" class="switch-label">
                    <span class="switch-track">
                        <span class="switch-thumb"></span>
                    </span>
                    <span class="option-label-text judgment-switch-label-text">{{ option.label }}</span>
                  </label>
                </div>
                <p class="form-hint">判断题：请选择一个作为正确答案。</p>
              </div>

              <div v-else class="standard-options-group">
                <div v-for="(option) in currentQuestion.options" :key="option.value" class="option-input-group">
                  <span class="option-prefix">{{ option.value }}.</span>
                  <input type="text" v-model="option.label" :placeholder="`选项 ${option.value} 内容`" class="form-control option-text-input" required />
                  <div class="correct-marker">
                    <input
                        v-if="isSingleChoice"
                        type="radio"
                        :id="'correct-opt-' + option.value"
                        name="single-choice-group" :value="option.value"
                        :checked="option.isCorrect" @click="setSingleCorrectOption(option.value)" class="visually-hidden-input"
                    />
                    <input
                        v-if="isMultipleChoice"
                        type="checkbox"
                        :id="'correct-opt-' + option.value"
                        :name="'multi-choice-correct-answer-' + option.value"
                        :value="option.value"
                        v-model="option.isCorrect" class="visually-hidden-input"
                    />
                    <label :for="'correct-opt-' + option.value" class="switch-label">
                       <span class="switch-track">
                           <span class="switch-thumb"></span>
                       </span>
                    </label>
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
<script setup>
import { ref, computed, onMounted, watch } from 'vue';
// --- 类型映射 ---
const QUESTION_TYPES_MAP = {
  SINGLE_CHOICE: { backend: 'Single Choice', frontend: '单选' },
  MULTIPLE_CHOICE: { backend: 'Multiple Choice', frontend: '多选' },
  TRUE_FALSE: { backend: 'True/False', frontend: '判断' }
};
// --- 类型转换函数 ---
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

// --- 默认选项生成函数 ---
const defaultSingleMultiOptions = (count = 4) => {
  const opts = [];
  for (let i = 0; i < count; i++) {
    opts.push({ value: String.fromCharCode(65 + i), label: '', isCorrect: false });
  }
  return opts;
};
const defaultJudgmentOptionsFE = () => [
  { value: 'A', label: '正确', isCorrect: false },
  { value: 'B', label: '错误', isCorrect: false }
];

// --- 当前题目对象 ---
const currentQuestion = ref({
  id: null,
  questionId: null,
  type: QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend,
  subject: '',
  text: '',
  tag: '',
  creator: '出题老师',
  options: defaultSingleMultiOptions(),
  // subjectCategory, tags, questionText, questionType 字段在转换到后端DTO时使用
  // correctAnswer 字段存储的是后端格式的答案字符串（如 "A", "AC"）
  correctAnswer: ''
});
// --- 工具函数 ---
const optionLetterToIdValue = (letter) => {
  if (!letter || typeof letter !== 'string' || letter.length !== 1) return null;
  const val = letter.charCodeAt(0) - 64; // A=1, B=2
  return val > 0 ? val : null;
};
const optionIdValueToLetter = (idValue) => {
  if (idValue === null || typeof idValue !== 'number' || idValue < 1) return '';
  return String.fromCharCode(64 + idValue); // 1=A, 2=B
};

// --- 数据转换函数 ---
const convertFrontendToBackendDto = (frontendQuestion) => {
  const backendDto = {
    questionId: frontendQuestion.questionId,
    subjectCategory: frontendQuestion.subject,
    tags: frontendQuestion.tag,
    questionText: frontendQuestion.text,
    questionType: getBackendType(frontendQuestion.type),
    creator: frontendQuestion.creator,
    correctAnswer: '',
    options: []
  };
  let correctAnswersLetters = [];
  if (frontendQuestion.options && frontendQuestion.options.length > 0) {
    frontendQuestion.options.forEach((opt) => {
      const optionIdVal = optionLetterToIdValue(opt.value);
      if (optionIdVal !== null) {
        backendDto.options.push({ optionIdValue: optionIdVal, optionText: opt.label });
        if (opt.isCorrect) {
          correctAnswersLetters.push(opt.value);
        }
      }
    });
  }
  if (frontendQuestion.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    backendDto.correctAnswer = correctAnswersLetters.length > 0 ? correctAnswersLetters[0] : '';
  } else if (frontendQuestion.type === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.frontend) {
    backendDto.correctAnswer = correctAnswersLetters.sort().join('');
  } else { // SINGLE_CHOICE
    backendDto.correctAnswer = correctAnswersLetters.length > 0 ? correctAnswersLetters[0] : '';
  }
  return backendDto;
};

const convertBackendToFrontendItem = (backendDto) => {
  const frontendKey = backendDto.questionId || `temp-${Date.now()}-${Math.random()}`;
  const frontendTypeDisplay = getFrontendType(backendDto.questionType);
  const frontendItem = {
    id: frontendKey,
    questionId: backendDto.questionId,
    type: frontendTypeDisplay,
    subject: backendDto.subjectCategory,
    text: backendDto.questionText,
    tag: backendDto.tags,
    creator: backendDto.creator,
    correctAnswer: backendDto.correctAnswer, // 保存后端原始的 correctAnswer 字符串
    options: [],
    // 保留这些原始字段可能对于某些逻辑有用，但当前主要依赖上面转换的字段
    // subjectCategory: backendDto.subjectCategory,
    // tags: backendDto.tags,
    // questionText: backendDto.questionText,
    // questionTypeOriginal: backendDto.questionType,
  };
  if (frontendItem.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    const defaultOpts = defaultJudgmentOptionsFE().map(opt => ({...opt}));
    defaultOpts[0].isCorrect = (frontendItem.correctAnswer === 'A'); // 'A' for True
    defaultOpts[1].isCorrect = (frontendItem.correctAnswer === 'B'); // 'B' for False
    frontendItem.options = defaultOpts;
  } else if (backendDto.options && backendDto.options.length > 0) {
    const correctChars = backendDto.questionType === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.backend && backendDto.correctAnswer
        ? backendDto.correctAnswer.split('') // "AC" -> ["A", "C"]
        : (backendDto.correctAnswer ? [backendDto.correctAnswer] : []); // "A" -> ["A"], "" -> []
    frontendItem.options = backendDto.options.map(optDto => {
      const optionValLetter = optionIdValueToLetter(optDto.optionIdValue);
      return {
        value: optionValLetter,
        label: optDto.optionText,
        isCorrect: correctChars.includes(optionValLetter)
      };
    }).sort((a,b) => (a.value || '').localeCompare(b.value || ''));
  }
  return frontendItem;
};

// --- API 调用函数 ---
const API_BASE_URL = 'http://localhost:8080/api/questions';
const fetchQuestionsFromAPI = async (params = {}) => {
  isLoading.value = true;
  try {
    const url = new URL(API_BASE_URL);
    if (params.subject) url.searchParams.append('subjectCategory', params.subject);
    if (params.type) url.searchParams.append('questionType', getBackendType(params.type));
    if (params.tags) url.searchParams.append('tags', params.tags); // Assuming tags is a string for query
    const res = await fetch(url.toString());
    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    const data = await res.json();
    if (Array.isArray(data)) {
      questions.value = data.map(dto => convertBackendToFrontendItem(dto));
    } else {
      questions.value = [];
    }
  } catch (error) {
    alert('加载题目数据失败: ' + error.message);
    questions.value = [];
  } finally {
    isLoading.value = false;
  }
};
const createQuestionAPI = async (frontendQuestionData) => {
  const payload = convertFrontendToBackendDto(frontendQuestionData);
  try {
    const res = await fetch(API_BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    return await res.json();
  } catch (error) {
    alert('添加题目失败: ' + error.message);
    return null;
  }
};
const updateQuestionAPI = async (questionId, frontendQuestionData) => {
  if (!questionId) {
    alert('错误：题目ID缺失，无法更新。');
    return null;
  }
  const payload = convertFrontendToBackendDto(frontendQuestionData);
  try {
    const res = await fetch(`${API_BASE_URL}/${questionId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    return await res.json();
  } catch (error) {
    alert('修改题目失败: ' + error.message);
    return null;
  }
};
const deleteQuestionAPI = async (questionId) => {
  if (!questionId) {
    alert('错误：题目ID缺失，无法删除。');
    return false;
  }
  try {
    const res = await fetch(`${API_BASE_URL}/${questionId}`, {
      method: 'DELETE'
    });
    if (!res.ok && res.status !== 204) {
      const errorText = await res.text();
      throw new Error(`HTTP error ${res.status}: ${errorText}`);
    }
    return true;
  } catch (error) {
    alert('删除题目失败: ' + error.message);
    console.error('删除题目失败:', error);
    return false;
  }
};

// --- 组件生命周期与UI交互逻辑 ---
onMounted(() => {
  fetchQuestionsFromAPI();
});

const handleQuestionTypeChange = () => {
  const type = currentQuestion.value.type;
  // 当题目类型改变时，重置选项为该类型的默认状态
  if (type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    currentQuestion.value.options = defaultJudgmentOptionsFE().map(opt => ({...opt}));
  } else { // SINGLE_CHOICE or MULTIPLE_CHOICE
    currentQuestion.value.options = defaultSingleMultiOptions().map(opt => ({...opt}));
  }
  // 清除非判断题的判断题答案模型
  if (type !== QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    judgmentCorrectAnswerValue.value = null;
  } else {
    // 如果切换到判断题，也清空单选/多选的 correctAnswer 字符串（如果之前有的话）
    // (虽然currentQuestion.correctAnswer主要在editQuestion中从qCopy设置，这里是类型切换逻辑)
  }
};

watch(() => currentQuestion.value.type, (newType, oldType) => {
  if (newType !== oldType) {
    handleQuestionTypeChange();
    // 注意：如果是在编辑状态下切换题目类型，用户可能需要重新选择正确答案，
    // 因为 handleQuestionTypeChange 会将选项重置为新类型的默认isCorrect=false状态。
  }
});

// 这个 watcher 主要用于同步判断题的 judgmentCorrectAnswerValue 和 currentQuestion.correctAnswer
// 但 currentQuestion.correctAnswer 主要由 editQuestion 初始化，并在 saveQuestion 前同步。
watch([() => currentQuestion.value.correctAnswer, () => currentQuestion.value.type],
    ([newCorrectAnswer, newType]) => {
      if (newType === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
        if (judgmentCorrectAnswerValue.value !== newCorrectAnswer) {
          judgmentCorrectAnswerValue.value = newCorrectAnswer;
        }
      }
    }, { deep: true }
);

const saveQuestion = async () => {
  if (!currentQuestion.value.text.trim()) { alert('题目内容不能为空'); return; }
  if (!currentQuestion.value.subject.trim()) { alert('所属科目不能为空'); return; }
  if (!currentQuestion.value.creator.trim()) { alert('创建者不能为空'); return; }

  // 在保存前，确保判断题的 isCorrect 状态与 judgmentCorrectAnswerValue 同步
  if (currentQuestion.value.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    if (judgmentCorrectAnswerValue.value === null) { alert('判断题请选择一个答案'); return; }
    currentQuestion.value.options.forEach(option => {
      option.isCorrect = (option.value === judgmentCorrectAnswerValue.value);
    });
  } else { // 单选/多选的 isCorrect 应该已经通过交互（setSingleCorrectOption 或 v-model for checkbox）更新了
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
  if (resultDto) {
    closeModal(); // 关闭模态框
    await fetchQuestionsFromAPI(); // 重新从服务器获取所有题目
  }
};

const showAddQuestionDialog = () => {
  isEditing.value = false;
  const defaultType = QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend;
  currentQuestion.value = {
    id: null, questionId: null,
    type: defaultType,
    subject: '', text: '', tag: '', creator: '默认出题老师',
    options: defaultSingleMultiOptions().map(opt => ({...opt})),
    correctAnswer: ''
    // questionType (backend string) will be derived by getBackendType in convertFrontendToBackendDto
  };
  judgmentCorrectAnswerValue.value = null;
  showModal.value = true;
};

const editQuestion = (questionToEdit) => {
  isEditing.value = true;
  const qCopy = JSON.parse(JSON.stringify(questionToEdit));

  currentQuestion.value = {
    id: qCopy.id,
    questionId: qCopy.questionId,
    type: qCopy.type, // 前端类型 ("单选", "多选", "判断")
    subject: qCopy.subject,
    text: qCopy.text,
    tag: qCopy.tag,
    creator: qCopy.creator,
    options: [], // 将在下方根据类型和qCopy填充
    correctAnswer: qCopy.correctAnswer // 后端格式的答案字符串 ("A", "AC")
    // questionType (backend string) will be derived by getBackendType in convertFrontendToBackendDto
  };

  // === USER'S PROPOSED FIX for editQuestion option initialization ===
  let parsedCorrectAnswers = [];
  // currentQuestion.value.type 是前端中文类型, qCopy.correctAnswer 是后端答案字符串
  if (currentQuestion.value.type === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.frontend) {
    parsedCorrectAnswers = (qCopy.correctAnswer || '').split('');
  } else if (currentQuestion.value.type === QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend) {
    parsedCorrectAnswers = qCopy.correctAnswer ? [qCopy.correctAnswer] : [];
  }

  if (currentQuestion.value.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend) {
    // 判断题特殊处理：选项固定为"正确"和"错误"
    currentQuestion.value.options = defaultJudgmentOptionsFE().map(opt => ({...opt}));
    judgmentCorrectAnswerValue.value = qCopy.correctAnswer; // "A" or "B"
    // 同步 isCorrect 状态到 currentQuestion.options (虽然判断题的 isCorrect 更多是内部使用)
    currentQuestion.value.options.forEach(opt => {
      opt.isCorrect = (opt.value === judgmentCorrectAnswerValue.value);
    });
  } else { // 单选题 或 多选题
    // 直接使用 qCopy.options 中的原始选项数据 (value, label)
    // 并根据解析后的 parsedCorrectAnswers 来设置 isCorrect
    if (qCopy.options && qCopy.options.length > 0) {
      currentQuestion.value.options = qCopy.options.map(originalOption => ({
        ...originalOption, // 复制原始选项的所有属性 (如 value, label)
        isCorrect: parsedCorrectAnswers.includes(originalOption.value) // 根据解析结果重设 isCorrect
      }));
    } else {
      // 如果 qCopy.options 为空 (异常情况), 则生成默认选项结构并尝试设置isCorrect
      currentQuestion.value.options = defaultSingleMultiOptions().map(defaultOpt => ({
        ...defaultOpt,
        isCorrect: parsedCorrectAnswers.includes(defaultOpt.value)
      }));
    }
    judgmentCorrectAnswerValue.value = null; // 清空判断题的答案模型
  }
  // === END OF USER'S PROPOSED FIX INTEGRATION AREA ===
  showModal.value = true;
};

const setSingleCorrectOption = (optionValue) => {
  if (currentQuestion.value.type === QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend) {
    // 创建一个全新的选项数组以强制 Vue 更新视图
    const newOptions = currentQuestion.value.options.map(opt => ({
      ...opt,
      isCorrect: (opt.value === optionValue)
    }));
    currentQuestion.value.options = newOptions;
  }
};

const deleteQuestion = async (question) => {
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
  // 实际的详情显示逻辑可以扩展
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
        (q.tag && q.tag.toLowerCase().includes(query)) // 标签搜索
    );
  }
  if (subject) {
    result = result.filter(q => q.subject === subject);
  }
  return result;
});
const isSingleChoice = computed(() => currentQuestion.value.type === QUESTION_TYPES_MAP.SINGLE_CHOICE.frontend);
const isMultipleChoice = computed(() => currentQuestion.value.type === QUESTION_TYPES_MAP.MULTIPLE_CHOICE.frontend);
const isJudgmentQuestion = computed(() => currentQuestion.value.type === QUESTION_TYPES_MAP.TRUE_FALSE.frontend);

watch(questions, (newVal) => { /* console.log('questions changed'); */ }, { deep: true });
watch(currentQuestion, (newVal) => { /* console.log('currentQuestion changed'); */ }, { deep: true });

</script>
<style scoped>
/* 全局与页面布局 */
.question-bank-page {
  padding: 20px 30px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa;
  min-height: 100vh;
  color: #333;
}

.page-header h1 {
  color: #2c3e50;
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
  flex-wrap: wrap;
  gap: 15px;
}

.search-filter-group {
  display: flex;
  gap: 15px;
  flex-grow: 1;
  flex-wrap: wrap;
}

.control-input {
  padding: 10px 15px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 1em;
  transition: border-color 0.2s, box-shadow 0.2s;
  height: 42px;
  box-sizing: border-box;
}
.control-input:focus {
  border-color: #007bff;
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
  line-height: 1.5;
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

.danger-outline-btn {
  background-color: transparent;
  color: #dc3545;
  border: 1px solid #dc3545;
}
.danger-outline-btn:hover {
  background-color: #dc3545;
  color: white;
}

.icon-btn i {
  line-height: 1;
}

.content-section { }
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
  border-top: 4px solid #007bff;
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
  align-items: flex-start;
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
  word-break: break-word;
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

.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  overflow-y: auto;
}

.modal-dialog {
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.2);
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  margin: auto;
}

.modal-header {
  padding: 18px 25px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
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
  overflow-y: auto;
  flex-grow: 1;
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
  padding: 20px;
  border-radius: 6px;
  margin-top: 10px;
  margin-bottom: 20px;
}
.options-fieldset legend {
  padding: 0 10px;
  font-weight: 600;
  color: #343a40;
  width: auto;
  margin-left: 10px;
  font-size: 1.1em;
}

.option-input-group {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 10px;
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

.visually-hidden-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  margin: 0;
  padding: 0;
  border: 0;
  pointer-events: none;
}

.switch-label {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  gap: 8px;
}

.switch-track {
  width: 40px;
  height: 20px;
  background-color: #ccc; /* Off state */
  border-radius: 10px;
  position: relative;
  transition: background-color 0.2s ease;
  display: inline-block;
}

.switch-thumb {
  width: 16px;
  height: 16px;
  background-color: white;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: transform 0.2s ease;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.visually-hidden-input:checked + .switch-label .switch-track {
  background-color: #007bff; /* On state */
}

.visually-hidden-input:checked + .switch-label .switch-thumb {
  transform: translateX(20px);
}

.judgment-switch-label-text, .option-label-text { /* For judgment options text */
  font-size: 1em;
  color: #333;
  line-height: 1.2em;
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
  background-color: #28a745;
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
  flex-shrink: 0;
}

.icon-add::before { content: "➕"; margin-right: 6px; font-size: 0.9em; }
.icon-edit::before { content: "✎"; margin-right: 6px; }
.icon-delete::before { content: "🗑"; margin-right: 6px; }
.icon-save::before { content: "💾"; margin-right: 6px; }
.icon-add-circle::before { content: "⊕"; margin-right: 6px; }
.icon-remove-circle::before { content: "⊖"; margin-right: 6px; }


@media (max-width: 768px) {
  .question-bank-page { padding: 15px; }
  .page-header h1 { font-size: 1.8em; }
  .controls-section { flex-direction: column; align-items: stretch; }
  .search-filter-group { width: 100%; margin-bottom: 15px; flex-direction: column; }
  .search-input, .subject-select { width: 100%; flex-basis: auto; }
  .add-question-btn { width: 100%; }
  .form-row { flex-direction: column; gap: 0; }
  .form-row .form-group { margin-bottom: 18px; }
  .modal-dialog { max-width: 95%; margin: 10px; max-height: 95vh; }
  .modal-title { font-size: 1.3em; }
  .modal-body { padding: 20px; }
  .modal-footer { padding: 15px 20px; }
  .card-actions { flex-direction: column; align-items: stretch; }
  .card-actions .btn { width: 100%; margin-bottom: 8px; }
  .card-actions .btn:last-child { margin-bottom: 0; }
}
</style>