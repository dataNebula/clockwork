<template>
    <el-row :gutter="40" class="panel-group">
        <el-col :lg="6" :sm="12" :xs="12" class="card-panel-col">
            <div @click="handleSetLineChartData('taskOperation')" class="card-panel">
                <div class="card-panel-icon-wrapper icon-people">
                    <svg-icon class-name="card-panel-icon" icon-class="task-total"/>
                </div>
                <div class="card-panel-description">
                    <div class="card-panel-text">
                        总任务数
                    </div>
                    <template>
                        <span style="font-size: 20px">{{total}}</span>
                    </template>
                </div>
            </div>
        </el-col>
        <el-col :lg="6" :sm="12" :xs="12" class="card-panel-col">
            <div @click="handleSetLineChartData('taskHourSuccess')" class="card-panel">
                <div class="card-panel-icon-wrapper icon-message">
                    <svg-icon class-name="card-panel-icon" icon-class="task-success"/>
                </div>
                <div class="card-panel-description">
                    <div class="card-panel-text">
                        成功任务
                    </div>
                    <template>
                        <span style="font-size: 20px">{{success}}</span>
                    </template>
                </div>
            </div>
        </el-col>
        <el-col :lg="6" :sm="12" :xs="12" class="card-panel-col">
            <div @click="handleSetLineChartData('taskHourFailed')" class="card-panel">
                <div class="card-panel-icon-wrapper icon-money">
                    <svg-icon class-name="card-panel-icon" icon-class="task-failed"/>
                </div>
                <div class="card-panel-description">
                    <div class="card-panel-text">
                        失败任务
                    </div>
                    <template>
                        <span style="font-size: 20px">{{failed}}</span>
                    </template>
                </div>
            </div>
        </el-col>
        <el-col :lg="6" :sm="12" :xs="12" class="card-panel-col">
            <div @click="handleSetLineChartData('taskNodeRunCount')" class="card-panel">
                <div class="card-panel-icon-wrapper icon-shopping">
                    <svg-icon class-name="card-panel-icon" icon-class="task-run"/>
                </div>
                <div class="card-panel-description">
                    <div class="card-panel-text">
                        运行中任务
                    </div>
                    <template>
                        <span style="font-size: 20px">{{running}}</span>
                    </template>
                </div>
            </div>
        </el-col>

    </el-row>
</template>

<script>
    import countTo from 'vue-count-to'

    const lineChartData = {
        taskOperation: {
            title: '今日任务运行情况',
            xName: [],
            data: [],
        },
        taskHourSuccess: {
            title: '每小时成功任务',
            xName: [],
            data: []
        },
        taskHourFailed: {
            title: '每小时失败任务',
            xName: [],
            data: []
        },
        taskNodeRunCount: {
            title: '各节点运行中任务',
            xName: [],
            data: []
        }
    };

    export default {
        components: {
            countTo
        },
        data() {
            return {
                total: 0,
                success: 0,
                failed: 0,
                running: 0
            }
        },
        methods: {
            handleSetLineChartData(type) {
                this.lineChartData = lineChartData[type];
                this.$emit('handleSetLineChartData', this.lineChartData)
            },
            taskTodayRunStatus() {
                let _self = this;
                let userName = this.$store.state.user.name;
                if (null !== userName) {
                    _self.axios.get('/clockwork/web/dashboard/taskTodayRunStatus', {params: {userName: userName}})
                        .then(response => {
                            if (response.code === 'OK') {

                                // 导航栏数据
                                this.total = response.data.total;
                                this.success = response.data.success;
                                this.failed = response.data.failed;
                                this.running = response.data.running;

                                // 今日任务运行情况图数据
                                let taskStatusCnt = [];
                                taskStatusCnt.push(response.data.total);
                                taskStatusCnt.push(response.data.online);
                                taskStatusCnt.push(response.data.success);
                                taskStatusCnt.push(response.data.failed);
                                taskStatusCnt.push(response.data.exception);
                                taskStatusCnt.push(response.data.submit);
                                taskStatusCnt.push(response.data.masterHasReceived);
                                taskStatusCnt.push(response.data.workerHasReceived);
                                taskStatusCnt.push(response.data.running);
                                taskStatusCnt.push(response.data.offline);

                                lineChartData.taskOperation.xName = ['总任务', '上线任务', '成功任务', '失败任务', '异常任务', '待运行任务', '入队待调度', '入队待执行', '运行中任务', '下线任务'];
                                lineChartData.taskOperation.data = taskStatusCnt;

                                // 默认加载
                                this.$emit('handleSetLineChartData', lineChartData.taskOperation);

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
            },
            getTaskHourSuccess() {
                let _self = this;
                let userName = this.$store.state.user.name;
                if (null !== userName) {
                    _self.axios.get('/clockwork/web/dashboard/getTaskHourSuccess', {params: {userName: userName}})
                        .then(response => {
                            if (response.code === 'OK') {
                                lineChartData.taskHourSuccess.xName = [];
                                lineChartData.taskHourSuccess.data = [];
                                for (let i = 0; i < response.data.length; i++) {
                                    lineChartData.taskHourSuccess.xName.push(response.data[i].hours + '点');
                                    lineChartData.taskHourSuccess.data.push(response.data[i].cnt);
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
                }
            },
            getTaskHourFailed() {
                let _self = this;
                let userName = this.$store.state.user.name;
                if (null !== userName) {
                    _self.axios.get('/clockwork/web/dashboard/getTaskHourFailed', {params: {userName: userName}})
                        .then(response => {
                            if (response.code === 'OK') {
                                lineChartData.taskHourFailed.xName = [];
                                lineChartData.taskHourFailed.data = [];
                                for (let i = 0; i < response.data.length; i++) {
                                    lineChartData.taskHourFailed.xName.push(response.data[i].hours + '点');
                                    lineChartData.taskHourFailed.data.push(response.data[i].cnt);
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
                }
            },
            getTaskNodeRunCount() {
                let _self = this;
                let userName = this.$store.state.user.name;
                if (null !== userName) {
                    _self.axios.get('/clockwork/web/dashboard/getTaskNodeRunCount', {params: {userName: userName}})
                        .then(response => {
                            if (response.code === 'OK') {
                                lineChartData.taskNodeRunCount.xName = [];
                                lineChartData.taskNodeRunCount.data = [];
                                for (let i = 0; i < response.data.length; i++) {
                                    lineChartData.taskNodeRunCount.xName.push(response.data[i].domainName);
                                    lineChartData.taskNodeRunCount.data.push(response.data[i].cnt);
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
                }
            },
        },
        mounted() {
            this.taskTodayRunStatus();
            this.getTaskHourSuccess();
            this.getTaskHourFailed();
            this.getTaskNodeRunCount();
        }
    }
</script>

<style lang="scss" scoped>
    .panel-group {
        margin-top: 18px;

        .card-panel-col {
            margin-bottom: 32px;
        }

        .card-panel {
            height: 108px;
            cursor: pointer;
            font-size: 12px;
            position: relative;
            overflow: hidden;
            color: #666;
            background: #fff;
            box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
            border-color: rgba(0, 0, 0, .05);

            &:hover {
                .card-panel-icon-wrapper {
                    color: #fff;
                }

                .icon-people {
                    background: #36a3f7;
                }

                .icon-message {
                    background: #32CD32;
                }

                .icon-money {
                    background: #f4516c;
                }

                .icon-shopping {
                    background: #40c9c6
                }
            }

            .icon-people {
                color: #36a3f7;
            }

            .icon-message {
                color: #32CD32;
            }

            .icon-money {
                color: #f4516c;
            }

            .icon-shopping {
                color: #40c9c6
            }

            .card-panel-icon-wrapper {
                float: left;
                margin: 14px 0 0 14px;
                padding: 16px;
                transition: all 0.38s ease-out;
                border-radius: 6px;
            }

            .card-panel-icon {
                float: left;
                font-size: 48px;
            }

            .card-panel-description {
                float: right;
                font-weight: bold;
                margin: 26px 42px 26px 0;

                .card-panel-text {
                    line-height: 18px;
                    color: rgba(0, 0, 0, 0.45);
                    font-size: 16px;
                    margin-bottom: 12px;
                }

                .card-panel-num {
                    font-size: 20px;
                }
            }
        }
    }

    @media (max-width: 550px) {
        .card-panel-description {
            display: none;
        }

        .card-panel-icon-wrapper {
            float: none !important;
            width: 100%;
            height: 100%;
            margin: 0 !important;

            .svg-icon {
                display: block;
                margin: 14px auto !important;
                float: none !important;
            }
        }
    }
</style>
