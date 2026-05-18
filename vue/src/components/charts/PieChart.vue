<template>
  <div class="pie-chart" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, getCurrentInstance } from 'vue'

const props = defineProps({
  // 图表数据 [{ name: '', value: '' }]
  data: {
    type: Array,
    default: () => []
  },
  // 图表标题
  title: {
    type: String,
    default: ''
  },
  // 图表高度
  height: {
    type: [Number, String],
    default: 300
  },
  // 是否为环形图
  donut: {
    type: Boolean,
    default: true
  },
  // 内圈半径（环形图）
  innerRadius: {
    type: String,
    default: '50%'
  },
  // 外圈半径
  outerRadius: {
    type: String,
    default: '70%'
  },
  // 颜色配置
  colors: {
    type: Array,
    default: () => ['#4A90D9', '#52C41A', '#FF7A45', '#722ED1', '#13C2C2', '#909399']
  },
  // 是否显示图例
  showLegend: {
    type: Boolean,
    default: true
  },
  // 中心文字
  centerText: {
    type: String,
    default: ''
  },
  // 中心数值
  centerValue: {
    type: [String, Number],
    default: ''
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
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E4E7ED',
      borderWidth: 1,
      textStyle: {
        color: '#303133'
      },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: props.showLegend ? {
      orient: 'horizontal',
      bottom: 0,
      textStyle: {
        color: '#606266'
      }
    } : null,
    color: props.colors,
    series: [
      {
        type: 'pie',
        radius: props.donut ? [props.innerRadius, props.outerRadius] : props.outerRadius,
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        labelLine: {
          show: false
        },
        data: props.data
      }
    ],
    // 中心文字
    graphic: props.donut && (props.centerText || props.centerValue) ? [
      {
        type: 'group',
        left: 'center',
        top: '40%',
        children: [
          {
            type: 'text',
            style: {
              text: props.centerValue,
              textAlign: 'center',
              fill: '#303133',
              fontSize: 24,
              fontWeight: 'bold'
            }
          },
          {
            type: 'text',
            top: 30,
            style: {
              text: props.centerText,
              textAlign: 'center',
              fill: '#909399',
              fontSize: 12
            }
          }
        ]
      }
    ] : null
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
.pie-chart {
  width: 100%;
  height: v-bind('typeof height === "number" ? height + "px" : height');
}
</style>
