<template>
    <div class="app-container">
        <div class="filter-container" v-show="showFilterContainer">
            <div class="box_l">
                <div class="inner_contain">
                    <el-row>
                        <el-col :span="8">
                            <el-date-picker
                                @change="changeNowTime()"
                                class="handle-input"
                                clearable
                                placeholder="开始时间"
                                size="medium"
                                type="date"
                                v-model="nowTime"
                                value-format="yyyy-MM-dd">
                            </el-date-picker>
                        </el-col>
                        <el-col :span="8">
                            <el-input class="handle-input" clearable
                                      onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                                      placeholder="任务Id" size="medium" type="number" v-model="taskId"></el-input>
                        </el-col>
                        <el-col :span="8">
                            <el-input class="handle-input" clearable placeholder="任务名" size="medium"
                                      v-model="taskName"/>
                        </el-col>
                    </el-row>
                    <div v-if="showMore">
                        <el-row>
                            <el-col :span="8">
                                <el-input class="handle-input" clearable
                                          onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                                          placeholder="节点Id" size="medium" type="number" v-model="nodeId"></el-input>
                            </el-col>
                            <el-col :span="8">
                                <el-select class="handle-input" clearable placeholder="任务状态"
                                           size="medium" v-model="status">
                                    <el-option :key="item.value" :label="item.label" :value="item.value"
                                               v-for="item in taskStatusOptions"/>
                                </el-select>
                            </el-col>
                            <el-col :span="8">
                                <el-select class="handle-input" clearable placeholder="执行类型" size="medium"
                                           v-model="executeType">
                                    <el-option :key="item.value" :label="item.label" :value="item.value"
                                               v-for="item in executeTypeOptions"/>
                                </el-select>
                            </el-col>
                        </el-row>
                        <el-row>
                            <el-col :span="8">
                                <el-select class="handle-input" clearable placeholder="是否结束" size="medium"
                                           v-model="isEnd">
                                    <el-option :key="item.value" :label="item.label" :value="item.value"
                                               v-for="item in isEndOptions"/>
                                </el-select>
                            </el-col>
                            <el-col :span="8">
                                <el-input class="handle-input" clearable
                                          onKeypress="return(/[\d]/.test(String.fromCharCode(event.keyCode)))"
                                          placeholder="日志Id" size="medium" type="number" v-model="id"></el-input>
                            </el-col>
                            <el-col :span="8">
                                <el-select class="handle-input" clearable placeholder="来源" size="medium"
                                           v-model="source">
                                    <el-option :key="item.value" :label="item.label" :value="item.value"
                                               v-for="item in sourceFilterOptions"/>
                                </el-select>
                            </el-col>
                        </el-row>
                        <el-row>
                            <el-col :span="8">
                                <el-input class="handle-input" clearable
                                          placeholder="创建人" size="medium" v-model="createUser"></el-input>
                            </el-col>
                            <el-col :span="16"></el-col>
                        </el-row>
                    </div>
                    <el-button :class="showMore ? 'upBtn' : 'moreBtn'" :icon="showMore ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
                               @click="moreSearch" round size="medium"
                               type="success">{{showMore ? '收起' :'更多'}}
                    </el-button>
                </div>
            </div>
            <div class="box_r">
                <el-button @click="reset" class="fr" icon="el-icon-refresh-left" size="medium" type="info">重置
                </el-button>
                <el-button @click="getData" class="fr marR15" icon="el-icon-search" size="medium" type="primary">搜索
                </el-button>
            </div>
        </div>
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
            <el-table-column label="进程Id" prop="pid" width="0"></el-table-column>
            <el-table-column label="状态" prop="status" width="140">
                <template slot-scope="scorp">
                    <el-tag :type="scorp.row.status | statusStyleFilter">
                        {{ scorp.row.status | statusFilter }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="是否结束" prop="isEnd" width="95">
                <template slot-scope="scorp">
                    <el-tag :type="scorp.row.isEnd?'success':'primary'">
                        {{ scorp.row.isEnd | isNotFilter }}
                    </el-tag>
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
            <el-table-column label="日志名" prop="logName" width="300"></el-table-column>
            <el-table-column label="执行引擎" prop="runEngine" width="95"></el-table-column>
            <el-table-column label="返回码" prop="returnCode" width="80"></el-table-column>
            <el-table-column label="来源" prop="source" width="100">
                <template slot-scope="scorp">
                    {{ scorp.row.source | sourceFilter }}
                </template>
            </el-table-column>
            <el-table-column label="节点ID" prop="nodeId" width="80"></el-table-column>
            <el-table-column label="节点IP" prop="nodeIp" width="160"></el-table-column>
            <el-table-column label="节点Port" prop="nodePort" width="80"></el-table-column>
            <el-table-column label="批次号" prop="rerunBatchNumber" width="250"></el-table-column>
            <el-table-column label="创建人" prop="createUser" width="160"></el-table-column>
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
                showFilterContainer: true,  // 是否显示查询条件
                isEndOptions: this.GLOBAL.isEndOptions,
                executeTypeOptions: this.GLOBAL.executeTypeOptions,
                taskStatusOptions: this.GLOBAL.taskStatusOptions,
                sourceFilterOptions: this.GLOBAL.sourceFilterOptions,
                executeType: 1,
                isEnd: null,
                nowTime: 0,
                startTime: null,
                endTime: null,
                source: null,
                createUser: null,
                showMore: false,
            }
        },
        created() {
            this.$route.meta.activeMenu = this.$route.params.activeMenu;

            //开始时间 结束时间 默认值
            this.nowTime = new Date();
            const y = this.nowTime.getFullYear();
            const m = this.nowTime.getMonth() + 1;
            const d = this.nowTime.getDate();
            const ymd = y + '-' + m + '-' + d;
            this.startTime = ymd + ' ' + "00:00:00";
            this.endTime = ymd + ' ' + "23:59:59";
            this.getData();

        },
        methods: {
            changeNowTime() {
                this.nowTime == null ? this.startTime = null : this.startTime = this.nowTime + ' ' + "00:00:00";
                this.nowTime == null ? this.endTime = null : this.endTime = this.nowTime + ' ' + "23:59:59";
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
                this.id = null;
                this.taskId = null;
                this.taskName = null;
                this.status = null;
                this.nodeId = null;
                this.isEnd = null;
                this.source = null;
                this.createUser = '';
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

                if (typeof (this.$route.params.showFilterContainer) !== "undefined") {
                    _self.showFilterContainer = this.$route.params.showFilterContainer;
                }
                if (typeof (this.$route.params.taskId) !== "undefined") {
                    _self.taskId = this.$route.params.taskId;
                }

                this.id == "" ? taskLog.id = null : taskLog.id = this.id;
                this.taskId == "" ? taskLog.taskId = null : taskLog.taskId = this.taskId;
                this.taskName == '' ? taskLog.taskName = null : taskLog.taskName = this.taskName;
                this.status == '' ? taskLog.status = null : taskLog.status = this.status;
                this.nodeId == "" ? taskLog.nodeId = null : taskLog.nodeId = this.nodeId;
                this.executeType == "" ? taskLog.executeType = null : taskLog.executeType = this.executeType;
                taskLog.isEnd = this.isEnd;
                this.startTime == '' ? taskLog.startTime = null : taskLog.startTime = this.startTime;
                this.endTime == '' ? taskLog.endTime = null : taskLog.endTime = this.endTime;
                this.source === '' ? taskLog.source = null : taskLog.source = this.source;
                this.createUser == '' ? taskLog.createUser = null : taskLog.createUser = this.createUser;
                pageParam.condition = JSON.stringify(taskLog);

                this.loading = true;
                _self.tableData = [];
                _self.axios.post('/clockwork/web/task/log/searchPageTaskLogList', JSON.stringify(pageParam), {headers: {'Content-Type': 'application/json'}})
                    .then(response => {
                        if (response.code === 'OK') {
                            for (let i = 0; i < response.data.list.length; i++) {
                                const obj = {};
                                obj.id = response.data.list[i].id;
                                obj.taskId = response.data.list[i].taskId;
                                obj.taskName = response.data.list[i].taskName;
                                obj.source = response.data.list[i].source;
                                obj.createUser = response.data.list[i].createUser;
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
                            this.loading = false;
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
                        activeMenu: '/log/logList', //设置当前高亮菜单
                    }
                });
            },
            moreSearch() {
                this.showMore = !this.showMore;
            }

        }
    }

</script>

<style scoped>
</style>
