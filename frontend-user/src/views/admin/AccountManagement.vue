<template>
  <div class="account-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号查询">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="已封禁" value="banned" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchAccounts">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card>
      <el-table :data="accounts" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />
        <el-table-column prop="loginStatus" label="登录状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.loginStatus ? 'success' : 'info'">
              {{ scope.row.loginStatus ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button 
              v-if="scope.row.loginStatus" 
              type="warning" 
              link 
              @click="kickAccount(scope.row)"
            >
              踢出
            </el-button>
            <el-button 
              v-if="scope.row.status === 'normal'" 
              type="danger" 
              link 
              @click="banAccount(scope.row)"
            >
              封禁
            </el-button>
            <el-button 
              v-if="scope.row.status === 'banned'" 
              type="success" 
              link 
              @click="unbanAccount(scope.row)"
            >
              解封
            </el-button>
            <el-button type="primary" link @click="viewAccountDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 账号详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="账号详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ selectedAccount.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedAccount.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ selectedAccount.phone }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selectedAccount.registerTime }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ selectedAccount.lastLoginTime }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ selectedAccount.lastLoginIp }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="getStatusType(selectedAccount.status)">
            {{ getStatusText(selectedAccount.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="登录状态">
          <el-tag :type="selectedAccount.loginStatus ? 'success' : 'info'">
            {{ selectedAccount.loginStatus ? '在线' : '离线' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <div v-if="selectedAccount.banReason" class="ban-info">
        <div class="ban-title">封禁原因</div>
        <div class="ban-content">{{ selectedAccount.banReason }}</div>
      </div>
    </el-dialog>
    
    <!-- 封禁对话框 -->
    <el-dialog
      v-model="banDialogVisible"
      title="账号封禁"
      width="500px"
    >
      <el-form :model="banForm" label-width="80px">
        <el-form-item label="封禁原因">
          <el-input
            v-model="banForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入封禁原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="banDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBan" :loading="banLoading">
            确认封禁
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

// 搜索表单
const searchForm = ref({
  username: '',
  status: ''
})

// 表格数据
const accounts = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 账号详情
const dialogVisible = ref(false)
const selectedAccount = ref({})

// 封禁对话框
const banDialogVisible = ref(false)
const banLoading = ref(false)
const banForm = reactive({
  reason: '',
  accountId: null
})

// 获取账号列表
const fetchAccounts = async () => {
  loading.value = true
  try {
    // TODO: 替换为axios请求
    // 模拟数据
    setTimeout(() => {
      accounts.value = Array(20).fill().map((_, index) => ({
        id: index + 1,
        username: `user${index + 1}`,
        email: `user${index + 1}@example.com`,
        phone: `1380013${index.toString().padStart(4, '0')}`,
        registerTime: new Date(Date.now() - Math.random() * 10000000000).toLocaleString(),
        lastLoginTime: new Date(Date.now() - Math.random() * 1000000000).toLocaleString(),
        lastLoginIp: `192.168.1.${Math.floor(Math.random() * 255)}`,
        loginStatus: Math.random() > 0.7,
        status: Math.random() > 0.8 ? 'banned' : 'normal',
        banReason: Math.random() > 0.8 ? '多次发布违规内容，予以封禁' : ''
      }))
      total.value = 100 // 模拟总数
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取账号列表失败')
    loading.value = false
  }
}

// 搜索账号
const searchAccounts = () => {
  currentPage.value = 1
  fetchAccounts()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value.username = ''
  searchForm.value.status = ''
  searchAccounts()
}

// 踢出账号
const kickAccount = (account) => {
  ElMessageBox.confirm(`确定要踢出账号 "${account.username}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: 替换为axios请求
      // 模拟操作
      account.loginStatus = false
      ElMessage.success('账号已踢出')
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 封禁账号
const banAccount = (account) => {
  banForm.accountId = account.id
  banForm.reason = ''
  banDialogVisible.value = true
}

// 确认封禁
const confirmBan = async () => {
  if (!banForm.reason.trim()) {
    ElMessage.warning('请输入封禁原因')
    return
  }
  
  banLoading.value = true
  try {
    // TODO: 替换为axios请求
    // 模拟操作
    const account = accounts.value.find(item => item.id === banForm.accountId)
    if (account) {
      account.status = 'banned'
      account.banReason = banForm.reason
      account.loginStatus = false
    }
    
    banDialogVisible.value = false
    ElMessage.success('账号已封禁')
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    banLoading.value = false
  }
}

// 解封账号
const unbanAccount = (account) => {
  ElMessageBox.confirm(`确定要解封账号 "${account.username}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      // TODO: 替换为axios请求
      // 模拟操作
      account.status = 'normal'
      account.banReason = ''
      ElMessage.success('账号已解封')
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 查看账号详情
const viewAccountDetail = (account) => {
  selectedAccount.value = { ...account }
  dialogVisible.value = true
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    normal: 'success',
    banned: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    normal: '正常',
    banned: '已封禁'
  }
  return statusMap[status] || '未知'
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchAccounts()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchAccounts()
}

// 初始化
onMounted(() => {
  fetchAccounts()
})
</script>

<style scoped>
.account-management {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.ban-info {
  margin-top: 20px;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  padding: 10px;
}

.ban-title {
  font-weight: bold;
  color: #f56c6c;
  margin-bottom: 8px;
}

.ban-content {
  color: #606266;
  line-height: 1.5;
}
</style> 