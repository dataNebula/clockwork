<template>
  <div class="app-container">
    <div class="filter-container">
      <div class="box_l">
        <div class="inner_contain">
          <el-row>
            <el-col :span="8">
              <el-input
                class="handle-input"
                clearable
                onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                placeholder="DagID"
                type="number"
                v-model="id"
              />
            </el-col>
            <el-col :span="8">
              <el-input
                class="handle-input"
                clearable
                onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                placeholder="taskId"
                type="number"
                v-model="leaderTaskId"
              />
            </el-col>
            <el-col :span="8">
              <el-input
                class="handle-input"
                clearable
                placeholder="taskName"
                v-model="leaderTaskName"
              />
            </el-col>
          </el-row>
        </div>
      </div>
      <div class="box_r">
        <el-button
          @click="refreshHandler"
          class="fr"
          icon="el-icon-refresh-left"
          size="medium"
          type="success"
        >
          刷新
        </el-button>
        <el-button
          @click="reset"
          class="marR15 fr"
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

    <!-- 弹窗：任务详情列表 -->
    <el-dialog
      :visible.sync="dialogTableVisible"
      title="任务详情列表"
      width="75%"
    >
      <el-table
        :data="taskListData"
        append
        border
        ref="singleTable"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column label="ID" prop="id" sortable width="120" />
        <el-table-column label="任务名" prop="name" width="200" />
        <el-table-column label="组ID" prop="groupId" sortable width="120" />
        <el-table-column label="组名称" prop="groupName" width="200" />
        <el-table-column label="状态" prop="status" width="140">
          <template slot-scope="scorp">
            <el-tag :type="scorp.row.status | statusStyleFilter">
              {{ scorp.row.status | statusFilter }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="上下线" prop="online" width="75">
          <template slot-scope="scorp">
            <el-tag :type="scorp.row.online ? 'success' : 'warning'">
              {{ scorp.row.online | onlineFilter }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="触发类型" prop="triggerMode" width="95" />
        <el-table-column label="依赖任务" prop="dependencyId" width="180" />
        <el-table-column label="频率" prop="runFrequency" width="110" />
        <el-table-column
          label="下一次触发时间"
          prop="nextTriggerTime"
          width="160"
        />
        <el-table-column
          label="上次开始时间"
          prop="lastStartTime"
          width="160"
        />
        <el-table-column label="上次结束时间" prop="lastEndTime" width="160" />
        <el-table-column label="创建者" prop="createUser" width="230" />
        <el-table-column label="创建时间" prop="createTime" width="160" />
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="taskListCurrentPage"
          :page-size="taskListPageSize"
          :page-sizes="[10, 20, 40, 100]"
          :total="taskListTotal"
          @current-change="taskListHandleCurrentChange"
          @size-change="taskListHandleSizeChange"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-dialog>
    <!-- 弹窗：任务详情列表 END -->

    <!-- 弹窗：任务关系图 -->
    <el-dialog
      :visible.sync="dialogGraphVisible"
      title="任务关系图"
      width="85%"
    >
      <div class="chart" id="dialogChart" style="width: 100%;height: 500px" />
    </el-dialog>
    <!-- 弹窗：任务关系图 END -->

    <!-- tableData START -->
    <el-table
      :data="tableData"
      append
      border
      ref="singleTable"
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column label="ID" prop="id" sortable width="100" />
      <el-table-column label="名称" prop="name" width="120" />
      <el-table-column label="起始任务ID" prop="leaderTaskId" width="120" />
      <el-table-column
        label="起始任务名"
        min-width="180"
        prop="leaderTaskName"
      />
      <el-table-column label="任务数" prop="taskCount" width="100">
        <template slot-scope="scope">
          <p @click="handleTaskList(scope.row.id)" style="color: #2a88bd">
            {{ scope.row.taskCount }}
          </p>
        </template>
      </el-table-column>
      <el-table-column label="描述" prop="description" width="200" />
      <el-table-column label="更新时间" prop="updateTime" width="180" />
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column fixed="left" label="操作" width="150">
        <template slot-scope="scope">
          <div class="operate-block">
            <el-tooltip
              class="item"
              content="任务列表"
              effect="dark"
              placement="top"
            >
              <em>
                <i
                  @click="handleTaskList(scope.row.id)"
                  class="el-icon-tickets"
                />
              </em>
            </el-tooltip>
            <el-tooltip
              class="item"
              content="任务关系图"
              effect="dark"
              placement="top"
            >
              <em>
                <i
                  @click="buildDagGraphForPositionByDagId(scope.row.id)"
                  class="el-icon-set-up"
                />
              </em>
            </el-tooltip>
            <el-tooltip
              class="item"
              content="刷新dag信息"
              effect="dark"
              placement="top"
            >
              <em>
                <i
                  @click="refreshDagInfoById(scope.row)"
                  class="el-icon-refresh-left"
                />
              </em>
            </el-tooltip>
            <el-popover
              :popper-options="{ boundariesElement: 'body' }"
              placement="right"
              trigger="click"
              v-if="loginName === scope.row.createUser || isAdmin"
              width="200"
            >
              <ul class="operate-ul">
                <li>
                  <el-popconfirm
                    @onConfirm="rerunTaskByDagId(scope.row.id)"
                    icon="el-icon-info"
                    icon-color="red"
                    title="你确定要触发该Dag下所有任务吗？"
                  >
                    <i class="el-icon-thumb" slot="reference">
                      触发该所有任务
                    </i>
                  </el-popconfirm>
                </li>
                <li>
                  <el-popconfirm
                    title="确定要成环检测？"
                    @onConfirm="ringCheck(scope.row)"
                  >
                    <div slot="reference">
                      <i class="el-icon-help">
                        成环检测
                      </i>
                    </div>
                  </el-popconfirm>
                </li>
              </ul>
              <em slot="reference"><i class="el-icon-more" /></em>
            </el-popover>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <!-- tableData END -->
    <div class="pagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 40, 100]"
        :total="total"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
  </div>
</template>

<script>
require("../../../node_modules/echarts/lib/chart/chord");
require("../../../node_modules/echarts/lib/chart/graph");
export default {
  data() {
    return {
      dialogTableVisible: false,
      dialogGraphVisible: false,
      loading: false,
      tableData: [],
      currentPage: 1,
      total: 0,
      pageSize: 10,
      taskListData: [],
      dagId: null,
      taskListCurrentPage: 1,
      taskListTotal: 0,
      taskListPageSize: 10,
      id: null,
      name: null,
      leaderTaskId: null,
      leaderTaskName: null,
      isAdmin: null,
      is_search: false,
      loginName: null,
      // 图
      nodes: [],
      links: [],
      taskForm: {
        taskName: "",
        upDeepLevel: 1,
        downDeepLevel: 1
      },
      focus: 0,
      showMore: false
    };
  },
  created() {
    this.getData();
  },
  methods: {
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
    taskListHandleSizeChange(taskListPageSize) {
      this.taskListPageSize = taskListPageSize;
      console.log(`每页 ${taskListPageSize} 条`);
      this.handleTaskList(null);
    },
    taskListHandleCurrentChange(taskListCurrentPage) {
      this.taskListCurrentPage = taskListCurrentPage;
      console.log(`当前页: ${taskListCurrentPage}`);
      this.handleTaskList(null);
    },
    reset() {
      this.id = null;
      this.name = null;
      this.leaderTaskId = null;
      this.leaderTaskName = null;
      this.getData();
    },
    getData() {
      this.loading = true;
      let _self = this;

      _self.loginName = this.$store.state.user.name;
      _self.isAdmin = this.$store.state.user.isAdmin;

      let pageParam = {};
      pageParam.pageNum = _self.currentPage;
      pageParam.pageSize = _self.pageSize;
      pageParam.userName = _self.loginName;
      if (_self.name === "") {
        _self.name = null;
      }
      pageParam.condition = JSON.stringify({
        id: this.id === "" ? null : this.id,
        name: this.name === "" ? null : this.name,
        leaderTaskId: this.leaderTaskId === "" ? null : this.leaderTaskId,
        leaderTaskName: this.leaderTaskName === "" ? null : this.leaderTaskName
      });
      _self.tableData = [];
      _self.axios
        .post(
          "/clockwork/web/dag/searchDagPageList",
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
              obj.name = response.data.list[i].name;
              obj.leaderTaskId = response.data.list[i].leaderTaskId;
              obj.leaderTaskName = response.data.list[i].leaderTaskName;
              obj.taskCount = response.data.list[i].taskCount;
              obj.description = response.data.list[i].description;
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
    handleTaskList(dagId) {
      this.dialogTableVisible = true;
      let _self = this;

      let pageParam = {};
      pageParam.pageNum = _self.taskListCurrentPage;
      pageParam.pageSize = _self.taskListPageSize;
      pageParam.userName = _self.loginName;
      if (_self.isAdmin) {
        pageParam.role = "admin";
      } else {
        pageParam.role = "normal";
      }

      let task = {};
      if (dagId === null || dagId === "") {
        task.dagId = _self.dagId;
      } else {
        task.dagId = dagId;
        _self.dagId = dagId;
      }
      task.online = true;
      _self.taskListData = [];

      pageParam.condition = JSON.stringify(task);
      _self.axios
        .post(
          "/clockwork/web/task/searchPageListTask",
          JSON.stringify(pageParam),
          {
            headers: {
              "Content-Type": "application/json"
            }
          }
        )
        .then(response => {
          if (response.code === "OK") {
            if (response.data.list) {
              for (let i = 0; i < response.data.list.length; i++) {
                let obj = {};
                obj.id = response.data.list[i].id;
                obj.name = response.data.list[i].name;
                obj.dagId = response.data.list[i].dagId;
                obj.groupId = response.data.list[i].groupId;
                obj.groupName = response.data.list[i].groupName;
                obj.status = response.data.list[i].status;
                obj.online = response.data.list[i].online;
                if (response.data.list[i].triggerMode === 1) {
                  obj.triggerMode = "时间触发";
                  obj.runFrequency =
                    response.data.list[i].runFrequency +
                    " " +
                    response.data.list[i].timeType +
                    "/per";
                  obj.nextTriggerTime = response.data.list[i].nextTriggerTime;
                } else if (response.data.list[i].triggerMode === 0) {
                  obj.triggerMode = "依赖触发";
                  obj.dependencyId = response.data.list[i].dependencyId;
                } else if (response.data.list[i].triggerMode === 2) {
                  obj.triggerMode = "时间触发和依赖触发";
                  obj.dependencyId = response.data.list[i].dependencyId;
                  obj.runFrequency =
                    response.data.list[i].runFrequency +
                    " " +
                    response.data.list[i].timeType +
                    "/per";
                  obj.nextTriggerTime = response.data.list[i].nextTriggerTime;
                } else if (response.data.list[i].triggerMode === 3) {
                  obj.triggerMode = "信号触发";
                }
                obj.lastStartTime = response.data.list[i].lastStartTime;
                obj.lastEndTime = response.data.list[i].lastEndTime;
                obj.createUser = response.data.list[i].createUser;
                obj.createTime = response.data.list[i].createTime;

                _self.taskListData.push(obj);
              }
              _self.taskListTotal = response.data.total;
            }
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
    refreshHandler() {
      this.loading = true;
      this.axios
        .get("/clockwork/web/dag/cleanEmptyDagInfo", null)
        .then(response => {
          if (response.code === "OK") {
            this.$message({
              message: "刷新成功",
              type: "success"
            });
            this.getData();
          } else {
            this.$message({
              message: response.msg,
              type: "warning",
              duration: 5000
            });
          }
        })
        .catch(err => {
          console.log(err);
        });
      this.loading = false;
    },
    buildDagGraphForPositionByDagId(dagId) {
      this.dialogGraphVisible = true;
      let _self = this;
      _self.nodes = [];
      _self.links = [];
      _self.axios
        .get("/clockwork/web/graph/buildDagGraphForPositionByDagId", {
          params: {
            dagId: dagId,
            userName: _self.loginName
          }
        })
        .then(response => {
          if (response.code === "OK") {
            _self.nodes = JSON.parse(response.data.nodes);
            _self.links = JSON.parse(response.data.links);
            _self.dialogDrawGraph();
          } else {
            let dialogChart = this.echarts.init(
              document.getElementById("dialogChart")
            );
            dialogChart.clear();
            _self.$message({
              message: response.msg + ": " + response.data,
              type: "warning",
              duration: 5000
            });
          }
        })
        .catch(err => {
          console.log(err);
        });
    },
    dialogDrawGraph() {
      // 绘制图表
      let _self = this;
      let dialogChart = this.echarts.init(
        document.getElementById("dialogChart")
      );
      dialogChart.clear();
      dialogChart.on("click", function(params) {
        if (_self.focus === 0) {
          //点击高亮
          _self.dialogChart.dispatchAction({
            type: "focusNodeAdjacency",
            dataIndex: params.dataIndex // 使用 dataIndex 来定位节点。
          });
          if (params.dataType === "edge") {
            _self.handleClick(params);
          } else if (params.dataType === "node") {
            if (_self.firstNode === "") {
              _self.firstNode = params.name;
            } else {
              _self.secondNode = params.name;
            }
          }
          _self.focus = 1;
        } else {
          // 取消高亮
          _self.dialogChart.dispatchAction({
            type: "unfocusNodeAdjacency",
            // 使用 seriesId 或 seriesIndex 或 seriesName 来定位 series.
            seriesIndex: params.seriesIndex
          });
          _self.focus = 0;
        }
      });
      _self.dialogChart = dialogChart;

      _self.nodes.forEach(function(node) {
        // 0是依赖触发、1是时间触发
        switch (node.category) {
          case 1:
            node.symbol = "diamond";
            node.symbolSize = 25;
            break;
          case 2:
            node.symbol = "diamond";
            node.symbolSize = 25;
            break;
          case 3:
            node.symbol = "rect";
            node.symbolSize = 25;
            break;
          case 4:
            node.symbol = "roundRect";
            node.symbolSize = 25;
            break;
          default:
            node.symbol = "circle";
            node.symbolSize = 25;
            break;
        }
        switch (node.task.status) {
          case "enable":
            node.category = 1;
            break;
          case "submit":
            node.category = 2;
            break;
          case "running":
            node.category = 3;
            break;
          case "success":
            node.category = 4;
            break;
          case "failed":
            node.category = 5;
            break;
          default:
            node.category = 0;
            break;
        }
      });
      let option = {
        title: {
          top: "5%"
        },
        toolbox: {
          show: true,
          left: "right",
          feature: {
            restore: { show: true },
            magicType: { show: true, type: ["force", "chord"] },
            saveAsImage: { show: true }
          }
        },
        color: [
          "#3d6099",
          "#76c5b5",
          "#ED7C30",
          "#EEEE00",
          "#0ec810",
          "#ee0719"
        ],
        legend: {
          // orient: 'vertical',
          left: "left",
          data: [
            { name: "disable" },
            { name: "enable" },
            { name: "submit" },
            { name: "running" },
            { name: "success" },
            { name: "failed" }
          ]
        },
        series: [
          {
            name: "Task DAG",
            // top:'40%',
            position: "top",
            type: "graph",
            layout: "none",
            nodes: _self.nodes,
            links: _self.links,
            categories: [
              { name: "disable" },
              { name: "enable" },
              { name: "submit" },
              { name: "running" },
              { name: "success" },
              { name: "failed" }
            ],
            roam: true,
            center: ["0", "10%"],
            // focusNodeAdjacency: true,
            draggable: true, //指示节点是否可以拖动
            edgeSymbol: ["", "arrow"], // 边两端的标记类型，可以是一个数组分别指定两端，也可以是单个统一指定。默认不显示标记，常见的可以设置为箭头
            edgeSymbolSize: 9,
            force: {
              //力引导图基本配置
              repulsion: 100, // 节点之间的斥力因子。支持数组表达斥力范围，值越大斥力越大。
              gravity: 0.01, //节点受到的向中心的引力因子。该值越大节点越往中心点靠拢。
              edgeLength: 300, //边的两个节点之间的距离，这个距离也会受 repulsion。[10, 50] 。值越小则长度越长
              layoutAnimation: true //因为力引导布局会在多次迭代后才会稳定，这个参数决定是否显示布局的迭代动画，在浏览器端节点数据较多（>100）的时候不建议关闭，布局过程会造成浏览器假死。
            },
            itemStyle: {
              normal: {
                borderColor: "#fff",
                borderWidth: 1,
                shadowBlur: 10,
                shadowColor: "rgba(0, 0, 0, 0.3)"
              }
            },
            label: {
              // 图形上的文本标签，可用于说明图形的一些数据信息
              normal: {
                show: true,
                position: "inside",
                formatter: function(params) {
                  return params.data.name;
                }
              },
              emphasis: {
                show: true, //显示
                position: "auto", //相对于节点标签的位置
                //回调函数，你期望节点标签上显示什么
                formatter: function(params) {
                  let status = "";
                  let triggerTime = "";
                  let lastStartTime = "";
                  let lastEndTime = "";
                  if (typeof params.data.task.status !== "undefined") {
                    status = params.data.task.status;
                  }
                  if (typeof params.data.task.triggerTime !== "undefined") {
                    triggerTime = dateFormat(params.data.task.triggerTime);
                  }
                  if (typeof params.data.task.lastStartTime !== "undefined") {
                    lastStartTime = dateFormat(params.data.task.lastStartTime);
                  }
                  if (typeof params.data.task.lastEndTime !== "undefined") {
                    lastEndTime = dateFormat(params.data.task.lastEndTime);
                  }
                  return [
                    "{title|" + params.data.name + "}",
                    "{element|status:}{value|" + status + "}",
                    "{element|触发时间:}{value|" + triggerTime + "}",
                    "{element|start_time:}{value|" + lastStartTime + "}",
                    "{element|end_time:}{value|" + lastEndTime + "}"
                  ].join("\n");
                },
                backgroundColor: "rgba(242,242,242,0.5)",
                borderColor: "#aaa",
                borderWidth: 1,
                borderRadius: 4,
                padding: [4, 10],
                lineHeight: 26,
                rich: {
                  // 定义不同地方的文字的字体大小和颜色
                  title: {
                    align: "center",
                    color: "#fff",
                    fontSize: 18,
                    textShadowBlur: 2,
                    textShadowColor: "#000",
                    textShadowOffsetX: 0,
                    textShadowOffsetY: 1,
                    textBorderColor: "#333",
                    textBorderWidth: 2
                  },
                  element: {
                    color: "#000",
                    textBorderColor: "#fff",
                    textBorderWidth: 0,
                    // width: 80,
                    padding: [3, 10],
                    align: "left"
                  },
                  value: {
                    // color: '#ff8811',
                    color: "#ff1322",
                    textBorderColor: "#fff",
                    textBorderWidth: 0,
                    align: "right"
                  }
                }
              }
            },
            lineStyle: {
              normal: {
                color: "source",
                curveness: 0
              },
              emphasis: {
                // 高亮的图形样式。
                width: 2,
                color: "#000"
              }
            }
          }
        ]
      };
      dialogChart.setOption(option);
    },
    refreshDagInfoById(row) {
      this.loading = true;
      this.axios
        .get("/clockwork/web/dag/refreshDagInfoById", {
          params: { dagId: row.id }
        })
        .then(response => {
          if (response.code === "OK") {
            this.$message({
              message: "刷新Dag信息成功",
              type: "success"
            });
            this.getData();
          } else {
            this.$message({
              message: response.msg,
              type: "warning",
              duration: 5000
            });
          }
        })
        .catch(err => {
          console.log(err);
        });
      console.log(row.id);
    },
    rerunTaskByDagId(dagId) {
      let _self = this;
      _self.axios
        .post(
          "/clockwork/web/task/operation/rerunTaskByDagId",
          _self.qs.stringify({
            dagId: dagId,
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
    ringCheck(item) {
      this.loading = true;
      const { name } = this.$store.state.user;
      this.axios
        .get("/clockwork/web/dag/check/checkDagById", {
          params: {
            dagId: item.id,
            userName: name
          }
        })
        .then(response => {
          if (response && response.code === "OK") {
            if (response.data && !response.data.isrange) {
              this.$message({
                message: "未检测到成环",
                type: "success"
              });
            } else {
              this.$message({
                message: response.data && response.data.taskInfos,
                type: "warning",
                duration: 5000
              });
            }
          } else {
            this.$message.error("成环检测失败!");
          }
          this.loading = false;
        })
        .catch(error => {
          this.loading = false;
          console.log("接口错误");
          console.log(error);
        });
    },
    moreSearch() {
      this.showMore = !this.showMore;
    }
  }
};

function add0(m) {
  return m < 10 ? "0" + m : m;
}

function dateFormat(timeStamp) {
  let time = new Date(timeStamp);
  let y = time.getFullYear();
  let m = time.getMonth() + 1;
  let d = time.getDate();
  let h = time.getHours();
  let mm = time.getMinutes();
  let s = time.getSeconds();
  return (
    y +
    "-" +
    add0(m) +
    "-" +
    add0(d) +
    " " +
    add0(h) +
    ":" +
    add0(mm) +
    ":" +
    add0(s)
  );
}
</script>
