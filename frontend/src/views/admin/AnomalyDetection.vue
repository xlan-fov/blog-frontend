<template>
  <div class="anomaly-container">
    <!-- 时间范围筛选 -->
    <div class="filter-section">
      <div class="date-range">
        <div class="date-picker-container">
          <label>开始时间</label>
          <input 
            type="date" 
            class="date-input" 
            v-model="startDate"
            :max="endDate || today"
            @change="validateStartDate"
          />
        </div>
        <span class="range-separator">至</span>
        <div class="date-picker-container">
          <label>结束时间</label>
          <input 
            type="date" 
            class="date-input" 
            v-model="endDate"
            :min="startDate"
            :max="today"
            @change="validateEndDate"
          />
        </div>
      </div>
      
      <div class="user-search">
        <input 
          type="text" 
          v-model="searchUsername" 
          placeholder="搜索用户名" 
          class="username-input"
        />
      </div>
      
      <button class="query-btn" @click="queryAnomalies">查询</button>
    </div>
    
    <!-- 错误提示信息 -->
    <div v-if="errorMessage" class="error-message">
      <i class="error-icon">⚠️</i> {{ errorMessage }}
    </div>
    
    <!-- 异常数据表格 -->
    <div class="anomaly-table">
      <table>
        <thead>
          <tr>
            <th>账号</th>
            <th>异常原因</th>
            <th>账号状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="anomaly in filteredAnomalies" :key="anomaly.id">
            <td>{{ anomaly.username }}</td>
            <td>{{ anomaly.reason }}</td>
            <td>{{ anomaly.status }}</td>
            <td>{{ anomaly.timestamp }}</td>
            <td class="actions-cell">
              <button 
                v-if="anomaly.status === '已登录'" 
                class="action-btn kick-btn" 
                @click="kickUser(anomaly)"
              >
                踢踢
              </button>
              <button 
                class="action-btn ban-btn" 
                @click="banUser(anomaly)"
              >
                封禁
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

// 时间范围 - 使用ISO格式的日期字符串 (YYYY-MM-DD)
const startDate = ref('')
const endDate = ref('')
const errorMessage = ref('')
const searchUsername = ref('') // 添加用户名搜索

// 示例异常数据
const anomalies = ref([
  {
    id: 1,
    username: '张三',
    reason: '多地异常登录',
    status: '已登录',
    timestamp: '2024-05-08 12:30:45'
  },
  {
    id: 2,
    username: '李四',
    reason: '密码多次错误',
    status: '已封禁',
    timestamp: '2024-05-07 18:22:10'
  },
  {
    id: 3, 
    username: '王五',
    reason: '异常操作频率',
    status: '已登录',
    timestamp: '2024-05-06 09:15:00'
  }
])

// 获取今天的日期字符串（YYYY-MM-DD格式）
const today = computed(() => {
  const now = new Date()
  return now.toISOString().split('T')[0]
})

// 添加日期校验的响应式监听
watch([startDate, endDate], ([newStart, newEnd]) => {
  // 清除之前的错误信息
  errorMessage.value = ''
  
  if (newStart && newEnd) {
    const start = new Date(newStart)
    const end = new Date(newEnd)
    const now = new Date()
    now.setHours(23, 59, 59, 999)
    
    if (start > end) {
      errorMessage.value = '开始时间不能晚于结束时间'
      // 可选：自动纠正日期
      // endDate.value = newStart
    }
    
    if (end > now) {
      errorMessage.value = '结束时间不能晚于今天'
      // 可选：自动纠正日期
      // endDate.value = today.value
    }
  }
}, { immediate: true })

// 过滤后的异常记录
const filteredAnomalies = computed(() => {
  // 首先根据日期范围过滤
  let result = anomalies.value
  
  if (startDate.value && endDate.value) {
    const start = new Date(startDate.value)
    const end = new Date(endDate.value)
    end.setHours(23, 59, 59, 999) // 设置为当天结束时间
    
    result = result.filter(item => {
      const timestamp = new Date(item.timestamp)
      return timestamp >= start && timestamp <= end
    })
  }
  
  // 然后根据用户名过滤
  if (searchUsername.value) {
    const keyword = searchUsername.value.toLowerCase()
    result = result.filter(item => 
      item.username.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

// 组件挂载时设置默认日期范围（过去7天）
onMounted(() => {
  const now = new Date()
  const sevenDaysAgo = new Date(now)
  sevenDaysAgo.setDate(now.getDate() - 7)
  
  endDate.value = now.toISOString().split('T')[0]
  startDate.value = sevenDaysAgo.toISOString().split('T')[0]
})

// 查询异常数据
function queryAnomalies() {
  // 清除之前的错误信息
  errorMessage.value = ''
  
  // 验证日期
  if (!startDate.value || !endDate.value) {
    errorMessage.value = '请选择完整的时间范围'
    return
  }
  
  const start = new Date(startDate.value)
  const end = new Date(endDate.value)
  const now = new Date()
  
  // 设置当天时间为23:59:59，以便进行比较
  now.setHours(23, 59, 59, 999)
  
  if (start > end) {
    errorMessage.value = '开始时间不能晚于结束时间'
    return
  }
  
  if (end > now) {
    errorMessage.value = '结束时间不能晚于今天'
    return
  }
  
  // 前端已通过计算属性完成过滤
  console.log('查询时间范围:', startDate.value, '至', endDate.value)
}

// 踢出用户
function kickUser(user) {
  // 实际项目中应调用API踢出用户
  alert(`已踢出用户: ${user.username}`)
  // 更新用户状态
  user.status = '未登录'
}

// 封禁用户
function banUser(user) {
  // 实际项目中应调用API封禁用户
  alert(`已封禁用户: ${user.username}`)
  // 更新用户状态
  user.status = '已封禁'
}

// 新增：验证并格式化日期选择
function validateStartDate(event) {
  const selectedDate = event.target.value
  if (selectedDate && endDate.value && new Date(selectedDate) > new Date(endDate.value)) {
    errorMessage.value = '开始时间不能晚于结束时间'
  } else {
    errorMessage.value = ''
  }
}

function validateEndDate(event) {
  const selectedDate = event.target.value
  const now = new Date()
  now.setHours(23, 59, 59, 999)
  
  if (selectedDate) {
    if (startDate.value && new Date(selectedDate) < new Date(startDate.value)) {
      errorMessage.value = '结束时间不能早于开始时间'
    } else if (new Date(selectedDate) > now) {
      errorMessage.value = '结束时间不能晚于今天'
    } else {
      errorMessage.value = ''
    }
  }
}
</script>

<style scoped>
.anomaly-container {
  width: 100%;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  align-items: flex-end;
}

.date-range {
  display: flex;
  align-items: center;
}

.date-picker-container {
  display: flex;
  flex-direction: column;
}

.date-picker-container label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.date-input {
  width: 180px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 15px;
  font-size: 14px;
  cursor: pointer;
  background-color: white;
}

.range-separator {
  margin: 0 15px;
  color: #666;
  align-self: flex-end;
  padding-bottom: 10px;
}

.query-btn {
  min-width: 100px;
  height: 40px;
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0 20px;
  cursor: pointer;
  font-size: 14px;
}

.anomaly-table {
  background-color: white;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  font-weight: normal;
  color: #333;
  background-color: #fafafa;
}

td:nth-child(2) {
  max-width: 400px; /* 限制异常原因列的宽度 */
}

.actions-cell {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 5px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.kick-btn {
  background-color: #e6f7ff;
  color: #1890ff;
}

.ban-btn {
  background-color: #fff0f0;
  color: #ff4d4f;
}

/* 空状态提示 */
.empty-state {
  text-align: center;
  padding: 30px 0;
  color: #999;
}

/* 错误信息样式 */
.error-message {
  color: #ff4d4f;
  margin-bottom: 15px;
  font-size: 14px;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  padding: 8px 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.error-icon {
  margin-right: 8px;
  font-style: normal;
}

/* 高亮显示有问题的输入框 */
input.date-input.error {
  border-color: #ff4d4f;
  background-color: #fff2f0;
}

/* 添加用户搜索框样式 */
.user-search {
  margin-left: 15px;
}

.username-input {
  width: 180px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 15px;
  font-size: 14px;
}
</style>
