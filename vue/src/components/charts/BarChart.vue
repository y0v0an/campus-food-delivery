<template>
  <div class="bar-chart" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, getCurrentInstance } from 'vue'

const props = defineProps({
  // 图表数据
  data: {
    type: Array,
    default: () => []
  },
  // X轴字段名
  xField: {
    type: String,
    default: 'name'
  },
  // Y轴字段名
  yField: {
    type: String,
    default: 'value'
  },
  // 图表标题
  title: {
    type: String,
    default: ''
  },
  // 是否水平显示
  horizontal: {
    type: Boolean,
    default: false
  },
  // 图表高度
  height: {
    type: [Number, String],
    default: 300
  },
  // 柱子颜色
  color: {
    type: String,
    default: '#4A90D9'
  },
  // 是否显示渐变
  gradient: {
    type: Boolean,
    default: true
  },
  // 是否显示数值标签
  showLabel: {
    type: Boolean,
    default: false
  },
  // 柱子圆角
  barRadius: {
    type: Number,
    default: 4
  }
})

const chartRef = ref(null)
let chartInstance = null
const { proxy } = getCurrentInstance()

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = proxy.$echarts.init(chartRef.value)
  updateChart()
  
  window.addEventListener('resize', handleResize)
}

// 更新图表
const updateChart = () => {
  if (!chartInstance || !props.data.length) return
  
  const xData = props.data.map(item => item[props.xField])
  const yData = props.data.map(item => item[props.yField])
  
  const barColor = props.gradient ? {
    type: 'linear',
    x: props.horizontal ? 0 : 0,
    y: props.horizontal ? 0 : 1,
    x2: props.horizontal ? 1 : 0,
    y2: 0,
    colorStops: [
      { offset: 0, color: props.color },
      { offset: 1, color: `${props.color}80` }
    ]
  } : props.color

  const option = {
    title: props.title ? {
      text: props.title,
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 500,
        color: '#303133'
      }
    } : null,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E4E7ED',
      borderWidth: 1,
      textStyle: {
        color: '#303133'
      },
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: props.title ? '15%' : '10%',
      containLabel: true
    },
    xAxis: {
      type: props.horizontal ? 'value' : 'category',
      data: props.horizontal ? null : xData,
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      axisLabel: {
        color: '#909399',
        rotate: props.horizontal ? 0 : (xData.length > 6 ? 30 : 0)
      },
      axisTick: {
        show: false
      },
      splitLine: props.horizontal ? {
        lineStyle: {
          color: '#F2F6FC',
          type: 'dashed'
        }
      } : null
    },
    yAxis: {
      type: props.horizontal ? 'category' : 'value',
      data: props.horizontal ? xData : null,
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#909399'
      },
      splitLine: props.horizontal ? null : {
        lineStyle: {
          color: '#F2F6FC',
          type: 'dashed'
        }
      }
    },
    series: [{
      type: 'bar',
      data: yData,
      barWidth: '60%',
      itemStyle: {
        color: barColor,
        borderRadius: props.horizontal 
          ? [0, props.barRadius, props.barRadius, 0]
          : [props.barRadius, props.barRadius, 0, 0]
      },
      label: props.showLabel ? {
        show: true,
        position: props.horizontal ? 'right' : 'top',
        color: '#606266',
        fontSize: 12
      } : null,
      emphasis: {
        itemStyle: {
          color: props.color
        }
      }
    }]
  }

  chartInstance.setOption(option)
}

// 处理窗口大小变化
const handleResize = () => {
  chartInstance?.resize()
}

// 监听数据变化
watch(() => props.data, updateChart, { deep: true })

onMounted(() => {
  initChart()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.bar-chart {
  width: 100%;
  height: v-bind('typeof height === "number" ? height + "px" : height');
}
</style>
