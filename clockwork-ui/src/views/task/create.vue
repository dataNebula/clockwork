<template>
  <div class="app-container">
    <!-- form-box 表单页面 -->
    <div class="form-box">
      <el-form :model="form" :rules="rules" label-width="100px" ref="form">
        <el-row>
          <el-col :span="12">
            <el-tooltip
              class="item"
              content="任务名称仅含有英文字母或数字或下划线，例如：hello_task"
              effect="dark"
              placement="top"
            >
              <el-form-item label="任务名称" prop="name">
                <el-input
                  placeholder="任务名称仅含有英文字母或数字或下划线，例如：hello_task"
                  v-model="form.name"
                ></el-input>
              </el-form-item>
            </el-tooltip>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务描述" prop="description">
              <el-input
                placeholder="请描述任务功能"
                v-model="form.description"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="脚本目录">
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
                placeholder="请输入脚本名称"
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
                placeholder="脚本目录+脚本名称"
                v-model="form.command"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属类型" prop="isPrivate">
              <el-select v-model="form.isPrivate">
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
                @select="handleSelectGroup"
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
                v-model="form.run_timeout"
              ></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="执行机组" prop="node_gid">
              <el-select
                @visible-change="handNodeGidChange"
                placeholder="请选择任务执行机组"
                v-model="form.node_gid"
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
              <el-switch v-model="form.is_sync_file"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务来源" prop="source">
              <el-select
                class="handle-input"
                placeholder="DDS任务，来源选择DDS"
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
              <el-switch v-model="form.is_replace"></el-switch>
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
                <div class="el-upload__text" style="width:290px;font-size:12px">
                  若已上传过文件，则无需再上传；将文件拖到此处，或
                  <em>点击上传</em>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="12"></el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item class="submit-item">
              <el-button @click="reBack" icon="el-icon-back">返回</el-button>
              <el-button @click="reset" icon="el-icon-refresh-left">
                重置
              </el-button>
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

        <!-- dialog 触发方式选项 弹窗 -->
        <el-dialog
          :before-close="handleClose"
          :visible.sync="dialogVisible"
          title="触发方式选项"
          width="75%"
        >
          <!-- 时间 -->
          <el-form-item
            label="触发时间"
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
            v-if="form.triggerMode === '1' || form.triggerMode === '2'"
          >
            <el-row>
              <el-col :span="11">
                <el-input
                  :disabled="form.checkPattern"
                  @change="handleTriggerParamChange"
                  placeholder="例如：1"
                  v-model="form.runFrequency"
                ></el-input>
              </el-col>
              <el-col :span="1" class="line">-</el-col>
              <el-col :span="12">
                <el-form-item prop="timeType">
                  <el-select
                    :disabled="form.checkPattern"
                    @change="handleTriggerParamChange"
                    placeholder="请选择"
                    v-model="form.timeType"
                  >
                    <el-option key="day" label="天/次" value="day"></el-option>
                    <el-option
                      key="week"
                      label="周/次"
                      value="week"
                    ></el-option>
                    <el-option
                      key="month"
                      label="月/次"
                      value="month"
                    ></el-option>
                    <el-option
                      key="year"
                      label="年/次"
                      value="year"
                    ></el-option>
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
                </el-form-item>
              </el-col>
            </el-row>
          </el-form-item>
          <div v-if="form.triggerMode === '1' || form.triggerMode === '2'">
            <p style="border-top:1px dotted #E4E7ED"></p>
            <el-form-item label="CRON模式">
              <el-switch
                @change="handleCheckPattern"
                v-model="form.checkPattern"
              />
            </el-form-item>
            <el-form-item label="CRON表达式">
              <el-input :disabled="!form.checkPattern" v-model="form.cronExp" />
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
            <el-button @click="dialogVisible = false">取 消</el-button>
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
export default {
  data() {
    let nameRule1 = (rule, value, callback) => {
      let regExp = /^[_a-zA-Z0-9.]+$/;
      if (regExp.test(value) === false) {
        callback(new Error("任务名称仅能含有英文字母或数字或下划线或."));
      } else {
        callback();
      }
    };
    let nameRule2 = (rule, value, callback) => {
      let _self = this;
      try {
        _self.axios
          .post(
            "/clockwork/web/task/checkTaskName",
            _self.qs.stringify({
              taskName: value
            })
          )
          .then(response => {
            if (response.code === "OK") {
              if (response.data > 0) {
                callback(new Error("任务名称已存在，请输入新的"));
              } else {
                callback();
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
      } catch (err) {
        _self.$message({
          message: "系统错误",
          type: "warning",
          duration: 5000
        });
        console.log(err);
      }
    };

    return {
      tableData: [],
      leftColumns: [
        { label: "任务id", id: "id", width: "90px" },
        { label: "任务名", id: "name" }
      ],
      taskGroups: [],
      locationPrefixList: [], // 文件前缀list
      locationPrefix: "",
      locationSuffix: "",
      sourceFilterOptions: this.GLOBAL.sourceFilterOptions,
      transferDependencyData: [], // 穿梭框下拉数据
      form: {
        name: "",
        description: "",
        location: "",
        command: "",
        triggerMode: "",
        timeType: "",
        runFrequency: "",
        triggerTime: "",
        groupId: null,
        groupName: "",
        isPrivate: "false",
        runEngine: "",
        emailList: "",
        parameter: "",
        expiredTime: "",
        proxyUser: "",
        dependencyId: [],
        scriptName: "",
        scriptParameter: "",
        scriptType: "sh",
        checkPattern: false,
        cronExp: "",
        node_gid: null,
        nodeGroupSelect: [],
        failedRetries: "",
        run_timeout: 120,
        is_sync_file: true,
        is_replace: true,
        resultStatusJudge: [],
        resultStatusJudgeData: []
      },
      uploadFileUrl: "/clockwork/web/dfs/file/uploadFile",
      fileList: [],
      dialogVisible: false,
      dependencyId: [], // 缓存
      rules: {
        name: [
          {
            required: true,
            message: "任务名称不能为空",
            trigger: "blur"
          },
          {
            validator: nameRule1,
            trigger: "blur"
          },
          {
            validator: nameRule2,
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
        node_gid: [
          {
            required: true,
            message: "请选择执行机组",
            trigger: "change"
          }
        ],
        source: [
          {
            required: true,
            message: "请选择任务来源",
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
    this.taskGroups = this.getTaskGroupIdAndNameByUserName(); // 任务组
    this.getNodeGroup(); // 提交机组
    this.getUploadPathPrefix(); // 上传文件前缀
    this.getTransferDependencyData(); // 获取穿梭框任务
  },
  methods: {
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
        if (this.form.tableData === "" || this.form.tableData === null) {
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
        .get("/clockwork/web/task/getTaskIdAndNameByUserName", {
          params: {
            userName: userName
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
    handTriggerMode(val) {
      // 选择触发模式
      if (!val) {
        this.dialogVisible = true;
      }
    },
    handNodeGidChange(val) {
      // 特殊处理
      if (this.form.node_gid === 1) {
        this.form.is_sync_file = true;
        this.form.is_replace = true;
      } else {
        this.form.is_sync_file = false;
        this.form.is_replace = false;
      }
    },
    handleClose() {
      this.dialogVisible = false;
    },
    getNodeGroup() {
      // 获取执行机器组
      this.axios
        .get("/clockwork/web/node/group/getAllNodeGroup", {})
        .then(response => {
          this.form.nodeGroupSelect = response.data;
          //下拉显示默认值
          response.data.forEach((item, index) => {
            if (item.name === "default") {
              this.form.node_gid = item.id;
            }
          });
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
    handleCheckPattern(val) {
      this.form.checkPattern = val;
      if (val) {
        //选中清空
        this.form.triggerTime = null;
        this.form.runFrequency = "";
        this.form.timeType = "";
      } else {
        //未选中
        this.cronExp = "";
      }
    },
    submitForm(formName) {
      // 提交数据
      let _self = this;
      let task = {};
      task.name = this.form.name;
      if (
        this.form.name === "" ||
        this.form.location === "" ||
        this.form.scriptName === "" ||
        this.form.triggerMode === "" ||
        this.form.groupName === "" ||
        this.form.source === ""
      ) {
        this.$message({
          message: "*号必填",
          type: "warning"
        });
        return false;
      }

      task.description = this.form.description;
      task.location = this.form.location;
      task.command = this.form.command;
      task.triggerMode = this.form.triggerMode;
      if (this.form.triggerMode === "1" || this.form.triggerMode === "2") {
        task.timeType = this.form.timeType;
        task.runFrequency = this.form.runFrequency;
        task.triggerTime = this.form.triggerTime;
      }
      if (this.form.triggerMode === "0" || this.form.triggerMode === "2") {
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
      task.groupId = this.form.groupId;
      task.isPrivate = this.form.isPrivate;
      if (this.form.runEngine !== "") {
        task.runEngine = this.form.runEngine;
      }
      if (this.form.emailList.trim() !== "") {
        task.emailList = this.form.emailList;
      }
      if (this.form.parameter.trim() !== "") {
        task.parameter = this.form.parameter;
      }
      if (this.form.proxyUser.trim() !== "") {
        task.proxyUser = this.form.proxyUser;
      }
      if (this.form.expiredTime !== "") {
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
      if (this.form.scriptName !== "") {
        task.scriptName = this.form.scriptName;
      }
      task.scriptParameter = this.form.scriptParameter;
      task.createUser = this.$store.state.user.name;
      task.operatorName = this.$store.state.user.name;

      task.nodeGid = this.form.node_gid;
      task.failedRetries = this.form.failedRetries;
      task.runTimeout = this.form.run_timeout;
      task.isSyncFile = this.form.is_sync_file;
      task.isReplace = this.form.is_replace;
      task.source = this.form.source;
      task.scriptType = this.form.scriptType;
      task.cronExp = this.form.cronExp;
      task.errorKeywordIds = this.form.resultStatusJudge.join(",");

      //上传文件
      this.$refs.upload.submit();
      this.$refs[formName].validate(valid => {
        if (valid) {
          _self.axios
            .post(
              "/clockwork/web/task/operation/addTask",
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
                  message: "添加任务成功",
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

    // 任务组相关查询和搜索
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

    setTriggerTime(val) {
      let d = new Date(val);
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
    handleSelectGroup(item) {
      this.form.groupName = item.value;
      this.form.groupId = item.id;
    },

    reset() {
      window.location.reload();
    },
    reBack() {
      this.$router.push("/task/taskList");
    },
    getUploadPathPrefix() {
      // 获取上传文件前缀
      this.locationPrefixList = [];
      this.axios
        .get("/clockwork/web/task/uploadPathPrefix")
        .then(res => {
          this.locationPrefixList = res.data;
          this.locationPrefix = res.data[0];
        })
        .catch(err => {
          this.$message({
            message: err.msg,
            type: "warning",
            duration: 5000
          });
        });
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
    }
  }
};
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

.el-transfer-panel {
  width: 350px !important;
}

.el-form-item {
  width: 100%;
}

#transfer {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /*text-align: center;*/
  color: #2c3e50;
  /*margin-top: 60px;*/
}
</style>
