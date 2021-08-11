<template>
    <div class="app-container">

        <div class="filter-container">
            <div class="form_item">
                <el-input class="handle-input" clearable placeholder="文件名" v-model="uploadFileAbsolutePath"></el-input>
            </div>
            <el-button @click="reset" class="fr marB15" icon="el-icon-refresh-left" size="medium" type="info">重置
            </el-button>
            <el-button @click="getData" class="marR15 fr" icon="el-icon-search" size="medium" type="primary">搜索
            </el-button>
        </div>

        <el-table :data="tableData" append border ref="singleTable" style="width: 100%" v-loading="loading">
            <el-table-column label="ID" prop="id" sortable width="90"></el-table-column>
            <el-table-column label="文件" prop="uploadFileAbsolutePath" min-width="600"></el-table-column>
            <el-table-column label="状态" prop="status" width="100"></el-table-column>
            <el-table-column label="操作类型" prop="operateType" width="100"></el-table-column>
            <el-table-column label="最后操作人" prop="operatorEmail" width="100"></el-table-column>
            <el-table-column label="更新时间" prop="updateTime" width="180"></el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>

            <el-table-column fixed="left" label="操作" width="80">
                <template slot-scope="scope">
                    <div class="operate-block">

                        <el-tooltip @onConfirm="handleSelect(scope.row)" class="item" content="查看" effect="dark"
                                    placement="top" slot="reference">
                            <em><i class="el-icon-view"></i></em>
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
                layout="total, sizes, prev, pager, next, jumper">
            </el-pagination>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                tableData: [],
                currentPage: 1,
                total: 0,
                pageSize: 10,
                uploadFileAbsolutePath: null,     // 搜索
                loading: false,
                loginName: null,
                isAdmin: null,
                is_search: false,
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
                this.uploadFileAbsolutePath = null;
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
                if (_self.uploadFileAbsolutePath === '') {
                    _self.uploadFileAbsolutePath = null
                }
                pageParam.condition = JSON.stringify({
                    uploadFileAbsolutePath: _self.uploadFileAbsolutePath == '' ? null : this.uploadFileAbsolutePath
                });
                _self.tableData = [];
                _self.axios.post('/clockwork/web/file/searchFilePageList', JSON.stringify(pageParam), {
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
                                    obj.uploadFileAbsolutePath = response.data.list[i].uploadFileAbsolutePath;
                                    obj.status = response.data.list[i].status;
                                    obj.operateType = response.data.list[i].operateType;
                                    obj.operatorEmail = response.data.list[i].operatorEmail;
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
            handleSelect() {
            },
        }
    }
</script>

<style scoped>
    .handle-input {
        width: 300px;
        display: inline-block;
    }
</style>
