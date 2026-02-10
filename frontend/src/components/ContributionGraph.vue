<template>
  <div class="contribution-graph">
    <div class="graph-main">
      <div class="graph-header">
        <span class="graph-title">📊 学习记录</span>
        <span class="graph-subtitle">{{ currentYear }}年的笔记提交</span>
      </div>
      <div class="graph-container">
        <div class="graph-wrapper">
          <div class="months">
            <span
              v-for="(month, index) in monthLabels"
              :key="index"
              class="month-label"
              :style="{ left: month.offset + 'px' }"
            >{{ month.name }}</span>
          </div>
          <div class="graph-body">
            <div class="weekdays">
              <span v-for="day in weekdayLabels" :key="day">{{ day }}</span>
            </div>
            <div class="squares">
              <!-- 按周列渲染 -->
              <div
                v-for="(week, weekIndex) in contributionData"
                :key="weekIndex"
                class="week-column"
                :class="{ 'first-week': weekIndex === 0, 'last-week': weekIndex === contributionData.length - 1 }"
              >
                <!-- 每周7天，按行排列 -->
                <template v-for="dayOfWeek in 7" :key="dayOfWeek">
                  <el-tooltip
                    v-if="week[dayOfWeek - 1]"
                    :content="week[dayOfWeek - 1].tooltip"
                    placement="top"
                    :show-after="100"
                  >
                    <div
                      class="square"
                      :class="getLevelClass(week[dayOfWeek - 1].count)"
                    ></div>
                  </el-tooltip>
                  <div v-else class="square-placeholder"></div>
                </template>
              </div>
            </div>
          </div>
        </div>
        <div class="graph-legend">
          <span>Less</span>
          <div class="legend-square level-0"></div>
          <div class="legend-square level-1"></div>
          <div class="legend-square level-2"></div>
          <div class="legend-square level-3"></div>
          <div class="legend-square level-4"></div>
          <span>More</span>
        </div>
      </div>
      <div class="stats-summary">
        <div class="stat-item">
          <span class="stat-value">{{ totalContributions }}</span>
          <span class="stat-label">总提交</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ currentStreak }}</span>
          <span class="stat-label">连续天数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ maxContributions }}</span>
          <span class="stat-label">单日最多</span>
        </div>
      </div>
    </div>
    <div class="year-selector">
      <div
        v-for="year in availableYears"
        :key="year"
        class="year-item"
        :class="{ active: year === currentYear }"
        @click="selectYear(year)"
      >
        {{ year }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  contributions: {
    type: Array,
    default: () => []
  }
})

// 当前选中的年份
const currentYear = ref(new Date().getFullYear())

// 可选的年份列表
const availableYears = computed(() => {
  const years = new Set()
  const current = new Date().getFullYear()

  // 从贡献数据中提取所有年份
  props.contributions.forEach(c => {
    const year = new Date(c.date).getFullYear()
    years.add(year)
  })

  // 添加当前年份
  years.add(current)

  return Array.from(years).sort((a, b) => b - a)
})

// 选择年份
const selectYear = (year) => {
  currentYear.value = year
}

// 星期标签（显示所有7天，从周日开始）
const weekdayLabels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

// 月份标签
const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

// 格式化日期为 YYYY-MM-DD
const formatDate = (date) => {
  const d = new Date(date)
  return d.getFullYear() + '-' +
    String(d.getMonth() + 1).padStart(2, '0') + '-' +
    String(d.getDate()).padStart(2, '0')
}

// 生成当年的贡献数据
// 数据结构：数组的数组，每个内部数组代表一周（一列），包含7个元素（周日到周六），非当年日期为 null
const contributionData = computed(() => {
  const year = currentYear.value
  const startDate = new Date(year, 0, 1)
  const endDate = new Date(year, 11, 31)

  // 1月1日是星期几 (0=周日, 1=周一...)
  const startDayOfWeek = startDate.getDay()

  // 计算总天数
  const totalDays = Math.floor((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1

  // 计算需要多少列（周数）
  // 公式：(前面的空白天数 + 总天数 + 后面的空白天数) / 7，向上取整
  const totalCells = startDayOfWeek + totalDays
  const totalWeeks = Math.ceil(totalCells / 7)

  // 创建一个大数组，包含所有日期的数据
  const allDays = []

  // 填充前面的空白（这些是非当年的日期，用 null 表示）
  for (let i = 0; i < startDayOfWeek; i++) {
    allDays.push(null)
  }

  // 填充实际的日期数据
  for (let i = 0; i < totalDays; i++) {
    const currentDate = new Date(startDate)
    currentDate.setDate(startDate.getDate() + i)
    const dateStr = formatDate(currentDate)

    // 查找贡献数据
    const contribution = props.contributions.find(c => {
      const cDate = new Date(c.date)
      const cYear = cDate.getFullYear()
      const cDateStr = formatDate(cDate)
      return cYear === year && cDateStr === dateStr
    })

    const count = contribution ? contribution.count : 0

    allDays.push({
      date: dateStr,
      count: count,
      tooltip: `${currentDate.toLocaleString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })}: ${count} 条笔记`
    })
  }

  // 填充后面的空白，使总数是7的倍数
  while (allDays.length < totalWeeks * 7) {
    allDays.push(null)
  }

  // 按周分组（每7个为一组，每组成一列）
  const weeks = []
  for (let week = 0; week < totalWeeks; week++) {
    const weekData = []
    for (let day = 0; day < 7; day++) {
      const index = week * 7 + day
      weekData.push(allDays[index])
    }
    weeks.push(weekData)
  }

  return weeks
})

// 计算月份标签位置
const monthLabels = computed(() => {
  const year = currentYear.value
  const labels = []
  const squareSize = 14
  const gapSize = 3
  const weekWidth = squareSize + gapSize

  // 1月1日是周几
  const firstDay = new Date(year, 0, 1)
  const firstDayWeekday = firstDay.getDay()

  for (let month = 0; month < 12; month++) {
    const firstDayOfMonth = new Date(year, month, 1)

    // 计算该月1日是当年的第几天
    const dayOfYear = Math.floor((firstDayOfMonth - firstDay) / (1000 * 60 * 60 * 24))

    // 计算该月1日是第几周（从0开始）
    const weekIndex = Math.floor((dayOfYear + firstDayWeekday) / 7)

    labels.push({
      name: monthNames[month],
      offset: weekIndex * weekWidth
    })
  }

  return labels
})

// 获取颜色等级
const getLevelClass = (count) => {
  if (count === 0) return 'level-0'
  if (count <= 2) return 'level-1'
  if (count <= 4) return 'level-2'
  if (count <= 6) return 'level-3'
  return 'level-4'
}

// 过滤出当前年份的贡献数据
const yearContributions = computed(() => {
  return props.contributions.filter(c => {
    const year = new Date(c.date).getFullYear()
    return year === currentYear.value
  })
})

// 统计计算
const totalContributions = computed(() => {
  return yearContributions.value.reduce((sum, c) => sum + c.count, 0)
})

const currentStreak = computed(() => {
  let streak = 0
  const today = new Date().toISOString().split('T')[0]

  // 将二维数组扁平化，只保留实际日期（过滤掉 null）
  const allDays = contributionData.value.flat().filter(d => d !== null)

  for (let i = allDays.length - 1; i >= 0; i--) {
    const day = allDays[i]
    if (day.count > 0) {
      streak++
    } else if (day.date < today) {
      break
    }
  }

  return streak
})

const maxContributions = computed(() => {
  if (yearContributions.value.length === 0) return 0
  return Math.max(...yearContributions.value.map(c => c.count))
})
</script>

<style lang="scss" scoped>
.contribution-graph {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: var(--card-shadow);
  display: flex;
  gap: 20px;

  .graph-main {
    flex: 1;
    min-width: 0;
  }

  .graph-header {
    margin-bottom: 20px;

    .graph-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin-right: 10px;
    }

    .graph-subtitle {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }

  .graph-container {
    overflow-x: auto;
    padding-bottom: 10px;

    .graph-wrapper {
      min-width: 750px;

      .months {
        position: relative;
        height: 24px;
        margin-left: 44px;
        margin-bottom: 10px;

        .month-label {
          position: absolute;
          font-size: 13px;
          color: var(--text-secondary);
          white-space: nowrap;
          font-weight: 500;
        }
      }

      .graph-body {
        display: flex;

        .weekdays {
          display: flex;
          flex-direction: column;
          gap: 3px;
          margin-right: 10px;
          padding-top: 0;

          span {
            font-size: 11px;
            color: var(--text-secondary);
            line-height: 14px;
            height: 14px;
            display: flex;
            align-items: center;
          }
        }

        .squares {
          display: flex;
          flex-direction: row;
          gap: 3px;

          .week-column {
            display: flex;
            flex-direction: column;
            gap: 3px;

            .square {
              width: 14px;
              height: 14px;
              border-radius: 3px;
              cursor: pointer;
              transition: all 0.2s ease;

              &:hover {
                transform: scale(1.2);
                box-shadow: 0 0 4px rgba(0, 0, 0, 0.2);
              }

              &.level-0 {
                background-color: var(--contribution-level-0, #ebedf0);
              }

              &.level-1 {
                background-color: var(--contribution-level-1, #9be9a8);
              }

              &.level-2 {
                background-color: var(--contribution-level-2, #40c463);
              }

              &.level-3 {
                background-color: var(--contribution-level-3, #30a14e);
              }

              &.level-4 {
                background-color: var(--contribution-level-4, #216e39);
              }
            }

            .square-placeholder {
              width: 14px;
              height: 14px;
              border-radius: 3px;
              background-color: transparent;
              pointer-events: none;
            }
          }
        }
      }
    }

    .graph-legend {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 4px;
      margin-top: 8px;
      font-size: 12px;
      color: var(--text-secondary);

      .legend-square {
        width: 12px;
        height: 12px;
        border-radius: 2px;

        &.level-0 {
          background-color: var(--contribution-level-0, #ebedf0);
        }

        &.level-1 {
          background-color: var(--contribution-level-1, #9be9a8);
        }

        &.level-2 {
          background-color: var(--contribution-level-2, #40c463);
        }

        &.level-3 {
          background-color: var(--contribution-level-3, #30a14e);
        }

        &.level-4 {
          background-color: var(--contribution-level-4, #216e39);
        }
      }
    }
  }

  .stats-summary {
    display: flex;
    gap: 30px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid var(--border-color);

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: var(--primary-color);
      }

      .stat-label {
        font-size: 13px;
        color: var(--text-secondary);
        margin-top: 4px;
      }
    }
  }

  .year-selector {
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: 80px;
    border-left: 1px solid var(--border-color);
    padding-left: 20px;

    .year-item {
      padding: 8px 16px;
      border-radius: 6px;
      cursor: pointer;
      text-align: center;
      font-size: 14px;
      font-weight: 500;
      color: var(--text-secondary);
      background: var(--bg-secondary);
      transition: all 0.2s ease;
      border: 2px solid transparent;

      &:hover {
        background: var(--primary-color-light);
        color: var(--primary-color);
      }

      &.active {
        background: #0969da;
        color: #ffffff;
        border-color: #0550ae;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        font-weight: 600;
      }
    }
  }
}

// 深色模式适配
:root[data-theme="dark"] {
  .contribution-graph {
    .squares {
      .square {
        &.level-0 {
          background-color: #161b22;
        }

        &.level-1 {
          background-color: #0e4429;
        }

        &.level-2 {
          background-color: #006d32;
        }

        &.level-3 {
          background-color: #26a641;
        }

        &.level-4 {
          background-color: #39d353;
        }
      }
    }

    .graph-legend {
      .legend-square {
        &.level-0 {
          background-color: #161b22;
        }

        &.level-1 {
          background-color: #0e4429;
        }

        &.level-2 {
          background-color: #006d32;
        }

        &.level-3 {
          background-color: #26a641;
        }

        &.level-4 {
          background-color: #39d353;
        }
      }
    }
  }
}
</style>
