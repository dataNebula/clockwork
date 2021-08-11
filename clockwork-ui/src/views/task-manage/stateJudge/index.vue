<template>
  <div class="app-container">
    <div class="filter-container">
      <div class="form_item">
        <el-input
          class="handle-input"
          onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
          placeholder="错误描述ID"
          size="medium"
          type="number"
          v-model="id"
        />
        <el-input
          class="handle-input-word"
          placeholder="错误描述"
          size="medium"
          clearable
          v-model="errorWord"
        />
      </div>
      <el-button
        @click="handleCreate"
        class="fr"
        icon="el-icon-circle-plus-outline"
        size="medium"
        type="primary"
      >
        添加错误描述
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

    <el-dialog
      :visible.sync="dialogFormVisible"
      title="添加错误描述"
      destroy-on-close
    >
      <el-form :model="nodeGroup" :rules="rules" label-width="100px" ref="node">
        <el-form-item label="错误描述" prop="description">
          <el-input
            placeholder="请输入错误描述"
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
    <el-table style="width: 100%" border :data="tableData" v-loading="loading">
      <el-table-column label="ID" prop="id" sortable width="120" />
      <el-table-column label="错误描述" prop="errorWord" min-width="300">
        <template slot-scope="scope">
          <template v-if="scope.row.edit">
            <el-input
              class="edit-input"
              size="small"
              v-model="scope.row.errorWord"
            />
          </template>
          <span v-else>{{ scope.row.errorWord }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="200">
        <template slot-scope="scorp">
          {{ scorp.row.createTime | formatDayFilter }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updateTime" width="200">
        <template slot-scope="scorp">
          {{ scorp.row.updateTime | formatDayFilter }}
        </template>
      </el-table-column>
      <el-table-column fixed="left" label="操作" width="130">
        <template slot-scope="scope">
          <div class="operate-block">
            <el-popconfirm
              @onConfirm="handleDelete(scope.row)"
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
      id: null,
      errorWord: null,
      nodeGroup: {
        description: ""
      },
      rules: {
        description: [
          { required: true, message: "请添加错误描述", trigger: "blur" }
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
    getData() {
      this.loading = true;
      const { name, role } = this.$store.state.user;
      const { currentPage, pageSize, id, errorWord } = this;
      const condition = JSON.stringify({
        // eslint-disable-next-line
        id: id ? (Number(id) ? Number(id) : id) : null,
        // eslint-disable-next-line
        errorWord: errorWord ? errorWord : null
      });
      const paramsObj = {
        pageNum: currentPage,
        userName: name,
        condition,
        pageSize,
        role
      };
      this.axios
        .post("/clockwork/web/task/keyword/searchPageKeyWordList", {
          ...paramsObj
        })
        .then(response => {
          if (response && response.code === "OK") {
            const { data } = response;
            const tableData = data && data.list;
            this.tableData =
              tableData &&
              tableData.map(item => {
                return {
                  ...item,
                  edit: false,
                  originalErrorWord: item.errorWord
                };
              });

            this.total = data && data.total;
          } else {
            this.$message.error(response && response.msg);
          }
          this.loading = false;
        })
        .catch(error => {
          this.$message.error("接口出错!");
          this.loading = false;
          console.log(error);
        });
    },
    cancelEdit(row) {
      row.edit = false;
      row.errorWord = row.originalErrorWord;
      this.$message({
        message: "错误描述, 已还原为原始值",
        type: "warning"
      });
    },
    confirmEdit(row) {
      row.edit = false;
      let _self = this;
      _self.axios
        .post(
          "/clockwork/web/task/keyword/updateKeyWord",
          JSON.stringify(row),
          {
            headers: { "Content-Type": "application/json" }
          }
        )
        .then(response => {
          if (response.code === "OK") {
            _self.$message({
              message: "修改错误描述成功",
              type: "success"
            });
          } else {
            row.errorWord = row.originalErrorWord;
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
    handleCreate() {
      this.dialogFormVisible = true;
    },
    submitCreate(formName) {
      const { description } = this.nodeGroup;
      let _self = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          _self.axios
            .post(
              "/clockwork/web/task/keyword/createKeyWord",
              JSON.stringify({
                errorWord: description
              }),
              {
                headers: { "Content-Type": "application/json" }
              }
            )
            .then(response => {
              if (response.code === "OK") {
                _self.$message({
                  message: "添加错误描述成功",
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
    handleDelete(row) {
      let id = row.id;
      let _self = this;
      _self.axios
        .get("/clockwork/web/task/keyword/daleteKeyWord", {
          params: {
            keywordId: id
          }
        })
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
    }
  }
};
</script>

<style scoped>
.handle-box {
  margin-bottom: 20px;
}

.handle-input {
  width: 150px;
  display: inline-block;
  margin-right: 20px;
}
.handle-input-word {
  width: 250px;
  display: inline-block;
}
</style>
