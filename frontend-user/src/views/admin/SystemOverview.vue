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

// 根据选择的时间周期格式化日期显示
const formatTimeByPeriod = (timeStr, period) => {
  if (!timeStr) return '';
  
  try {
    // 处理可能包含日期范围的情况，如"2023-01-01 to 2023-01-07"
    let originalDate = timeStr;
    if (timeStr.includes(' to ')) {
      originalDate = timeStr.split(' to ')[0];
    }
    
    const date = new Date(originalDate);
    if (isNaN(date.getTime())) return timeStr; // 无效日期直接返回原字符串
    
    // 根据不同周期返回适当的格式
    switch (period) {
      case 'day':
        return `${date.getMonth() + 1}-${date.getDate()}`; // 月份从0开始，所以+1
      case 'week':
        // 对于周，返回原始字符串，排序已通过originalDate处理
        return timeStr;
      case 'month':
        // 对于月，返回原始字符串，排序已通过originalDate处理
        return timeStr;
      case 'year':
        // 对于年，返回原始字符串，排序已通过originalDate处理
        return timeStr;
      default:
        return timeStr;
    }
  } catch (error) {
    console.error('日期格式化错误:', error);
    return timeStr;
  }
};

// 解析日期字符串，处理各种格式以获取有效的日期对象
const parseTimeString = (timeStr) => {
  if (!timeStr) return null;
  
  try {
    // 如果是日期范围（格式如"2023-01-01 to 2023-01-07"），取第一个日期
    if (timeStr.includes(' to ')) {
      timeStr = timeStr.split(' to ')[0];
    }
    
    // 处理年份格式（如"2023"）
    if (/^\d{4}$/.test(timeStr)) {
      return new Date(parseInt(timeStr), 0, 1); // 以该年的1月1日作为日期
    }
    
    // 处理月份格式（如"2023-01"）
    if (/^\d{4}-\d{2}$/.test(timeStr)) {
      const [year, month] = timeStr.split('-');
      return new Date(parseInt(year), parseInt(month) - 1, 1); // 以该月的1日作为日期
    }
    
    // 其他格式，尝试标准解析
    return new Date(timeStr);
  } catch (error) {
    console.error('日期解析错误:', error);
    return null;
  }
};

// 渲染用户活跃度图表
const renderUserChart = () => {
  if (!userActivityChart.value) return;
  
  if (!userChart) {
    userChart = echarts.init(userActivityChart.value);
  }

  // 使用一个临时Map来存储日期和数据的映射
  const dateMap = new Map();
  
  // 处理活跃用户数据
  userStats.activeUsers.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, userPeriod.value);
    const originalDate = parseTimeString(item.time);
    
    if (!originalDate) return; // 跳过无效日期
    
    const sortKey = originalDate.getTime(); // 使用时间戳作为排序键
    
    if (!dateMap.has(sortKey)) {
      dateMap.set(sortKey, {
        sortKey,
        displayTime,
        activeUserCount: item.activeUserCount || 0,
        newUserCount: 0
      });
    } else {
      const existing = dateMap.get(sortKey);
      existing.activeUserCount = item.activeUserCount || 0;
    }
  });
  
  // 处理新用户数据
  userStats.newUsers.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, userPeriod.value);
    const originalDate = parseTimeString(item.time);
    
    if (!originalDate) return; // 跳过无效日期
    
    const sortKey = originalDate.getTime(); // 使用时间戳作为排序键
    
    if (!dateMap.has(sortKey)) {
      dateMap.set(sortKey, {
        sortKey,
        displayTime,
        activeUserCount: 0,
        newUserCount: item.newUserCount || 0
      });
    } else {
      const existing = dateMap.get(sortKey);
      existing.newUserCount = item.newUserCount || 0;
    }
  });
  
  // 将Map转为数组并按时间顺序排序
  const sortedData = Array.from(dateMap.values()).sort((a, b) => a.sortKey - b.sortKey);
  
  // 提取排序后的数据
  const times = sortedData.map(item => item.displayTime);
  const activeUserData = sortedData.map(item => item.activeUserCount);
  const newUserData = sortedData.map(item => item.newUserCount);
  
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

  // 使用一个临时Map来存储日期和数据的映射
  const dateMap = new Map();
  
  // 处理新增博客数据
  contentStats.newBlogs.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, contentPeriod.value);
    const originalDate = parseTimeString(item.time);
    
    if (!originalDate) return; // 跳过无效日期
    
    const sortKey = originalDate.getTime(); // 使用时间戳作为排序键
    
    if (!dateMap.has(sortKey)) {
      dateMap.set(sortKey, {
        sortKey,
        displayTime,
        newBlogCount: item.newBlogCount || 0,
        publishedBlogCount: 0
      });
    } else {
      const existing = dateMap.get(sortKey);
      existing.newBlogCount = item.newBlogCount || 0;
    }
  });
  
  // 处理已发布博客数据
  contentStats.publishedBlogs.forEach(item => {
    const displayTime = formatTimeByPeriod(item.time, contentPeriod.value);
    const originalDate = parseTimeString(item.time);
    
    if (!originalDate) return; // 跳过无效日期
    
    const sortKey = originalDate.getTime(); // 使用时间戳作为排序键
    
    if (!dateMap.has(sortKey)) {
      dateMap.set(sortKey, {
        sortKey,
        displayTime,
        newBlogCount: 0,
        publishedBlogCount: item.publishedBlogCount || 0
      });
    } else {
      const existing = dateMap.get(sortKey);
      existing.publishedBlogCount = item.publishedBlogCount || 0;
    }
  });
  
  // 将Map转为数组并按时间顺序排序
  const sortedData = Array.from(dateMap.values()).sort((a, b) => a.sortKey - b.sortKey);
  
  // 提取排序后的数据
  const times = sortedData.map(item => item.displayTime);
  const newBlogData = sortedData.map(item => item.newBlogCount);
  const publishedBlogData = sortedData.map(item => item.publishedBlogCount);
  
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
