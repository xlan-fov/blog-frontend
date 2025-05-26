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
import * as echarts from 'echarts';
import adminApi from '@/api/admin';

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
  
  if (!userChart) {
    userChart = echarts.init(userActivityChart.value);
  }

  // 准备数据
  let times = [];
  let activeUserData = [];
  let newUserData = [];
  
  // 对数据进行排序，确保按时间顺序显示
  const sortedActiveUsers = [...userStats.activeUsers].sort((a, b) => new Date(a.time) - new Date(b.time));
  const sortedNewUsers = [...userStats.newUsers].sort((a, b) => new Date(a.time) - new Date(b.time));
  
  // 使用一个临时Map来存储日期和数据的映射，用于后续排序
  const dateMap = new Map();
  
  sortedActiveUsers.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, userPeriod.value);
    // 使用原始日期作为key来确保后续可以正确排序
    const originalDate = new Date(item.time);
    
    if (!dateMap.has(displayTime)) {
      dateMap.set(displayTime, {
        originalDate,
        displayTime,
        activeUserCount: item.activeUserCount,
        newUserCount: 0
      });
    } else {
      const data = dateMap.get(displayTime);
      data.activeUserCount = item.activeUserCount;
    }
  });
  
  sortedNewUsers.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, userPeriod.value);
    const originalDate = new Date(item.time);
    
    if (!dateMap.has(displayTime)) {
      dateMap.set(displayTime, {
        originalDate,
        displayTime,
        activeUserCount: 0,
        newUserCount: item.newUserCount
      });
    } else {
      const data = dateMap.get(displayTime);
      data.newUserCount = item.newUserCount;
    }
  });
  
  // 将Map转为数组并按原始日期排序
  const sortedData = Array.from(dateMap.values()).sort((a, b) => a.originalDate - b.originalDate);
  
  // 提取排序后的数据到对应数组
  times = sortedData.map(item => item.displayTime);
  activeUserData = sortedData.map(item => item.activeUserCount);
  newUserData = sortedData.map(item => item.newUserCount);
  
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
      // 将图例移到右上角
      right: '10%',
      top: '4%',
      data: ['活跃用户', '新增用户']
    },
    grid: {
      // 调整网格布局，为下方标签提供足够空间
      left: '3%',
      right: '4%',
      bottom: '20%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: {
        interval: 0,  // 显示全部标签
        rotate: 0,    // 水平显示，不旋转
        fontSize: 10  // 字体稍微小一些
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
  
  if (!contentChart) {
    contentChart = echarts.init(contentActivityChart.value);
  }

  // 准备数据
  let times = [];
  let newBlogData = [];
  let publishedBlogData = [];
  
  // 对数据进行排序，确保按时间顺序显示
  const sortedNewBlogs = [...contentStats.newBlogs].sort((a, b) => new Date(a.time) - new Date(b.time));
  const sortedPublishedBlogs = [...contentStats.publishedBlogs].sort((a, b) => new Date(a.time) - new Date(b.time));
  
  // 使用一个临时Map来存储日期和数据的映射，用于后续排序
  const dateMap = new Map();
  
  sortedNewBlogs.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, contentPeriod.value);
    const originalDate = new Date(item.time);
    
    if (!dateMap.has(displayTime)) {
      dateMap.set(displayTime, {
        originalDate,
        displayTime,
        newBlogCount: item.newBlogCount,
        publishedBlogCount: 0
      });
    } else {
      const data = dateMap.get(displayTime);
      data.newBlogCount = item.newBlogCount;
    }
  });
  
  sortedPublishedBlogs.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, contentPeriod.value);
    const originalDate = new Date(item.time);
    
    if (!dateMap.has(displayTime)) {
      dateMap.set(displayTime, {
        originalDate,
        displayTime,
        newBlogCount: 0,
        publishedBlogCount: item.publishedBlogCount
      });
    } else {
      const data = dateMap.get(displayTime);
      data.publishedBlogCount = item.publishedBlogCount;
    }
  });
  
  // 将Map转为数组并按原始日期排序
  const sortedData = Array.from(dateMap.values()).sort((a, b) => a.originalDate - b.originalDate);
  
  // 提取排序后的数据到对应数组
  times = sortedData.map(item => item.displayTime);
  newBlogData = sortedData.map(item => item.newBlogCount);
  publishedBlogData = sortedData.map(item => item.publishedBlogCount);
  
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
      // 将图例移到右上角
      right: '10%',
      top: '4%',
      data: ['新增博客', '已发布博客']
    },
    grid: {
      // 调整网格布局，为下方标签提供足够空间
      left: '3%',
      right: '4%',
      bottom: '20%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: {
        interval: 0,  // 显示全部标签
        rotate: 0,    // 水平显示，不旋转
        fontSize: 10  // 字体稍微小一些
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

// 根据选择的时间周期格式化日期显示
const formatTimeByPeriod = (timeStr, period) => {
  if (!timeStr) return '';
  
  try {
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) return timeStr; // 无效日期直接返回原字符串
    
    // 对于日统计只显示月-日
    if (period === 'day') {
      return `${date.getMonth() + 1}-${date.getDate()}`; // 月份从0开始，所以+1
    }
    
    // 其他周期保持原样
    return timeStr;
  } catch (error) {
    console.error('日期格式化错误:', error);
    return timeStr;
  }
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
  fetchOverviewData();
  fetchUserStats();
  fetchContentStats();
  
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
