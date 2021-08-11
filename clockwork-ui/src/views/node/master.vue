<template>
    <div class="app-container">
        <div class="filter-container">
            <div class="box_l">
                <div class="inner_contain">
                    <el-row>
                        <el-col :span="8">
                            <el-input class="handle-input"
                                      onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                                      placeholder="节点ID" size="medium" type="number" v-model="id" clearable/>
                        </el-col>
                        <el-col :span="8">
                            <el-input class="handle-input" placeholder="IP" size="medium" v-model="ip" clearable/>
                        </el-col>
                        <el-col :span="8">
                            <el-input class="handle-input" placeholder="域名" size="medium" v-model="domainName"
                                      clearable/>
                        </el-col>
                    </el-row>
                    <div v-if="showMore">
                        <el-row>
                            <el-col :span="8">
                                <el-select class="handle-input" placeholder="状态" size="medium" v-model="status" clearable>
                                        <el-option label="启用" value="enable"/>
                                        <el-option label="禁用" value="disable"/>
                                </el-select>
                            </el-col>
                            <el-col :span="8"></el-col>
                            <el-col :span="8"></el-col>
                        </el-row>
                    </div>
                    <el-button
                        type="success"
                        :class="showMore ? 'upBtn' : 'moreBtn'"
                        :icon="showMore ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
                        size="medium"
                        round
                        @click="moreSearch"
                    >{{ showMore ? "收起" : "更多" }}
                    </el-button
                    >
                </div>
            </div>
            <div class="box_r">
                <el-button @click="handleCreate" icon="el-icon-circle-plus-outline" type="primary" size="medium"
                           class="fr">添加节点
                </el-button>
                <el-button @click="reset" class="fr marR15" icon="el-icon-refresh-left" size="medium" type="info">重置
                </el-button>
                <el-button @click="getData" class="marR15 fr" icon="el-icon-search" size="medium" type="primary">搜索
                </el-button>
            </div>
        </div>
        <el-table :data="tableData" append border ref="singleTable" style="width: 100%" v-loading="loading">
            <el-table-column label="ID" prop="id" sortable width="80"></el-table-column>
            <el-table-column label="角色" prop="role" width="120"></el-table-column>
            <el-table-column label="ip" prop="ip" width="140"></el-table-column>
            <el-table-column label="域名" min-width="200" prop="domainName"></el-table-column>
            <el-table-column label="端口" prop="port" width="90"></el-table-column>
            <el-table-column label="状态" prop="status" width="90">
                <template slot-scope="scorp">
                    <el-tag :type="scorp.row.status==='enable'?'success':'warning'">
                        {{ scorp.row.status | nodeStatusFilter }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>
            <el-table-column label="更新时间" prop="updateTime" width="180"></el-table-column>
            <el-table-column fixed="left" label="操作" width="100">
                <template slot-scope="scope">
                    <div class="operate-block">
                        <el-tooltip class="item" content="修改" effect="dark" placement="top" slot="reference">
                            <em><i @click="handleEdit(scope.$index, scope.row)" class="el-icon-edit"/></em>
                        </el-tooltip>

                        <el-popconfirm @onConfirm="handleDelete(scope.$index, scope.row)" icon="el-icon-info"
                                       iconColor="red" title="你确定要删除该节点吗？">
                            <el-tooltip class="item" content="删除" effect="dark" placement="top" slot="reference">
                                <em><i class="el-icon-delete"/></em>
                            </el-tooltip>
                        </el-popconfirm>
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
            >
            </el-pagination>
        </div>
        <el-dialog :visible.sync="dialogFormVisible" title="添加节点">
            <el-form :model="node" :rules="rules" label-width="100px" ref="node">
                <el-form-item label="域名" prop="domainName">
                    <el-input placeholder="请输入域名" v-model="node.domainName"></el-input>
                </el-form-item>
                <el-form-item label="ip" prop="ip">
                    <el-input placeholder="请输入ip" v-model="node.ip"></el-input>
                </el-form-item>
                <el-form-item label="port" prop="port">
                    <el-input placeholder="请输入端口" v-model="node.port"></el-input>
                </el-form-item>
            </el-form>
            <div class="dialog-footer" slot="footer">
                <el-button @click="dialogFormVisible = false">取 消</el-button>
                <el-button
                    @click="submitCreate('node')"
                    icon="el-icon-document-add"
                    type="primary">提 交
                </el-button>
            </div>
        </el-dialog>

        <el-dialog :visible.sync="dialogEditVisible" title="修改节点">
            <el-form ref="editForm" :model="editForm" :rules="addRules" label-width="100px">
                <el-form-item label="域名" prop="domainName">
                    <el-input
                        v-model="editForm.domainName"
                        placeholder="请输入机器域名"
                    ></el-input>
                </el-form-item>
                <el-form-item label="ip" prop="ip">
                    <el-input
                        v-model="editForm.ip"
                        placeholder="请输入机器ip"
                    ></el-input>
                </el-form-item>
            </el-form>
            <div class="dialog-footer" slot="footer">
                <el-button @click="dialogEditVisible = false">取 消</el-button>
                <el-button
                    @click="submitForm('editForm')"
                    icon="el-icon-document-add"
                    type="primary">提 交
                </el-button>
            </div>
        </el-dialog>
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
                id: null, // 搜索
                ip: null, // 搜索
                nodeGroupId: null, // 搜索
                domainName: null, // 搜索
                role: null, // 搜索
                groupName: null, // 搜索
                status: "",
                node: {
                    role: "master",
                    domainName: "",
                    ip: "",
                    port: ""
                },

                rules: {
                    role: [
                        {
                            required: true,
                            message: "请选择节点角色",
                            trigger: "blur"
                        }
                    ],
                    domainName: [
                        {required: true, message: "请输入域名", trigger: "blur"}
                    ],
                    ip: [{required: true, message: "请输入ip", trigger: "blur"}],
                    port: [
                        {required: true, message: "请输入端口", trigger: "blur"}
                    ]
                },
                dialogEditVisible: false,
                editForm: {
                    role: "",
                    domainName: "",
                    ip: "",
                    nodeId: ""
                },

                addRules: {
                    role: [
                        {
                            required: true,
                            message: "请选择节点角色",
                            trigger: "blur"
                        }
                    ],
                    domainName: [
                        {
                            required: true,
                            message: "请输入机器域名",
                            trigger: "blur"
                        }
                    ],
                    ip: [
                        {
                            required: true,
                            message: "请输入机器ip",
                            trigger: "blur"
                        }
                    ]
                },
                showMore: false,
            };
        },
        methods: {
            getData() {
                const _self = this;
                this.loading = true;

                _self.loginName = this.$store.state.user.name;
                _self.isAdmin = this.$store.state.user.isAdmin;

                const pageParam = {};
                pageParam.pageNum = _self.currentPage;
                pageParam.pageSize = _self.pageSize;
                pageParam.userName = _self.loginName;

                // 检索条件
                if (_self.id === "") {
                    _self.id = null;
                }
                if (_self.nodeGroupId === "") {
                    _self.nodeGroupId = null;
                }
                pageParam.condition = JSON.stringify({
                    id: _self.id,
                    ip: _self.ip,
                    domainName: _self.domainName,
                    nodeGroupId: _self.nodeGroupId,
                    groupName: _self.groupName,
                    status: _self.status,
                    role: "master"
                });
                _self.tableData = [];
                _self.axios
                    .post(
                        "/clockwork/web/node/searchNodePageList",
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
                                    const obj = {};
                                    obj.id = response.data.list[i].id;
                                    obj.nodeGroupId =
                                        response.data.list[i].nodeGroupId;
                                    obj.groupName = response.data.list[i].groupName;
                                    obj.role = response.data.list[i].role;
                                    obj.ip = response.data.list[i].ip;
                                    obj.domainName =
                                        response.data.list[i].domainName;
                                    obj.port = response.data.list[i].port;
                                    obj.status = response.data.list[i].status;
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
            },
            handleCurrentChange(currentPage) {
                this.currentPage = currentPage;
                this.getData();
            },
            handleSizeChange(pageSize) {
                this.pageSize = pageSize;
                this.getData();
            },
            handleCreate() {
                this.resetTemp();
                this.dialogFormVisible = true;
                // this.$nextTick(() => {
                //     this.$refs['dataForm'].clearValidate()
                // })
            },
            reset() {
                this.id = null;
                this.ip = null;
                this.domainName = null;
                this.status = "";
                this.getData();
            },
            resetTemp() {
                this.node = {
                    domainName: "",
                    ip: "",
                    role: "master",
                    port: ""
                };
            },
            submitCreate(formName) {
                const _self = this;
                this.$refs[formName].validate(valid => {
                    if (valid) {
                        _self.axios
                            .post(
                                "/clockwork/web/node/addNode",
                                JSON.stringify(this.node),
                                {headers: {"Content-Type": "application/json"}}
                            )
                            .then(response => {
                                if (response.code === "OK") {
                                    _self.$message({
                                        message: "添加节点成功",
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
            handleEdit(index, row) {
                this.editForm.nodeId = row.id;
                this.editForm.ip = row.ip;
                this.editForm.domainName = row.domainName;
                this.dialogEditVisible = true;
            },
            submitForm(formName) {
                const node = {};
                node.id = this.editForm.nodeId;
                node.role = "master";
                node.domainName = this.editForm.domainName;
                node.ip = this.editForm.ip;

                const _self = this;
                this.$refs[formName].validate(valid => {
                    if (valid) {
                        _self.axios
                            .post(
                                "/clockwork/web/node/updateNode",
                                JSON.stringify(node),
                                {headers: {"Content-Type": "application/json"}}
                            )
                            .then(response => {
                                if (response.code === "OK") {
                                    _self.$message({
                                        message: "修改节点成功",
                                        type: "success"
                                    });
                                    this.getData();
                                    this.dialogEditVisible = false;
                                } else {
                                    _self.$message({
                                        message: response.msg,
                                        type: "warning",
                                        duration: 5000
                                    });
                                    this.dialogEditVisible = true;
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
                        this.dialogEditVisible = true;
                        return false;
                    }
                });
            },
            handleDelete(status, row) {
                const id = row.id;
                const _self = this;
                _self.axios
                    .post(
                        "/clockwork/web/node/deleteNode",
                        _self.qs.stringify({nodeId: id})
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
            },
            moreSearch() {
                this.showMore = !this.showMore;
            }
        },
        mounted() {
            this.getData();
        }
    };
</script>
