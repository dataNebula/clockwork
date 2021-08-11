<template>
    <div :class="className" :style="{height:height,width:width}"/>
</template>

<script>
    import echarts from 'echarts'
    import resize from './mixins/resize'

    require('echarts/theme/green'); // echarts theme

    export default {
        mixins: [resize],
        props: {
            className: {
                type: String,
                default: 'chart'
            },
            width: {
                type: String,
                default: '100%'
            },
            height: {
                type: String,
                default: '300px'
            }
        },
        data() {
            return {
                pieCharData: {
                    lData: [],
                    sData: [],
                },
                chart: null
            }
        },
        beforeDestroy() {
            if (!this.chart) {
                return
            }
            this.chart.dispose();
            this.chart = null
        },
        methods: {
            initChart() {
                this.chart = echarts.init(this.$el, 'macarons');
                this.setOptions(this.pieCharData)
            },
            setOptions({lData, sData} = {}) {
                this.chart.setOption({
                    title: {
                        show: true,
                        text: '[状态图]',
                        x: 'left',
                        y: 'top',
                        textAlign: 'left',
                        textStyle: {
                            fontSize: 14,
                            fontWeight: 700
                        },
                    },
                    tooltip: {
                        trigger: 'item',
                        formatter: '{a} <br/>{b} : {c} ({d}%)'
                    },
                    legend: {
                        left: 'center',
                        bottom: '10',
                        data: lData,
                        selected: {'成功': false}
                    },
                    series: [
                        {
                            name: '任务状态',
                            type: 'pie',
                            roseType: 'radius',
                            radius: [15, 95],
                            center: ['50%', '38%'],
                            data: sData,
                            animationEasing: 'cubicInOut',
                            animationDuration: 2600
                        }
                    ]
                })
            },
            taskTodayRunStatus() {
                let _self = this;
                let userName = this.$store.state.user.name;
                if (null !== userName) {
                    _self.axios.get('/clockwork/web/dashboard/taskTodayRunStatus', {params: {userName: userName}})
                        .then(response => {
                            if (response.code === 'OK') {
                                const lData = [];
                                const sData = [];
                                if (response.data.success) {
                                    lData.push('成功');
                                    sData.push({value: response.data.success, name: '成功'})
                                }
                                if (response.data.failed) {
                                    lData.push('失败');
                                    sData.push({value: response.data.failed, name: '失败'})
                                }
                                if (response.data.running) {
                                    lData.push('运行中');
                                    sData.push({value: response.data.running, name: '运行中'})
                                }
                                if (response.data.enable) {
                                    lData.push('准备调度');
                                    sData.push({value: response.data.enable, name: '准备调度'})
                                }
                                if (response.data.rerunSchedulePrep) {
                                    lData.push('重启调度准备');
                                    sData.push({value: response.data.rerunSchedulePrep, name: '重启调度准备'})
                                }
                                if (response.data.submit) {
                                    lData.push('等待运行');
                                    sData.push({value: response.data.submit, name: '等待运行'})
                                }
                                if (response.data.masterHasReceived) {
                                    lData.push('入队待调度');
                                    sData.push({value: response.data.masterHasReceived, name: '入队待调度'})
                                }
                                if (response.data.workerHasReceived) {
                                    lData.push('入队待执行');
                                    sData.push({value: response.data.workerHasReceived, name: '入队待执行'})
                                }
                                if (response.data.exception) {
                                    lData.push('异常结束');
                                    sData.push({value: response.data.exception, name: '异常结束'})
                                }
                                if (response.data.killing) {
                                    lData.push('停止中');
                                    sData.push({value: response.data.killing, name: '停止中'})
                                }
                                if (response.data.killed) {
                                    lData.push('已杀死');
                                    sData.push({value: response.data.killed, name: '已杀死'})
                                }
                                if (response.data.runTimeoutKilling) {
                                    lData.push('运行超时停止中');
                                    sData.push({value: response.data.runTimeoutKilling, name: '运行超时停止中'})
                                }
                                if (response.data.runTimeoutKilled) {
                                    lData.push('运行超时已杀死');
                                    sData.push({value: response.data.runTimeoutKilled, name: '运行超时已杀死'})
                                }
                                if (response.data.fatherNotSuccess) {
                                    lData.push('父任务不成功');
                                    sData.push({value: response.data.fatherNotSuccess, name: '父任务不成功'})
                                }
                                if (response.data.lifeCycleReset) {
                                    lData.push('周期重置');
                                    sData.push({value: response.data.lifeCycleReset, name: '周期重置'})
                                }
                                _self.pieCharData.lData = lData;
                                _self.pieCharData.sData = sData;
                                this.initChart();
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
        }
    }
</script>
