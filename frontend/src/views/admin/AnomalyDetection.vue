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
          />
        </div>
        <span class="range-separator">至</span>
        <div class="date-picker-container">
          <label>结束时间</label>
          <input 
            type="date" 
            class="date-input" 
            v-model="endDate"
          />
        </div>
      </div>
      <button class="query-btn" @click="queryAnomalies">查询</button>
    </div>
    
    <!-- 异常数据表格 -->
    <div class="anomaly-table">
      <table>
        <thead>
          <tr>
            <th>账号</th>
            <th>异常原因</th>
            <th>账号状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(anomaly, index) in anomalies" :key="index">
            <td>{{ anomaly.username }}</td>
            <td>{{ anomaly.reason }}</td>
            <td>{{ anomaly.status }}</td>
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
import { ref } from 'vue'

// 时间范围 - 使用ISO格式的日期字符串 (YYYY-MM-DD)
const startDate = ref('')
const endDate = ref('')

// 异常数据列表
const anomalies = ref([
  {
    username: '张三',
    reason: '从XX至XX期间，进行过X次登录，均因密码错误登录失败',
    status: '已登录'
  },
  {
    username: '李四',
    reason: '在XX时间，准备发布一篇带有YY违禁词的Blog',
    status: '未登录'
  }
])

// 查询异常数据
function queryAnomalies() {
  // 实际项目中应调用API获取指定时间范围的异常数据
  console.log('查询时间范围:', startDate.value, '至', endDate.value)
  
  // 模拟API调用结果
  if (startDate.value && endDate.value) {
    // 这里可以添加加载状态、调用API等逻辑
    alert(`查询时间范围: ${startDate.value} 至 ${endDate.value}`)
  } else {
    alert('请选择完整的时间范围')
  }
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
</script>

<style scoped>
.anomaly-container {
  width: 100%;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  align-items: center;
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
</style>
