<template>
  <div class="app-container">
    <div class="head-panel">
      <div class="box_l">
        <el-row>
          <el-col :span="8">
            <el-date-picker
              @change="changeNowTime"
              class="handle-input"
              clearable
              placeholder="开始时间"
              size="medium"
              type="date"
              v-model="nowTime"
              value-format="yyyy-MM-dd"
            />
          </el-col>
          <el-col :span="8">
            <el-input
              class="handle-input"
              clearable
              placeholder="DAGid"
              v-model="dagid"
            />
          </el-col>
          <el-col :span="8">
            <el-input
              class="handle-input"
              clearable
              placeholder="操作人"
              v-model="operator"
            />
          </el-col>
        </el-row>
      </div>
      <div class="box_r">
        <el-button
          @click="ringCheck"
          class="marR15 fr"
          icon="el-icon-help"
          size="medium"
          type="primary"
        >
          成环检测
        </el-button>
        <el-button
          @click="fetchData"
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
      v-loading="pageLoading.tableLoading"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="主键" width="100" />
      <el-table-column prop="dagId" label="DAGid" sortable width="100" />
      <el-table-column prop="operator" label="操作人" width="180" />
      <el-table-column prop="isRange" label="是否成环">
        <template slot-scope="scorp">
          <el-tag :type="scorp.row.isRange | typeStatusFilter">
            {{ scorp.row.isRange | isNotFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="taskInfo" label="任务信息" width="600" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template slot-scope="scorp">
          {{ scorp.row.createTime | formatDayFilter }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180">
        <template slot-scope="scorp">
          {{ scorp.row.updateTime | formatDayFilter }}
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
  name: "RingCheckLog",
  data() {
    return {
      nowTime: 0,
      tableData: [],
      pageLoading: {
        tableLoading: false,
        checkLoading: false
      },
      currentPage: 1,
      pageSize: 10,
      total: 0,
      startTime: null,
      endTime: null,
      dagid: "",
      operator: null
    };
  },
  created() {
    //开始时间 结束时间 默认值
    this.nowTime = new Date();
    const y = this.nowTime.getFullYear();
    const m =
      this.nowTime.getMonth() + 1 < 10
        ? "0" + (this.nowTime.getMonth() + 1)
        : this.nowTime.getMonth() + 1;
    const d = this.nowTime.getDate();
    const ymd = y + "-" + m + "-" + d;
    this.startTime = ymd + " " + "00:00:00";
    this.endTime = ymd + " " + "23:59:59";

    this.fetchData();
  },
  methods: {
    ringCheck() {
      const { name } = this.$store.state.user;
      this.axios
        .get("/clockwork/web/dag/check/checkAllDags", {
          params: {
            userName: name
          }
        })
        .then(response => {
          if (response && response.code === "OK") {
            if (response.data) {
              this.$message({
                message: response.data,
                type: "success"
              });
            }
          } else {
            this.$message.error("成环检测失败!");
          }
        });
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.fetchData();
    },
    handleCurrentChange(currentPage) {
      this.currentPage = currentPage;
      this.fetchData();
    },
    changeNowTime() {
      this.nowTime
        ? (this.startTime = this.nowTime + " " + "00:00:00")
        : (this.startTime = null);
      this.nowTime
        ? (this.endTime = this.nowTime + " " + "23:59:59")
        : (this.endTime = null);
    },
    fetchData() {
      const { name, role } = this.$store.state.user;
      const {
        pageSize,
        currentPage,
        operator,
        dagid,
        startTime,
        endTime
      } = this;
      const condition = JSON.stringify({
        operator,
        id: null,
        dagId: dagid === "" ? null : dagid,
        beginDate: startTime,
        endDate: endTime
      });
      const paramObj = {
        role,
        condition,
        pageSize,
        pageNum: currentPage,
        userName: name
      };
      this.pageLoading.tableLoading = true;
      this.axios
        .post("/clockwork/web/dag/check/searchDagCheckList", {
          ...paramObj
        })
        .then(response => {
          if (response && response.code === "OK") {
            const { data } = response;
            this.tableData = data && data.list;
            this.total = data && data.total;
          } else {
            this.$message.error(response && response.msg);
          }
          this.pageLoading.tableLoading = false;
        })
        .catch(error => {
          this.pageLoading.tableLoading = false;
          console.log(error);
          this.$message.error("接口出错!");
        });
    }
  }
};
</script>
<style lang="scss" scoped>
.head-panel {
  overflow: hidden;
}
</style>
