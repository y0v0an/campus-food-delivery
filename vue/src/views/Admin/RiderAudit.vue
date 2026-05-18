<template>
  <div class="rider-audit-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">骑手审核</h1>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeStatus" @change="filterByStatus">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">
          待审核 ({{ getStatusCount('pending') }})
        </el-radio-button>
        <el-radio-button label="approved">已通过</el-radio-button>
        <el-radio-button label="rejected">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 申请列表 -->
    <div class="application-list">
      <el-table :data="filteredApplications" stripe>
        <el-table-column prop="studentName" label="申请人" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column label="可配送时间">
          <template #default="{ row }">
            <el-tag
              v-for="time in row.availableTime"
              :key="time"
              size="small"
              style="margin-right: 4px; margin-bottom: 4px"
            >
              {{ time }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="success" link @click="approveApplication(row)">
                通过
              </el-button>
              <el-button type="danger" link @click="rejectApplication(row)">
                拒绝
              </el-button>
            </template>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredApplications.length === 0" description="暂无申请记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/axios/request'

// 申请列表
const applications = ref([])

// 当前筛选状态
const activeStatus = ref('')

// 过滤后的申请
const filteredApplications = computed(() => {
  if (!activeStatus.value) return applications.value
  return applications.value.filter(a => a.status === activeStatus.value)
})

// 获取状态数量
const getStatusCount = (status) => {
  return applications.value.filter(a => a.status === status).length
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return texts[status] || status
}

// 获取状态标签类型
const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || ''
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 解析可配送时间
const parseAvailableTime = (timeStr) => {
  if (!timeStr) return []
  try {
    return JSON.parse(timeStr)
  } catch (e) {
    return [timeStr]
  }
}

// 加载申请
const loadApplications = async () => {
  try {
    const result = await request.get('/rider/applications', { params: { status: 'all' } })
    applications.value = (result || []).map(app => ({
      ...app,
      availableTime: parseAvailableTime(app.availableTime)
    }))
  } catch (error) {
    console.error('加载申请列表失败:', error)
  }
}

// 按状态筛选
const filterByStatus = () => {
  // 已通过 computed 实现
}

// 通过申请
const approveApplication = async (application) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过「${application.studentName}」的骑手申请吗？`,
      '审核确认',
      {
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await request.put(`/rider/approve/${application.id}`)
    ElMessage.success('申请已通过')
    loadApplications()
  } catch (e) {
    // 用户取消或操作失败
  }
}

// 拒绝申请
const rejectApplication = async (application) => {
  try {
    await ElMessageBox.confirm(
      `确定要拒绝「${application.studentName}」的骑手申请吗？`,
      '审核确认',
      {
        confirmButtonText: '拒绝',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await request.put(`/rider/reject/${application.id}`)
    ElMessage.success('申请已拒绝')
    loadApplications()
  } catch (e) {
    // 用户取消或操作失败
  }
}

onMounted(() => {
  loadApplications()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.rider-audit-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;

    .page-title {
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }
  }

  .filter-bar {
    margin-bottom: $spacing-lg;
  }

  .application-list {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-base;

    .no-action {
      color: $color-text-placeholder;
    }
  }
}
</style>
