<template>
    <div class="app-container">
        <div class="filter-container">
            <div class="box_l">
                <div class="inner_contain">
                    <el-row>
                        <el-col :span="8">
                            <el-input class="handle-input" clearable placeholder="taskIds" size="medium"
                                      v-model="taskIds"></el-input>
                        </el-col>
                        <el-col :span="8">
                            <el-input class="handle-input" clearable placeholder="批次号" size="medium" type="number"
                                      v-model="rerunBatchNumber"/>
                        </el-col>
                        <el-col :span="8">
                            <el-input class="handle-input" clearable placeholder="补数类型" size="medium"
                                      v-model="fillDataType"/>
                        </el-col>
                    </el-row>
                    <div v-if="showMore">
                        <el-row>
                            <el-col :span="8">
                                <el-input class="handle-input" clearable placeholder="操作人" size="medium"
                                          v-model="operatorName"/>
                            </el-col>
                            <el-col :span="8">
                                <el-date-picker
                                    class="handle-input"
                                    placeholder="开始时间"
                                    size="medium"
                                    type="date"
                                    v-model="nowTime"
                                    @change="changeNowTime()"
                                    value-format="yyyy-MM-dd"
                                    clearable>
                                </el-date-picker>
                            </el-col>
                            <el-col :span="8">
                            </el-col>
                        </el-row>
                    </div>
                    <el-button type="success" :class="showMore ? 'upBtn' : 'moreBtn'"
                               :icon="showMore ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" size="medium" round
                               @click="moreSearch">{{showMore ? '收起' :'更多'}}
                    </el-button>
                </div>
            </div>
            <div class="box_r">
                <el-button @click="reset" class="fr" icon="el-icon-refresh-left" size="medium" type="info">重置
                </el-button>
                <el-button @click="getData" class="marR15 fr" icon="el-icon-search" size="medium" type="primary">搜索
                </el-button>
            </div>
        </div>

        <el-table :data="tableData" append border ref="singleTable" style="width: 100%" v-loading="loading">
            <el-table-column label="ID" prop="id" sortable width="80"></el-table-column>
            <el-table-column label="[总数]：taskIds" prop="taskIds" sortable width="280"></el-table-column>
            <el-table-column label="[总数]：补数时间" prop="fillDataTime" width="180">
                <template slot-scope="scope">
                    <div v-html="scope.row.fillDataTime"></div>
                </template>
            </el-table-column>
            <el-table-column label="[序号]：当前补数时间" prop="currFillDataTime" width="200"></el-table-column>
            <el-table-column label="补数类型" prop="fillDataType" width="90"></el-table-column>
            <el-table-column label="任务成功数" prop="taskCountSuccess" width="100"></el-table-column>
            <el-table-column label="补数状态" prop="status" width="140">
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
            <el-table-column label="批次号" prop="rerunBatchNumber" width="180"></el-table-column>
            <el-table-column label="操作人" prop="operatorName" width="220"></el-table-column>
            <el-table-column label="更新时间" prop="updateTime" width="180"></el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>
            <el-table-column fixed="left" label="操作" width="120">
                <template slot-scope="scope">
                    <div class="operate-block">
                        <el-tooltip class="item" content="查看当前补数批次任务" effect="dark" placement="top">
                            <em><i @click="handleFillTaskList(scope.row.rerunBatchNumber)"
                                   class="el-icon-view"></i></em>
                        </el-tooltip>
                        <el-tooltip class="item" content="图" effect="dark" placement="top">
                            <em><i @click="handleFillTaskDagGraph(scope.row)" class="el-icon-orange"></i></em>
                        </el-tooltip>
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
            return {
                dialogFormVisible: false,
                loading: false,
                tableData: [],
                currentPage: 1,
                total: 0,
                pageSize: 10,
                taskIds: null,
                fillDataTime: null,
                fillDataType: null,
                rerunBatchNumber: null,
                operatorName: null,
                isAdmin: null,
                is_search: false,
                loginName: null,
                showMore: false,
                nowTime: 0,
                createTimeStart: null,
                createTimeEnd: null,
            }
        },
        created() {
            this.getData();
        },
        methods: {
            changeNowTime() {
                this.nowTime == null ? this.createTimeStart = null : this.createTimeStart = this.nowTime + ' ' + "00:00:00";
                this.nowTime == null ? this.createTimeEnd = null : this.createTimeEnd = this.nowTime + ' ' + "23:59:59";
            },
            formatIsEnd(row, column, cellValue) {
                return row.isEnd ? "是" : "否"
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
                this.taskIds = null;
                this.fillDataTime = null;
                this.fillDataType = null;
                this.rerunBatchNumber = null;
                this.operatorName = null;
                this.nowTime = null;
                this.getData();
            },
            getData: function () {
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
                    taskIds: this.taskIds == '' ? null : this.taskIds,
                    createTimeStart: this.createTimeStart == '' ? null : this.createTimeStart,
                    createTimeEnd: this.createTimeEnd == '' ? null : this.createTimeEnd,
                    fillDataType: this.fillDataType == '' ? null : this.fillDataType,
                    rerunBatchNumber: this.rerunBatchNumber == '' ? null : this.rerunBatchNumber,
                    operatorName: this.operatorName == '' ? null : this.operatorName
                });
                _self.tableData = [];
                _self.axios.post('/clockwork/web/task/fillData/searchFillDataPageList', JSON.stringify(pageParam), {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => {
                        if (response.code === 'OK') {
                            for (let i = 0; i < response.data.list.length; i++) {
                                let obj = {};
                                obj.id = response.data.list[i].id;
                                obj.taskIds = '[' + response.data.list[i].taskCount + ']：' + response.data.list[i].taskIds;
                                if (response.data.list[i].fillDataTime === null || response.data.list[i].fillDataTime === '') {
                                    obj.fillDataTime = '[0]';
                                } else {
                                    let replace = response.data.list[i].fillDataTime;
                                    obj.fillDataTime = '[' + response.data.list[i].fillDataTimeCount + ']：</br>' + replace.toString().replaceAll(',', '</br>');
                                }
                                obj.currFillDataTime = '[' + response.data.list[i].currFillDataTimeSort + ']：' + response.data.list[i].currFillDataTime;
                                obj.fillDataType = response.data.list[i].fillDataType;
                                obj.taskCountSuccess = response.data.list[i].taskCountSuccess;
                                obj.status = response.data.list[i].status;
                                obj.startTime = response.data.list[i].startTime;
                                obj.executeTime = response.data.list[i].executeTime;
                                obj.endTime = response.data.list[i].endTime;
                                obj.isEnd = response.data.list[i].isEnd;
                                obj.rerunBatchNumber = response.data.list[i].rerunBatchNumber;
                                obj.operatorName = response.data.list[i].operatorName;
                                obj.updateTime = response.data.list[i].updateTime;
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
            handleFillTaskList(rerunBatchNumber) {
                this.$router.push({
                    name: 'rerunList',
                    params: {
                        showFilterContainer: false,
                        rerunBatchNumber: rerunBatchNumber,
                        executeType: 2,
                    }
                });
            },
            handleFillTaskDagGraph(row) {
                // TODO
            },
            moreSearch() {
                this.showMore = !this.showMore;
            }
        }
    }
</script>
