<template>
  <div class="account-manage-container">
    <!-- 顶部操作栏 -->
    <div class="top-actions">
      <div class="search-box">
        <input 
          type="text" 
          placeholder="输入账号搜索" 
          v-model="searchKeyword"
          @keyup.enter="searchAccounts" 
        />
        <button class="search-btn" @click="searchAccounts">搜索</button>
      </div>
      <button class="create-account-btn" @click="createAccount">创建账号</button>
    </div>
    
    <!-- 账号表格 -->
    <div class="account-table">
      <table>
        <thead>
          <tr>
            <th>账号</th>
            <th>登录密码</th>
            <th>手机号</th>
            <th>账号状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(account, index) in filteredAccounts" :key="index">
            <td>{{ account.username }}</td>
            <td>{{ account.password }}</td>
            <td>{{ account.phone }}</td>
            <td>{{ account.status }}</td>
            <td class="actions-cell">
              <button class="action-btn detail-btn" @click="viewDetail(account)">详情</button>
              
              <template v-if="account.status === '已登录' || account.status === '未登录'">
                <button class="action-btn ban-btn" @click="banAccount(account)">封禁</button>
              </template>
              
              <template v-else-if="account.status === '已封禁'">
                <button class="action-btn unban-btn" @click="unbanAccount(account)">解封</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 创建账号对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay">
      <div class="dialog-content">
        <h3>创建账号</h3>
        <div class="form-item">
          <label>账号名称</label>
          <input v-model="newAccount.username" placeholder="请输入账号名称" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input type="password" v-model="newAccount.password" placeholder="请输入密码" />
        </div>
        <div class="form-item">
          <label>手机号</label>
          <input v-model="newAccount.phone" placeholder="请输入手机号" />
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" @click="showCreateDialog = false">取消</button>
          <button class="confirm-btn" @click="confirmCreate">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 搜索关键词
const searchKeyword = ref('')

// 是否显示创建账号对话框
const showCreateDialog = ref(false)

// 新账号信息
const newAccount = ref({
  username: '',
  password: '',
  phone: ''
})

// 账号数据
const accounts = ref([
  {
    username: '张三',
    password: '******',
    phone: '159****7777',
    status: '已登录'
  },
  {
    username: '李四',
    password: '******',
    phone: '186****1234',
    status: '已封禁'
  },
  {
    username: '王五',
    password: '******',
    phone: '111****2222',
    status: '未登录'
  }
])

// 增强的前端搜索过滤 - 支持多个字段搜索
const filteredAccounts = computed(() => {
  if (!searchKeyword.value.trim()) {
    return accounts.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return accounts.value.filter(account => 
    account.username.toLowerCase().includes(keyword) ||
    account.phone.toLowerCase().includes(keyword) ||
    account.status.toLowerCase().includes(keyword)
  )
})

// 创建账号
function createAccount() {
  showCreateDialog.value = true
  newAccount.value = {
    username: '',
    password: '',
    phone: ''
  }
}

// 确认创建账号
function confirmCreate() {
  if (!newAccount.value.username || !newAccount.value.password || !newAccount.value.phone) {
    alert('请填写完整信息')
    return
  }
  
  // 实际项目中应该调用API创建账号
  accounts.value.push({
    username: newAccount.value.username,
    password: '******', // 显示为星号
    phone: newAccount.value.phone.substring(0, 3) + '****' + newAccount.value.phone.substring(7), // 部分隐藏
    status: '未登录'
  })
  
  showCreateDialog.value = false
}

// 查看账号详情
function viewDetail(account) {
  alert(`账号详情：${account.username}`)
  // 实际项目中应该跳转到详情页或打开详情对话框
}

// 封禁账号
function banAccount(account) {
  // 实际项目中应该调用API封禁账号
  account.status = '已封禁'
}

// 解封账号
function unbanAccount(account) {
  // 实际项目中应该调用API解封账号
  account.status = '未登录'
}

// 搜索账号 - 可用于添加前端排序等额外功能
function searchAccounts() {
  console.log('搜索关键字:', searchKeyword.value)
  // filteredAccounts 计算属性已自动完成搜索
  // 这里可以添加其他功能，如切换排序顺序等
}
</script>

<style scoped>
.account-manage-container {
  width: 100%;
}

.top-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  width: 400px;
  height: 40px; /* 添加固定高度 */
}

.search-box input {
  flex: 1;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  padding: 0 15px;
  font-size: 14px;
  border-right: none;
  box-sizing: border-box; /* 确保padding不会增加高度 */
}

.search-btn {
  width: 80px;
  height: 40px;
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  padding: 0 15px;
  cursor: pointer;
  font-size: 14px;
  box-sizing: border-box; /* 确保padding不会增加高度 */
}

.create-account-btn {
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0 20px;
  height: 40px;
  cursor: pointer;
  font-size: 14px;
}

.account-table {
  background-color: white;
  border-radius: 4px;
  overflow: hidden;
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

.actions-cell {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.detail-btn {
  background-color: #f0f0f0;
  color: #333;
}

.ban-btn {
  background-color: #fff0f0;
  color: #ff4d4f;
}

.unban-btn {
  background-color: #f0f8ff;
  color: #4e6ef2;
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  width: 400px;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.dialog-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #333;
}

.form-item {
  margin-bottom: 15px;
}

.form-item label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #666;
}

.form-item input {
  width: 100%;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 12px;
  font-size: 14px;
  box-sizing: border-box;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  gap: 10px;
}

.cancel-btn {
  padding: 0 20px;
  height: 36px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.confirm-btn {
  padding: 0 20px;
  height: 36px;
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}
</style>
