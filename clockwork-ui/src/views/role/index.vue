<template>
    <div class="app-container">
        <div class="filter-container">
            <div class="box_l">
                <el-row>
                    <el-col :span="8">
                           <el-input class="handle-input" placeholder="角色" v-model="name" clearable></el-input>
                    </el-col>
                    <el-col :span="8">
                           <el-input class="handle-input" placeholder="用户" v-model="userName" clearable></el-input>
                    </el-col>
                    <el-col :span="8">
                    </el-col>
                </el-row>
            </div>
            <div class="box_r">
                <el-button @click="handleCreate" class="fr" icon="el-icon-circle-plus-outline" type="primary">添加角色</el-button>
                 <el-button @click="reset" class="marR15 fr" icon="el-icon-refresh-left" size="medium" type="info">重置
                </el-button>
                <el-button @click="getData" class="marR15 fr" icon="el-icon-search" size="medium" type="primary">搜索
                </el-button>
            </div>
        </div>


        <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
            <el-form :model="role" :rules="rules" label-width="100px" ref="dataForm">

                <el-form-item label="角色名称" prop="name">
                    <el-input placeholder="请输入用户名" v-model="role.name"></el-input>
                </el-form-item>

                <el-form-item label="角色描述" prop="description">
                    <el-input placeholder="请输入用户名" v-model="role.description"></el-input>
                </el-form-item>

                <el-form-item label="是否是管理员" prop="isAdmin">
                    <template>
                        <el-radio :label="true" v-model="role.isAdmin">是</el-radio>
                        <el-radio :label="false" v-model="role.isAdmin">否</el-radio>
                    </template>
                </el-form-item>

            </el-form>
            <div class="dialog-footer" slot="footer">
                <el-button @click="dialogFormVisible = false">取 消</el-button>
                <el-button @click="dialogStatus==='create'?createData():updateData()" icon="el-icon-circle-check"
                           type="primary">提 交
                </el-button>
            </div>
        </el-dialog>

        <el-table :data="tableData" append border ref="singleTable" style="width: 100%" v-loading="loading">
            <el-table-column label="ID" prop="id" sortable width="100"></el-table-column>
            <el-table-column label="角色" prop="name" width="100"></el-table-column>
            <el-table-column label="描述" prop="description" width="180"></el-table-column>
            <el-table-column label="管理员" prop="isAdmin" width="100">
                <template slot-scope="scorp">
                    {{ scorp.row.isAdmin | isNotFilter }}
                </template>
            </el-table-column>
            <el-table-column label="用户" prop="userName" min-width="200">
                <template slot-scope="scope">
                    <div v-html="scope.row.userName"></div>
                </template>
            </el-table-column>
            <el-table-column label="更新时间" prop="updateTime" width="180"></el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>

            <el-table-column fixed="left" label="操作" width="120">
                <template slot-scope="scope">
                    <div class="operate-block">

                        <el-popconfirm @onConfirm="handleUpdate(scope.row)" icon="el-icon-edit"
                                       title="你确定要修改该角色吗？">
                            <el-tooltip class="item" content="修改" effect="dark" placement="top" slot="reference">
                                <em><i class="el-icon-edit"></i></em>
                            </el-tooltip>
                        </el-popconfirm>

                        <el-popconfirm @onConfirm="handleDelete(scope.$index, scope.row)" icon="el-icon-info"
                                       iconColor="red"
                                       title="你确定要删除该角色吗？">
                            <el-tooltip class="item" content="删除" effect="dark" placement="top" slot="reference">
                                <em><i class="el-icon-delete"></i></em>
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
                dialogStatus: '',   // 创建|修改
                tableData: [],
                currentPage: 1,
                total: 0,
                pageSize: 10,
                name: null,     // 搜索
                userName: null, // 搜索
                loading: false,
                loginName: null,
                isAdmin: null,
                is_search: false,
                textMap: {
                    update: '修改角色',
                    create: '添加角色'
                },
                role: {
                    id: null,
                    name: null,
                    description: null,
                    isAdmin: false
                },
                rules: {
                    name: [
                        {required: true, message: '请选角色', trigger: 'blur'}
                    ],
                    description: [
                        {required: true, message: '请输入角色描述', trigger: 'blur'}
                    ]
                },
            }
        },
        created() {
            this.getData();
        },
        methods: {
            handleSizeChange(pageSize) {
                this.pageSize = pageSize;
                this.getData()
            },
            handleCurrentChange(currentPage) {
                this.currentPage = currentPage;
                this.getData()
            },
            reset() {
                this.name = null;
                this.userName = null;
                this.getData();
            },
            formatterIsAdmin(row, column) {
                switch (row.isAdmin) {
                    case false:
                        return '否';
                        break;
                    case true:
                        return '是';
                        break;
                    default:
                        return ''
                }

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
                if (_self.name === '') {
                    _self.name = null
                }
                if (_self.userName === '') {
                    _self.userName = null
                }
                pageParam.condition = JSON.stringify({
                    name: _self.name == '' ? null : this.name,
                    userName: _self.userName== '' ? null : this.userName
                });
                _self.tableData = [];
                _self.axios.post('/clockwork/web/role/searchRolePageList', JSON.stringify(pageParam), {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => {
                        if (response.code === 'OK') {
                            this.loading = false;
                            if (response.data.list) {
                                for (let i = 0; i < response.data.list.length; i++) {
                                    let obj = {};
                                    obj.id = response.data.list[i].id;
                                    obj.name = response.data.list[i].name;
                                    obj.description = response.data.list[i].description;
                                    obj.isAdmin = response.data.list[i].isAdmin;
                                    obj.userName = response.data.list[i].userName;
                                    if (response.data.list[i].userName === null || response.data.list[i].userName === '') {
                                        obj.userName = '';
                                    } else {
                                        obj.userName = response.data.list[i].userName.replaceAll(',', '</br>');
                                    }
                                    obj.createTime = response.data.list[i].createTime;
                                    obj.updateTime = response.data.list[i].updateTime;
                                    _self.tableData.push(obj)
                                }
                            }
                            _self.total = response.data.total;
                        } else {
                            _self.$message({
                                message: response.msg,
                                type: 'warning',
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
            resetTemp() {
                this.role = {
                    name: null,
                    description: null,
                    isAdmin: false
                }
            },
            handleCreate() {
                this.resetTemp();
                this.dialogStatus = 'create';
                this.dialogFormVisible = true;
                this.$nextTick(() => {
                    this.$refs['dataForm'].clearValidate()
                })
            },
            createData() {
                let _self = this;
                this.$refs["dataForm"].validate((valid) => {
                    if (valid) {
                        _self.axios.post('/clockwork/web/role/addRole', JSON.stringify(this.role), {
                            headers: {'Content-Type': 'application/json'}
                        })
                            .then(response => {
                                if (response.code === 'OK') {
                                    _self.$message({
                                        message: '添加角色成功',
                                        type: 'success'
                                    });
                                    this.dialogFormVisible = false;
                                    this.getData();
                                } else {
                                    _self.$message({
                                        message: response.msg,
                                        type: 'warning',
                                        duration: 5000
                                    });
                                }
                            })
                            .catch(err => {
                                    console.log(err);
                                }
                            );
                    } else {
                        _self.$message({
                            message: '表单填写不符合规则，提交失败！',
                            type: 'warning',
                            duration: 5000
                        });
                        return false;
                    }
                });
            },
            handleUpdate(row) {
                this.role = Object.assign({}, row); // copy obj
                this.dialogStatus = 'update';
                this.dialogFormVisible = true;
                this.$nextTick(() => {
                    this.$refs['dataForm'].clearValidate()
                })
            },
            updateData() {
                let _self = this;
                this.$refs['dataForm'].validate((valid) => {
                    if (valid) {
                        _self.axios.post('/clockwork/web/role/updateRole', JSON.stringify(_self.role), {
                            headers: {'Content-Type': 'application/json'}
                        })
                            .then(response => {
                                if (response.code === 'OK') {
                                    _self.$message({
                                        message: '修改角色成功',
                                        type: 'success'
                                    });
                                    this.dialogFormVisible = false;
                                    this.getData();
                                } else {
                                    _self.$message({
                                        message: response.msg,
                                        type: 'warning',
                                        duration: 5000
                                    });
                                }
                            })
                            .catch(err => {
                                    console.log(err);
                                }
                            );
                    } else {
                        _self.$message({
                            message: '表单填写不符合规则，提交失败！',
                            type: 'warning',
                            duration: 5000
                        });
                        return false;
                    }
                })
            },
            handleDelete(status, row) {
                let id = row.id;
                let _self = this;
                _self.axios.post('/clockwork/web/role/deleteRole', _self.qs.stringify({id: id}))
                    .then(response => {
                        if (response.code === 'OK') {
                            _self.$message({
                                message: '操作成功',
                                type: 'success'
                            });
                            row.status = status;
                            this.getData();
                        } else {
                            _self.$message({
                                message: response.msg,
                                type: 'warning',
                                duration: 5000
                            });
                        }
                    })
                    .catch(err => {
                            console.log(err);
                        }
                    );
            }
        }
    }
</script>

