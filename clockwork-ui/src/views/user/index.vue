<template>
    <div class="app-container">
        <div class="filter-container">
            <div class="box_l">
                <el-row>
                    <el-col :span="8">
                        <el-input class="handle-input" placeholder="筛选用户名" v-model="userName"></el-input>
                    </el-col>
                    <el-col :span="8">
                        <el-select class="handle-input" clearable placeholder="角色" size="medium" v-model="roleName">
                            <el-option :key="item.key" :label="item.label" :value="item.label"
                                       v-for="item in optionsRoleData"/>
                        </el-select>
                    </el-col>
                    <el-col :span="8">
                    </el-col>
                </el-row>
            </div>
            <div class="box_r">
                <el-button @click="handleCreate" class="fr" icon="el-icon-circle-plus-outline" type="primary">添加用户
                </el-button>
                <el-button @click="reset" class="fr marR15" icon="el-icon-refresh-left" size="medium" type="info">重置
                </el-button>
                <el-button @click="getData" class="marR15 fr" icon="el-icon-search" size="medium" type="primary">搜索
                </el-button>
            </div>
        </div>

        <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible" width="70%">
            <el-form :model="user" :rules="rules" label-width="100px" ref="dataForm">
                <el-form-item label="用户名" prop="userName">
                    <el-input placeholder="请输入用户名" v-model="user.userName"></el-input>
                </el-form-item>

                <el-form-item label="是否激活" prop="isActive">
                    <template>
                        <el-radio :label="true" v-model="user.isActive">是</el-radio>
                        <el-radio :label="false" v-model="user.isActive">否</el-radio>
                    </template>
                </el-form-item>

                <el-form-item label="选择角色" prop="roleName">
                    <template>
                        <el-transfer :button-texts="['移除角色', '添加角色']" :data="transferRoleData"
                                     :filter-method="filterMethod" :titles="['选择角色', '已有角色']"
                                     filter-placeholder="输入角色可搜索" filterable v-model="user.roleName">
                        </el-transfer>
                    </template>
                </el-form-item>
            </el-form>
            <div class="dialog-footer" slot="footer">
                <el-button @click="dialogFormVisible = false">取 消</el-button>
                <el-button @click="dialogStatus === 'create' ? createData() : updateData()" icon="el-icon-circle-check"
                           type="primary">提 交
                </el-button>
            </div>
        </el-dialog>

        <el-table :data="tableData" append border ref="singleTable" style="width: 100%" v-loading="loading">
            <el-table-column label="ID" prop="id" sortable width="100"></el-table-column>
            <el-table-column label="用户名" prop="userName" width="210"></el-table-column>
            <el-table-column label="角色" min-width="250" prop="roleName"></el-table-column>
            <el-table-column label="激活" prop="isActive" width="70">
                <template slot-scope="scorp">
                    {{ scorp.row.isActive | isNotFilter }}
                </template>
            </el-table-column>
            <el-table-column label="更新时间" prop="updateTime" width="180"></el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>

            <el-table-column fixed="left" label="操作" width="120">
                <template slot-scope="scope">
                    <div class="operate-block">
                        <el-popconfirm @onConfirm="handleUpdate(scope.row)" icon="el-icon-edit" title="你确定要修改该用户吗？">
                            <el-tooltip class="item" content="修改" effect="dark" placement="top" slot="reference">
                                <em><i class="el-icon-edit"></i></em>
                            </el-tooltip>
                        </el-popconfirm>

                        <el-popconfirm @onConfirm="handleDelete(scope.$index, scope.row)" icon="el-icon-info"
                                       iconColor="red" title="你确定要删除该用户吗？">
                            <el-tooltip class="item" content="删除" effect="dark" placement="top" slot="reference">
                                <em><i class="el-icon-delete"></i></em>
                            </el-tooltip>
                        </el-popconfirm>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination">
            <el-pagination :current-page="currentPage" :page-size="pageSize" :page-sizes="[10, 20, 40, 100]"
                           :total="total" @current-change="handleCurrentChange" @size-change="handleSizeChange"
                           layout="total, sizes, prev, pager, next, jumper">
            </el-pagination>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                dialogFormVisible: false,
                dialogStatus: "", // 创建|修改
                tableData: [],
                transferRoleData: [], // 角色下拉数据
                optionsRoleData: [], // 角色下拉数据
                currentPage: 1,
                total: 0,
                pageSize: 10,
                userName: null, // 搜索
                roleName: null, // 搜索
                loading: false,
                loginName: null,
                isAdmin: null,
                is_search: false,
                textMap: {
                    update: "修改用户",
                    create: "添加用户"
                },
                user: {
                    // 新增数据
                    userName: "",
                    isActive: true,
                    roleName: [], // [k1,k2]
                    createTime: null
                },
                rules: {
                    roleName: [
                        {required: true, message: "请选角色", trigger: "blur"}
                    ],
                    userName: [
                        {required: true, message: "请输入用户名", trigger: "blur"}
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
                this.roleName = null;
                this.userName = null;
                this.getData();
            },
            getData() {
                let _self = this;

                _self.loginName = this.$store.state.user.name;
                _self.isAdmin = this.$store.state.user.isAdmin;

                let pageParam = {};
                pageParam.pageNum = _self.currentPage;
                pageParam.pageSize = _self.pageSize;
                pageParam.userName = _self.loginName;

                // 检索条件
                if (_self.userName === "") {
                    _self.userName = null;
                }
                if (_self.roleName === "") {
                    _self.roleName = null;
                }
                pageParam.condition = JSON.stringify({
                    userName: _self.userName == '' ? null : this.userName,
                    roleName: _self.roleName == '' ? null : this.roleName
                });
                _self.tableData = [];
                _self.axios
                    .post(
                        "/clockwork/web/user/searchUserPageList",
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
                                for (
                                    let i = 0;
                                    i < response.data.list.length;
                                    i++
                                ) {
                                    let obj = {};
                                    obj.id = response.data.list[i].id;
                                    obj.roleName = response.data.list[i].roleName;
                                    obj.userName = response.data.list[i].userName;
                                    obj.isActive = response.data.list[i].isActive;
                                    obj.createTime =
                                        response.data.list[i].createTime;
                                    obj.updateTime =
                                        response.data.list[i].updateTime;
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
                // 设置角色数据
                this.setRoleData();
            },
            setRoleData() {
                let _self = this;
                _self.transferRoleData = [];
                _self.axios
                    .get("/clockwork/web/role/getAllRole", {})
                    .then(response => {
                        if (response.code === "OK") {
                            if (response.data) {
                                for (let i = 0; i < response.data.length; i++) {
                                    let obj = {};
                                    obj.key = response.data[i].id;
                                    obj.label = response.data[i].name;
                                    obj.serach = response.data[i].name;
                                    _self.transferRoleData.push(obj);
                                }
                                _self.optionsRoleData = _self.transferRoleData;
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
            setUserRoleData(userId) {
                console.log("-===");
                let _self = this;
                _self.user.roleName = []; // [k1,k2]
                _self.axios
                    .get("/clockwork/web/role/getRoleByUserId", {
                        params: {userId: userId}
                    })
                    .then(response => {
                        if (response.code === "OK") {
                            if (response.data) {
                                for (let i = 0; i < response.data.length; i++) {
                                    _self.user.roleName.push(response.data[i].id);
                                    // obj.label = response.data[i].name;
                                    // obj.serach = response.data[i].name;

                                    // _self.transferRoleData.re
                                }
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
            filterMethod(query, item) {
                return item.serach.indexOf(query) > -1;
            },
            resetTemp() {
                this.user = {
                    id: null,
                    userName: "",
                    isActive: true,
                    roleName: []
                };
            },
            handleCreate() {
                this.resetTemp();
                this.dialogFormVisible = true;
                this.dialogStatus = "create";
                this.$nextTick(() => {
                    this.$refs["dataForm"].clearValidate();
                });
            },
            createData() {
                let _self = this;
                this.$refs["dataForm"].validate(valid => {
                    if (valid) {
                        let obj = {};
                        obj.userName = _self.user.userName;
                        obj.roleName = _self.user.roleName.join(",");
                        obj.isActive = _self.user.isActive;
                        _self.axios
                            .post(
                                "/clockwork/web/user/addUser",
                                JSON.stringify(obj),
                                {headers: {"Content-Type": "application/json"}}
                            )
                            .then(response => {
                                if (response.code === "OK") {
                                    _self.$message({
                                        message: "添加用户成功",
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
            handleUpdate(row) {
                this.user = Object.assign({}, row); // copy obj
                this.setUserRoleData(row.id);
                this.dialogStatus = "update";
                this.dialogFormVisible = true;
                this.$nextTick(() => {
                    this.$refs["dataForm"].clearValidate();
                });
            },
            updateData() {
                let _self = this;
                this.$refs["dataForm"].validate(valid => {
                    if (valid) {
                        let obj = Object.assign({}, _self.user); // copy obj;
                        obj.roleName = _self.user.roleName.join(",");
                        _self.axios
                            .post(
                                "/clockwork/web/user/updateUser",
                                JSON.stringify(obj),
                                {headers: {"Content-Type": "application/json"}}
                            )
                            .then(response => {
                                if (response.code === "OK") {
                                    _self.$message({
                                        message: "修改用户成功",
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
                        "/clockwork/web/user/deleteUser",
                        _self.qs.stringify({id: id})
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
</style>
