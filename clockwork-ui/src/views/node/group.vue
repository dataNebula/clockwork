<template>
  <div class="app-container">
    <div class="filter-container">
      <div class="form_item">
        <el-input
          class="handle-input"
          onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
          placeholder="节点组ID"
          size="medium"
          type="number"
          v-model="id"
        />
      </div>
      <el-button
        @click="handleCreate"
        class="fr"
        icon="el-icon-circle-plus-outline"
        size="medium"
        type="primary"
      >
        添加节点组
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

    <el-dialog :visible.sync="dialogFormVisible" title="添加节点组">
      <el-form :model="nodeGroup" :rules="rules" label-width="100px" ref="node">
        <el-form-item label="节点组名称" prop="name">
          <el-input
            placeholder="请输入节点组名称"
            v-model="nodeGroup.name"
          ></el-input>
        </el-form-item>
        <el-form-item label="节点组描述" prop="description">
          <el-input
            placeholder="请输入节点组描述"
            v-model="nodeGroup.description"
          ></el-input>
        </el-form-item>
      </el-form>
      <div class="dialog-footer" slot="footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button
          @click="submitCreate('node')"
          icon="el-icon-document-add"
          type="primary"
        >
          提 交
        </el-button>
      </div>
    </el-dialog>

    <el-table
      :data="tableData"
      append
      border
      ref="singleTable"
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column
        label="ID"
        prop="id"
        sortable
        width="120"
      ></el-table-column>
      <el-table-column label="组名称" prop="name" width="180">
        <template slot-scope="scope">
          <template v-if="scope.row.edit">
            <el-input
              class="edit-input"
              size="small"
              v-model="scope.row.name"
            />
          </template>
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="描述" min-width="250" prop="description">
        <template slot-scope="scope">
          <template v-if="scope.row.edit">
            <el-input
              class="edit-input"
              size="small"
              v-model="scope.row.description"
            />
          </template>
          <span v-else>{{ scope.row.description }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        prop="createTime"
        width="200"
      ></el-table-column>
      <el-table-column
        label="更新时间"
        prop="updateTime"
        width="200"
      ></el-table-column>
      <el-table-column fixed="left" label="操作" width="130">
        <template slot-scope="scope">
          <div class="operate-block">
            <el-popconfirm
              @onConfirm="handleDelete(scope.$index, scope.row)"
              icon="el-icon-info"
              iconColor="red"
              title="你确定要删除该节点组吗？"
            >
              <el-tooltip
                class="item"
                content="删除"
                effect="dark"
                placement="top"
                slot="reference"
              >
                <em><i class="el-icon-delete" /></em>
              </el-tooltip>
            </el-popconfirm>

            <el-tooltip
              class="item"
              content="修改"
              effect="dark"
              placement="top"
              slot="reference"
              v-if="!scope.row.edit"
            >
              <em>
                <i
                  @click="scope.row.edit = !scope.row.edit"
                  class="el-icon-edit"
                />
              </em>
            </el-tooltip>

            <el-tooltip
              class="item"
              content="确认"
              effect="dark"
              placement="top"
              slot="reference"
              v-if="scope.row.edit"
            >
              <em>
                <i @click="confirmEdit(scope.row)" class="el-icon-check" />
              </em>
            </el-tooltip>

            <el-tooltip
              class="item"
              content="取消"
              effect="dark"
              placement="top"
              slot="reference"
              v-if="scope.row.edit"
            >
              <em>
                <i @click="cancelEdit(scope.row)" class="el-icon-close" />
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
      dialogFormVisible: false,
      tableData: [],
      currentPage: 1,
      total: 0,
      pageSize: 10,
      loading: false,
      loginName: null,
      isAdmin: null,
      is_search: false,
      id: null,
      edit: false,
      nodeGroup: {
        name: "",
        description: ""
      },

      rules: {
        name: [
          { required: true, message: "请添加节点组名称", trigger: "blur" }
        ],
        description: [
          { required: true, message: "请添加节点组描述", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getData();
  },
  methods: {
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.getData();
    },
    handleCurrentChange(currentPage) {
      this.currentPage = currentPage;
      this.getData();
    },
    reset() {
      this.id = null;
      this.getData();
    },
    getData() {
      let _self = this;
      this.loading = true;

      _self.loginName = this.$store.state.user.name;
      _self.isAdmin = this.$store.state.user.isAdmin;

      let pageParam = {};
      pageParam.pageNum = _self.currentPage;
      pageParam.pageSize = _self.pageSize;
      pageParam.userName = _self.loginName;

      // 检索条件
      if (_self.id === "") {
        _self.id = null;
      }
      pageParam.condition = JSON.stringify({
        id: _self.id == "" ? null : this.id
      });
      _self.tableData = [];
      _self.axios
        .post(
          "/clockwork/web/node/group/searchNodeGroupPageList",
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
                obj.description = response.data.list[i].description;
                obj.createTime = response.data.list[i].createTime;
                obj.updateTime = response.data.list[i].updateTime;
                obj.edit = false;
                obj.originalName = obj.name;
                obj.originalDescription = obj.description;
                _self.tableData.push(obj);
              }
            }
            _self.total = response.data.total;
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
    cancelEdit(row) {
      row.edit = false;
      row.name = row.originalName;
      row.description = row.originalDescription;
      this.$message({
        message: "name|description, 已还原为原始值",
        type: "warning"
      });
    },
    confirmEdit(row) {
      row.edit = false;
      let _self = this;
      _self.axios
        .post(
          "/clockwork/web/node/group/updateNodeGroup",
          JSON.stringify(row),
          {
            headers: { "Content-Type": "application/json" }
          }
        )
        .then(response => {
          if (response.code === "OK") {
            _self.$message({
              message: "修改节点组信息成功",
              type: "success"
            });
          } else {
            row.name = row.originalName;
            row.description = row.originalDescription;
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
    resetTemp() {
      this.nodeGroup = {
        name: "",
        description: ""
      };
    },
    handleCreate() {
      this.resetTemp();
      this.dialogFormVisible = true;
    },
    submitCreate(formName) {
      let _self = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          _self.axios
            .post(
              "/clockwork/web/node/group/addNodeGroup",
              JSON.stringify(this.nodeGroup),
              {
                headers: { "Content-Type": "application/json" }
              }
            )
            .then(response => {
              if (response.code === "OK") {
                _self.$message({
                  message: "添加节点组成功",
                  type: "success"
                });
                this.dialogFormVisible = false;
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
          _self.$message({
            message: "表单填写不符合规则，提交失败！",
            type: "warning",
            duration: 5000
          });
          return false;
        }
      });
    },
    handleDelete(status, row) {
      let id = row.id;
      let _self = this;
      _self.axios
        .post(
          "/clockwork/web/node/group/deleteNodeGroup",
          _self.qs.stringify({ id: id })
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
  }
};
</script>

<style scoped>
.handle-box {
  margin-bottom: 20px;
}

.handle-input {
  width: 300px;
  display: inline-block;
}
</style>
