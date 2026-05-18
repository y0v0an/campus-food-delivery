<template>
  <div class="user-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户"
          :prefix-icon="Search"
          clearable
          style="width: 250px; margin-right: 12px"
          @input="handleSearch"
        />
        <el-button type="primary" :icon="Plus" @click="openAddDialog">新增用户</el-button>
      </div>
    </div>

    <!-- 用户类型筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeType" @change="filterByType">
        <el-radio-button label="">全部 ({{ users.length }})</el-radio-button>
        <el-radio-button label="student">学生 ({{ getTypeCount('student') }})</el-radio-button>
        <el-radio-button label="merchant">商家 ({{ getTypeCount('merchant') }})</el-radio-button>
        <el-radio-button label="rider">骑手 ({{ getTypeCount('rider') }})</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 用户列表 -->
    <div class="user-list">
      <el-table :data="filteredUsers" stripe>
        <el-table-column label="头像" width="88">
          <template #default="{ row }">
            <div class="user-avatar-cell">
              <img
                v-if="avatarUrl(row)"
                :src="avatarUrl(row)"
                class="user-avatar-img"
                alt=""
              />
              <span v-else class="user-avatar-fallback">{{ row.name?.charAt(0) || '?' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" />
        <el-table-column label="账号">
          <template #default="{ row }">
            {{ row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(displayRole(row))" size="small">
              {{ getTypeName(displayRole(row)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="isDisabledEffective(row) ? 'danger' : 'success'" size="small">
              {{ isDisabledEffective(row) ? '已禁用' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!isDisabledEffective(row)"
              type="danger"
              link
              @click="disableUser(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              @click="enableUser(row)"
            >
              启用
            </el-button>
            <el-button type="primary" link @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增用户弹窗 -->
    <el-dialog v-model="addDialogVisible" title="新增用户" width="500px">
      <el-form ref="formRef" :model="userForm" :rules="formRules" label-width="100px">
        <el-form-item label="用户类型" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择用户类型">
            <el-option label="学生" value="student" />
            <el-option label="骑手" value="rider" />
            <el-option label="商家" value="merchant" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入登录密码" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <el-descriptions :column="1" border v-if="currentUser">
        <el-descriptions-item label="头像">
          <div class="user-avatar-cell user-avatar-cell--lg">
            <img
              v-if="avatarUrl(currentUser)"
              :src="avatarUrl(currentUser)"
              class="user-avatar-img"
              alt=""
            />
            <span v-else class="user-avatar-fallback">{{ currentUser.name?.charAt(0) || '?' }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentUser.name }}</el-descriptions-item>
        <el-descriptions-item label="账号">
          {{ currentUser.username }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">
          {{ getTypeName(displayRole(currentUser)) }}
        </el-descriptions-item>
        <el-descriptions-item label="骑手资格">
          {{ isRiderEffective(currentUser) ? '是（可使用骑手端）' : '否' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="isDisabledEffective(currentUser) ? 'danger' : 'success'" size="small">
            {{ isDisabledEffective(currentUser) ? '已禁用' : '正常' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatTime(currentUser.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'

// 用户列表
const users = ref([])

const avatarUrl = (u) => {
  const raw = u?.avatar ?? u?.avatar_url ?? ''
  const url = getImageUrl(raw)
  return url || undefined
}

/** 与后端 tinyint / 字符串兼容，避免 "0" 被当成 true */
const isRiderEffective = (u) => {
  if (!u) return false
  const r = u.isRider ?? u.is_rider
  if (r === true || r === 1) return true
  if (typeof r === 'string') {
    const t = r.trim().toLowerCase()
    return t === '1' || t === 'true'
  }
  return false
}

const isDisabledEffective = (u) => {
  if (!u) return false
  const d = u.isDisabled ?? u.is_disabled
  if (d === true || d === 1) return true
  if (typeof d === 'string') {
    const t = d.trim().toLowerCase()
    return t === '1' || t === 'true'
  }
  return false
}

/**
 * 列表标签：商家/管理员按 role；学生账号里 is_rider=1 的显示为骑手，否则为学生
 * 筛选「学生」= 所有 role=student（含同时具备骑手身份的用户）；「骑手」= is_rider 为真的学生
 */
const displayRole = (u) => {
  if (!u) return ''
  if (u.role === 'merchant' || u.role === 'admin') return u.role
  if (u.role === 'rider') return 'rider'
  if (u.role === 'student') return isRiderEffective(u) ? 'rider' : 'student'
  return 'student'
}

const isStudentRole = (u) => u?.role === 'student' || u?.role === 'rider'

// 搜索关键词
const searchKeyword = ref('')

// 当前筛选类型
const activeType = ref('')

// 详情弹窗
const detailVisible = ref(false)
const currentUser = ref(null)

// 新增用户弹窗
const addDialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const userForm = ref({
  role: 'student',
  name: '',
  username: '',
  password: '123456',
  phone: ''
})

const formRules = {
  role: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

// 过滤后的用户
const filteredUsers = computed(() => {
  let result = users.value

  if (activeType.value) {
    if (activeType.value === 'student') {
      result = result.filter((u) => u.role === 'student')
    } else if (activeType.value === 'rider') {
      result = result.filter((u) => isRiderEffective(u) && isStudentRole(u))
    } else {
      result = result.filter((u) => u.role === activeType.value)
    }
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(u =>
      u.name?.toLowerCase().includes(keyword) ||
      u.phone?.includes(keyword) ||
      u.username?.includes(keyword)
    )
  }

  return result
})

const getTypeCount = (type) => {
  if (type === 'student') {
    return users.value.filter((u) => u.role === 'student').length
  }
  if (type === 'rider') {
    return users.value.filter((u) => isRiderEffective(u) && isStudentRole(u)).length
  }
  return users.value.filter((u) => u.role === type).length
}

// 获取类型名称
const getTypeName = (type) => {
  const names = {
    student: '学生',
    merchant: '商家',
    rider: '骑手',
    admin: '管理员'
  }
  return names[type] || type
}

// 获取类型标签样式
const getTypeTag = (type) => {
  const tags = {
    student: 'primary',
    merchant: 'warning',
    rider: 'success',
    admin: 'danger'
  }
  return tags[type] || ''
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 加载用户（统一驼峰，避免后端/序列化差异）
const loadUsers = async () => {
  try {
    const result = await request.get('/user/list')
    users.value = (result || []).map((raw) => ({
      ...raw,
      isRider: raw.isRider ?? raw.is_rider,
      isDisabled: raw.isDisabled ?? raw.is_disabled,
      avatar: raw.avatar ?? raw.avatar_url ?? ''
    }))
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  // 已通过 computed 实现
}

// 按类型筛选
const filterByType = () => {
  // 已通过 computed 实现
}

// 打开新增弹窗
const openAddDialog = () => {
  userForm.value = {
    role: 'student',
    name: '',
    username: '',
    password: '123456',
    phone: ''
  }
  addDialogVisible.value = true
}

// 保存用户
const saveUser = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await request.post('/user/add', userForm.value)
      ElMessage.success('用户添加成功')
      addDialogVisible.value = false
      loadUsers()
    } catch (error) {
      ElMessage.error(error.message || '添加失败')
    } finally {
      submitting.value = false
    }
  })
}

// 禁用用户
const disableUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要禁用用户「${user.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.put(`/user/disable/${user.id}`)
    ElMessage.success('用户已禁用')
    loadUsers()
  } catch (e) {
    // 用户取消或操作失败
  }
}

// 启用用户
const enableUser = async (user) => {
  try {
    await request.put(`/user/enable/${user.id}`)
    ElMessage.success('用户已启用')
    loadUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看详情
const viewDetail = (user) => {
  currentUser.value = user
  detailVisible.value = true
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.user-manage {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;

    .page-title {
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }

    .header-actions {
      display: flex;
      align-items: center;
    }
  }

  .filter-bar {
    margin-bottom: $spacing-lg;
  }

  .user-list {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-base;
  }
}

.user-avatar-cell {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ebeef5;
  vertical-align: middle;
}

.user-avatar-cell--lg {
  width: 56px;
  height: 56px;
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.user-avatar-fallback {
  font-size: 15px;
  font-weight: 600;
  color: #606266;
}

.user-avatar-cell--lg .user-avatar-fallback {
  font-size: 20px;
}
</style>
