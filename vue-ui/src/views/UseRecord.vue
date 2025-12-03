<template>
  <div class="record-container">
    <!-- 使用统计 -->
    <el-row :gutter="20" class="statistics-container">
      <el-col :span="6">
        <el-card class="statistics-card primary-card" shadow="hover">
          <div slot="header">
            <span>今日使用次数</span>
          </div>
          <div class="statistics-value">{{ statistics.todayCount ? statistics.todayCount : 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card success-card" shadow="hover">
          <div slot="header">
            <span>本月使用次数</span>
          </div>
          <div class="statistics-value">{{ statistics.monthCount ? statistics.monthCount : 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card warning-card" shadow="hover">
          <div slot="header">
            <span>是否有使用中记录</span>
          </div>
          <div class="statistics-value">{{ statistics.usingCount ? statistics.usingCount : 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card info-card" shadow="hover">
          <div slot="header">
            <span>使用用户数</span>
          </div>
          <div class="statistics-value">{{ statistics.userCount ? statistics.userCount : 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="化学品">
          <el-input v-model="searchForm.chemicalName" placeholder="请输入化学品名称"></el-input>
        </el-form-item>
        <el-form-item label="使用人">
          <el-input v-model="searchForm.userName" placeholder="请输入使用人名称"></el-input>
        </el-form-item>
        <el-form-item label="使用目的">
          <el-input v-model="searchForm.usagePurpose" placeholder="请输入使用目的"></el-input>
        </el-form-item>
        <el-form-item label="使用时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
<!--          <el-button type="success" size="small" @click="handleAdd" style="margin-left: 10px;">新增记录</el-button>-->
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%">
      <el-table-column
        prop="chemical.name"
        label="化学品名称"
        width="150">
      </el-table-column>
      <el-table-column
        prop="amount"
        label="使用数量"
        width="120">
        <template slot-scope="scope">
          {{ scope.row.amount }} {{ scope.row.unit }}
        </template>
      </el-table-column>
      <el-table-column
        prop="user.name"
        label="使用人"
        width="120">
      </el-table-column>
      <el-table-column
        prop="usageTime"
        label="使用时间"
        width="180">
      </el-table-column>
      <el-table-column
        prop="usagePurpose"
        label="使用目的"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="notes"
        label="备注"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        width="180">
        <template slot-scope="scope">
          <el-button @click="handleDetail(scope.row)" type="text" size="small">详情</el-button>
          <el-button @click="handleEdit(scope.row)" type="text" size="small">编辑</el-button>
          <el-button @click="handleDelete(scope.row)" type="text" size="small" style="color: #F56C6C;">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 数据汇总 -->
    <div class="summary-container" v-if="tableData.length > 0">
      <el-card class="summary-card" shadow="hover">
        <div slot="header">
          <span>当前页数据汇总</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="calculateSummary">刷新汇总</el-button>
        </div>
        <el-row :gutter="18">
          <el-col :span="8">
            <div class="summary-item">
              <div class="summary-label">记录数量:</div>
              <div class="summary-value">{{ summary.count }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item">
              <div class="summary-label">使用总量:</div>
              <div class="summary-value">{{ summary.totalAmount }} {{ summary.commonUnit }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item">
              <div class="summary-label">平均使用量:</div>
              <div class="summary-value">{{ summary.avgAmount }} {{ summary.commonUnit }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    <!-- 柱状图 -->
      <template>
        <div ref="echarts" :style="myChartStyle">
        </div>
      </template>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.currentPage"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total">
      </el-pagination>
    </div>

    <!-- 使用记录详情对话框 -->
    <el-dialog title="使用记录详情" :visible.sync="detailDialogVisible" width="50%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="化学品名称">{{ currentDetail.chemical && currentDetail.chemical.name }}</el-descriptions-item>
        <el-descriptions-item label="使用数量">{{ currentDetail.amount }} {{ currentDetail.unit }}</el-descriptions-item>
        <el-descriptions-item label="使用人">{{ currentDetail.user && currentDetail.user.name }}</el-descriptions-item>
        <el-descriptions-item label="使用时间">{{ currentDetail.usageTime }}</el-descriptions-item>
        <el-descriptions-item label="使用目的">{{ currentDetail.usagePurpose }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentDetail.notes }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="form.id ? '编辑使用记录' : '新增使用记录'" :visible.sync="dialogVisible" width="50%">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="化学品" prop="chemicalId">
          <el-select v-model="form.chemicalId" placeholder="请选择化学品" style="width :100%;">
            <el-option
              v-for="item in chemicalOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="使用数量" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 200px;"></el-input-number>
          <el-input v-model="form.unit" placeholder="单位" style="width: 100px; margin-left: 10px;"></el-input>
        </el-form-item>
        <el-form-item label="使用人" prop="userId">
          <el-select v-model="form.userId" placeholder="请选择使用人" style="width: 100%;">
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="使用时间" prop="usageTime">
          <el-date-picker
            v-model="form.usageTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="使用目的" prop="usagePurpose">
          <el-input v-model="form.usagePurpose" placeholder="请输入使用目的"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input type="textarea" v-model="form.notes" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';


export default {
  name: 'Record',
  data() {
    return {
      // 查询条件
      searchForm: {
        chemicalName: null,
        userName: null,
        usagePurpose: null,
        timeRange: []
      },
      // 化学品选项
      chemicalOptions: [],
      // 用户选项
      userOptions: [],
      // 表格数据
      tableData: [],
      // 加载状态
      loading: false,
      // 分页信息
      pagination: {
        currentPage: 1,
        pageSize: 5,
        total: 0
      },
      //柱状图
      xData: ['记录数量', '使用总量', '平均使用量'],
      myChart: null,
      myChartStyle: { width: '100%', height: '400px' },
      // 统计数据
      statistics: {
        todayCount: 0,
        monthCount: 0,
        usingCount: 0,
        userCount: 0
      },
      // 汇总数据
      summary: {
        count: 0,
        totalAmount: 0,
        avgAmount: 0,
        commonUnit: 'kg'
      },
      // 详情对话框
      detailDialogVisible: false,
      currentDetail: {},
      // 新增/编辑对话框
      dialogVisible: false,
      // 表单数据
      form: {
        id: null,
        chemicalId: null,
        amount: 0,
        unit: '',
        userId: null,
        usageTime: '',
        usagePurpose: '',
        notes: ''
      },
      // 表单验证规则
      rules: {
        chemicalId: [
          { required: true, message: '请选择化学品', trigger: 'change' }
        ],
        amount: [
          { required: true, message: '请输入使用数量', trigger: 'blur' }
        ],
        userId: [
          { required: true, message: '请选择使用人', trigger: 'change' }
        ],
        usageTime: [
          { required: true, message: '请选择使用时间', trigger: 'change' }
        ],
        usagePurpose: [
          { required: true, message: '请输入使用目的', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchData()
    this.fetchChemicals()
    this.fetchUsers()
    this.fetchStatistics()
  },
  mounted() {
    this.initEcharts();
  },
  methods: {
    // 获取表格数据
    async fetchData() {
      this.loading = true
      try {
        const { startTime, endTime } = this.getTimeRange()
        console.log('发送请求参数:', {
          chemicalName: this.searchForm.chemicalName || null,
          userName: this.searchForm.userName || null,
          usagePurpose: this.searchForm.usagePurpose || null,
          startTime,
          endTime,
          page: this.pagination.currentPage,
          size: this.pagination.pageSize
        })

        const response = await this.$http.get('/usage/list', {
          params: {
            chemicalName: this.searchForm.chemicalName || null,
            userName: this.searchForm.userName || null,
            usagePurpose: this.searchForm.usagePurpose || null,
            startTime: startTime || null,
            endTime: endTime || null,
            page: this.pagination.currentPage,
            size: this.pagination.pageSize
          }
        })

        console.log('接收到响应:', response.data)

        if (response.data.code === 200) {
          this.tableData = response.data.data.records || []
          this.pagination.total = response.data.data.total || 0

          if (this.tableData.length === 0) {
            this.$message.info('未找到符合条件的记录')
          } else {
            // 计算汇总数据
            this.calculateSummary()
          }
        } else {
          this.$message.error(response.data.message || '获取记录失败')
        }
      } catch (error) {
        console.error('获取记录失败:', error)
        this.$message.error('获取记录失败:' + (error.message || error))
      }
      this.loading = false
    },
    //柱状图内容控制
    initEcharts() {
      if (!this.$refs.echarts) return;
      if (!this.myChart) {
        this.myChart = echarts.init(this.$refs.echarts);
        window.addEventListener('resize', () => {
          this.myChart && this.myChart.resize();
        });
      }
      const yData = [
        Number(this.summary.count),
        Number(this.summary.totalAmount),
        Number(this.summary.avgAmount)
      ];
      const option = {
        tooltip: {},
        xAxis: { data: this.xData },
        yAxis: {},
        series: [{ type: 'bar', data: yData }]
      };
      this.myChart.setOption(option);
    },
    // 获取化学品列表
    async fetchChemicals() {
      try {
        const response = await this.$http.get('/chemical/list')
        if (response.data && response.data.code === 200) {
          this.chemicalOptions = response.data.data || []
        } else {
          this.$message.error('获取化学品列表失败')
        }
      } catch (error) {
        this.$message.error('获取化学品列表失败')
      }
    },
    // 获取用户列表
    async fetchUsers() {
      try {
        const response = await this.$http.get('/man/list')
        if (response.data && response.data.code === 200) {
          this.userOptions = response.data.data || []
        } else {
          this.$message.error('获取用户列表失败')
        }
      } catch (error) {
        this.$message.error('获取用户列表失败')
      }
    },
    // 获取统计数据
    async fetchStatistics() {
      try {
        const response = await this.$http.get('/usage/statistics')
        console.log('获取统计数据响应:', response.data)

        if (response.data.code === 200) {
          const data = response.data.data || {}
          this.statistics = {
            todayCount: data.dailyCount || 0,
            monthCount: data.monthlyCount || 0,
            activeCount: data.dailyTotal || 0,
            userCount: data.monthlyTotal || 0
          }
        } else {
          console.error('获取统计数据失败:', response.data.message)
          this.$message.error(response.data.message || '获取统计数据失败')
        }
      } catch (error) {
        console.error('获取统计数据失败', error)
        this.$message.error('获取统计数据失败:' + (error.message || error))
      }
    },
    // 处理查询
    handleSearch() {
      this.pagination.currentPage = 1
      this.fetchData()
    },
    // 重置查询
    resetSearch() {
      this.searchForm = {
        chemicalName: null,
        userName: null,
        usagePurpose: null,
        timeRange: []
      }
      this.handleSearch()
    },
    // 处理详情
    handleDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
    },
    // 处理新增
    handleAdd() {
      this.form = {
        id: null,
        chemicalId: null,
        amount: 0,
        unit: 'kg',
        userId: null,
        usageTime: new Date().toISOString().split('.')[0].replace('T', ' '),
        usagePurpose: '',
        notes: ''
      }
      this.dialogVisible = true
      // 清除之前的验证结果
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
    },
    // 处理编辑
    handleEdit(row) {
      this.form = {
        id: row.id,
        chemicalId: row.chemicalId || (row.chemical ? row.chemical.id : null),
        amount: row.amount,
        unit: row.unit || 'kg',
        userId: row.userId || (row.user ? row.user.id : null),
        usageTime: row.usageTime,
        usagePurpose: row.usagePurpose,
        notes: row.notes
      }
      this.dialogVisible = true
      // 清除之前的验证结果
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    // 处理删除
    handleDelete(row) {
      this.$confirm('此操作将永久删除该使用记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$http.delete(`/usage/delete/${row.id}`)
          if (response.data && response.data.code === 200) {
            this.$message.success('删除成功!')
            this.fetchData()
            this.fetchStatistics()
          } else {
            this.$message.error(response.data.message || '删除失败')
          }
        } catch (error) {
          this.$message.error('删除失败: ' + (error.message || error))
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 处理提交表单
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            let response
            if (this.form.id) {
              // 编辑
              response = await this.$http.put(`/usage/update/${this.form.id}`, this.form)
            } else {
              // 新增
              response = await this.$http.post('/usage/add', this.form)
            }
            if (response.data && response.data.code === 200) {
              this.$message.success(this.form.id ? '更新成功!' : '添加成功!')
              this.dialogVisible = false
              this.fetchData()
              this.fetchStatistics()
            } else {
              this.$message.error(response.data.message || (this.form.id ? '更新失败' : '添加失败'))
            }
          } catch (error) {
            this.$message.error((this.form.id ? '更新' : '添加') + '失败: ' + (error.message || error))
          }
        }
      })
    },
    // 处理分页大小变化
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.fetchData()
    },
    // 处理页码变化
    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.fetchData()
    },
    // 获取时间范围
    getTimeRange() {
      const range = this.searchForm.timeRange
      if (!range || !range.length || range.length < 2) {
        return {
          startTime: null,
          endTime: null
        }
      }

      // 时间格式已经在日期选择器中通过 value-format 指定为 yyyy-MM-dd HH:mm:ss
      return {
        startTime: range[0],
        endTime: range[1]
      }
    },
    // 计算汇总数据
    calculateSummary() {
      if (!this.tableData || this.tableData.length === 0) {
        this.summary = {
          count: 0,
          totalAmount: 0,
          avgAmount: 0,
          commonUnit: 'kg'
        }
        return
      }

      // 计算记录数量
      const count = this.tableData.length

      // 检查单位是否一致，如果一致则使用该单位，否则使用默认单位
      let units = new Set()
      this.tableData.forEach(record => {
        if (record.unit) units.add(record.unit)
      })

      const commonUnit = units.size === 1 ? Array.from(units)[0] : 'kg'

      // 计算总量
      let totalAmount = 0
      this.tableData.forEach(record => {
        if (record.amount) totalAmount += parseFloat(record.amount)
      })

      // 计算平均值
      const avgAmount = count > 0 ? (totalAmount / count).toFixed(2) : 0

      this.summary = {
        count,
        totalAmount: totalAmount.toFixed(2),
        avgAmount,
        commonUnit
      }
      this.initEcharts();
    }
  }
}
</script>

<style scoped>
.record-container {
  padding: 20px;
}

.statistics-container {
  margin-bottom: 20px;
}

.statistics-card {
  text-align: center;
}

.statistics-value {
  font-size: 24px;
  font-weight: bold;
}

.primary-card .statistics-value {
  color: #409EFF;
}

.success-card .statistics-value {
  color: #67C23A;
}

.warning-card .statistics-value {
  color: #E6A23C;
}

.info-card .statistics-value {
  color: #909399;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 18px;
  text-align: right;
}

.summary-container {
  margin-top: 18px;
}

.summary-card {
  margin-bottom: 18px;
}

.summary-item {
  margin-bottom: 10px;
}

.summary-label {
  font-weight: bold;
}

.summary-value {
  margin-left: 10px;
}
</style>
