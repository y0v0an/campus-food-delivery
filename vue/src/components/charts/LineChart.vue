<template>
  <div class="line-chart" ref="chartRef"></div>
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
    default: 'date'
  },
  // Y轴字段名（可以是数组，支持多条线）
  yField: {
    type: [String, Array],
    default: 'value'
  },
  // 图表标题
  title: {
    type: String,
    default: ''
  },
  // 是否显示面积
  showArea: {
    type: Boolean,
    default: true
  },
  // 是否平滑曲线
  smooth: {
    type: Boolean,
    default: true
  },
  // 图表高度
  height: {
    type: [Number, String],
    default: 300
  },
  // 颜色配置
  colors: {
    type: Array,
    default: () => ['#4A90D9', '#52C41A', '#FF7A45']
  },
  // 图例名称
  legendNames: {
    type: Array,
    default: () => []
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
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

// 更新图表
const updateChart = () => {
  if (!chartInstance || !props.data.length) return
  
  const yFields = Array.isArray(props.yField) ? props.yField : [props.yField]
  const legendNames = props.legendNames.length ? props.legendNames : yFields
  
  const series = yFields.map((field, index) => ({
    name: legendNames[index] || field,
    type: 'line',
    smooth: props.smooth,
    data: props.data.map(item => item[field]),
    itemStyle: {
      color: props.colors[index % props.colors.length]
    },
    lineStyle: {
      width: 2,
      color: props.colors[index % props.colors.length]
    },
    areaStyle: props.showArea ? {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [
          { offset: 0, color: `${props.colors[index % props.colors.length]}40` },
          { offset: 1, color: `${props.colors[index % props.colors.length]}05` }
        ]
      }
    } : null,
    symbol: 'circle',
    symbolSize: 6,
    emphasis: {
      focus: 'series',
      itemStyle: {
        borderWidth: 2,
        borderColor: '#fff'
      }
    }
  }))

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
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: yFields.length > 1 ? {
      data: legendNames,
      bottom: 0,
      textStyle: {
        color: '#606266'
      }
    } : null,
    grid: {
      left: '3%',
      right: '4%',
      bottom: yFields.length > 1 ? '15%' : '3%',
      top: props.title ? '15%' : '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.data.map(item => item[props.xField]),
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      axisLabel: {
        color: '#909399'
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#909399'
      },
      splitLine: {
        lineStyle: {
          color: '#F2F6FC',
          type: 'dashed'
        }
      }
    },
    series
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
.line-chart {
  width: 100%;
  height: v-bind('typeof height === "number" ? height + "px" : height');
}
</style>
