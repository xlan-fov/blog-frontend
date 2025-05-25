<template>
  <div class="anomaly-detection">
    <el-tabs v-model="activeTab" class="detection-tabs">
      <el-tab-pane label="账号异常登录" name="abnormalLogin">
        <el-card class="search-card">
          <el-form :inline="true" :model="searchForm">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :shortcuts="dateShortcuts"
              />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchAbnormalLogins">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card>
          <el-table :data="paginatedAbnormalLogins" border style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="ip" label="IP地址" width="150" />
            <el-table-column prop="location" label="地理位置" width="150" />
            <el-table-column prop="time" label="发生时间" width="180" />
            <el-table-column prop="attemptCount" label="失败次数" width="100" />
            <el-table-column prop="status" label="账号状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button 
                  v-if="scope.row.status === 'normal'" 
                  type="danger" 
                  link 
                  @click="banAccount(scope.row)"
                >
                  封禁账号
                </el-button>
                <el-button type="primary" link @click="viewDetail(scope.row, 'login')">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalAbnormalLogins"
              :page-size="pageSize"
              :current-page="currentPage"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="违禁内容展示" name="prohibitedContent">
        <el-card class="search-card">
          <el-form :inline="true" :model="contentSearchForm">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="contentSearchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :shortcuts="dateShortcuts"
              />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="contentSearchForm.username" placeholder="请输入用户名" clearable style="width: 200px;" />
            </el-form-item>
            <el-form-item label="拒绝原因">
              <el-select v-model="contentSearchForm.reason" placeholder="选择拒绝原因" clearable style="width: 200px;">
                <el-option label="全部" value="" />
                <el-option label="政治敏感" value="political" />
                <el-option label="色情低俗" value="pornography" />
                <el-option label="暴力恐怖" value="violence" />
                <el-option label="不实信息" value="fake_news" />
                <el-option label="侵犯隐私" value="privacy" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchProhibitedContent">查询</el-button>
              <el-button @click="resetContentSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card>
          <el-table :data="paginatedProhibitedContents" border style="width: 100%" v-loading="contentLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="title" label="内容标题" show-overflow-tooltip />
            <el-table-column prop="type" label="内容类型" width="100" />
            <el-table-column prop="rejectTime" label="拒绝时间" width="180" />
            <el-table-column prop="rejectReason" label="拒绝原因" width="150">
              <template #default="scope">
                <el-tag :type="getRejectReasonType(scope.row.rejectReason)">
                  {{ getRejectReasonText(scope.row.rejectReason) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="userStatus" label="账号状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.userStatus)">
                  {{ getStatusText(scope.row.userStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" link @click="viewDetail(scope.row, 'content')">
                  查看内容
                </el-button>
                <el-button 
                  v-if="scope.row.userStatus === 'normal'" 
                  type="danger" 
                  link 
                  @click="banAccount(scope.row, 'content')"
                >
                  封禁账号
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalProhibitedContents"
              :page-size="contentPageSize"
              :current-page="contentCurrentPage"
              @size-change="handleContentSizeChange"
              @current-change="handleContentCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 异常登录详情对话框 -->
    <el-dialog
      v-model="loginDetailDialogVisible"
      title="异常登录详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ selectedDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ selectedDetail.ip }}</el-descriptions-item>
        <el-descriptions-item label="地理位置">{{ selectedDetail.location }}</el-descriptions-item>
        <el-descriptions-item label="设备信息">{{ selectedDetail.device || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ selectedDetail.time }}</el-descriptions-item>
        <el-descriptions-item label="失败次数">{{ selectedDetail.attemptCount }}</el-descriptions-item>
        <el-descriptions-item label="尝试记录" :span="2">
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in selectedDetail.attempts"
              :key="index"
              :timestamp="activity.time"
              :type="activity.status === 'failed' ? 'danger' : 'success'"
            >
              {{ activity.status === 'failed' ? '登录失败' : '登录成功' }}
              <p class="small-text">
                {{ activity.message }}
              </p>
            </el-timeline-item>
          </el-timeline>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <!-- 违禁内容详情对话框 -->
    <el-dialog
      v-model="contentDetailDialogVisible"
      title="违禁内容详情"
      width="800px"
    >
      <div class="content-detail">
        <h3>{{ selectedContent.title }}</h3>
        <div class="content-meta">
          <span>用户: {{ selectedContent.username }}</span>
          <span>时间: {{ selectedContent.createTime }}</span>
          <span>拒绝原因: {{ getRejectReasonText(selectedContent.rejectReason) }}</span>
        </div>
        <div class="content-body" v-html="selectedContent.content"></div>
        
        <div class="reject-info">
          <h4>审核信息</h4>
          <div>审核员: {{ selectedContent.reviewer }}</div>
          <div>审核时间: {{ selectedContent.rejectTime }}</div>
          <div>审核备注: {{ selectedContent.reviewComment }}</div>
        </div>
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
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import adminApi from '@/api/admin'

// 活动选项卡
const activeTab = ref('abnormalLogin')

// 搜索表单 - 异常登录
const searchForm = ref({
  dateRange: [new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()],
  username: ''
})

// 搜索表单 - 违禁内容
const contentSearchForm = ref({
  dateRange: [new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()],
  username: '',
  reason: ''
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 异常登录数据
const abnormalLogins = ref([])
const totalAbnormalLogins = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 违禁内容数据
const prohibitedContents = ref([])
const totalProhibitedContents = ref(0)
const contentLoading = ref(false)
const contentCurrentPage = ref(1)
const contentPageSize = ref(10)

// 详情对话框
const loginDetailDialogVisible = ref(false)
const contentDetailDialogVisible = ref(false)
const selectedDetail = ref({})
const selectedContent = ref({})

// 封禁对话框
const banDialogVisible = ref(false)
const banLoading = ref(false)
const banForm = reactive({
  reason: '',
  username: '',
  source: '' // 'login' 或 'content'
})

// 计算分页后的异常登录数据
const paginatedAbnormalLogins = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return abnormalLogins.value.slice(startIndex, endIndex);
});

// 计算分页后的违禁内容数据
const paginatedProhibitedContents = computed(() => {
  const startIndex = (contentCurrentPage.value - 1) * contentPageSize.value;
  const endIndex = startIndex + contentPageSize.value;
  return prohibitedContents.value.slice(startIndex, endIndex);
});

// 获取异常登录记录
const fetchAbnormalLogins = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAnomalyLogins({
      username: searchForm.value.username || undefined,
      startDate: searchForm.value.dateRange[0] ? new Date(searchForm.value.dateRange[0]).toISOString() : undefined,
      endDate: searchForm.value.dateRange[1] ? new Date(searchForm.value.dateRange[1]).toISOString() : undefined
    })
    
    if (res.code === 200 && res.data) {
      abnormalLogins.value = res.data.list || []
      totalAbnormalLogins.value = abnormalLogins.value.length // 使用数组长度作为总数
    } else {
      ElMessage.error(res.message || '获取异常登录记录失败')
      abnormalLogins.value = []
      totalAbnormalLogins.value = 0
    }
  } catch (error) {
    console.error('获取异常登录记录失败:', error)
    ElMessage.error('获取异常登录记录失败')
    abnormalLogins.value = []
    totalAbnormalLogins.value = 0
  } finally {
    loading.value = false
  }
}

// 获取违禁内容
const fetchProhibitedContents = async () => {
  contentLoading.value = true
  try {
    const res = await adminApi.getAnomalyContents({
      username: contentSearchForm.value.username || undefined,
      reason: contentSearchForm.value.reason || undefined,
      startDate: contentSearchForm.value.dateRange[0] ? new Date(contentSearchForm.value.dateRange[0]).toISOString() : undefined,
      endDate: contentSearchForm.value.dateRange[1] ? new Date(contentSearchForm.value.dateRange[1]).toISOString() : undefined
    })
    
    if (res.code === 200 && res.data) {
      prohibitedContents.value = res.data.list || []
      totalProhibitedContents.value = prohibitedContents.value.length // 使用数组长度作为总数
    } else {
      ElMessage.error(res.message || '获取违禁内容记录失败')
      prohibitedContents.value = []
      totalProhibitedContents.value = 0
    }
  } catch (error) {
    console.error('获取违禁内容记录失败:', error)
    ElMessage.error('获取违禁内容记录失败')
    prohibitedContents.value = []
    totalProhibitedContents.value = 0
  } finally {
    contentLoading.value = false
  }
}

// 搜索异常登录
const searchAbnormalLogins = () => {
  currentPage.value = 1
  fetchAbnormalLogins()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value.username = ''
  searchForm.value.dateRange = [new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()]
  searchAbnormalLogins()
}

// 搜索违禁内容
const searchProhibitedContent = () => {
  contentCurrentPage.value = 1
  fetchProhibitedContents()
}

// 重置内容搜索
const resetContentSearch = () => {
  contentSearchForm.value.username = ''
  contentSearchForm.value.reason = ''
  contentSearchForm.value.dateRange = [new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()]
  searchProhibitedContent()
}

// 查看详情
const viewDetail = (item, type) => {
  if (type === 'login') {
    selectedDetail.value = { ...item }
    loginDetailDialogVisible.value = true
  } else if (type === 'content') {
    selectedContent.value = { ...item }
    contentDetailDialogVisible.value = true
  }
}

// 封禁账号
const banAccount = (item, source = 'login') => {
  banForm.username = item.username
  banForm.reason = source === 'login' 
    ? `检测到账号异常登录行为，于${item.time}在${item.location}有${item.attemptCount}次失败尝试` 
    : `发布违禁内容，内容类型：${getRejectReasonText(item.rejectReason)}`
  banForm.source = source
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
    if (banForm.source === 'login') {
      const account = abnormalLogins.value.find(item => item.username === banForm.username)
      if (account) {
        account.status = 'banned'
      }
    } else {
      const content = prohibitedContents.value.find(item => item.username === banForm.username)
      if (content) {
        content.userStatus = 'banned'
      }
    }
    
    banDialogVisible.value = false
    ElMessage.success('账号已封禁')
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    banLoading.value = false
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

// 获取拒绝原因类型
const getRejectReasonType = (reason) => {
  const reasonMap = {
    political: 'danger',
    pornography: 'warning',
    violence: 'danger',
    fake_news: 'warning',
    privacy: 'info',
    other: 'info'
  }
  return reasonMap[reason] || 'info'
}

// 获取拒绝原因文本
const getRejectReasonText = (reason) => {
  const reasonMap = {
    political: '政治敏感',
    pornography: '色情低俗',
    violence: '暴力恐怖',
    fake_news: '不实信息',
    privacy: '侵犯隐私',
    other: '其他'
  }
  return reasonMap[reason] || '未知'
}

// 分页处理 - 异常登录（更新为客户端分页）
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置为第一页
  // 无需重新获取数据，因为我们使用计算属性处理分页
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  // 无需重新获取数据，因为我们使用计算属性处理分页
}

// 分页处理 - 违禁内容（更新为客户端分页）
const handleContentSizeChange = (size) => {
  contentPageSize.value = size
  contentCurrentPage.value = 1 // 重置为第一页
  // 无需重新获取数据，因为我们使用计算属性处理分页
}

const handleContentCurrentChange = (page) => {
  contentCurrentPage.value = page
  // 无需重新获取数据，因为我们使用计算属性处理分页
}

// 监听标签切换
watch(activeTab, (newVal) => {
  if (newVal === 'abnormalLogin') {
    fetchAbnormalLogins()
  } else if (newVal === 'prohibitedContent') {
    fetchProhibitedContents()
  }
})

// 初始化
onMounted(() => {
  fetchAbnormalLogins()
})
</script>

<style scoped>
.anomaly-detection {
  padding: 16px;
}

.detection-tabs {
  background-color: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.small-text {
  font-size: 12px;
  color: #606266;
  margin: 0;
}

.content-detail {
  padding: 16px;
}

.content-detail h3 {
  margin-top: 0;
  margin-bottom: 16px;
}

.content-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #606266;
}

.content-body {
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 24px;
  min-height: 200px;
}

.reject-info {
  padding: 16px;
  background-color: #fef0f0;
  border-radius: 4px;
  color: #f56c6c;
}

.reject-info h4 {
  margin-top: 0;
  margin-bottom: 8px;
}

.reject-info div {
  margin-bottom: 8px;
}
</style>