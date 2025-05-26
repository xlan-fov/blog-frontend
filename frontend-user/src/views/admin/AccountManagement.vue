<template>
  <div class="account-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号查询">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px;">
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
      <el-table :data="paginatedAccounts" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column label="注册时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="loginStatus" label="登录状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.loginStatus ? 'success' : 'info'">
              {{ scope.row.loginStatus ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240">
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
        <el-descriptions-item label="手机号">{{ selectedAccount.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDateTime(selectedAccount.registerTime || selectedAccount.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ formatDateTime(selectedAccount.lastLoginTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ selectedAccount.lastLoginIp || '未记录' }}</el-descriptions-item>
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
      
      <!-- 添加封禁历史记录 -->
      <div v-if="selectedAccount.banHistory && selectedAccount.banHistory.length > 0" class="ban-info" style="margin-top: 16px;">
        <div class="ban-title">封禁历史</div>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in selectedAccount.banHistory"
            :key="index"
            :timestamp="formatDateTime(item.createdAt)"
            :type="item.action === 'ban' ? 'danger' : 'success'"
          >
            {{ item.action === 'ban' ? '封禁' : '解封' }}: {{ item.reason || '未提供原因' }}
          </el-timeline-item>
        </el-timeline>
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
import { ref, onMounted, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import adminApi from '@/api/admin'

// 表单和表格数据
const searchForm = ref({
  username: '',
  status: ''
})
const accounts = ref([])
const total = ref(0)
const loading = ref(false)
// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedAccounts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return accounts.value.slice(start, start + pageSize.value)
})

// 账号详情
const dialogVisible = ref(false)
const selectedAccount = ref({})

// 封禁对话框
const banDialogVisible = ref(false)
const banLoading = ref(false)
const banForm = ref({
  reason: '',
  accountId: null
})

// 获取账号列表
const fetchAccounts = async () => {
  loading.value = true
  try {
    // 确保参数名称与后端一致
    const params = {
      keyword: searchForm.value.username || undefined, // username 在后端对应 keyword
      status: searchForm.value.status || undefined, // status 在后端对应 status
    };
    
    console.log('发送获取用户列表请求，参数:', params);
    
    const res = await adminApi.getUsers(params);
    
    if (res.code === 200 && res.data) {
      // 转换数据格式，确保每个用户对象都有正确的 status 和 loginStatus 字段
      const userList = res.data.list || [];
      accounts.value = userList.map(user => {
        console.log('原始用户数据:', user);
        // 检查并转换字段
        return {
          ...user,
          // 根据 isBanned 字段设置状态，如果不存在则保持原状态
          status: user.isBanned !== undefined 
            ? (user.isBanned === 1 ? 'banned' : 'normal') 
            : user.status || 'normal',
          // 根据 isLoggedIn 字段设置登录状态，如果不存在则保持原状态
          loginStatus: user.isLoggedIn !== undefined 
            ? user.isLoggedIn === 1 
            : user.loginStatus || false,
          // 确保时间字段映射正确
          registerTime: user.registerTime || user.created_at || user.createdAt || user.create_time || user.createTime,
          lastLoginTime: user.lastLoginTime || user.last_login_time || user.lastLoginTime || user.last_login
        };
      });
      total.value = res.data.total || 0;
      
      console.log('转换后的用户列表数据:', accounts.value);
    } else {
      ElMessage.error(res.message || '获取用户列表失败');
      accounts.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户列表失败');
    accounts.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
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
  ElMessageBox.prompt(`请输入踢出账号 "${account.username}" 的原因:`, '踢出账号', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    inputType: 'textarea',
    inputPlaceholder: '请输入踢出原因',
  }).then(async ({ value: reason }) => {
    if (!reason || reason.trim() === '') {
      ElMessage.warning('请填写踢出原因');
      return;
    }

    try {
      // 调用踢出用户的API
      const res = await adminApi.kickUser(account.id, reason);
      
      if (res.code === 200) {
        // 更新本地状态
        account.loginStatus = false;
        ElMessage.success('账号已踢出');
        // 重新获取用户列表以确保数据同步
        fetchAccounts();
      } else {
        ElMessage.error(res.message || '踢出失败');
      }
    } catch (error) {
      console.error('踢出用户失败:', error);
      ElMessage.error('踢出操作失败');
    }
  }).catch(() => {
    // 用户取消操作，不做任何处理
  });
}

// 封禁账号
const banAccount = (account) => {
  banForm.value.accountId = account.id
  banForm.value.reason = ''
  // 保存当前选中的账号信息
  selectedAccount.value = account
  banDialogVisible.value = true
}

// 确认封禁
const confirmBan = async () => {
  if (!banForm.value.reason.trim()) {
    ElMessage.warning('请输入封禁原因')
    return
  }
  
  banLoading.value = true
  try {
    const res = await adminApi.banUser(selectedAccount.value.username, { reason: banForm.value.reason })
    
    if (res.code === 200) {
      ElMessage.success('封禁成功')
      // 刷新用户列表
      fetchAccounts()
    } else {
      ElMessage.error(res.message || '封禁失败')
    }
  } catch (error) {
    console.error('封禁用户失败:', error)
    ElMessage.error('封禁操作失败')
  } finally {
    banLoading.value = false
    banDialogVisible.value = false
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
      const res = await adminApi.unbanUser(account.username, {})
      
      if (res.code === 200) {
        ElMessage.success('解封成功')
        // 刷新用户列表
        fetchAccounts()
      } else {
        ElMessage.error(res.message || '解封失败')
      }
    } catch (error) {
      console.error('解封用户失败:', error)
      ElMessage.error('解封操作失败')
    }
  }).catch(() => {})
}

// 查看账号详情
const viewAccountDetail = async (account) => {
  try {
    const res = await adminApi.getUserInfo(account.username)
    
    if (res.code === 200 && res.data) {
      console.log('获取到用户详情(原始数据):', res.data);
      
      // 正确的字段映射表 - 将后端返回的字段名映射到前端期望的字段名
      const fieldMappings = {
        // 后端SQL别名 -> 前端期望字段名
        createTime: 'createdAt',        // 注册时间
        lastLoginTime: 'lastLoginTime', // 最后登录时间（保持不变）
        
        // 处理可能的其他字段名格式
        created_at: 'createdAt',
        last_login_time: 'lastLoginTime',
        last_login_ip: 'lastLoginIp',
        is_banned: 'isBanned',
        is_logged_in: 'isLoggedIn',
        ban_reason: 'banReason',
        
        // 直接映射的字段
        username: 'username',
        phone: 'phone',
        status: 'status',
        // 删除 email 字段的映射
        articleCount: 'articleCount'
      };
      
      // 创建映射后的数据对象
      const mappedData = {};
      
      // 复制并映射所有字段
      Object.keys(res.data).forEach(key => {
        console.log(`处理字段: ${key} = ${res.data[key]}`);
        const mappedKey = fieldMappings[key] || key;
        mappedData[mappedKey] = res.data[key];
      });
      
      console.log('字段映射后的数据:', mappedData);
      
      // 处理状态转换
      let userStatus = 'normal';
      if (mappedData.status === 'banned') {
        userStatus = 'banned';
      } else if (mappedData.status === 'normal') {
        userStatus = 'normal';
      }
      
      // 更新 selectedAccount
      selectedAccount.value = {
        ...mappedData,
        // 确保状态字段正确
        status: userStatus,
        loginStatus: false, // 详情页面暂时没有在线状态信息
        // 确保时间字段存在并正确命名
        createdAt: mappedData.createdAt,
        registerTime: mappedData.createdAt, // 注册时间就是创建时间
        lastLoginTime: mappedData.lastLoginTime
      }
      
      console.log('最终显示数据:', selectedAccount.value);
      
      // 打开详情对话框
      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '暂无记录';
  
  try {
    const date = new Date(dateTime);
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) return dateTime;
    
    // 格式化为 YYYY-MM-DD HH:MM:SS
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).replace(/\//g, '-');
  } catch (error) {
    console.error('日期格式化错误:', error);
    return dateTime; // 出错时返回原始值
  }
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
</style>