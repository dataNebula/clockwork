<template>
    <div class="app-container">
        <div class="filter-container">
            <div class="box_l">
                <div class="inner_contain">
                    <el-row>
                        <el-col :span="8">
                                <el-input class="handle-input" clearable
                                onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                                placeholder="请输入任务组ID" type="number" v-model="id"></el-input>
                        </el-col>
                        <el-col :span="8">
                                <el-input class="handle-input" clearable placeholder="请输入筛选任务组名称"
                            v-model="name"/>
                        </el-col>
                        <el-col :span="8">
                                <el-select class="handle-input" placeholder="请输入任务组状态" v-model="status" clearable>
                                    <el-option label="空" value="none"></el-option>
                                    <el-option label="开始" value="running"></el-option>
                                    <el-option label="成功" value="success"></el-option>
                                    <el-option label="失败" value="failed"></el-option>
                                </el-select>
                        </el-col>
                    </el-row>
                    <div v-if="showMore">
                        <el-row>
                            <el-col :span="8">
                                    <el-select class="handle-input" placeholder="上线/下线" v-model="takeEffectStatus" clearable>
                                        <el-option label="上线" value="enable"></el-option>
                                        <el-option label="下线" value="disable"></el-option>
                                    </el-select>
                            </el-col>
                            <el-col :span="8">
                            </el-col>
                            <el-col :span="8">
                            </el-col>
                        </el-row>
                    </div>
                    <el-button type="success" :class="showMore ? 'upBtn' : 'moreBtn'" :icon="showMore ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="medium" round @click="moreSearch">{{showMore ? '收起' :'更多'}}</el-button>
                </div>
            </div>
            <div class="box_r">
                <el-button @click="handleCreate" class="fr" icon="el-icon-circle-plus-outline" size="medium" type="primary">添加任务组</el-button>
                <el-button @click="reset" class="fr marR15" icon="el-icon-refresh-left" size="medium" type="info">重置
                </el-button>
                <el-button @click="getData" class="fr" icon="el-icon-search" size="medium" type="primary">搜索
                </el-button>
            </div>
        </div>

        <el-dialog :visible.sync="dialogFormVisible" title="添加任务组">
            <el-form :model="form" :rules="rules" label-width="100px" ref="form">
                <el-form-item label="任务组名称" prop="name">
                    <el-input placeholder="任务组名称仅含有英文字母或数字或下划线，例如：hello_task_group" v-model="form.name"></el-input>
                </el-form-item>
                <el-form-item label="任务组描述" prop="description">
                    <el-input placeholder="请描述任务组功能" v-model="form.description"></el-input>
                </el-form-item>
            </el-form>
            <div class="dialog-footer" slot="footer">
                <el-button @click="dialogFormVisible = false">取 消</el-button>
                <el-button @click="submitCreate('form')" icon="el-icon-document-add" type="primary">提 交</el-button>
            </div>
        </el-dialog>

        <el-dialog
            :visible.sync="visibleEdit"
            title="修改任务组">
            <el-form :model="formEdit" :rules="rulesEdit" label-width="100px" ref="formEdit">
                <el-form-item label="任务组名称" prop="editName">
                    <el-input v-model="formEdit.editName"></el-input>
                </el-form-item>
                <el-form-item label="任务组描述" prop="editDescription">
                    <el-input v-model="formEdit.editDescription"></el-input>
                </el-form-item>
            </el-form>
            <span class="dialog-footer" slot="footer">
                <el-button @click="visibleEdit = false">取 消</el-button>
                <el-button @click="submitEdit('formEdit')" type="primary">确 定</el-button>
            </span>
        </el-dialog>

        <el-table :data="tableData" append border ref="singleTable" style="width: 100%" v-loading="loading">
            <el-table-column label="ID" prop="id" sortable width="80"></el-table-column>
            <el-table-column label="名称" prop="name" width="200"></el-table-column>
            <el-table-column label="描述" prop="description" width="200"></el-table-column>
            <el-table-column :formatter="formatStatus" label="状态" prop="status"></el-table-column>
            <el-table-column :formatter="takeEffectStatusFn" label="上下线" prop="takeEffectStatus"
                             width="80"></el-table-column>
            <el-table-column label="上次开始时间" prop="lastStartTime" width="180"></el-table-column>
            <el-table-column label="上次结束时间" prop="lastEndTime" width="180"></el-table-column>
            <el-table-column label="所属部门" prop="userGroupName" width="400"></el-table-column>
            <el-table-column label="创建者" prop="userName" width="250"></el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>
            <el-table-column fixed="left" label="操作" width="120">
                <template slot-scope="scope">
                    <div class="operate-block">
                        <el-tooltip class="item" content="查看任务" effect="dark" placement="top">
                            <em><i @click="getTasks(scope.$index, scope.row)" class="el-icon-view"></i></em>
                        </el-tooltip>
                        <el-tooltip class="item" content="修改任务" effect="dark" placement="top">
                            <em><i @click="handleEdit(scope.$index, scope.row)" class="el-icon-edit"></i></em>
                        </el-tooltip>

                        <el-popover placement="right" trigger="click"
                                    v-if="loginName === scope.row.userName || isAdmin"
                                    width="50">
                            <ul class="operate-ul">
                                <li v-if="scope.row.takeEffectStatus === 'disable'">
                                    <el-popconfirm @onConfirm="handleStatus(scope.row)" title="你确定要下线该任务组吗？">
                                        <i class="el-icon-edit" slot="reference">使上线</i>
                                    </el-popconfirm>
                                </li>

                                <li v-if="scope.row.takeEffectStatus === 'enable'">
                                    <el-popconfirm @onConfirm="handleStatus(scope.row)" title="你确定要下线该任务组吗？">
                                        <i class="el-icon-edit" slot="reference">使下线</i>
                                    </el-popconfirm>
                                </li>

                                <li>
                                    <el-popconfirm @onConfirm="handleDelete(scope.row)" icon="el-icon-info"
                                                   iconColor="red"
                                                   title="你确定要删除该任务组吗？删除任务组会删除下面所有任务！">
                                        <i class="el-icon-delete-solid" slot="reference">删除</i>
                                    </el-popconfirm>
                                </li>
                                <li>
                                    <el-popconfirm @onConfirm="rerunTaskByGroupId(scope.row.id)" icon="el-icon-info"
                                                   icon-color="red"
                                                   title="你确定要触发该组下所有任务吗？">
                                        <i class="el-icon-thumb" slot="reference">触发该组所有任务</i>
                                    </el-popconfirm>
                                </li>
                            </ul>
                            <em slot="reference"><i class="el-icon-more"></i></em>
                        </el-popover>
                    </div>
                </template>
            </el-table-column>
        </el-table>
        <div class="pagination">
            <el-pagination :current-page="currentPage" :page-size="pageSize" :page-sizes="[10, 20, 40, 100]"
                           :total="total" @current-change="handleCurrentChange"
                           @size-change="handleSizeChange" layout="total, sizes, prev, pager, next, jumper">
            </el-pagination>
        </div>

    </div>

</template>


<script>
    export default {
        data() {
            let nameRule1 = (rule, value, callback) => {
                let regExp = /^[_a-zA-Z0-9]+$/;
                if (regExp.test(value) === false) {
                    callback(new Error('任务组名称仅含有英文字母或数字或下划线'));
                } else {
                    callback();
                }
            };
            let nameRule2 = (rule, value, callback) => {
                let _self = this;
                try {
                    _self.axios.post('/clockwork/web/task/group/checkTaskGroupName', _self.qs.stringify({taskGroupName: value}))
                        .then(response => {
                            if (response.code === 'OK') {
                                if (response.data > 0) {
                                    callback(new Error('任务组名称已存在，请输入新的'));
                                } else {
                                    callback();
                                }
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
                } catch (err) {
                    _self.$message({
                        message: '系统错误',
                        type: 'warning',
                        duration: 5000
                    });
                    console.log(err)
                }
            };
            return {
                form: {
                    name: '',
                    description: '',
                },
                rules: {
                    name: [
                        {required: true, message: '任务组名称不能为空', trigger: 'blur'},
                        {validator: nameRule1, trigger: 'blur'},
                        {validator: nameRule2, trigger: 'blur'}
                    ],
                    description: [
                        {required: true, message: '请输入任务组的功能描述', trigger: 'blur'}
                    ]
                },
                formEdit: {
                    editName: '',
                    editDescription: ''
                },
                rulesEdit: {
                    editName: [
                        {required: true, message: '请输入任务组名称', trigger: 'blur'},
                    ],
                    editDescription: [
                        {required: true, message: '请输入任务组描述', trigger: 'blur'},
                    ]
                },
                dialogFormVisible: false,
                loading: false,
                tableData: [],
                currentPage: 1,
                total: 0,
                pageSize: 10,
                name: null,
                id: null,
                status: null,
                takeEffectStatus: null,
                isAdmin: null,
                loginName: null,
                is_search: false,
                visibleEdit: false,
                editId: null,
                showMore:false,
            }
        },
        created() {
            this.getData();
        },
        methods: {
            formatStatus(row, column, cellValue) {
                if (row.status === "running") {
                    return "开始"
                }
                if (row.status === "success") {
                    return "成功"
                }
                if (row.status === "failed") {
                    return "失败"
                }
            },
            takeEffectStatusFn(row, column, cellValue) {
                if (row.takeEffectStatus === "enable") {
                    return "上线"
                }
                if (row.takeEffectStatus === "disable") {
                    return "下线"
                }
            },
            handleDelete(row) {
                this.loading = true;
                this.axios.post('/clockwork/web/task/group/delete', this.qs.stringify({
                    taskGroupId: row.id
                }))
                    .then(response => {
                        if (response.code === 'OK') {
                            this.$message({
                                message: '删除成功',
                                type: 'success'
                            });
                            this.getData();
                        } else {
                            this.$message({
                                message: response.msg,
                                type: 'warning',
                                duration: 5000
                            });
                        }
                    })
                    .catch(err => {
                        console.log(err);
                    });
                console.log(row.id)
            },
            handleSizeChange(pageSize) {
                this.pageSize = pageSize;
                console.log(`每页 ${pageSize} 条`);
                this.getData()
            },
            handleCurrentChange(currentPage) {
                this.currentPage = currentPage;
                console.log(`当前页: ${currentPage}`);
                this.getData()
            },
            reset() {
                this.name = null;
                this.id = null;
                this.status = null;
                this.takeEffectStatus = null;
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
                if (_self.name === '') {
                    _self.name = null
                }
                pageParam.condition = JSON.stringify({
                    name: this.name=='' ? null :this.name,
                    id: this.id == '' ? null : this.id,
                    status: this.status=='' ? null : this.status,
                    takeEffectStatus: this.takeEffectStatus == '' ? null :this.takeEffectStatus
                });
                _self.tableData = [];
                _self.axios.post('/clockwork/web/task/group/getAllTaskGroupByUserName', JSON.stringify(pageParam), {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => {
                        if (response.code === 'OK') {
                            for (let i = 0; i < response.data.list.length; i++) {
                                let obj = {};
                                obj.id = response.data.list[i].id;
                                obj.name = response.data.list[i].name;
                                obj.description = response.data.list[i].description;
                                obj.status = response.data.list[i].status;
                                obj.userGroupName = response.data.list[i].userGroupName;
                                obj.lastStartTime = response.data.list[i].lastStartTime;
                                obj.lastEndTime = response.data.list[i].lastEndTime;
                                obj.createTime = response.data.list[i].createTime;
                                obj.takeEffectStatus = response.data.list[i].takeEffectStatus;
                                obj.userName = response.data.list[i].userName;
                                _self.tableData.push(obj)
                            }
                            _self.total = response.data.total;
                            this.loading = false;
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
            search() {
                this.is_search = true;
            },
            getTasks(index, row) {
                this.$router.push({
                    name: 'taskList',
                    params: {
                        taskGroupId: row.id
                    }
                });
            },
            handleEdit(index, row) {
                this.visibleEdit = true;
                this.formEdit.editName = row.name;
                this.formEdit.editDescription = row.description;
                this.editId = row.id;
            },
            submitEdit(formEdit) {
                let taskGroup = {};
                taskGroup.id = this.editId;
                taskGroup.name = this.formEdit.editName;
                taskGroup.description = this.formEdit.editDescription;
                let _self = this;
                this.$refs[formEdit].validate((valid) => {
                    if (valid) {
                        this.axios.post('/clockwork/web/task/group/updateTaskGroup', JSON.stringify(taskGroup), {headers: {'Content-Type': 'application/json'}}).then(res => {
                            if(res.code=="OK"){
                                this.$message({
                                    message: '修改成功',
                                    type: 'success'
                                });
                                this.visibleEdit = false;
                            }else {
                                this.visibleEdit = true;
                                return false;
                            }
                        }).catch(err => {
                            
                        })
                    } 
                })

            },
            handleStatus(row) {
                if (row.takeEffectStatus === "disable") { //使生效
                    this.axios.post('/clockwork/web/task/group/enableTaskGroup', this.qs.stringify({
                        taskGroupId: row.id
                    }))
                        .then(response => {
                            if (response.code === 'OK') {
                                this.$message({
                                    message: '操作成功',
                                    type: 'success'
                                });
                                this.getData();
                            } else {
                                this.$message({
                                    message: response.msg,
                                    type: 'warning',
                                    duration: 5000
                                });
                            }
                        })
                        .catch(err => {
                            console.log(err);
                        })

                } else if (row.takeEffectStatus === "enable") { //使失效
                    this.axios.post('/clockwork/web/task/group/disableTaskGroup', this.qs.stringify({
                        taskGroupId: row.id
                    }))
                        .then(response => {
                            if (response.code === 'OK') {
                                this.$message({
                                    message: '操作成功',
                                    type: 'success'
                                });
                                this.getData();
                            } else {
                                this.$message({
                                    message: response.msg,
                                    type: 'warning',
                                    duration: 5000
                                });
                            }
                        })
                        .catch(err => {
                            console.log(err);
                        })
                } else {

                }

            },
            resetTemp() {
                this.form = {
                    name: '',
                    description: ''
                }
            },
            handleCreate() {
                this.resetTemp();
                this.dialogFormVisible = true;
                // this.$nextTick(() => {
                //     this.$refs['dataForm'].clearValidate()
                // })
            },
            submitCreate(form) {
                let taskGroup = {};
                taskGroup.name = this.form.name;
                taskGroup.description = this.form.description;
                taskGroup.userName = this.userName;
                let _self = this;
                this.$refs[form].validate((valid) => {
                    if (valid) {
                        _self.axios.post('/clockwork/web/task/group/addTaskGroup', JSON.stringify(taskGroup), {headers: {'Content-Type': 'application/json'}})
                            .then(response => {
                                if (response.code === 'OK') {
                                    _self.$message({
                                        message: '添加任务组成功',
                                        type: 'success'
                                    });
                                    this.dialogFormVisible = false
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
            rerunTaskByGroupId(groupId) {
                let _self = this;
                _self.axios.post('/clockwork/web/task/operation/rerunTaskByGroupId', _self.qs.stringify({
                    groupId: groupId,
                    operatorName: _self.loginName
                }))
                    .then(response => {
                        if (response.code === 'OK') {
                            _self.$message({
                                message: '提交成功，等待执行',
                                type: 'success'
                            });
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
                    });

            },
            moreSearch(){
                this.showMore=!this.showMore;
            }
        }
    }
</script>

<style scoped>

</style>
