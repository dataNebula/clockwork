<template>
    <div>
        <div ref="oDiv" style="width:500px;height:500px;"></div>
        <div class="aa"></div>
    </div>
</template>
<script>
var echarts = require('echarts')
export default {
    data(){
        return{
            option:{
                series: [
                    {
                        type: 'graph',
                        layout: 'none',
                        symbolSize: 50,
                        roam: true,
                        label: {
                            show: true
                        },
                        edgeSymbol: ['circle', 'arrow'],
                        edgeSymbolSize: [4, 10],
                        edgeLabel: {
                            fontSize: 20
                        },
                        data: [
                        //     {
                        //     name: '节点1',
                        //     x: 300,
                        //     y: 300,
                        //     itemStyle: {
                        //         color: '#000'
                        //     }
                        // }, 
                        {
                
                            name: 12188,
                            x: 0,
                            y: 0
                        }, 
                        // {
                        //     name: '节点3',
                        //     x: 550,
                        //     y: 100
                        // }, 
                        // {
                        //     name: '节点4',
                        //     x: 550,
                        //     y: 500
                        // }
                        ],
                        // links: [],
                        links: [
                            // {
                            //     source: '节点1',
                            //     target: '节点2',
                            //     symbolSize: [5, 10],//边两端的标记大小
                            //     // label: {show: true},//箭头上的数字
                            //     lineStyle: {//关系边的线条样式
                            //         width: 2,
                            //         curveness: 0.2,
                            //         color:'red'
                            //     },
                            // }, 
                            // {
                            //     source: '节点2',
                            //     target: '节点1',
                            //     // label: {show: true},
                            //     lineStyle: {curveness: 0.2},
                            // }, 
                            // {
                            //     source: '节点1',
                            //     target: '节点3'
                            // }, 
                            // {
                            //     source: '节点2',
                            //     target: '节点3'
                            // }, 
                            // {
                            //     source: '节点2',
                            //     target: '节点4'
                            // }, 
                            // {
                            //     source: '节点1',
                            //     target: '节点4'
                            // }
                        ],
                        // lineStyle: {
                        //     opacity: 0.9,
                        //     width: 2,
                        //     curveness: 0
                        // }
                    }
                ]
            }
        }
    },
    mounted() {
        this.initCharts();
    },
    methods: {
        initCharts() {
            this.chart = echarts.init(this.$refs['oDiv']);
            console.log(this.option.series,"kkk")
            // this.chart.setOption({
            //     series: this.option.series
            // })
            // this.setOptions();
        },
        // setOptions() {
        //     this.chart.setOption({
        //         title: {
        //             text: 'test'
        //         },
        //         tooltip: {},
        //         animationDurationUpdate: 1500,
        //         animationEasingUpdate: 'quinticInOut',
        //         series: this.option.series
        //     })
        // }
    }
}


</script>
