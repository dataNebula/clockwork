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
                placeholder="请输入任务ID"
                size="medium"
                type="number"
                v-model="id"
              />
            </el-col>
            <el-col :span="8">
              <el-input
                class="handle-input"
                clearable
                placeholder="请输入任务名称"
                size="medium"
                v-model="name"
              />
            </el-col>
            <el-col :span="8">
              <el-select
                class="handle-input"
                clearable
                placeholder="任务状态"
                size="medium"
                v-model="status"
              >
                <el-option
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                  v-for="item in taskStatusOptions"
                />
              </el-select>
            </el-col>
          </el-row>
          <div v-if="showMore">
            <el-row>
              <el-col :span="8">
                <el-input
                  class="handle-input"
                  clearable
                  onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                  placeholder="请输入任务组ID"
                  size="medium"
                  type="number"
                  v-model="groupId"
                />
              </el-col>
              <el-col :span="8">
                <el-input
                  class="handle-input"
                  clearable
                  placeholder="请输入任务组名称"
                  size="medium"
                  v-model="groupName"
                />
              </el-col>
              <el-col :span="8">
                <el-input
                  class="handle-input"
                  clearable
                  onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                  placeholder="请输入DagID"
                  size="medium"
                  type="number"
                  v-model="dagId"
                />
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="8">
                <el-select
                  class="handle-input"
                  clearable
                  placeholder="上下线"
                  size="medium"
                  v-model="online"
                  @change="handleOnline"
                >
                  <el-option
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                    v-for="item in onlineOptions"
                  />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-select
                  class="handle-input"
                  clearable
                  placeholder="请输入任务触发方式"
                  size="medium"
                  v-model="triggerMode"
                >
                  <el-option
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                    v-for="item in triggerModeOptions"
                  />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-input
                  class="handle-input"
                  clearable
                  placeholder="请输入业务信息"
                  size="medium"
                  v-model="businessInfo"
                />
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="8">
                <el-input
                  class="handle-input"
                  clearable
                  placeholder="请输入创建者"
                  size="medium"
                  v-model="createUser"
                />
              </el-col>
              <el-col :span="8">
                <el-select
                  class="handle-input"
                  clearable
                  placeholder="来源"
                  size="medium"
                  v-model="source"
                >
                  <el-option
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                    v-for="item in sourceFilterOptions"
                  />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-date-picker
                  @change="changeNowTime()"
                  class="handle-input"
                  clearable
                  placeholder="请输入上次开始时间"
                  size="medium"
                  type="date"
                  v-model="nowTime"
                  value-format="yyyy-MM-dd"
                ></el-date-picker>
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
          @click="goAddTask"
          class="fr"
          icon="el-icon-circle-plus-outline"
          size="medium"
          type="primary"
        >
          添加任务
        </el-button>
        <el-button
          @click="reset"
          class="fr marR15"
          icon="el-icon-refresh-left"
          size="medium"
          type="info"
        >
          重置
        </el-button>
        <el-button
          @click="getData"
          class="fr"
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
      <el-table-column fixed="left" label="操作" width="120">
        <template slot-scope="scope">
          <div class="operate-block">
            <el-tooltip
              class="item"
              content="日志"
              effect="dark"
              placement="top"
            >
              <em>
                <i
                  @click="handleLatestTaskCatLogFile(scope.row)"
                  class="el-icon-tickets"
                ></i>
              </em>
            </el-tooltip>

            <el-tooltip
              class="item"
              content="历史"
              effect="dark"
              placement="top"
            >
              <em>
                <i @click="handleLogList(scope.row)" class="el-icon-time"></i>
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
                    @onConfirm="handleEdit(scope.$index, scope.row)"
                    title="你确定要修改该任务吗？"
                  >
                    <i class="el-icon-edit" slot="reference">
                      修改
                    </i>
                  </el-popconfirm>
                </li>

                <li>
                  <i
                    @click="read(scope.row)"
                    class="el-icon-goods"
                    slot="reference"
                  >
                    订阅
                  </i>
                </li>
                <li>
                  <i
                    @click="checkPic(scope.row, 1)"
                    class="el-icon-share"
                    slot="reference"
                  >
                    查看上游结构图
                  </i>
                </li>
                <li>
                  <i
                    @click="checkPic(scope.row, 0)"
                    class="el-icon-share"
                    slot="reference"
                  >
                    查看下游结构图
                  </i>
                </li>
                <!-- 更多操作 开始 -->
                <el-popover
                  :popper-options="{ boundariesElement: 'body' }"
                  placement="right"
                  v-if="loginName === scope.row.createUser || isAdmin"
                  width="200"
                >
                  <ul class="operate-ul">
                    <li>
                      <el-popconfirm
                        @onConfirm="handleStatus(scope.row, 'enable')"
                        title="你确定要启用该任务吗？"
                      >
                        <i class="el-icon-s-promotion" slot="reference">
                          启用
                        </i>
                      </el-popconfirm>
                    </li>
                    <li v-if="'disable' !== scope.row.status">
                      <el-popconfirm
                        @onConfirm="rerunTask(scope.row.id, -1)"
                        title="你确定要触发该任务吗？"
                      >
                        <i class="el-icon-thumb" slot="reference">
                          手动触发(仅自己)
                        </i>
                      </el-popconfirm>
                    </li>
                    <li v-if="'disable' !== scope.row.status">
                      <el-popconfirm
                        @onConfirm="rerunTask(scope.row.id, 3)"
                        title="你确定要触发所有子任务，不包括自己吗？"
                      >
                        <i
                          class="el-icon-thumb"
                          slot="reference"
                          style="font-size: 12px;"
                        >
                          触发所有子任务
                          <i style="font-weight: 500">不包括自己</i>
                        </i>
                      </el-popconfirm>
                    </li>
                    <li v-if="'disable' !== scope.row.status">
                      <el-popconfirm
                        @onConfirm="rerunTask(scope.row.id, 4)"
                        title="你确定要触发所有子任务，包括自己吗？"
                      >
                        <i
                          class="el-icon-thumb"
                          slot="reference"
                          style="font-size: 12px;"
                        >
                          触发所有子任务
                          <i style="font-weight: 500">包括自己</i>
                        </i>
                      </el-popconfirm>
                    </li>
                    <li v-if="'disable' !== scope.row.status">
                      <el-popover
                        :popper-options="{ boundariesElement: 'body' }"
                        placement="right"
                        trigger="hover"
                        width="220"
                      >
                        <ul class="operate-ul">
                          <li @click="stopSelf(scope.row)">
                            <i class="el-icon-video-pause" slot="reference">
                              停止任务(仅自己)
                            </i>
                          </li>
                          <li @click="stopTaskAll(scope.row, 3)">
                            <i class="el-icon-video-pause" slot="reference">
                              停止所有子任务
                              <i style="font-weight: 500">不包括自己</i>
                            </i>
                          </li>
                          <li @click="stopTaskAll(scope.row, 4)">
                            <i class="el-icon-video-pause" slot="reference">
                              停止所有子任务
                              <i style="font-weight: 500">包括自己</i>
                            </i>
                          </li>
                        </ul>
                        <span slot="reference">
                          <i class="el-icon-d-arrow-right"></i>
                          停止任务
                        </span>
                      </el-popover>
                    </li>
                    <li>
                      <el-popconfirm
                        @onConfirm="handleStatus(scope.row, 'disable')"
                        icon="el-icon-info"
                        iconColor="red"
                        title="你确定要禁用该任务吗？"
                      >
                        <i
                          class="el-icon-document-delete"
                          slot="reference"
                          style="color: red"
                        >
                          禁用(下线)
                        </i>
                      </el-popconfirm>
                    </li>
                    <li v-if="isAdmin">
                      <el-popconfirm
                        @onConfirm="handleDelete(scope.row)"
                        icon="el-icon-info"
                        iconColor="red"
                        title="你确定要删除该任务吗？"
                      >
                        <i
                          class="el-icon-delete-solid"
                          slot="reference"
                          style="color: red"
                        >
                          删除
                        </i>
                      </el-popconfirm>
                    </li>
                  </ul>
                  <em slot="reference" style="color:#409EFF;cursor:pointer;">
                    <i class="el-icon-d-arrow-right"></i>
                    更多操作
                  </em>
                </el-popover>
                <!-- 更多操作 结束 -->
              </ul>
              <em slot="reference"><i class="el-icon-more"></i></em>
            </el-popover>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="ID" prop="id" sortable width="80" />
      <el-table-column label="任务名" prop="name" width="200" />
      <el-table-column label="组ID" prop="groupId" sortable width="100" />
      <el-table-column label="DagID" prop="dagId" sortable width="100" />
      <el-table-column label="组名称" prop="groupName" width="180" />
      <el-table-column label="执行机组" prop="nodeGName" width="120" />
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
      <el-table-column
        label="依赖任务/(CronExp)"
        prop="dependencyId"
        width="180"
      />
      <el-table-column label="频率" prop="runFrequency" width="110" />
      <el-table-column
        label="下一次触发时间"
        prop="nextTriggerTime"
        width="160"
      />
      <el-table-column label="上次开始时间" prop="lastStartTime" width="160" />
      <el-table-column label="上次结束时间" prop="lastEndTime" width="160" />
      <el-table-column label="执行引擎" prop="runEngine" width="80" />
      <el-table-column label="所属类型" prop="isPrivate" width="80" />
      <el-table-column label="描述" prop="description" width="200" />
      <el-table-column label="创建者" prop="createUser" width="230" />
      <el-table-column label="邮件列表" prop="emailList" width="300" />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="来源" prop="source" width="100">
        <template slot-scope="scorp">
          {{ scorp.row.source | sourceFilter }}
        </template>
      </el-table-column>
      <el-table-column label="业务信息" prop="businessInfo" width="200" />
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

    <StructurePic
      v-if="showStructureModal"
      :getIsShow="showStructureModal"
      :rowList="rowData"
      :dirFlag="dir"
      @getIsClose="closeChildren"
    ></StructurePic>
    <Subscribe
      v-if="showSubscribeModal"
      :getIsShow="showSubscribeModal"
      :rowList="rowData"
      @getIsClose="closeSubscribeModal"
    ></Subscribe>
  </div>
</template>

<script>
import StructurePic from "./structurePic";
import Subscribe from "./subscribe";
export default {
  components: {
    StructurePic,
    Subscribe
  },
  data() {
    return {
      loading: false,
      tableData: [],
      currentPage: 1,
      total: 0,
      pageSize: 10,
      loginName: null,
      isAdmin: null,
      tasks: [],
      id: null,
      name: null,
      groupId: null,
      dagId: null,
      groupName: null,
      status: null,
      triggerMode: null,
      createUser: null,
      source: null,
      createTime: null,
      nowTime: null,
      lastStartTime: null,
      lastEndTime: null,
      onlineOptions: this.GLOBAL.onlineOptions,
      triggerModeOptions: this.GLOBAL.triggerModeOptions,
      taskStatusOptions: this.GLOBAL.taskStatusOptions,
      sourceFilterOptions: this.GLOBAL.sourceFilterOptions,
      online: true,
      businessInfo: null,
      showMore: false,
      showStructureModal: false,
      rowData: [],
      dir: 0, //上下游标志
      showSubscribeModal: false
    };
  },
  created() {
    this.getData();
  },
  methods: {
    checkPic(row, type) {
      this.rowData = row;
      this.dir = type;
      this.showStructureModal = true;
    },
    closeChildren(data) {
      if (data.changeType) {
        this.showStructureModal = false;
      }
    },
    read(row) {
      this.rowData = row;
      this.showSubscribeModal = true;
    },
    closeSubscribeModal(data) {
      if (data.changeType) {
        this.showSubscribeModal = false;
      }
    },
    changeNowTime() {
      this.nowTime == null
        ? (this.lastStartTime = null)
        : (this.lastStartTime = this.nowTime + " " + "00:00:00");
      this.nowTime == null
        ? (this.lastEndTime = null)
        : (this.lastEndTime = this.nowTime + " " + "23:59:59");
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.getData();
    },

    handleCurrentChange(currentPage) {
      this.currentPage = currentPage;
      this.getData();
    },
    handleOnline(val) {
      console.log(val, "online");
      this.online = val;
    },
    reset() {
      this.id = "";
      this.name = null;
      this.groupId = null;
      this.dagId = null;
      this.$route.params.taskGroupId = null;
      this.groupName = null;
      this.status = null;
      this.triggerMode = null;
      this.createUser = null;
      this.source = null;
      this.createTime = null;
      this.online = true;
      this.businessInfo = null;
      this.getData();
    },
    getData() {
      this.loading = true;
      let _self = this;
      let auto = setInterval(function() {
        if (_self.loginName !== null) {
          clearInterval(auto);
        }
      }, 1000);

      _self.loginName = this.$store.state.user.name;
      _self.isAdmin = this.$store.state.user.isAdmin;

      let pageParam = {};
      pageParam.pageNum = _self.currentPage;
      pageParam.pageSize = _self.pageSize;
      pageParam.userName = _self.loginName;
      pageParam.role = this.$store.state.user.role;

      let task = {};
      task.id = _self.id;
      if (task.id === "") {
        task.id = null;
      }
      task.name = _self.name;
      if (task.name === "") {
        task.name = null;
      }

      task.groupId = this.groupId;
      if (_self.$route.params.taskGroupId !== null) {
        // 任务组列表传过来任务组id （taskGroupId）
        task.groupId = _self.$route.params.taskGroupId;
        this.groupId = _self.$route.params.taskGroupId;
        _self.$route.params.taskGroupId = null;
      } else if (this.groupId === "" || task.groupId === null) {
        // 任务组id搜索为空的时候
        task.groupId = null;
        this.groupId = null;
      }

      task.dagId = this.dagId;
      if (_self.$route.params.dagId !== null) {
        // 任务组列表传过来任务组id （dagId）
        task.dagId = _self.$route.params.dagId;
        this.dagId = _self.$route.params.dagId;
        _self.$route.params.dagId = null;
      } else if (this.dagId === "" || task.dagId === null) {
        // 搜索为空的时候
        task.dagId = null;
        this.dagId = null;
      }

      this.groupName === ""
        ? (task.groupName = null)
        : (task.groupName = this.groupName);
      this.status === "" ? (task.status = null) : (task.status = this.status);
      this.triggerMode === "" || this.triggerMode == null
        ? (task.triggerMode = null)
        : (task.triggerMode = this.triggerMode);
      this.createUser === ""
        ? (task.createUser = null)
        : (task.createUser = this.createUser);
      this.source === "" || this.source == null
        ? (task.source = null)
        : (task.source = this.source);
      task.online = this.online;
      this.businessInfo === ""
        ? (task.businessInfo = null)
        : (task.businessInfo = this.businessInfo);
      this.lastStartTime === ""
        ? (task.lastStartTime = null)
        : (task.lastStartTime = this.lastStartTime);
      this.lastEndTime === ""
        ? (task.lastEndTime = null)
        : (task.lastEndTime = this.lastEndTime);

      _self.tableData = [];

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
            this.loading = false;
            if (response.data.list) {
              for (let i = 0; i < response.data.list.length; i++) {
                let obj = {};
                obj.id = response.data.list[i].id;
                obj.name = response.data.list[i].name;
                obj.dagId = response.data.list[i].dagId;
                obj.groupId = response.data.list[i].groupId;
                obj.groupName = response.data.list[i].groupName;
                obj.nodeGName = response.data.list[i].nodeGName;
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
                  obj.dependencyId =
                    "( " + response.data.list[i].cronExp + " )";
                } else if (response.data.list[i].triggerMode === 0) {
                  obj.triggerMode = "依赖触发";
                  obj.dependencyId = response.data.list[i].dependencyId;
                } else if (response.data.list[i].triggerMode === 2) {
                  obj.triggerMode = "时间触发和依赖触发";
                  obj.dependencyId =
                    response.data.list[i].dependencyId +
                    " (" +
                    response.data.list[i].cronExp +
                    ")";
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
                obj.runEngine = response.data.list[i].runEngine;
                obj.isPrivate =
                  response.data.list[i].isPrivate === "1" ? "私有" : "公有";
                obj.description = response.data.list[i].description;
                obj.createUser = response.data.list[i].createUser;
                obj.emailList = response.data.list[i].emailList;
                obj.createTime = response.data.list[i].createTime;
                obj.source = response.data.list[i].source;
                obj.businessInfo = response.data.list[i].businessInfo;

                _self.tableData.push(obj);
              }
              _self.total = response.data.total;
            }
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
          console.log(err);
        });
    },
    handleEdit(index, row) {
      this.$router.push({
        name: "updateTask",
        params: {
          taskId: row.id
        }
      });
    },

    handleStatus(row, status) {
      let taskId = row.id;
      let _self = this;
      if (status === "enable") {
        _self.axios
          .post(
            "/clockwork/web/task/operation/enableTask",
            _self.qs.stringify({
              taskId: taskId
            })
          )
          .then(response => {
            if (response.code === "OK") {
              _self.$message({
                message: "操作成功",
                type: "success"
              });
              row.status = status;
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
      } else {
        _self.axios
          .post(
            "/clockwork/web/task/operation/disableTask",
            _self.qs.stringify({
              taskId: taskId
            })
          )
          .then(response => {
            if (response.code === "OK") {
              _self.$message({
                message: "操作成功",
                type: "success"
              });
              row.status = status;
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
      }
    },
    handleDelete(row) {
      let _self = this;
      _self
        .$confirm(
          "删除当前任务会把上下游的依赖关系也删除！确认删除吗?",
          "提示",
          {}
        )
        .then(() => {
          let taskId = row.id;
          _self.axios
            .post(
              "/clockwork/web/task/operation/deleteTask",
              _self.qs.stringify({
                taskId: taskId
              })
            )
            .then(response => {
              if (response.code === "OK") {
                _self.$message({
                  message: "操作成功",
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
        })
        .catch(() => {});
    },
    rerunTask(taskId, taskReRunType) {
      let _self = this;
      _self.axios
        .post(
          "/clockwork/web/task/operation/rerunTask",
          _self.qs.stringify({
            taskId: taskId,
            taskReRunType: taskReRunType,
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
    stopTaskAll(row, num) {
      let taskId = row.id;
      this.axios
        .post(
          "/clockwork/web/task/operation/stopTaskAndChildrens",
          this.qs.stringify({
            taskId: taskId,
            stopType: num
          })
        )
        .then(response => {
          if (response.code === "OK") {
            this.$message({
              message: "操作成功",
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
    },
    stopSelf(row) {
      let taskId = row.id;
      this.axios
        .post(
          "/clockwork/web/task/operation/stopTask",
          this.qs.stringify({
            taskId: taskId
          })
        )
        .then(response => {
          if (response.code === "OK") {
            this.$message({
              message: "操作成功",
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
    },
    handleLatestTaskCatLogFile(row) {
      let taskId = row.id;
      let _self = this;
      try {
        _self.axios
          .get("/clockwork/web/task/log/getLatestTaskLogFileParamByTaskId", {
            params: {
              taskId: taskId
            }
          })
          .then(response => {
            if (response.code === "OK") {
              this.$router.push({
                name: "logFile",
                params: {
                  nodeIp: response.data.nodeIp,
                  nodePort: response.data.nodePort,
                  logName: response.data.logName,
                  createTime: response.data.createTime
                }
              });
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
      } catch (err) {
        _self.$message({
          message: "系统错误",
          type: "warning",
          duration: 5000
        });
        console.log(err);
      }
    },
    handleLogList(row) {
      this.$router.push({
        name: "history",
        params: {
          taskId: row.id
        }
      });
      // this.$router.push({
      //     name: 'logList',
      //     params: {
      //         taskId: row.id, showFilterContainer: false
      //     }
      // });
    },

    goAddTask() {
      this.$router.push("/task/create");
    },
    moreSearch() {
      this.showMore = !this.showMore;
    }
  }
};
</script>
