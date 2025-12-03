<template>
  <div class="inventory-container">
    <!-- 查询条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="化学品">
          <el-input v-model="searchForm.chemicalName" placeholder="请输入化学品名称"></el-input>
        </el-form-item>
        <el-form-item label="存储位置">
          <el-input v-model="searchForm.location" placeholder="请输入存储位置"></el-input>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.status" placeholder="请选择库存状态" clearable>
            <el-option label="正常" value="normal"></el-option>
            <el-option label="不足" value="low"></el-option>
            <el-option label="超储" value="high"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 库存概览 -->
    <el-row :gutter="20" class="statistics-container">
      <el-col :span="6">
        <el-card class="statistics-card" shadow="hover">
          <div slot="header">
            <span>总库存种类</span>
          </div>
          <div class="statistics-value">{{ statistics.totalTypes || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card warning-card" shadow="hover">
          <div slot="header">
            <span>库存预警</span>
          </div>
          <div class="statistics-value">{{ statistics.warningCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card danger-card" shadow="hover">
          <div slot="header">
            <span>库存不足</span>
          </div>
          <div class="statistics-value">{{ statistics.lowCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card info-card" shadow="hover">
          <div slot="header">
            <span>超储数量</span>
          </div>
          <div class="statistics-value">{{ statistics.highCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%">
      <el-table-column
        type="selection"
        width="55px">
      </el-table-column>
      <el-table-column
        property="id"
        label="ID"
        width="60px"
        align="center">
      </el-table-column>
      <el-table-column
        property="chemical.name"
        label="危化品名称"
        width="120px"
        align="center">
      </el-table-column>
      <el-table-column
        property="currentAmount"
        label="当前库存"
        width="100px"
        align="center">
        <template slot-scope="scope">
          {{ scope.row.currentAmount }} {{ scope.row.unit }}
        </template>
      </el-table-column>
      <el-table-column
        property="location"
        label="存储位置"
        width="150px"
        align="center">
      </el-table-column>
      <el-table-column
        property="lastCheckTime"
        label="最后盘点时间"
        width="150px"
        align="center">
        <template slot-scope="scope">
          {{ scope.row.lastCheckTime ? new Date(scope.row.lastCheckTime).toLocaleString() : '未盘点' }}
        </template>
      </el-table-column>
      <el-table-column
        property="createTime"
        label="创建时间"
        width="150px"
        align="center">
        <template slot-scope="scope">
          {{ new Date(scope.row.createTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column
        property="updateTime"
        label="更新时间"
        width="150px"
        align="center">
        <template slot-scope="scope">
          {{ new Date(scope.row.updateTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360px" align="center">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="success"
            @click="handleStorageIn(scope.row)">入库
          </el-button>
          <el-button
            size="mini"
            type="warning"
            @click="handleStorageOut(scope.row)">出库
          </el-button>
          <el-button
            size="mini"
            type="primary"
            @click="handleCheck(scope.row)">盘点
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 统计信息 -->
    <el-card class="statistics-card">
      <div slot="header">
        <span>库存统计</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="statistics-item">
            <div class="label">总库存量</div>
            <div class="value">{{ statistics.totalAmount }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="statistics-item">
            <div class="label">低于预警阈值</div>
            <div class="value warning">{{ statistics.belowThreshold }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="statistics-item">
            <div class="label">库存记录数</div>
            <div class="value">{{ statistics.totalRecords }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

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

    <!-- 库存详情对话框 -->
    <el-dialog title="库存详情" :visible.sync="detailDialogVisible" width="50%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="化学品名称">{{ currentDetail.chemical ? currentDetail.chemical.name : '' }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ currentDetail.currentAmount }} {{ currentDetail.unit }}</el-descriptions-item>
        <el-descriptions-item label="存储位置">{{ currentDetail.location }}</el-descriptions-item>
        <el-descriptions-item label="预警阈值">{{ currentDetail.chemical && currentDetail.chemical.warningThreshold }}</el-descriptions-item>
        <el-descriptions-item label="最后检查时间">{{ currentDetail.lastCheckTime }}</el-descriptions-item>
        <el-descriptions-item label="库存状态">
          <el-tag :type="getStatusType(currentDetail)">{{ getStatusText(currentDetail) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="detail-charts">
        <div class="chart-container">
          <div ref="stockTrendChart" style="width: 100%; height: 300px;"></div>
        </div>
      </div>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog title="库存历史记录" :visible.sync="historyDialogVisible" width="70%">
      <el-table :data="historyData" border>
        <el-table-column prop="operationType" label="操作类型" width="100px">
          <template slot-scope="scope">
            <el-tag :type="getOperationType(scope.row.operationType)">{{ scope.row.operationType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="数量变化" width="120px"></el-table-column>
        <el-table-column prop="beforeAmount" label="变化前数量" width="120px"></el-table-column>
        <el-table-column prop="afterAmount" label="变化后数量" width="120px"></el-table-column>
        <el-table-column prop="operator.name" label="操作人" width="120px"></el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="180px"></el-table-column>
        <el-table-column prop="notes" label="备注"></el-table-column>
      </el-table>
    </el-dialog>

    <!-- 盘点对话框 -->
    <el-dialog title="库存盘点" :visible.sync="checkDialogVisible" width="30%">
      <el-form :model="checkForm" :rules="checkRules" ref="checkForm" label-width="100px">
        <el-form-item label="当前库存" prop="currentAmount">
          <el-input-number v-model="checkForm.currentAmount" :min="0" :precision="2" :step="0.1"></el-input-number>
          <span class="unit">{{ checkForm.unit }}</span>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input type="textarea" v-model="checkForm.notes" placeholder="请输入备注信息"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="checkDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitCheck">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 入库对话框 -->
    <el-dialog title="入库" :visible.sync="storageInDialogVisible" width="40%">
      <el-form :model="storageInForm" :rules="storageInRules" ref="storageInForm" label-width="100px">
        <el-form-item label="化学品名称">
          <span>{{ storageInForm.chemicalName }}</span>
        </el-form-item>
        <el-form-item label="入库数量" prop="amount">
          <el-input-number v-model="storageInForm.amount" :min="0.01" :precision="2" :step="0.1"></el-input-number>
          <span class="unit">{{ storageInForm.unit }}</span>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="storageInForm.batchNo" placeholder="请输入批次号"></el-input>
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="storageInForm.supplier" placeholder="请输入供应商"></el-input>
        </el-form-item>
        <el-form-item label="入库时间" prop="storageTime">
          <el-date-picker
            v-model="storageInForm.storageTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="yyyy-MM-dd HH:mm:ss">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="操作员" prop="operatorId">
          <el-select v-model="storageInForm.operatorId" placeholder="请选择操作员">
            <el-option 
              v-for="operator in operatorOptions" 
              :key="operator.id" 
              :label="operator.name" 
              :value="operator.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input type="textarea" v-model="storageInForm.notes" placeholder="请输入备注信息"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="storageInDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitStorageIn">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog title="出库" :visible.sync="storageOutDialogVisible" width="40%">
      <el-form :model="storageOutForm" :rules="storageOutRules" ref="storageOutForm" label-width="100px">
        <el-form-item label="化学品名称">
          <span>{{ storageOutForm.chemicalName }}</span>
        </el-form-item>
        <el-form-item label="当前库存" v-if="storageOutForm.currentAmount !== null">
          <span>{{ storageOutForm.currentAmount }} {{ storageOutForm.unit }}</span>
        </el-form-item>
        <el-form-item label="出库数量" prop="amount">
          <el-input-number 
            v-model="storageOutForm.amount" 
            :min="0.01" 
            :max="storageOutForm.currentAmount || 0" 
            :precision="2" 
            :step="0.1">
          </el-input-number>
          <span class="unit">{{ storageOutForm.unit }}</span>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="storageOutForm.batchNo" placeholder="请输入批次号"></el-input>
        </el-form-item>
        <el-form-item label="领用人" prop="recipient">
          <el-input v-model="storageOutForm.recipient" placeholder="请输入领用人"></el-input>
        </el-form-item>
        <el-form-item label="使用目的" prop="purpose">
          <el-input v-model="storageOutForm.purpose" placeholder="请输入使用目的"></el-input>
        </el-form-item>
        <el-form-item label="出库时间" prop="outboundTime">
          <el-date-picker
            v-model="storageOutForm.outboundTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="yyyy-MM-dd HH:mm:ss">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="操作员" prop="operatorId">
          <el-select v-model="storageOutForm.operatorId" placeholder="请选择操作员">
            <el-option 
              v-for="operator in operatorOptions" 
              :key="operator.id" 
              :label="operator.name" 
              :value="operator.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input type="textarea" v-model="storageOutForm.notes" placeholder="请输入备注信息"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="storageOutDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitStorageOut">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'Inventory',
  data() {
    return {
      // 查询条件
      searchForm: {
        chemicalName: '',
        location: '',
        status: ''
      },
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
      // 统计数据
      statistics: {
        totalAmount: 0,
        belowThreshold: 0,
        totalRecords: 0
      },
      // 详情对话框
      detailDialogVisible: false,
      currentDetail: {},
      // 历史记录对话框
      historyDialogVisible: false,
      historyData: [],
      // 盘点对话框
      checkDialogVisible: false,
      checkForm: {
        id: null,
        currentAmount: 0,
        unit: '',
        notes: ''
      },
      checkRules: {
        currentAmount: [
          { required: true, message: '请输入当前库存', trigger: 'blur' }
        ]
      },
      // 操作员选项
      operatorOptions: [],
      // 入库表单
      storageInDialogVisible: false,
      storageInForm: {
        inventoryId: null,
        chemicalId: null,
        chemicalName: '',
        amount: 0,
        unit: '',
        batchNo: '',
        supplier: '',
        operatorId: 1,
        storageTime: new Date().toISOString().substr(0, 19).replace('T', ' '),
        notes: ''
      },
      storageInRules: {
        amount: [
          { required: true, message: '请输入入库数量', trigger: 'blur' },
          { type: 'number', min: 0.01, message: '入库数量必须大于0', trigger: 'blur' }
        ],
        batchNo: [
          { required: true, message: '请输入批次号', trigger: 'blur' }
        ],
        supplier: [
          { required: true, message: '请输入供应商', trigger: 'blur' }
        ],
        storageTime: [
          { required: true, message: '请选择入库时间', trigger: 'change' }
        ],
        operatorId: [
          { required: true, message: '请选择操作员', trigger: 'change' }
        ]
      },
      // 出库表单
      storageOutDialogVisible: false,
      storageOutForm: {
        inventoryId: null,
        chemicalId: null,
        chemicalName: '',
        currentAmount: 0,
        amount: 0,
        unit: '',
        batchNo: '',
        recipient: '',
        purpose: '',
        operatorId: 1,
        outboundTime: new Date().toISOString().substr(0, 19).replace('T', ' '),
        notes: ''
      },
      storageOutRules: {
        amount: [
          { required: true, message: '请输入出库数量', trigger: 'blur' },
          { type: 'number', min: 0.01, message: '出库数量必须大于0', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value > this.storageOutForm.currentAmount) {
              callback(new Error('出库数量不能超过当前库存量'));
            } else {
              callback();
            }
          }, trigger: 'blur' }
        ],
        recipient: [
          { required: true, message: '请输入领用人', trigger: 'blur' }
        ],
        purpose: [
          { required: true, message: '请输入使用目的', trigger: 'blur' }
        ],
        outboundTime: [
          { required: true, message: '请选择出库时间', trigger: 'change' }
        ],
        operatorId: [
          { required: true, message: '请选择操作员', trigger: 'change' }
        ]
      },
      // 库存趋势图实例
      stockTrendChart: null
    }
  },
  created() {
    console.log('组件创建')
    this.fetchData().catch(err => console.error('获取数据失败:', err))
    this.fetchChemicals().catch(err => console.error('获取化学品列表失败:', err))
    this.fetchOperators().catch(err => console.error('获取操作员列表失败:', err))
    this.fetchStatistics().catch(err => console.error('获取统计数据失败:', err))
  },
  mounted() {
    console.log('组件挂载完成')
    this.fetchData()
    this.fetchStatistics()
  },
  updated() {
    console.log('组件更新')
  },
  methods: {
    // 获取表格数据
    async fetchData() {
      this.loading = true
      try {
        console.log('开始请求库存数据，查询条件:', JSON.stringify(this.searchForm))
        
        // 构建请求参数
        const params = {
          chemicalName: this.searchForm.chemicalName || null,
          location: this.searchForm.location || null,
          status: this.searchForm.status || null,
          page: this.pagination.currentPage,
          size: this.pagination.pageSize
        }
        
        console.log('请求参数:', params)
        
        const response = await this.$http.get('/inventory/list', {
          params: params
        })
        
        console.log('库存数据响应:', response)
        if (response.data && response.data.code === 200) {
          this.tableData = response.data.data.records || []
          this.pagination.total = response.data.data.total || 0
          console.log('成功获取库存数据:', this.tableData)
        } else {
          console.error('获取库存数据失败:', response.data)
          this.$message.error(response.data.message || '获取数据失败')
        }
      } catch (error) {
        console.error('获取库存数据请求异常:', error)
        this.$message.error('获取数据失败：' + error.message)
      }
      this.loading = false
    },
    // 获取化学品列表
    async fetchChemicals() {
      try {
        const response = await this.$http.get('/chemical/list', {
          params: { page: 1, size: 100 }
        })
        
        if (response.data && response.data.code === 200) {
          this.chemicalOptions = response.data.data.records || []
        } else {
          console.warn('获取化学品列表失败')
          this.chemicalOptions = []
        }
      } catch (error) {
        console.error('获取化学品列表失败:', error)
        this.$message.error('获取化学品列表失败')
        this.chemicalOptions = []
      }
    },
    // 获取操作员列表
    async fetchOperators() {
      try {
        const response = await this.$http.get('/man/list', {
          params: { page: 1, size: 100 }
        })
        
        if (response.data && response.data.code === 200) {
          this.operatorOptions = response.data.data.records || []
        } else {
          console.warn('获取操作员列表失败')
          this.operatorOptions = []
        }
      } catch (error) {
        console.error('获取操作员列表失败:', error)
        this.$message.error('获取操作员列表失败')
        this.operatorOptions = []
      }
    },
    // 获取统计数据
    async fetchStatistics() {
      try {
        const response = await this.$http.get('/inventory/statistics', {
          params: {
            chemicalId: this.searchForm.chemicalId || null,
            location: this.searchForm.location || null
          }
        })
        if (response.data && response.data.code === 200) {
          this.statistics = response.data.data || {
            totalAmount: 0,
            belowThreshold: 0,
            totalRecords: 0
          }
        } else {
          this.$message.error(response.data.message || '获取统计数据失败')
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.statistics = {
          totalAmount: 0,
          belowThreshold: 0,
          totalRecords: 0
        }
      }
    },
    // 获取库存历史记录
    async fetchHistory(inventoryId) {
      try {
        // 暂时使用模拟数据，因为后端接口不存在
        this.historyData = []
        this.$message.warning('历史记录功能暂未实现')
        const response = await this.$http.get(`/inventory/change-record`)
        this.historyData = response.data
      } catch (error) {
        console.error('获取历史记录失败:', error)
        this.$message.error('获取历史记录失败')
      }
    },
    // 初始化库存趋势图
    initStockTrendChart(data) {
      if (!this.stockTrendChart) {
        this.stockTrendChart = echarts.init(this.$refs.stockTrendChart)
      }
      const option = {
        title: {
          text: '库存变化趋势'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.dates
        },
        yAxis: {
          type: 'value',
          name: '库存量'
        },
        series: [{
          data: data.amounts,
          type: 'line',
          smooth: true
        }]
      }
      this.stockTrendChart.setOption(option)
    },
    // 获取库存状态类型
    getStatusType(row) {
      if (!row.currentAmount) return ''
      if (row.chemical && row.currentAmount < row.chemical.warningThreshold) {
        return 'danger'
      }
      if (row.chemical && row.currentAmount < row.chemical.warningThreshold * 1.2) {
        return 'warning'
      }
      return 'success'
    },
    // 获取库存状态文本
    getStatusText(row) {
      if (!row.currentAmount) return '未知'
      if (row.chemical && row.currentAmount < row.chemical.warningThreshold) {
        return '库存不足'
      }
      if (row.chemical && row.currentAmount < row.chemical.warningThreshold * 1.2) {
        return '库存预警'
      }
      return '正常'
    },
    // 获取数量显示样式
    getAmountClass(row) {
      if (!row.currentAmount || !row.chemical) return {}
      return {
        'amount-normal': row.currentAmount >= row.chemical.warningThreshold * 1.2,
        'amount-warning': row.currentAmount < row.chemical.warningThreshold * 1.2 && row.currentAmount >= row.chemical.warningThreshold,
        'amount-danger': row.currentAmount < row.chemical.warningThreshold
      }
    },
    // 获取操作类型样式
    getOperationType(type) {
      const types = {
        '入库': 'success',
        '出库': 'warning',
        '盘点': 'info'
      }
      return types[type] || ''
    },
    // 处理查询
    handleSearch() {
      this.pagination.currentPage = 1
      this.fetchData()
    },
    // 重置查询
    resetSearch() {
      this.searchForm = {
        chemicalName: '',
        location: '',
        status: ''
      }
      this.handleSearch()
    },
    // 处理详情
    handleDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
      this.$nextTick(() => {
        this.fetchStockTrend(row.chemicalId)
      })
    },
    // 处理历史记录
    handleHistory(row) {
      this.fetchHistory(row.id)
      this.historyDialogVisible = true
    },
    // 处理盘点
    handleCheck(row) {
      this.checkForm = {
        id: row.id,
        currentAmount: row.currentAmount,
        unit: row.unit,
        notes: ''
      }
      this.checkDialogVisible = true
    },
    // 提交盘点
    submitCheck() {
      this.$refs.checkForm.validate(valid => {
        if (valid) {
          this.$http.put(`/inventory/update/${this.checkForm.id}/amount`, null, {
            params: {
              amount: this.checkForm.currentAmount
            }
          }).then(response => {
            if (response.data && response.data.code === 200) {
              this.$message.success('盘点成功')
              this.checkDialogVisible = false
              this.fetchData()
              this.fetchStatistics()
            } else {
              this.$message.error(response.data.message || '盘点失败')
            }
          }).catch(error => {
            this.$message.error('盘点失败：' + error.message)
          })
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
    // 入库
    handleStorageIn(row) {
      console.log('处理入库操作:', row)
      const now = new Date()
      const formattedDate = now.toISOString().substr(0, 19).replace('T', ' ')
      const batchNo = 'BN' + now.getTime().toString().substr(-8)      
      this.storageInForm = {
        inventoryId: row.id,
        chemicalId: row.chemicalId,
        chemicalName: row.chemical ? row.chemical.name : '',
        amount: 0,
        unit: row.unit,
        batchNo: batchNo,
        supplier: '',
        operatorId: 1,
        storageTime: formattedDate,
        notes: ''
      }
      this.storageInDialogVisible = true
    },
    submitStorageIn() {
      this.$refs.storageInForm.validate(async valid => {
        if (valid) {
          try {
            this.loading = true
            console.log('提交入库数据:', this.storageInForm)
            
            // 构建入库记录数据 - 注意字段名称与后端保持一致
            const storageRecord = {
              chemicalId: this.storageInForm.chemicalId,
              chemicalName: this.storageInForm.chemicalName,
              inventoryId: this.storageInForm.inventoryId,
              amount: this.storageInForm.amount,
              unit: this.storageInForm.unit,
              batchNo: this.storageInForm.batchNo,
              supplier: this.storageInForm.supplier,
              storageTime: this.storageInForm.storageTime,
              operatorId: this.storageInForm.operatorId || 1,
              notes: this.storageInForm.notes
            }
            
            console.log('发送入库记录数据:', storageRecord)
            
            // 创建入库记录
            const response = await this.$http.post('/storage/add', storageRecord)
            if (response.data && response.data.code === 200) {
              this.$message.success('入库成功')
              this.storageInDialogVisible = false
              this.fetchData()
              this.fetchStatistics()
            } else {
              this.$message.error((response.data && response.data.message) || '入库失败')
            }
          } catch (error) {
            console.error('入库操作失败:', error)
            this.$message.error('入库失败: ' + (error.message || '未知错误'))
          } finally {
            this.loading = false
          }
        }
      })
    },
    // 出库
    handleStorageOut(row) {
      console.log('处理出库操作:', row)
      const now = new Date()
      const formattedDate = now.toISOString().substr(0, 19).replace('T', ' ')
      const batchNo = 'OUT' + now.getTime().toString().substr(-8)
      
      this.storageOutForm = {
        inventoryId: row.id,
        chemicalId: row.chemicalId,
        chemicalName: row.chemical ? row.chemical.name : '',
        currentAmount: row.currentAmount,
        amount: 0,
        unit: row.unit,
        batchNo: batchNo,
        recipient: '',
        purpose: '',
        operatorId: 1,
        outboundTime: formattedDate,
        notes: ''
      }
      this.storageOutDialogVisible = true
    },
    submitStorageOut() {
      this.$refs.storageOutForm.validate(async valid => {
        if (valid) {
          try {
            this.loading = true
            
            // 验证出库数量
            if (this.storageOutForm.amount > this.storageOutForm.currentAmount) {
              this.$message.error('出库数量不能超过当前库存量')
              this.loading = false
              return
            }
            
            console.log('提交出库数据:', this.storageOutForm)
            
            // 构建出库记录数据 - 注意字段名称与后端保持一致
            const outboundRecord = {
              chemicalId: this.storageOutForm.chemicalId,
              chemicalName: this.storageOutForm.chemicalName,
              inventoryId: this.storageOutForm.inventoryId,
              amount: this.storageOutForm.amount,
              unit: this.storageOutForm.unit,
              batchNo: this.storageOutForm.batchNo,
              recipient: this.storageOutForm.recipient,
              purpose: this.storageOutForm.purpose,
              outboundTime: this.storageOutForm.outboundTime,
              operatorId: this.storageOutForm.operatorId || 1,
              notes: this.storageOutForm.notes
            }
            
            console.log('发送出库记录数据:', outboundRecord)
            
            // 创建出库记录
            const response = await this.$http.post('/outbound/add', outboundRecord)
            
            if (response.data && response.data.code === 200) {
              this.$message.success('出库成功')
              this.storageOutDialogVisible = false
              this.fetchData()
              this.fetchStatistics()
            } else {
              this.$message.error((response.data && response.data.message) || '出库失败')
            }
          } catch (error) {
            console.error('出库操作失败:', error)
            this.$message.error('出库失败: ' + (error.message || '未知错误'))
          } finally {
            this.loading = false
          }
        }
      })
    },
    // 获取库存趋势数据
    async fetchStockTrend(chemicalId) {
      try {
        const response = await this.$http.get('/inventory/trend', {
          params: { chemicalId }
        })
        
        if (response.data && response.data.code === 200) {
          this.initStockTrendChart(response.data.data)
        } else {
          this.$message.warning('获取趋势数据失败')
          // 使用模拟数据初始化图表
          this.initStockTrendChart({
            dates: ['无数据'],
            amounts: [0]
          })
        }
      } catch (error) {
        console.error('获取趋势数据失败:', error)
        this.$message.error('获取趋势数据失败')
        this.initStockTrendChart({
          dates: ['无数据'],
          amounts: [0]
        })
      }
    }
  },
  beforeDestroy() {
    console.log('组件销毁前')
    // 清理图表实例
    if (this.stockTrendChart) {
      this.stockTrendChart.dispose()
      this.stockTrendChart = null
    }
  }
}
</script>

<style scoped>
.inventory-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
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
  color: #409EFF;
}

.warning-card .statistics-value {
  color: #E6A23C;
}

.danger-card .statistics-value {
  color: #F56C6C;
}

.info-card .statistics-value {
  color: #909399;
}

.amount-normal {
  color: #67C23A;
}

.amount-warning {
  color: #E6A23C;
}

.amount-danger {
  color: #F56C6C;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.detail-charts {
  margin-top: 20px;
}

.chart-container {
  margin-top: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 20px;
}

.tool-row {
  margin-bottom: 20px;
}

.statistics-card {
  margin-top: 20px;
}

.statistics-item {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.statistics-item .label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.statistics-item .value {
  font-size: 24px;
  color: #303133;
  font-weight: bold;
}

.statistics-item .value.warning {
  color: #e6a23c;
}
</style> 