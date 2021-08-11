<template>
    <div :class="className" :style="{height:height,width:width}"/>
</template>

<script>
    import echarts from 'echarts'
    import resize from './mixins/resize'

    require('echarts/theme/macarons'); // echarts theme

    const animationDuration = 6000;

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
                barCharData: {
                    xData: [],
                    series: [],
                },
                chart: null
            }
        },
        mounted() {
            this.$nextTick(() => {
                this.getTaskNodeHourRun();
            })
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
                this.setOptions(this.barCharData)
            },
            setOptions({xData, series} = {}) {
                this.chart.setOption({
                    title: {
                        show: true,
                        text: '[各节点/运行任务]',
                        x:'center',
                        y:'top',
                        textAlign:'left',
                        textStyle: {
                            fontSize: 14,
                            fontWeight: 700
                        },
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: { // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
                        }
                    },
                    grid: {
                        top: '12%',
                        left: '2%',
                        right: '2%',
                        bottom: '3%',
                        containLabel: true
                    },
                    xAxis: [{
                        type: 'category',
                        data: xData,
                        axisTick: {
                            alignWithLabel: true
                        }
                    }],
                    yAxis: [{
                        type: 'value',
                        axisTick: {
                            show: false
                        }
                    }],
                    series: series
                })
            },
            getTaskNodeHourRun() {
                let _self = this;
                let userName = this.$store.state.user.name;
                if (null !== userName) {
                    _self.axios.get('/clockwork/web/dashboard/getTaskNodeHourRun', {params: {userName: userName}})
                        .then(response => {
                            if (response.code === 'OK') {
                                _self.barCharData.xData = response.data.xData;
                                const chartData = response.data.chartData;
                                const series = [];
                                for (let i = 0; i < chartData.length; i++) {
                                    series.push({
                                        name: chartData[i].name,
                                        type: 'bar',
                                        stack: 'vistors',
                                        barWidth: '60%',
                                        data: chartData[i].data,
                                        animationDuration
                                    })
                                }
                                _self.barCharData.series = series;
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
        }
    }
</script>
