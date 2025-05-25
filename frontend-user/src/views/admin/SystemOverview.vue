<template>
  <div class="system-overview">
    <el-card class="summary-card">
      <template #header>
        <div class="card-header">
          <h2>系统概况</h2>
          <el-button type="primary" @click="refreshData" :loading="loading">刷新数据</el-button>
        </div>
      </template>
      
      <div class="summary-statistics">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
            <div class="stat-card primary">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </div>
          </el-col>
          
          <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
            <div class="stat-card success">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ overview.totalBlogs || 0 }}</div>
                <div class="stat-label">总博客数</div>
              </div>
            </div>
          </el-col>
          
          <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
            <div class="stat-card warning">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ overview.todayAbnomalLogins || 0 }}</div>
                <div class="stat-label">今日异常登录</div>
              </div>
            </div>
          </el-col>
          
          <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
            <div class="stat-card danger">
              <div class="stat-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ overview.pendingReviews || 0 }}</div>
                <div class="stat-label">待审核内容</div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="8">
            <div class="stat-card info">
              <div class="stat-content">
                <div class="stat-value">{{ overview.todayNewUsers || 0 }}</div>
                <div class="stat-label">今日新增用户</div>
              </div>
            </div>
          </el-col>
          
          <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="8">
            <div class="stat-card info">
              <div class="stat-content">
                <div class="stat-value">{{ overview.todayActiveUsers || 0 }}</div>
                <div class="stat-label">今日活跃用户</div>
              </div>
            </div>
          </el-col>
          
          <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="8">
            <div class="stat-card info">
              <div class="stat-content">
                <div class="stat-value">{{ overview.todayNewBlogs || 0 }}</div>
                <div class="stat-label">今日新增博客</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>用户活跃度统计</h3>
              <el-radio-group v-model="userPeriod" @change="fetchUserStats">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
                <el-radio-button label="year">年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" v-loading="userChartLoading">
            <div ref="userActivityChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>内容发布统计</h3>
              <el-radio-group v-model="contentPeriod" @change="fetchContentStats">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
                <el-radio-button label="year">年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" v-loading="contentChartLoading">
            <div ref="contentActivityChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue';
import { User, Document, Warning, ChatDotRound } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// 状态定义
const loading = ref(false);
const userChartLoading = ref(false);
const contentChartLoading = ref(false);
const userPeriod = ref('week');
const contentPeriod = ref('week');
const userActivityChart = ref(null);
const contentActivityChart = ref(null);
let userChart = null;
let contentChart = null;

const overview = reactive({
  totalUsers: 0,
  totalBlogs: 0,
  totalSensitiveWords: 0,
  todayNewUsers: 0,
  todayNewBlogs: 0,
  todayActiveUsers: 0,
  todayAbnomalLogins: 0,
  pendingReviews: 0
});

const userStats = reactive({
  activeUsers: [],
  newUsers: []
});

const contentStats = reactive({
  newBlogs: [],
  publishedBlogs: []
});

// 获取概览数据
const fetchOverviewData = async () => {
  loading.value = true;
  try {
    const res = await adminApi.getStatsOverview();
    
    if (res.code === 200 && res.data) {
      Object.assign(overview, res.data);
    } else {
      ElMessage.error(res.message || '获取系统概览失败');
    }
  } catch (error) {
    console.error('获取系统概览失败:', error);
    ElMessage.error('获取系统概览失败');
  } finally {
    loading.value = false;
  }
};

// 获取用户统计数据
const fetchUserStats = async () => {
  userChartLoading.value = true;
  try {
    const res = await adminApi.getUsersActive(userPeriod.value);
    
    if (res.code === 200 && res.data) {
      userStats.activeUsers = res.data.activeUsers || [];
      userStats.newUsers = res.data.newUsers || [];
      renderUserChart();
    } else {
      ElMessage.error(res.message || '获取用户活跃度统计失败');
    }
  } catch (error) {
    console.error('获取用户活跃度统计失败:', error);
    ElMessage.error('获取用户活跃度统计失败');
  } finally {
    userChartLoading.value = false;
  }
};

// 获取内容统计数据
const fetchContentStats = async () => {
  contentChartLoading.value = true;
  try {
    const res = await adminApi.getContentsStats(contentPeriod.value);
    
    if (res.code === 200 && res.data) {
      contentStats.newBlogs = res.data.newBlogs || [];
      contentStats.publishedBlogs = res.data.publishedBlogs || [];
      renderContentChart();
    } else {
      ElMessage.error(res.message || '获取内容发布统计失败');
    }
  } catch (error) {
    console.error('获取内容发布统计失败:', error);
    ElMessage.error('获取内容发布统计失败');
  } finally {
    contentChartLoading.value = false;
  }
};

// 渲染用户活跃度图表
const renderUserChart = () => {
  if (!userActivityChart.value) return;
  
  if (!userChart && window.echarts) {
    userChart = window.echarts.init(userActivityChart.value);
  } else if (!window.echarts) {
    ElMessage.warning('ECharts 加载失败，请刷新页面重试');
    return;
  }

  // 准备数据
  const times = [];
  const activeUserData = [];
  const newUserData = [];
  
  // 对数据进行排序，确保按时间顺序显示
  const sortedActiveUsers = [...userStats.activeUsers].sort((a, b) => new Date(a.time) - new Date(b.time));
  const sortedNewUsers = [...userStats.newUsers].sort((a, b) => new Date(a.time) - new Date(b.time));
  
  sortedActiveUsers.forEach(item => {
    times.push(item.time);
    activeUserData.push(item.activeUserCount);
  });
  
  sortedNewUsers.forEach(item => {
    // 只添加新用户数据，因为时间轴已经添加过了
    const index = times.indexOf(item.time);
    if (index !== -1) {
      newUserData[index] = item.newUserCount;
    } else {
      times.push(item.time);
      // 填充可能缺失的活跃用户数据
      activeUserData.push(0);
      newUserData.push(item.newUserCount);
    }
  });
  
  // 设置图表选项
  const option = {
    title: {
      text: '用户活跃度统计'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['活跃用户', '新增用户']
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '活跃用户',
        type: 'bar',
        data: activeUserData,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '新增用户',
        type: 'bar',
        data: newUserData,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  };
  
  // 设置图表
  userChart.setOption(option);
};

// 渲染内容发布统计图表
const renderContentChart = () => {
  if (!contentActivityChart.value) return;
  
  if (!contentChart && window.echarts) {
    contentChart = window.echarts.init(contentActivityChart.value);
  } else if (!window.echarts) {
    ElMessage.warning('ECharts 加载失败，请刷新页面重试');
    return;
  }

  // 准备数据
  const times = [];
  const newBlogData = [];
  const publishedBlogData = [];
  
  // 对数据进行排序，确保按时间顺序显示
  const sortedNewBlogs = [...contentStats.newBlogs].sort((a, b) => new Date(a.time) - new Date(b.time));
  const sortedPublishedBlogs = [...contentStats.publishedBlogs].sort((a, b) => new Date(a.time) - new Date(b.time));
  
  sortedNewBlogs.forEach(item => {
    times.push(item.time);
    newBlogData.push(item.newBlogCount);
  });
  
  sortedPublishedBlogs.forEach(item => {
    const index = times.indexOf(item.time);
    if (index !== -1) {
      publishedBlogData[index] = item.publishedBlogCount;
    } else {
      times.push(item.time);
      newBlogData.push(0); // 填充可能缺失的新博客数据
      publishedBlogData.push(item.publishedBlogCount);
    }
  });
  
  // 设置图表选项
  const option = {
    title: {
      text: '内容发布统计'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['新增博客', '已发布博客']
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增博客',
        type: 'line',
        data: newBlogData,
        itemStyle: {
          color: '#E6A23C'
        }
      },
      {
        name: '已发布博客',
        type: 'line',
        data: publishedBlogData,
        itemStyle: {
          color: '#F56C6C'
        }
      }
    ]
  };
  
  // 设置图表
  contentChart.setOption(option);
};

// 刷新数据
const refreshData = () => {
  fetchOverviewData();
  fetchUserStats();
  fetchContentStats();
};

// 处理窗口大小变化
const handleResize = () => {
  if (userChart) {
    userChart.resize();
  }
  if (contentChart) {
    contentChart.resize();
  }
};

// 在组件挂载时获取数据
onMounted(() => {
  // 添加 CDN 脚本
  const script = document.createElement('script');
  script.src = 'https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js';
  script.async = true;
  script.onload = () => {
    fetchOverviewData();
    fetchUserStats();
    fetchContentStats();
  };
  script.onerror = () => {
    ElMessage.error('加载图表库失败，部分功能可能无法正常工作');
    fetchOverviewData(); // 仍然获取基础数据
  };
  document.head.appendChild(script);
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
});

// 在组件卸载前清理事件监听和图表实例
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  
  if (userChart) {
    userChart.dispose();
    userChart = null;
  }
  
  if (contentChart) {
    contentChart.dispose();
    contentChart = null;
  }
});
</script>

<style scoped>
.system-overview {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
}

.summary-statistics {
  margin-top: 20px;
}

.stat-card {
  display: flex;
  padding: 16px;
  border-radius: 8px;
  color: white;
  height: 100px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 16px;
  display: flex;
  align-items: center;
}

.stat-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

.primary {
  background-color: var(--primary-color, #409EFF);
}

.success {
  background-color: var(--success-color, #67C23A);
}

.warning {
  background-color: var(--warning-color, #E6A23C);
}

.danger {
  background-color: var(--danger-color, #F56C6C);
}

.info {
  background-color: var(--info-color, #909399);
  text-align: center;
  height: 80px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 350px;
  width: 100%;
}

.chart {
  height: 100%;
  width: 100%;
}

@media screen and (max-width: 768px) {
  .stat-card {
    height: 80px;
    margin-bottom: 10px;
  }
  
  .stat-icon {
    font-size: 30px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>
