<template>
  <div :class="className" :style="{height:height,width:width}"/>
</template>

<script>
    import echarts from 'echarts'
    import resize from './mixins/resize'

    require('echarts/theme/macarons'); // echarts theme

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
                default: '350px'
            },
            autoResize: {
                type: Boolean,
                default: true
            },
            chartData: {
                type: Object,
                required: true
            }
        },
        data() {
            return {
                chart: null
            }
        },
        watch: {
            chartData: {
                deep: true,
                handler(val) {
                    this.setOptions(val)
                }
            }
        },
        mounted() {
            this.$nextTick(() => {
                this.initChart()
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
                this.setOptions(this.chartData)
            },
            setOptions({title, xName, data} = {}) {
                this.chart.setOption({
                    title: {
                        text: title,
                        x:'center',
                        y:'top',
                        textAlign:'left',
                        textStyle: {
                            fontSize: 16,
                            fontWeight: 800
                        },
                    },
                    grid: {
                        top: '14%',
                        left: '2%',
                        right: '2%',
                        bottom: '3%',
                        containLabel: true
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: { // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
                        }
                    },
                    xAxis: [{
                        type: 'category',
                        data: xName,
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
                    series: [{
                        itemStyle: {
                            normal: {
                                lineStyle: {
                                    color: '#FF005A',
                                    width: 2
                                },
                                label: {
                                    show: true, //开启显示
                                    position: 'top', //在上方显示
                                    textStyle: { //数值样式
                                        fontSize: 14
                                    }
                                }
                            }
                        },
                        smooth: true,
                        type: 'bar',
                        data: data,
                        barWidth: '50%',
                        animationDuration: 28,
                        animationEasing: 'cubicInOut'
                    }]
                })
            }
        }
    }
</script>
