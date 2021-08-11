<template>
  <div class="app-container">
    <!-- form-box 表单页面 -->
    <div class="form-box">
      <el-form :model="form" :rules="rules" label-width="100px" ref="form">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务名称" prop="name">
              <el-input :disabled="true" v-model="form.name"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务描述">
              <el-input
                placeholder="请描述任务功能"
                v-model="form.description"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="脚本目录" prop="location">
              <el-select
                @change="getCommand()"
                style="width:50%;float:left;"
                v-model="locationPrefix"
              >
                <el-option
                  :key="index"
                  :value="item"
                  v-for="(item, index) in locationPrefixList"
                >
                  {{ item }}
                </el-option>
              </el-select>

              <el-tooltip
                class="item"
                content="选择根目录后在此填写后面的路径"
                effect="dark"
                placement="top"
              >
                <el-input
                  @input="e => getCommand()"
                  style="width:50%;float:left;"
                  v-model="locationSuffix"
                ></el-input>
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="脚本类型" prop="scriptType">
              <el-select v-model="form.scriptType">
                <el-option label="sh" value="sh"></el-option>
                <el-option label="python" value="python"></el-option>
                <el-option label="python3" value="python3"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="脚本名称" prop="scriptName">
              <el-input
                @input="e => getCommand()"
                v-model="form.scriptName"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="脚本参数">
              <el-input
                @input="e => getCommand()"
                placeholder="请输入脚本参数"
                v-model="form.scriptParameter"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="执行命令">
              <el-input
                disabled
                placeholder="请输入执行命令，例如：test.sh"
                v-model="form.command"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属类型" prop="isPrivate">
              <el-select placeholder="请选择" v-model="form.isPrivate">
                <el-option
                  key="1"
                  label="私有(仅同部门可见)"
                  value="true"
                ></el-option>
                <el-option
                  key="0"
                  label="公有(全公司可见)"
                  value="false"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="触发方式" prop="triggerMode">
              <el-select
                @visible-change="handTriggerMode"
                placeholder="请选择任务触发方式"
                v-model="form.triggerMode"
              >
                <el-option key="1" label="时间触发" value="1"></el-option>
                <el-option key="0" label="依赖触发" value="0"></el-option>
                <el-option
                  key="2"
                  label="时间触发和依赖触发"
                  value="2"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重试次数">
              <el-input-number
                :min="0"
                placeholder="失败重试次数"
                v-model="form.failedRetries"
              ></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务组" prop="groupName">
              <el-autocomplete
                :fetch-suggestions="querySearchTaskGroup"
                :on-click="deleteSearchGroup"
                class="inline-input"
                placeholder="搜索任务所属的任务组"
                v-model="form.groupName"
              ></el-autocomplete>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务超时时间">
              <el-input-number
                :min="1"
                placeholder="失败重试次数"
                v-model="form.runTimeout"
              ></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="执行机组" prop="nodeGid">
              <el-select
                placeholder="请选择任务执行机组"
                v-model="form.nodeGid"
              >
                <el-option
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  v-for="item in form.nodeGroupSelect"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="是否同步脚本">
              <el-switch v-model="form.isSyncFile"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务来源" prop="source">
              <!-- 不能修改 -->
              <el-select
                :disabled="disabledSource"
                class="handle-input"
                size="medium"
                v-model="form.source"
              >
                <el-option
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                  v-for="item in sourceFilterOptions"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否替换参数">
              <el-switch v-model="form.isReplace"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="执行引擎" prop="runEngine">
              <el-select placeholder="请选择" v-model="form.runEngine">
                <el-option key="hive" label="hive" value="hive"></el-option>
                <el-option key="spark" label="spark" value="spark"></el-option>
                <el-option
                  key="presto"
                  label="presto"
                  value="presto"
                ></el-option>
                <el-option
                  key="moonbox"
                  label="moonbox"
                  value="moonbox"
                ></el-option>
                <el-option
                  key="unknown"
                  label="unknown"
                  value="unknown"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="内部变量">
              <el-input
                placeholder="JSON格式"
                v-model="form.parameter"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="代理用户">
              <el-input
                placeholder="用这个代理用户来执行脚本"
                v-model="form.proxyUser"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效时间">
              <el-col :span="11">
                <el-date-picker
                  @change="setExpiredTime"
                  format="yyyy-MM-dd HH:mm:ss"
                  placeholder="选择日期时间"
                  type="datetime"
                  v-model="form.expiredTime"
                ></el-date-picker>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="邮件列表">
              <el-input
                placeholder="如果多个邮件请以英文逗号分隔"
                v-model="form.emailList"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结果状态判定">
              <el-select
                placeholder="判定规则"
                v-model="form.resultStatusJudge"
                multiple
                filterable
                default-first-option
              >
                <el-option
                  v-for="item in form.resultStatusJudgeData"
                  :key="item.id"
                  :label="item.errorWord"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="上传脚本">
              <el-col>
                <el-upload
                  :action="uploadFileUrl"
                  :auto-upload="false"
                  :data="form"
                  :file-list="fileList"
                  class="upload-demo"
                  drag
                  multiple
                  ref="upload"
                >
                  <i class="el-icon-upload"></i>
                  <div
                    class="el-upload__text"
                    style="width:290px;font-size:12px"
                  >
                    将文件拖到此处，或
                    <em>点击上传</em>
                  </div>
                </el-upload>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="12"></el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item class="submit-item">
              <el-button @click="reBack" icon="el-icon-back">返回</el-button>
              <el-button
                @click="submitForm('form')"
                icon="el-icon-document-add"
                type="primary"
              >
                提交
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- dialog 触发方式选项 弹窗-->
        <el-dialog
          :visible.sync="dialogVisible"
          title="触发方式选项"
          width="75%"
        >
          <!-- 时间 -->
          <el-form-item
            label="触发时间"
            prop="triggerTime"
            v-if="form.triggerMode === '1' || form.triggerMode === '2'"
          >
            <el-date-picker
              :clearable="false"
              :disabled="form.checkPattern"
              @change="setTriggerTime"
              format="yyyy-MM-dd HH:mm:ss"
              placeholder="选择日期时间"
              style="width:46%;"
              type="datetime"
              v-model="form.triggerTime"
            ></el-date-picker>
          </el-form-item>
          <el-form-item
            label="执行频率"
            prop="runFrequency"
            v-if="form.triggerMode === '1' || form.triggerMode === '2'"
          >
            <el-row>
              <el-col :span="11">
                <el-input
                  :disabled="form.checkPattern"
                  @change="handleTriggerParamChange"
                  v-model="form.runFrequency"
                ></el-input>
              </el-col>
              <el-col :span="1" class="line">-</el-col>
              <el-col :span="12">
                <el-select
                  :disabled="form.checkPattern"
                  @change="handleTriggerParamChange"
                  placeholder="请选择"
                  v-model="form.timeType"
                >
                  <el-option key="day" label="天/次" value="day"></el-option>
                  <el-option key="week" label="周/次" value="week"></el-option>
                  <el-option
                    key="month"
                    label="月/次"
                    value="month"
                  ></el-option>
                  <el-option key="year" label="年/次" value="year"></el-option>
                  <el-option
                    key="century"
                    label="世纪/次"
                    value="century"
                  ></el-option>
                  <el-option
                    key="minute"
                    label="分钟/次"
                    value="minute"
                  ></el-option>
                  <el-option
                    key="hour"
                    label="小时/次"
                    value="hour"
                  ></el-option>
                </el-select>
              </el-col>
            </el-row>
          </el-form-item>

          <div v-if="form.triggerMode === '1' || form.triggerMode === '2'">
            <p style="border-top:1px dotted #E4E7ED"></p>
            <el-form-item label="CRON模式">
              <el-switch
                @change="handleCheckPattern"
                v-model="form.checkPattern"
              ></el-switch>
            </el-form-item>
            <el-form-item label="CRON表达式">
              <el-input
                :disabled="!form.checkPattern"
                v-model="form.cronExp"
                width="90%"
              ></el-input>
            </el-form-item>
          </div>

          <!-- 依赖任务相关 -->
          <el-form-item
            label="选择依赖"
            prop="dependencyId"
            v-if="form.triggerMode === '0' || form.triggerMode === '2'"
          >
            <div id="transfer">
              <template>
                <elt-transfer
                  :button-texts="['添加', '删除']"
                  :left-columns="leftColumns"
                  :pagination-call-back="paginationCallBack"
                  :query-texts="['搜索', '搜索']"
                  :show-pagination="true"
                  :show-query="true"
                  :table-row-key="row => row.id"
                  :title-texts="['选择依赖的父任务', '已选依赖']"
                  ref="eltTransfer"
                  v-model="tableData"
                >
                  <!-- 可以使用插槽获取到列信息和行信息，从而进行数据的处理 -->
                  <template v-slot:leftCondition="{ scope }">
                    <el-row :gutter="10">
                      <el-col :span="9">
                        <el-input
                          placeholder="任务Id"
                          size="medium"
                          v-model="scope.id"
                        ></el-input>
                      </el-col>
                      <el-col :span="9">
                        <el-input
                          placeholder="任务名"
                          size="medium"
                          v-model="scope.name"
                        ></el-input>
                      </el-col>
                    </el-row>
                  </template>
                  <template v-slot:rightCondition="{ scope }">
                    <el-row>
                      <el-col :span="9">
                        <el-input
                          placeholder="任务名称"
                          size="medium"
                          v-model="scope.name"
                        ></el-input>
                      </el-col>
                    </el-row>
                  </template>
                </elt-transfer>
              </template>
            </div>
          </el-form-item>

          <span class="dialog-footer" slot="footer">
            <el-button @click="cancel">取 消</el-button>
            <el-button @click="ok" type="primary">确 定</el-button>
          </span>
        </el-dialog>
        <!-- dialog 触发方式选项 弹窗 END -->
      </el-form>
    </div>
    <!-- form-box 表单页面 END -->
  </div>
</template>

<script>
import $ from "jquery";

export default {
  data: function() {
    return {
      leftColumns: [
        { label: "任务id", id: "id", width: "90px" },
        { label: "任务名", id: "name" }
      ],
      taskGroups: [],
      locationPrefixList: [], // 文件前缀list
      locationPrefix: "",
      locationSuffix: "",
      rowDataList: [],
      disabledSource: false,
      sourceFilterOptions: this.GLOBAL.sourceFilterOptions,
      transferDependencyData: [], // 穿梭框下拉数据
      tableData: [], // 穿梭框默认数据
      form: {
        name: "",
        description: "",
        location: "",
        command: "",
        triggerMode: "",
        timeType: "",
        runFrequency: "",
        triggerTime: "",
        groupId: "",
        groupName: "",
        isPrivate: "",
        runEngine: "",
        emailList: "",
        parameter: "",
        expiredTime: "",
        customScript: "",
        proxyUser: "",
        createUser: "",
        dependencyId: [],
        dependencyName: "",
        scriptName: "",
        scriptParameter: "",
        scriptType: "",
        checkPattern: false,
        cronExp: "",
        nodeGid: null,
        nodeGroupSelect: [],
        failedRetries: "",
        runTimeout: 120,
        isSyncFile: true,
        isReplace: true,
        resultStatusJudge: [],
        resultStatusJudgeData: []
      },
      uploadFileUrl: "/clockwork/web/dfs/file/uploadFile",
      fileList: [],
      dialogVisible: false,
      checkPattern: false,
      dependencyId: [], // 保存数组
      rules: {
        name: [
          {
            required: true,
            message: "请输入任务的英文名称",
            trigger: "blur"
          }
        ],
        location: [
          {
            required: true,
            message: "请输入任务的脚本存储路径",
            trigger: "blur"
          }
        ],

        triggerMode: [
          {
            required: true,
            message: "请选择任务的触发方式",
            trigger: "change"
          }
        ],

        nodeGid: [
          {
            required: true,
            message: "请选择执行机组",
            trigger: "change"
          }
        ],

        groupName: [
          {
            required: true,
            message: "请搜索后选择任务所属的任务组",
            trigger: "change"
          }
        ],
        scriptName: [
          {
            required: true,
            message: "请输入脚本名称",
            trigger: "blur"
          }
        ]
      }
    };
  },
  created() {
    this.getResultRules();
  },
  mounted() {
    this.getData();
    this.taskGroups = this.getTaskGroupIdAndNameByUserName();

    //执行机组
    this.getNodeGroup();
  },
  methods: {
    // 弹出框ok
    ok() {
      if (this.form.triggerMode === "1" || this.form.triggerMode === "2") {
        // 触发时间相关
        if (!this.form.checkPattern) {
          // 未选中CRON表达式
          if (
            this.form.triggerTime === "" ||
            this.form.timeType === "" ||
            this.form.runFrequency === ""
          ) {
            this.$message({
              message: "时间触发相关字段必填",
              type: "warning"
            });
            this.dialogVisible = true;
            return false;
          }
        } else {
          if (this.form.cronExp === "" || this.form.cronExp === null) {
            this.$message({
              message: "CRON表达式必填",
              type: "warning"
            });
            this.dialogVisible = true;
            return false;
          }
        }
        if (this.form.triggerMode === "1") {
          this.form.dependencyId = null;
        }
      }
      if (this.form.triggerMode === "0" || this.form.triggerMode === "2") {
        // 依赖触发相关
        if (this.form.tableData === null || this.form.tableData === "") {
          this.$message({
            message: "请选择相关依赖任务",
            type: "warning"
          });
          this.dialogVisible = true;
          return false;
        }
        if (this.form.triggerMode === "0") {
          this.form.triggerTime = null;
          this.form.timeType = "";
          this.form.runFrequency = "";
          this.form.cronExp = "";
        }
      }
      this.dialogVisible = false;
    },
    // 弹出框cancel取消
    cancel() {
      this.form.triggerMode = this.rowDataList.triggerMode + "";
      this.form.timeType = this.rowDataList.timeType;
      this.form.runFrequency = this.rowDataList.runFrequency;
      this.form.checkPattern = this.checkPattern;
      // this.form.dependencyId = this.dependencyId;
      this.form.cronExp = this.rowDataList.cronExp;
      this.dialogVisible = false;
    },
    getData() {
      // 加载任务相关数据
      let _self = this;
      if (_self.$route.params.taskId !== undefined) {
        sessionStorage.setItem("taskId", _self.$route.params.taskId);
      }
      let taskId = parseInt(sessionStorage.getItem("taskId"));
      _self.axios
        .get("/clockwork/web/task/getTaskJSONObjectById", {
          params: {
            taskId: taskId
          }
        })
        .then(response => {
          if (response.code === "OK") {
            _self.disabledSource = true; // 弹窗
            _self.rowDataList = response.data;
            _self.form.name = response.data.name;
            _self.form.description = response.data.description;
            _self.form.triggerMode = response.data.triggerMode + "";
            _self.form.location = response.data.location;
            _self.form.source = response.data.source;
            _self.form.groupId = response.data.groupId + "";
            _self.form.groupName = response.data.taskGroupName;
            _self.form.isPrivate = response.data.isPrivate + "";
            _self.form.runEngine = response.data.runEngine;
            _self.form.emailList = response.data.emailList;
            _self.form.parameter = response.data.parameter;
            _self.form.proxyUser = response.data.proxyUser;
            _self.form.createUser = response.data.createUser;
            _self.form.expiredTime = response.data.expiredTime;
            _self.form.scriptName = response.data.scriptName;
            _self.form.scriptParameter = response.data.scriptParameter;
            _self.form.nodeGid = response.data.nodeGid;
            _self.form.failedRetries = response.data.failedRetries;
            _self.form.runTimeout = response.data.runTimeout;
            _self.form.isSyncFile = response.data.isSyncFile;
            _self.form.isReplace = response.data.isReplace;
            _self.form.command = response.data.command;
            _self.form.scriptType = response.data.scriptType;
            _self.form.cronExp = response.data.cronExp;
            _self.form.resultStatusJudge = (() => {
              if (response.data.errorKeywordIds) {
                return response.data.errorKeywordIds
                  .split(",")
                  .map(item => Number(item));
              }
              return [];
            })();
            if (
              _self.form.triggerMode === "1" ||
              _self.form.triggerMode === "2"
            ) {
              // 时间触发相关
              _self.form.timeType = response.data.timeType;
              _self.form.runFrequency = response.data.runFrequency;
              _self.form.triggerTime = response.data.triggerTime;

              if (
                _self.form.triggerTime === null ||
                _self.form.triggerTime === "" ||
                _self.form.runFrequency === null ||
                _self.form.runFrequency === "" ||
                _self.form.timeType === null ||
                _self.form.timeType === ""
              ) {
                _self.form.checkPattern = true;
                _self.checkPattern = true;
              }
            }
            if (
              _self.form.triggerMode === "0" ||
              _self.form.triggerMode === "2"
            ) {
              // 依赖触发相关
              if (response.data.dependencyId != null) {
                this.getDependentTasks(response.data.dependencyId); // 获取依赖任务初始化数据
              }
            }
            if (_self.form.triggerMode === "4") {
              // 信号触发
              _self.form.customScript = response.data.customScript;
            }

            _self.getFileList(); // 文件列表
            this.getUploadPathPrefix(); // location 前缀
            // 设置穿梭框数据
            this.getTransferDependencyData();
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
    // 穿梭框数据搜索
    paginationCallBack(obj) {
      let id = obj.id === "" ? null : obj.id;
      let name = obj.name === "" ? null : obj.name;
      let size = -1;
      let d = this.transferDependencyData.filter((item, index) => {
        // eslint-disable-next-line
        if (id == null) {
          // eslint-disable-next-line
          if (name == null || item.name.indexOf(name) > -1) {
            ++size;
          } else {
            return false;
          }
        } else {
          // eslint-disable-next-line
          if (id == item.id && (name == null || item.name.indexOf(name) > -1)) {
            ++size;
          } else {
            return false;
          }
        }
        if (
          size >= (obj.pageIndex - 1) * obj.pageSize &&
          size < obj.pageIndex * obj.pageSize
        ) {
          // 那页的数据需要展示
          return true;
        }
        return false;
      });
      return new Promise((resolve, reject) => {
        try {
          resolve({ total: ++size, data: d });
        } catch {
          reject();
        }
      });
    },
    clearTransfer() {
      this.$refs.eltTransfer.clear();
    },
    filterMethod(query, item) {
      return item.serach.indexOf(query) > -1;
    },
    // 获取穿梭框的数据
    getTransferDependencyData() {
      let _self = this;
      let userName = _self.$store.state.user.name;
      _self.transferDependencyData = [];
      _self.axios
        .get("/clockwork/web/task/getTaskIdAndNameByUserNameNotInThisId", {
          params: {
            userName: userName,
            id: parseInt(sessionStorage.getItem("taskId"))
          }
        })
        .then(response => {
          if (response.code === "OK") {
            if (response.data) {
              _self.transferDependencyData = response.data;
              // console.log("穿梭框----", _self.transferDependencyData);
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
    getResultRules() {
      const { name, role } = this.$store.state.user;
      const condition = JSON.stringify({
        id: null,
        errorWord: null
      });
      const paramsObj = {
        pageNum: 1,
        pageSize: 0,
        userName: name,
        condition,
        role
      };
      this.axios
        .post("/clockwork/web/task/keyword/searchPageKeyWordList", {
          ...paramsObj
        })
        .then(response => {
          if (response && response.code === "OK") {
            const { list = [] } = response.data;
            this.form.resultStatusJudgeData = list;
          }
        })
        .catch(err => {
          console.log(err);
        });
    },
    handleCheckPattern(val) {
      this.form.checkPattern = val;
      if (val) {
        // 选中清空
        this.form.triggerTime = null;
        this.form.runFrequency = null;
        this.form.timeType = "";
      } else {
        // 未选中
        this.form.triggerTime = this.rowDataList.triggerTime;
        this.form.runFrequency = this.rowDataList.runFrequency;
        this.form.timeType = this.rowDataList.timeType;
        this.cronExp = "";
      }
    },
    handleTriggerParamChange() {
      let _self = this;
      if (
        _self.form.triggerTime !== null &&
        _self.form.triggerTime !== "" &&
        _self.form.runFrequency !== null &&
        _self.form.runFrequency !== "" &&
        _self.form.timeType !== null &&
        _self.form.timeType !== ""
      ) {
        _self.axios
          .get("/clockwork/web/task/createCronExpByTriggerTime", {
            params: {
              triggerTime: _self.form.triggerTime,
              runFrequency: _self.form.runFrequency,
              timeType: _self.form.timeType
            }
          })
          .then(response => {
            if (response.code === "OK") {
              _self.form.checkPattern = true;
              _self.form.cronExp = response.data;
              _self.form.checkPattern = false;
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

    handTriggerMode(val) {
      if (!val) {
        this.dialogVisible = true;
      }
    },
    getCommand() {
      if (this.locationSuffix) {
        this.form.location = this.locationPrefix + this.locationSuffix; //脚本目录
        this.form.command = (
          this.form.location +
          "/" +
          this.form.scriptName +
          " " +
          this.form.scriptParameter
        ).trim(); //执行命令
      } else {
        this.form.location = this.locationPrefix; //脚本目录
        this.form.command = (
          this.locationPrefix +
          this.form.scriptName +
          " " +
          this.form.scriptParameter
        ).trim(); //执行命令
      }
    },
    submitForm(formName) {
      let task = {};

      task.id = parseInt(sessionStorage.getItem("taskId"));
      task.name = this.form.name;
      task.description = this.form.description;

      if (!this.form.location.trim()) {
        this.$message({
          message: "请输入任务的脚本存储路径",
          type: "warning"
        });
        return false;
      }
      task.location = this.form.location;
      task.command = this.form.command;

      if (!this.form.groupName.trim()) {
        this.$message({
          message: "请搜索后选择任务所属的任务组",
          type: "warning"
        });
        return false;
      }

      task.triggerMode = this.form.triggerMode;
      if (this.form.triggerMode === "1" || this.form.triggerMode === "2") {
        // 时间触发相关
        task.timeType = this.form.timeType;
        task.runFrequency = this.form.runFrequency;
        task.triggerTime = this.form.triggerTime;
      }
      if (this.form.triggerMode === "0" || this.form.triggerMode === "2") {
        // 依赖任务相关
        const dpIds = [];
        if (
          this.tableData == null ||
          this.tableData === "" ||
          this.tableData.length === 0
        ) {
          this.$message({
            message: "请搜索后选择任务的依赖任务",
            type: "warning"
          });
          return false;
        }
        for (let i = 0; i < this.tableData.length; i++) {
          dpIds.push(this.tableData[i].id);
        }
        task.dependencyId = dpIds.join(",");
      }
      task.groupId = this.getGroupId(this.form.groupName);
      task.isPrivate = this.form.isPrivate;
      if (this.form.runEngine != null && this.form.runEngine !== "") {
        task.runEngine = this.form.runEngine;
      }
      task.emailList = this.form.emailList;
      task.parameter = this.form.parameter;
      task.proxyUser = this.form.proxyUser;
      if (
        this.form.expiredTime === null ||
        this.form.expiredTime === undefined ||
        this.form.expiredTime === "NaN-NaN-NaN NaN:NaN:NaN"
      ) {
        task.expiredTime = null;
      } else {
        let d = new Date(this.form.expiredTime);
        task.expiredTime =
          d.getFullYear() +
          "-" +
          (d.getMonth() + 1) +
          "-" +
          d.getDate() +
          " " +
          d.getHours() +
          ":" +
          d.getMinutes() +
          ":" +
          d.getSeconds();
      }

      task.scriptName = this.form.scriptName;
      task.scriptParameter = this.form.scriptParameter;
      task.createUser = this.form.createUser;
      task.operatorName = this.$store.state.user.name;

      task.nodeGid = this.form.nodeGid;
      task.failedRetries = this.form.failedRetries;
      task.runTimeout = this.form.runTimeout;
      task.isSyncFile = this.form.isSyncFile;
      task.isReplace = this.form.isReplace;
      task.source = this.form.source;
      task.scriptType = this.form.scriptType;
      task.cronExp = this.form.cronExp;
      task.errorKeywordIds = this.form.resultStatusJudge.join(",");
      //上传文件
      this.$refs.upload.submit();

      let _self = this;

      this.$refs[formName].validate(valid => {
        if (valid) {
          _self.axios
            .post(
              "/clockwork/web/task/operation/updateTask",
              JSON.stringify(task),
              {
                headers: {
                  "Content-Type": "application/json"
                }
              }
            )
            .then(response => {
              if (response.code === "OK") {
                _self.$message({
                  message: "修改任务成功",
                  type: "success"
                });
                this.$router.push("/task/taskList");
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
          _self.$message({
            message: "表单填写不符合规则，提交失败！",
            type: "warning",
            duration: 5000
          });
          return false;
        }
      });
    },
    // 任务组相关方法
    querySearchTaskGroup(queryString, cb) {
      let taskGroups = this.taskGroups;
      let results = queryString
        ? taskGroups.filter(this.createFilterTaskGroup(queryString))
        : taskGroups;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createFilterTaskGroup(queryString) {
      return taskGroup => {
        // return taskGroup.value.indexOf(queryString.toLowerCase()) === 0;
        return (
          taskGroup.value.toLowerCase().indexOf(queryString.toLowerCase()) !==
          -1
        );
      };
    },
    getTaskGroupIdAndNameByUserName() {
      let userName = this.$store.state.user.name;
      let data = [];
      let _self = this;
      _self.axios
        .get("/clockwork/web/task/group/getTaskGroupIdAndNameByUserName", {
          params: {
            userName: userName
          }
        })
        .then(response => {
          if (response.code === "OK") {
            for (let i = 0; i < response.data.length; i++) {
              let obj = {};
              obj.value = response.data[i].name;
              obj.id = response.data[i].id;
              data[i] = obj;
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

      return data;
    },
    deleteSearchGroup() {
      this.form.groupName = "";
    },
    getGroupId(name) {
      let groupId = 0;
      $.each(this.taskGroups, function(n, taskGroup) {
        if (taskGroup.value === name) {
          groupId = taskGroup.id;
        }
      });
      return groupId;
    },
    setTriggerTime(val) {
      let d = new Date(val);
      console.log(d);
      this.form.triggerTime =
        d.getFullYear() +
        "-" +
        (d.getMonth() + 1) +
        "-" +
        d.getDate() +
        " " +
        d.getHours() +
        ":" +
        d.getMinutes() +
        ":" +
        d.getSeconds();
      this.handleTriggerParamChange();
    },

    setExpiredTime(val) {
      this.form.expiredTime = val;
    },
    getGroupName(id) {
      let groupName = "";
      $.each(this.taskGroups, function(n, taskGroup) {
        if (taskGroup.id === id) {
          groupName = taskGroup.value;
        }
      });
      return groupName;
    },
    getFileList() {
      let _self = this;
      _self.axios
        .get("/clockwork/web/dfs/file/getDirectoryFileNames", {
          params: {
            directoryPath: _self.form.location,
            isSyncFile: _self.form.isSyncFile,
            taskId: _self.form.id
          }
        })
        .then(response => {
          if (response.code === "OK") {
            for (let i = 0; i < response.data.length; i++) {
              let obj = {};
              obj.name = response.data[i];
              this.fileList.push(obj);
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

    reBack() {
      this.$router.push("/task/taskList");
    },
    getNodeGroup() {
      let _self = this;
      _self.axios
        .get("/clockwork/web/node/group/getAllNodeGroup", {})
        .then(response => {
          this.form.nodeGroupSelect = response.data;
          // 下拉显示默认值
          response.data.forEach((item, index) => {
            if (item.name === _self.form.nodeGid) {
              _self.form.nodeGid = item.id;
            }
          });
        })
        .catch(err => {
          console.log(err);
        });
    },
    getUploadPathPrefix() {
      let _self = this;
      _self.axios
        .get("/clockwork/web/task/uploadPathPrefix")
        .then(res => {
          _self.locationPrefixList = res.data;
          const rtn = res.data
            .map(i => ({ raw: i, len: i.length }))
            .sort((p, n) => n.len - p.len)
            .map(i => i.raw);
          for (let i = 0; i < rtn.length; i++) {
            if (_self.form.location.startsWith(rtn[i])) {
              _self.locationPrefix = rtn[i];
              _self.locationSuffix = _self.form.location.split(rtn[i])[1]; //脚本目录可编辑的输入框
              return;
            }
          }
        })
        .catch(err => {
          this.$message({
            message: err.msg,
            type: "warning",
            duration: 5000
          });
        });
    },
    getDependentTasks(ids) {
      this.axios
        .get("/clockwork/web/task/getTaskByTaskIds", {
          params: {
            ids: ids
          }
        })
        .then(res => {
          res.data.forEach((item, index) => {
            this.tableData.push({ id: item.id, name: item.name });
          });
        })
        .catch(err => {
          console.log(err);
        });
    }
  }
};

// Array.prototype.remove = function(val) {
//   var index = this.indexOf(val);
//   if (index > -1) {
//     this.splice(index, 1);
//   }
// };
</script>

<style scoped>
.form-box {
  width: 100%;
}

.app-container {
  width: 100%;
}

.el-form {
  width: 100%;
}

.el-form-item {
  width: 90%;
}

.el-select {
  width: 100%;
}

.el-date-editor.el-input,
.el-date-editor.el-input__inner {
  width: 90%;
}

.el-autocomplete {
  width: 100%;
}

.el-input-number {
  width: 100%;
}

.el-date-editor.el-input {
  width: 220%;
}

.submit-item {
  border-top: 1px solid #ccc;
  padding-top: 25px;
  width: 100% !important;
  text-align: center;
}

.start:before {
  content: "*";
  color: #f56c6c;
  margin-right: 4px;
}
.form-box /deep/ .el-transfer-panel {
  width: 350px !important;
}

.form-box /deep/ .el-form-item {
  width: 100%;
}
</style>
