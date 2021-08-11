<template> 
    <div class="app-container">


        <el-table :data="tableData" append border ref="singleTable" style="width: 100%" v-loading="loading">
            <el-table-column label="操作" width="105">
                <template slot-scope="scope">
                    <el-button @click="handleCatLogFile(scope.row)" size="small">运行日志</el-button>
                </template>
            </el-table-column>

            <el-table-column label="ID" prop="id" sortable width="80"></el-table-column>
            <el-table-column label="任务ID" prop="taskId" width="80"></el-table-column>
            <el-table-column label="任务名称" prop="taskName" width="160"></el-table-column>
            <el-table-column label="组Id" prop="groupId" width="100"></el-table-column>
            <el-table-column label="进程Id" prop="pid" width="160"></el-table-column>
            <el-table-column label="状态" prop="status" width="140">
                <template slot-scope="scorp">
                    <el-tag :type="scorp.row.status | statusStyleFilter">
                        {{ scorp.row.status | statusFilter }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="是否结束" prop="isEnd" width="95">
                <template slot-scope="scorp">
                    {{ scorp.row.isEnd | isNotFilter }}
                </template>
            </el-table-column>
            <el-table-column label="开始时间" prop="startTime" width="180"></el-table-column>
            <el-table-column label="执行时间" prop="executeTime" width="180"></el-table-column>
            <el-table-column label="结束时间" prop="endTime" width="180"></el-table-column>
            <el-table-column label="执行类型" prop="executeType" width="95">
                <template slot-scope="scorp">
                    {{ scorp.row.executeType | executeTypeFilter }}
                </template>
            </el-table-column>
            <el-table-column label="执行引擎" prop="runEngine" width="95"></el-table-column>
            <el-table-column label="返回码" prop="returnCode" width="80"></el-table-column>
            <el-table-column label="日志名" prop="logName" width="300"></el-table-column>
            <el-table-column label="批次号" prop="rerunBatchNumber" width="250"></el-table-column>
            <el-table-column label="节点ID" prop="nodeId" width="80"></el-table-column>
            <el-table-column label="节点IP" prop="nodeIp" width="160"></el-table-column>
            <el-table-column label="节点Port" prop="nodePort" width="80"></el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>
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
            return {
                loading: false,
                tableData: [],
                currentPage: 1,
                total: 0,
                pageSize: 10,
                id: null,
                nodeId: null,
                taskId: null,
                taskName: null,
                status: null,
                isAdmin: false,
                is_search: false,
                loginName: this.$store.state.user.name,
                isEndOptions: this.GLOBAL.isEndOptions,
                executeTypeOptions: this.GLOBAL.executeTypeOptions,
                taskStatusOptions: this.GLOBAL.taskStatusOptions,
                executeType: null,
                isEnd: null,
            }
        },
        created() {
             this.$route.meta.activeMenu = this.$route.params.activeMenu;
            this.getData();
        },
        methods: {
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
                this.id = null;
                this.taskId = null;
                this.taskName = null;
                this.status = null;
                this.nodeId = null;
                this.isEnd = null;
                this.getData();
            },
            getData() {
                let _self = this;
                let pageParam = {};
                pageParam.pageNum = _self.currentPage;
                pageParam.pageSize = _self.pageSize;
                pageParam.userName = _self.loginName;
                pageParam.role = this.$store.state.user.role;
                let taskLog = {};

                

                taskLog.id = _self.id;
                taskLog.taskId = this.$route.params.taskId;
                taskLog.taskName = _self.taskName;
                taskLog.status = _self.status;
                taskLog.nodeId = _self.nodeId;
                taskLog.executeType = _self.executeType;
                taskLog.isEnd = _self.isEnd;
                pageParam.condition = JSON.stringify(taskLog);

                _self.tableData = [];
                _self.axios.post('/clockwork/web/task/log/searchPageTaskLogList', JSON.stringify(pageParam), {headers: {'Content-Type': 'application/json'}})
                    .then(response => {
                        if (response.code === 'OK') {
                            for (let i = 0; i < response.data.list.length; i++) {
                                const obj = {};
                                obj.id = response.data.list[i].id;
                                obj.taskId = response.data.list[i].taskId;
                                obj.taskName = response.data.list[i].taskName;
                                obj.groupId = response.data.list[i].groupId;
                                obj.pid = response.data.list[i].pid;
                                obj.status = response.data.list[i].status;
                                obj.isEnd = response.data.list[i].isEnd;
                                obj.startTime = response.data.list[i].startTime;
                                obj.executeTime = response.data.list[i].executeTime;
                                obj.endTime = response.data.list[i].endTime;
                                obj.executeType = response.data.list[i].executeType;
                                obj.runEngine = response.data.list[i].runEngine;
                                obj.returnCode = response.data.list[i].returnCode;
                                obj.logName = response.data.list[i].logName;
                                obj.rerunBatchNumber = response.data.list[i].rerunBatchNumber;
                                obj.nodeId = response.data.list[i].nodeId;
                                obj.nodeIp = response.data.list[i].nodeIp;
                                obj.nodePort = response.data.list[i].nodePort;
                                obj.createTime = response.data.list[i].createTime;
                                _self.tableData.push(obj)
                            }
                            _self.total = response.data.total;
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
            },
            search() {
                this.is_search = true;
            },
            handleCatLogFile(row) {
                this.$router.push({
                    name: 'logFile',
                    params: {
                        nodeIp: row.nodeIp,
                        nodePort: row.nodePort,
                        logName: row.logName,
                        createTime: row.createTime,
                        activeMenu:'/log/logList', //设置当前高亮菜单
                    }
                });
            }

        }
    }

</script>

<style scoped>
    .handle-input {
        width: 300px;
        display: inline-block;
    }
</style>
