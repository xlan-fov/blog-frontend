<template>
  <div class="sensitive-words-management">
    <el-card class="operation-card">
      <div class="card-header">
        <h2>敏感词库管理</h2>
        <div class="operations">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索敏感词"
            clearable
            style="width: 200px; margin-right: 10px"
            @input="filterWords"
          />
          <el-button type="primary" @click="showAddDialog">添加敏感词</el-button>
          <el-button type="danger" :disabled="!selectedWords.length" @click="showBatchDeleteDialog">
            批量删除
          </el-button>
          <el-button type="success" @click="showBatchAddDialog">批量添加</el-button>
        </div>
      </div>

      <el-table
        ref="wordTable"
        v-loading="loading"
        :data="displayedWords"
        style="width: 100%"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" link @click="editWord(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="deleteWord(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalWords"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 添加敏感词对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加敏感词"
      width="30%"
    >
      <el-form :model="addForm" label-width="80px" :rules="wordRules" ref="addFormRef">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="addForm.word" placeholder="请输入敏感词" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addWord" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量添加敏感词对话框 -->
    <el-dialog
      v-model="batchAddDialogVisible"
      title="批量添加敏感词"
      width="50%"
    >
      <p class="dialog-tip">请输入敏感词，每行一个</p>
      <el-input
        v-model="batchAddWords"
        type="textarea"
        :rows="10"
        placeholder="请输入敏感词，每行一个"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchAddDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addBatchWords" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量删除敏感词对话框 -->
    <el-dialog
      v-model="batchDeleteDialogVisible"
      title="批量删除敏感词"
      width="30%"
    >
      <p>确认要删除选中的 {{ selectedWords.length }} 个敏感词吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDeleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteBatchWords" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑敏感词对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑敏感词"
      width="30%"
    >
      <el-form :model="editForm" label-width="80px" :rules="wordRules" ref="editFormRef">
        <el-form-item label="原敏感词">
          <span>{{ editForm.oldWord }}</span>
        </el-form-item>
        <el-form-item label="新敏感词" prop="newWord">
          <el-input v-model="editForm.newWord" placeholder="请输入新的敏感词" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateWord" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi from '@/api/admin'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const searchKeyword = ref('')
const selectedWords = ref([])
const wordList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框控制
const addDialogVisible = ref(false)
const batchAddDialogVisible = ref(false)
const batchDeleteDialogVisible = ref(false)
const editDialogVisible = ref(false)

// 表单数据
const addForm = ref({
  word: ''
})
const batchAddWords = ref('')
const editForm = ref({
  oldWord: '',
  newWord: ''
})
const addFormRef = ref(null)
const editFormRef = ref(null)

// 表单验证规则
const wordRules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  newWord: [
    { required: true, message: '请输入新的敏感词', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 计算分页后的敏感词列表
const displayedWords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return wordList.value.slice(start, end)
})

// 总敏感词数量
const totalWords = computed(() => wordList.value.length)

// 获取敏感词列表
const fetchSensitiveWords = async () => {
  loading.value = true
  try {
    const res = await adminApi.getSensitiveWordsList()
    if (res.code === 200 && res.data) {
      // 过滤出未删除的敏感词
      wordList.value = res.data.filter(word => word.isDeleted !== 1)
      
      // 如果搜索关键词存在，则进行过滤
      if (searchKeyword.value) {
        filterWords()
      }
    } else {
      ElMessage.error(res.message || '获取敏感词列表失败')
    }
  } catch (error) {
    console.error('获取敏感词列表失败:', error)
    ElMessage.error('获取敏感词列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索过滤敏感词
const filterWords = () => {
  if (!searchKeyword.value) {
    fetchSensitiveWords() // 如果搜索关键词为空，重新获取所有敏感词
    return
  }
  
  // 本地过滤
  const keyword = searchKeyword.value.toLowerCase()
  wordList.value = wordList.value.filter(item => {
    return item.word.toLowerCase().includes(keyword)
  })
  
  // 重置分页
  currentPage.value = 1
}

// 显示添加敏感词对话框
const showAddDialog = () => {
  addForm.value.word = ''
  addDialogVisible.value = true
  
  // 在下一个事件循环中聚焦输入框
  setTimeout(() => {
    if (addFormRef.value) {
      addFormRef.value.resetFields()
    }
  }, 0)
}

// 添加敏感词
const addWord = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const res = await adminApi.addSensitiveWord(addForm.value.word)
      
      if (res.code === 200) {
        ElMessage.success('敏感词添加成功')
        addDialogVisible.value = false
        fetchSensitiveWords() // 刷新列表
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    } catch (error) {
      console.error('添加敏感词失败:', error)
      ElMessage.error('添加失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除敏感词
const deleteWord = async (row) => {
  ElMessageBox.confirm(`确定要删除敏感词"${row.word}"吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await adminApi.deleteSensitiveWord(row.word)
      
      if (res.code === 200) {
        ElMessage.success('敏感词删除成功')
        fetchSensitiveWords() // 刷新列表
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除敏感词失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 显示批量添加对话框
const showBatchAddDialog = () => {
  batchAddWords.value = ''
  batchAddDialogVisible.value = true
}

// 批量添加敏感词
const addBatchWords = async () => {
  if (!batchAddWords.value.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }
  
  const words = batchAddWords.value
    .split('\n')
    .map(word => word.trim())
    .filter(word => word.length > 0)
  
  if (words.length === 0) {
    ElMessage.warning('没有有效的敏感词')
    return
  }
  
  submitting.value = true
  try {
    const res = await adminApi.addSensitiveWords(words)
    
    if (res.code === 200) {
      ElMessage.success(`成功添加 ${words.length} 个敏感词`)
      batchAddDialogVisible.value = false
      fetchSensitiveWords() // 刷新列表
    } else {
      ElMessage.error(res.message || '批量添加失败')
    }
  } catch (error) {
    console.error('批量添加敏感词失败:', error)
    ElMessage.error('批量添加失败')
  } finally {
    submitting.value = false
  }
}

// 显示批量删除对话框
const showBatchDeleteDialog = () => {
  if (selectedWords.value.length === 0) {
    ElMessage.warning('请至少选择一个敏感词')
    return
  }
  
  batchDeleteDialogVisible.value = true
}

// 批量删除敏感词
const deleteBatchWords = async () => {
  if (selectedWords.value.length === 0) {
    ElMessage.warning('请至少选择一个敏感词')
    return
  }
  
  submitting.value = true
  try {
    const wordsToDelete = selectedWords.value.map(item => item.word)
    const res = await adminApi.deleteSensitiveWords(wordsToDelete)
    
    if (res.code === 200) {
      ElMessage.success(`成功删除 ${selectedWords.value.length} 个敏感词`)
      batchDeleteDialogVisible.value = false
      fetchSensitiveWords() // 刷新列表
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (error) {
    console.error('批量删除敏感词失败:', error)
    ElMessage.error('批量删除失败')
  } finally {
    submitting.value = false
  }
}

// 编辑敏感词
const editWord = (row) => {
  editForm.value.oldWord = row.word
  editForm.value.newWord = row.word
  editDialogVisible.value = true
  
  // 在下一个事件循环中聚焦输入框
  setTimeout(() => {
    if (editFormRef.value) {
      editFormRef.value.clearValidate()
    }
  }, 0)
}

// 更新敏感词
const updateWord = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (editForm.value.oldWord === editForm.value.newWord) {
      editDialogVisible.value = false
      return // 如果没有变化，直接关闭对话框
    }
    
    submitting.value = true
    try {
      const res = await adminApi.editSensitiveWord(editForm.value.oldWord, editForm.value.newWord)
      
      if (res.code === 200) {
        ElMessage.success('敏感词修改成功')
        editDialogVisible.value = false
        fetchSensitiveWords() // 刷新列表
      } else {
        ElMessage.error(res.message || '修改失败')
      }
    } catch (error) {
      console.error('修改敏感词失败:', error)
      ElMessage.error('修改失败')
    } finally {
      submitting.value = false
    }
  })
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedWords.value = selection
}

// 分页
const handlePageChange = (page) => {
  currentPage.value = page
  // 无需重新获取数据，computed property已处理分页
}

// 页面大小变化处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置为第一页
  // 无需重新获取数据，computed property已处理分页
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '暂无记录'
  
  try {
    const date = new Date(dateTime)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) return dateTime
    
    // 格式化为 YYYY-MM-DD HH:MM:SS
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).replace(/\//g, '-')
  } catch (error) {
    console.error('日期格式化错误:', error)
    return dateTime // 出错时返回原始值
  }
}

// 初始化
onMounted(() => {
  fetchSensitiveWords()
})
</script>

<style scoped>
.sensitive-words-management {
  padding: 16px;
}

.operation-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.operations {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.dialog-tip {
  color: #909399;
  margin-bottom: 10px;
}

/* 添加表格居中样式 */
:deep(.el-table .cell) {
  text-align: center;
}

:deep(.el-table th) {
  text-align: center !important;
}

:deep(.el-table td) {
  vertical-align: middle;
}

/* 确保标签和按钮也居中 */
:deep(.el-table .el-tag) {
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

:deep(.el-table .el-button-group),
:deep(.el-table .el-button) {
  display: inline-flex;
  justify-content: center;
}

/* 修复选择框列的居中问题 */
:deep(.el-table .el-table-column--selection .cell) {
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  padding: 0 !important;
}

:deep(.el-table .el-checkbox) {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

@media screen and (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .operations {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .operations .el-input {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 8px;
  }
}
</style>
