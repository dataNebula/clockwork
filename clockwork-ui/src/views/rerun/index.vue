<template>
  <div class="app-container">
    <div class="filter-container" v-show="showFilterContainer">
      <div class="box_l">
        <div class="inner_contain">
          <el-row>
            <el-col :span="8">
              <el-date-picker
                @change="changeNowTime()"
                class="handle-input"
                clearable
                placeholder="开始时间"
                size="medium"
                type="date"
                v-model="nowTime"
                value-format="yyyy-MM-dd"
              ></el-date-picker>
            </el-col>
            <el-col :span="8">
              <el-input
                class="handle-input"
                clearable
                placeholder="任务名称"
                v-model="taskName"
              />
            </el-col>
            <el-col :span="8">
              <el-input
                class="handle-input"
                clearable
                placeholder="批次号"
                type="number"
                v-model="rerunBatchNumber"
              />
            </el-col>
          </el-row>
          <div v-if="showMore">
            <el-row>
              <el-col :span="8">
                <el-input
                  class="handle-input"
                  clearable
                  onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                  placeholder="taskId"
                  type="number"
                  v-model="taskId"
                ></el-input>
              </el-col>
              <el-col :span="8">
                <el-input
                  class="handle-input"
                  clearable
                  placeholder="操作人"
                  v-model="operatorName"
                />
              </el-col>
              <el-col :span="8"></el-col>
            </el-row>
          </div>
          <el-button
            :class="showMore ? 'upBtn' : 'moreBtn'"
            :icon="showMore ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
            @click="moreSearch"
            round
            size="medium"
            type="success"
          >
            {{ showMore ? "收起" : "更多" }}
          </el-button>
        </div>
      </div>
      <div class="box_r">
        <el-button
          @click="reset"
          class="fr"
          icon="el-icon-refresh-left"
          size="medium"
          type="info"
        >
          重置
        </el-button>
        <el-button
          @click="getData"
          class="marR15 fr"
          icon="el-icon-search"
          size="medium"
          type="primary"
        >
          搜索
        </el-button>
      </div>
    </div>

    <el-table
      :data="tableData"
      append
      border
      ref="singleTable"
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column
        label="日志Id"
        prop="logId"
        width="100"
      ></el-table-column>
      <el-table-column
        label="序号"
        prop="id"
        sortable
        width="80"
      ></el-table-column>
      <el-table-column
        label="任务ID"
        prop="taskId"
        width="120"
      ></el-table-column>
      <el-table-column
        label="任务名"
        prop="taskName"
        width="180"
      ></el-table-column>
      <el-table-column
        label="批次号"
        prop="rerunBatchNumber"
        width="180"
      ></el-table-column>
      <el-table-column label="Root" prop="isFirst" width="80">
        <template slot-scope="scorp">
          {{ scorp.row.isFirst | isNotFilter }}
        </template>
      </el-table-column>

      <el-table-column label="执行类型" prop="executeType" width="100">
        <template slot-scope="scorp">
          {{ scorp.row.executeType | executeTypeFilter }}
        </template>
      </el-table-column>
      <el-table-column label="pid" prop="pid" width="100"></el-table-column>
      <el-table-column label="状态" prop="status" width="140">
        <template slot-scope="scorp">
          <el-tag :type="scorp.row.status | statusStyleFilter">
            {{ scorp.row.status | statusFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否结束" prop="isEnd" width="100">
        <template slot-scope="scorp">
          <el-tag :type="scorp.row.isEnd ? 'success' : 'primary'">
            {{ scorp.row.isEnd | isNotFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="开始时间"
        prop="startTime"
        width="180"
      ></el-table-column>
      <el-table-column
        label="执行时间"
        prop="executeTime"
        width="180"
      ></el-table-column>
      <el-table-column
        label="结束时间"
        prop="endTime"
        width="180"
      ></el-table-column>
      <el-table-column
        label="参数"
        prop="parameter"
        width="160"
      ></el-table-column>
      <el-table-column
        label="日志名"
        prop="logName"
        width="280"
      ></el-table-column>
      <el-table-column
        label="nodeId"
        prop="nodeId"
        width="80"
      ></el-table-column>
      <el-table-column
        label="nodeIp"
        prop="nodeIp"
        width="160"
      ></el-table-column>
      <el-table-column
        label="nodePort"
        prop="nodePort"
        width="80"
      ></el-table-column>
      <el-table-column
        label="操作人"
        prop="operatorName"
        width="220"
      ></el-table-column>
      <el-table-column
        label="更新时间"
        prop="updateTime"
        width="180"
      ></el-table-column>
      <el-table-column
        label="创建时间"
        prop="createTime"
        width="180"
      ></el-table-column>
      <el-table-column fixed="left" label="操作" width="100">
        <template slot-scope="scope">
          <div class="operate-block">
            <el-tooltip
              class="item"
              content="查看日志"
              effect="dark"
              placement="top"
            >
              <em>
                <i
                  @click="handleCatLogFile(scope.row)"
                  class="el-icon-postcard"
                ></i>
              </em>
            </el-tooltip>
            <el-tooltip
              class="item"
              content="重新运行"
              effect="dark"
              placement="top"
            >
              <em>
                <i
                  @click="handleRerunTask(scope.row)"
                  class="el-icon-refresh-left"
                ></i>
              </em>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 40, 100]"
        :total="total"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        layout="total, sizes, prev, pager, next, jumper"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      tableData: [],
      currentPage: 1,
      total: 0,
      pageSize: 10,
      taskId: null,
      taskName: null,
      rerunBatchNumber: null,
      executeType: null,
      operatorName: null,
      isAdmin: null,
      is_search: false,
      loginName: null,
      showFilterContainer: true, // 是否显示查询条件
      nowTime: 0,
      startTime: null,
      endTime: null,
      showMore: false
    };
  },
  created() {
    //开始时间 结束时间 默认值
    this.nowTime = new Date();
    const y = this.nowTime.getFullYear();
    const m = this.nowTime.getMonth() + 1;
    const d = this.nowTime.getDate();
    const ymd = y + "-" + m + "-" + d;
    this.startTime = ymd + " " + "00:00:00";
    this.endTime = ymd + " " + "23:59:59";
    this.getData();
  },
  methods: {
    changeNowTime() {
      this.nowTime == null
        ? (this.startTime = null)
        : (this.startTime = this.nowTime + " " + "00:00:00");
      this.nowTime == null
        ? (this.endTime = null)
        : (this.endTime = this.nowTime + " " + "23:59:59");
    },
    formatterExecuteType(row, column) {
      switch (row.executeType) {
        case 1:
          return "自动执行";
          break;
        case 0:
          return "手动执行";
          break;
        case 3:
          return "补数任务";
          break;
        default:
          return row.executeType;
      }
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      console.log(`每页 ${pageSize} 条`);
      this.getData();
    },
    handleCurrentChange(currentPage) {
      this.currentPage = currentPage;
      console.log(`当前页: ${currentPage}`);
      this.getData();
    },
    reset() {
      this.taskId = null;
      this.taskName = null;
      this.rerunBatchNumber = null;
      this.executeType = null;
      this.operatorName = null;
      this.getData();
    },
    getData: function() {
      this.loading = true;
      let _self = this;

      _self.loginName = this.$store.state.user.name;
      _self.isAdmin = this.$store.state.user.isAdmin;

      if (typeof this.$route.params.showFilterContainer !== "undefined") {
        _self.showFilterContainer = this.$route.params.showFilterContainer;
      }
      if (typeof this.$route.params.rerunBatchNumber !== "undefined") {
        _self.rerunBatchNumber = this.$route.params.rerunBatchNumber;
      }
      if (typeof this.$route.params.executeType !== "undefined") {
        _self.executeType = this.$route.params.executeType;
      }

      let pageParam = {};
      pageParam.pageNum = _self.currentPage;
      pageParam.pageSize = _self.pageSize;
      pageParam.userName = _self.loginName;
      if (_self.name === "") {
        _self.name = null;
      }
      pageParam.condition = JSON.stringify({
        taskId: this.taskId === "" ? null : this.taskId,
        taskName: this.taskName === "" ? null : this.taskName,
        rerunBatchNumber:
          this.rerunBatchNumber === "" ? null : this.rerunBatchNumber,
        operatorName: this.operatorName === "" ? null : this.operatorName,
        executeType:
          this.executeType === "" || this.executeType == null
            ? 0
            : this.executeType,
        startTime:
          this.startTime === "" || this.executeType === 2
            ? null
            : this.startTime,
        endTime:
          this.endTime === "" || this.executeType === 2 ? null : this.endTime
      });
      _self.tableData = [];
      _self.axios
        .post(
          "/clockwork/web/task/rerun/searchTaskRerunPageList",
          JSON.stringify(pageParam),
          {
            headers: {
              "Content-Type": "application/json"
            }
          }
        )
        .then(response => {
          if (response.code === "OK") {
            for (let i = 0; i < response.data.list.length; i++) {
              let obj = {};
              obj.id = response.data.list[i].id;
              obj.taskId = response.data.list[i].taskId;
              obj.taskName = response.data.list[i].taskName;
              obj.rerunBatchNumber = response.data.list[i].rerunBatchNumber;
              obj.isFirst = response.data.list[i].isFirst;
              obj.logId = response.data.list[i].logId;
              obj.executeType = response.data.list[i].executeType;
              obj.status = response.data.list[i].status;
              obj.pid = response.data.list[i].pid;
              obj.isEnd = response.data.list[i].isEnd;
              obj.startTime = response.data.list[i].startTime;
              obj.executeTime = response.data.list[i].executeTime;
              obj.endTime = response.data.list[i].endTime;
              obj.parameter = response.data.list[i].parameter;
              obj.nodeId = response.data.list[i].nodeId;
              obj.nodeIp = response.data.list[i].nodeIp;
              obj.nodePort = response.data.list[i].nodePort;
              obj.logName = response.data.list[i].logName;
              obj.operatorName = response.data.list[i].operatorName;
              obj.updateTime = response.data.list[i].updateTime;
              obj.createTime = response.data.list[i].createTime;
              _self.tableData.push(obj);
            }
            _self.total = response.data.total;
            this.loading = false;
          } else {
            _self.$message({
              message: response.msg,
              type: "warning",
              duration: 5000
            });
            this.loading = false;
          }
        })
        .catch(err => {
          this.loading = false;
          console.log(err);
        });
    },
    search() {
      this.is_search = true;
    },
    handleCatLogFile(row) {
      if (row.nodeIp === null || row.nodeIp === "") {
        this.$message({
          message: "暂无日志，请稍后查看!",
          type: "warning",
          duration: 5000
        });
        return;
      }

      this.$router.push({
        name: "logFile",
        params: {
          nodeIp: row.nodeIp,
          nodePort: row.nodePort,
          logName: row.logName,
          createTime: row.createTime,
          activeMenu: "/log/rerunList" //设置当前高亮菜单
        }
      });
    },
    handleRerunTask(row) {
      let _self = this;
      _self.axios
        .post(
          "/clockwork/web/task/operation/rerunTaskHis",
          _self.qs.stringify({
            taskId: row.taskId,
            logId: row.logId,
            taskReRunType: 0,
            operatorName: _self.loginName
          })
        )
        .then(response => {
          if (response.code === "OK") {
            _self.$message({
              message: "提交成功，等待执行",
              type: "success"
            });
            this.getData();
          } else {
            _self.$message({
              message: response.msg,
              type: "warning",
              duration: 5000
            });
          }
        })
        .catch(err => {
          console.log(err);
        });
    },
    moreSearch() {
      this.showMore = !this.showMore;
    }
  }
};
</script>
