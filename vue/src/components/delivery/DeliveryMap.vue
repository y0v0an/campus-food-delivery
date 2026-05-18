<template>
  <div class="delivery-map">
    <!-- 模拟地图区域 -->
    <div class="map-container">
      <!-- 地图背景 -->
      <div class="map-background">
        <div class="grid-lines"></div>
      </div>

      <!-- 商家位置标记 -->
      <div
        class="marker merchant-marker"
        :style="getMarkerStyle(merchantLocation)"
        v-if="merchantLocation"
      >
        <div class="marker-icon">🏪</div>
        <div class="marker-label">商家</div>
      </div>

      <!-- 学生位置标记 -->
      <div
        class="marker student-marker"
        :style="getMarkerStyle(studentLocation)"
        v-if="studentLocation"
      >
        <div class="marker-icon">🏠</div>
        <div class="marker-label marker-label--dest">送达</div>
      </div>

      <!-- 骑手位置标记 -->
      <div
        class="marker rider-marker"
        :style="getMarkerStyle(riderLocation)"
        v-if="riderLocation"
      >
        <div class="marker-icon">🛵</div>
        <div class="marker-label">我</div>
      </div>

      <!-- 配送路线 -->
      <svg class="route-line" v-if="showRoute">
        <polyline
          :points="routePoints"
          fill="none"
          stroke="rgba(45, 45, 45, 0.45)"
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>

      <!-- 轨迹点 -->
      <div
        v-for="(point, index) in trackPoints"
        :key="index"
        class="track-point"
        :style="getMarkerStyle(point)"
      >
        <div class="point-dot"></div>
      </div>
    </div>

    <!-- 地图信息面板 -->
    <div class="map-info" v-if="showInfo">
      <div class="info-item" v-if="estimatedTime">
        <el-icon><Clock /></el-icon>
        <span>预计 {{ estimatedTime }} 分钟送达</span>
      </div>
      <div class="info-item" v-if="distance">
        <el-icon><Location /></el-icon>
        <span>距离约 {{ distance }} 米</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Clock, Location } from '@element-plus/icons-vue'

const props = defineProps({
  merchantLocation: {
    type: Object,
    default: null
  },
  studentLocation: {
    type: Object,
    default: null
  },
  riderLocation: {
    type: Object,
    default: null
  },
  trackPoints: {
    type: Array,
    default: () => []
  },
  estimatedTime: {
    type: Number,
    default: 0
  },
  showInfo: {
    type: Boolean,
    default: true
  }
})

// 地图边界（用于坐标转换）
const mapBounds = {
  minLat: 30.570,
  maxLat: 30.576,
  minLng: 104.064,
  maxLng: 104.070
}

// 将经纬度转换为地图上的像素位置
const getMarkerStyle = (location) => {
  if (!location) return { display: 'none' }
  
  const { lat, lng } = location
  const x = ((lng - mapBounds.minLng) / (mapBounds.maxLng - mapBounds.minLng)) * 100
  const y = ((mapBounds.maxLat - lat) / (mapBounds.maxLat - mapBounds.minLat)) * 100
  
  return {
    left: `${Math.max(5, Math.min(95, x))}%`,
    top: `${Math.max(5, Math.min(95, y))}%`
  }
}

// 是否显示路线
const showRoute = computed(() => {
  return props.merchantLocation && props.studentLocation
})

// 路线点
const routePoints = computed(() => {
  if (!showRoute.value) return ''
  
  const points = []
  
  // 商家位置
  const merchantStyle = getMarkerStyle(props.merchantLocation)
  points.push(`${parseFloat(merchantStyle.left)},${parseFloat(merchantStyle.top)}`)
  
  // 骑手位置（如果有）
  if (props.riderLocation) {
    const riderStyle = getMarkerStyle(props.riderLocation)
    points.push(`${parseFloat(riderStyle.left)},${parseFloat(riderStyle.top)}`)
  }
  
  // 学生位置
  const studentStyle = getMarkerStyle(props.studentLocation)
  points.push(`${parseFloat(studentStyle.left)},${parseFloat(studentStyle.top)}`)
  
  return points.join(' ')
})

// 计算距离（简化计算）
const distance = computed(() => {
  if (!props.riderLocation || !props.studentLocation) return 0
  
  const R = 6371000 // 地球半径（米）
  const lat1 = props.riderLocation.lat * Math.PI / 180
  const lat2 = props.studentLocation.lat * Math.PI / 180
  const deltaLat = (props.studentLocation.lat - props.riderLocation.lat) * Math.PI / 180
  const deltaLng = (props.studentLocation.lng - props.riderLocation.lng) * Math.PI / 180
  
  const a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(deltaLng/2) * Math.sin(deltaLng/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  
  return Math.round(R * c)
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.delivery-map {
  position: relative;
  width: 100%;
  border-radius: $border-radius-md;
  overflow: hidden;
  background: $color-bg-white;
  box-shadow: $shadow-sm;
  contain: layout style paint;
}

.map-container {
  position: relative;
  width: 100%;
  height: 220px;
  background: #e8eaed;
  overflow: hidden;
}

.map-background {
  position: absolute;
  inset: 0;

  .grid-lines {
    width: 100%;
    height: 100%;
    opacity: 0.35;
    background-image: linear-gradient(rgba(0, 0, 0, 0.06) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 0, 0, 0.06) 1px, transparent 1px);
    background-size: 24px 24px;
  }
}

.marker {
  position: absolute;
  transform: translate(-50%, -100%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;

  .marker-icon {
    font-size: 28px;
    filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.25));
  }

  .marker-label {
    margin-top: 4px;
    padding: 4px 8px;
    background: #ffffff;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    color: #303133;
    border: 1px solid rgba(0, 0, 0, 0.12);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
    white-space: nowrap;
    line-height: 1.2;
  }

  .marker-label--dest {
    color: #1d2129;
  }
}

.rider-marker {
  z-index: 20;
}

.route-line {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.track-point {
  position: absolute;
  transform: translate(-50%, -50%);
  z-index: 5;

  .point-dot {
    width: 8px;
    height: 8px;
    background: $color-primary;
    border-radius: 50%;
    border: 2px solid $color-bg-white;
    box-shadow: $shadow-sm;
  }
}

.map-info {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  padding: $spacing-sm $spacing-base;
  background: #f7f8fa;
  border-top: 1px solid $color-border-lighter;

  .info-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-sm;
    font-weight: 500;
    color: #303133;

    .el-icon {
      color: #606266;
      font-size: 16px;
    }
  }
}
</style>
