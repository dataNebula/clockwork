<style scoped>
    .handle-box {
        display: flex;
        flex-direction: column;
        margin: 5px 5px;
        height: 100%;
    }

    .form {
        width: 100%;
        align-content: left;
        margin: 5px 5px 0px 5px;
    }


    .chart {
        width: 100%;
        flex: 1 1 800px;
        align-content: left;
        margin: 0 5px 5px 0px;;
    }

</style>
<template>
    <div class="handle-box">
        <div class="form">
            <el-form :inline="true" :model="taskForm" class="taskForm" ref="taskForm">
                <el-input clearable placeholder="任务ID" size="medium" style="width: 200px;"
                          v-model="taskForm.taskId"></el-input>
                <el-input clearable placeholder="任务名称" size="medium" style="width: 200px;"
                          v-model="taskForm.taskName"></el-input>
                <el-form-item label="上行深度">
                    <el-input-number :max="20" :min="0" controls-position="right"
                                     size="medium" style="width: 95px;" v-model="taskForm.upDeepLevel"/>
                </el-form-item>
                <el-form-item label="下行深度">
                    <el-input-number :max="20" :min="0" controls-position="right"
                                     size="medium" style="width: 95px;" v-model="taskForm.downDeepLevel"/>
                </el-form-item>
                <el-form-item>
                    <el-button @click="getTaskDagGraph(false)" icon="el-icon-search" size="medium" type="primary">搜索任务
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <el-button @click="getTaskDagGraph(true)" icon="el-icon-search" size="medium" type="primary">显示整个图
                    </el-button>
                </el-form-item>
            </el-form>
        </div>
        <div class="chart" id='myChart'></div>
    </div>
</template>

<script>
    import echarts from 'echarts'

    require('../../../node_modules/echarts/lib/chart/chord');
    require('../../../node_modules/echarts/lib/chart/graph');
    export default {
        name: "TaskDag",
        data() {
            return {
                nodes: [],
                links: [],
                taskForm: {
                    taskId: null,
                    taskName: null,
                    upDeepLevel: 1,
                    downDeepLevel: 1,
                },
                focus: 0


            }
        },
        methods: {
            getTaskDagGraph(showDag) {
                let _self = this;
                let userName = this.$store.state.user.name;
                _self.nodes = [];
                _self.links = [];
                _self.axios.get('/clockwork/web/graph/getTaskDagGraph', {
                    params:
                        {
                            'taskId': _self.taskForm.taskId,
                            'taskName': _self.taskForm.taskName,
                            'upDeepLevel': _self.taskForm.upDeepLevel,
                            'downDeepLevel': _self.taskForm.downDeepLevel,
                            'userName': userName,
                            'showDag': showDag // 是否展示整个图
                        }
                })
                    .then(response => {
                        if (response.code === 'OK') {
                            _self.nodes = JSON.parse(response.data.nodes);
                            _self.links = JSON.parse(response.data.links);
                            _self.drawGraph()
                        } else {
                            let myChart = _self.echarts.init(document.getElementById('myChart'));
                            myChart.clear();
                            _self.$message({
                                message: response.msg + ': ' + response.data,
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
            drawGraph() {
                // 绘制图表
                let _self = this;
                let myChart = echarts.init(document.getElementById('myChart'));
                myChart.clear();
                myChart.on('click', function (params) {
                    if (_self.focus === 0) {
                        //点击高亮
                        _self.myChart.dispatchAction({
                            type: 'focusNodeAdjacency',
                            dataIndex: params.dataIndex // 使用 dataIndex 来定位节点。
                        });
                        if (params.dataType === 'edge') {
                            _self.handleClick(params);
                        } else if (params.dataType === 'node') {
                            if (_self.firstNode === '') {
                                _self.firstNode = params.name;
                            } else {
                                _self.secondNode = params.name;
                            }
                        }
                        _self.focus = 1;
                    } else {
                        // 取消高亮
                        _self.myChart.dispatchAction({
                            type: 'unfocusNodeAdjacency',
                            // 使用 seriesId 或 seriesIndex 或 seriesName 来定位 series.
                            seriesIndex: params.seriesIndex,
                        });
                        _self.focus = 0;
                    }
                });
                _self.myChart = myChart;

                _self.nodes.forEach(function (node) {
                    //0是依赖触发、1是时间触发
                    switch (node.category) {
                        case 1:
                            node.symbol = 'diamond';
                            node.symbolSize = 25;
                            break;
                        case 2:
                            node.symbol = 'diamond';
                            node.symbolSize = 25;
                            break;
                        case 3:
                            node.symbol = 'rect';
                            node.symbolSize = 25;
                            break;
                        case 4:
                            node.symbol = 'roundRect';
                            node.symbolSize = 25;
                            break;
                        default:
                            node.symbol = 'circle';
                            node.symbolSize = 25;
                            break;
                    }
                    switch (node.task.status) {
                        case 'enable':
                            node.category = 1;
                            break;
                        case 'submit':
                            node.category = 2;
                            break;
                        case 'running':
                            node.category = 3;
                            break;
                        case 'success':
                            node.category = 4;
                            break;
                        case 'failed':
                            node.category = 5;
                            break;
                        default:
                            node.category = 0;
                            break;
                    }
                });
                let option = {
                    title: {
                        top: '5%',
                    },
                    toolbox: {
                        show: true,
                        left: 'right',
                        feature: {
                            restore: {show: true},
                            magicType: {show: true, type: ['force', 'chord']},
                            saveAsImage: {show: true}
                        }
                    },
                    color: ['#3d6099', '#76c5b5', '#ED7C30', '#EEEE00', '#0ec810', '#ee0719'],
                    legend: {
                        // orient: 'vertical',
                        left: 'left',
                        data: [{name: 'disable'}, {name: 'enable'}, {name: 'submit'}, {name: 'running'}, {name: 'success'}, {name: 'failed'}]
                    },
                    series: [
                        {
                            name: 'JOB DAG',
                            // top:'40%',
                            position: 'top',
                            type: 'graph',
                            layout: 'none',
                            nodes: _self.nodes,
                            links: _self.links,
                            categories: [
                                {name: 'disable'}, {name: 'enable'}, {name: 'submit'}, {name: 'running'}, {name: 'success'}, {name: 'failed'}],
                            roam: true,
                            center: ['0', '10%'],
                            // focusNodeAdjacency: true,
                            draggable: true,//指示节点是否可以拖动
                            edgeSymbol: ['', 'arrow'],//边两端的标记类型，可以是一个数组分别指定两端，也可以是单个统一指定。默认不显示标记，常见的可以设置为箭头
                            edgeSymbolSize: 9,
                            force: { //力引导图基本配置
                                repulsion: 100,//节点之间的斥力因子。支持数组表达斥力范围，值越大斥力越大。
                                gravity: 0.01,//节点受到的向中心的引力因子。该值越大节点越往中心点靠拢。
                                edgeLength: 300,//边的两个节点之间的距离，这个距离也会受 repulsion。[10, 50] 。值越小则长度越长
                                layoutAnimation: true //因为力引导布局会在多次迭代后才会稳定，这个参数决定是否显示布局的迭代动画，在浏览器端节点数据较多（>100）的时候不建议关闭，布局过程会造成浏览器假死。
                            },
                            itemStyle: {
                                normal: {
                                    borderColor: '#fff',
                                    borderWidth: 1,
                                    shadowBlur: 10,
                                    shadowColor: 'rgba(0, 0, 0, 0.3)'
                                }
                            },
                            label: { // 图形上的文本标签，可用于说明图形的一些数据信息
                                normal: {
                                    show: true,
                                    position: 'inside',
                                    formatter: function (params) {
                                        return params.data.name;
                                    }
                                },
                                emphasis: {
                                    show: true, //显示
                                    position: 'auto',//相对于节点标签的位置
                                    //回调函数，你期望节点标签上显示什么
                                    formatter: function (params) {
                                        let status = '';
                                        let triggerTime = '';
                                        let lastStartTime = '';
                                        let lastEndTime = '';
                                        if (typeof (params.data.task.status) !== 'undefined') {
                                            status = params.data.task.status;
                                        }
                                        if (typeof (params.data.task.triggerTime) !== 'undefined') {
                                            triggerTime = dateFormat(params.data.task.triggerTime);
                                        }
                                        if (typeof (params.data.task.lastStartTime) !== 'undefined') {
                                            lastStartTime = dateFormat(params.data.task.lastStartTime);
                                        }
                                        if (typeof (params.data.task.lastEndTime) !== 'undefined') {
                                            lastEndTime = dateFormat(params.data.task.lastEndTime);
                                        }
                                        return [
                                            '{title|' + params.data.name + '}',
                                            '{element|status:}{value|' + status + '}',
                                            '{element|触发时间:}{value|' + triggerTime + '}',
                                            '{element|start_time:}{value|' + lastStartTime + '}',
                                            '{element|end_time:}{value|' + lastEndTime + '}'
                                        ].join('\n');
                                    },
                                    backgroundColor: 'rgba(242,242,242,0.5)',
                                    borderColor: '#aaa',
                                    borderWidth: 1,
                                    borderRadius: 4,
                                    padding: [4, 10],
                                    lineHeight: 26,
                                    rich: { // 定义不同地方的文字的字体大小和颜色
                                        title: {
                                            align: 'center',
                                            color: '#fff',
                                            fontSize: 18,
                                            textShadowBlur: 2,
                                            textShadowColor: '#000',
                                            textShadowOffsetX: 0,
                                            textShadowOffsetY: 1,
                                            textBorderColor: '#333',
                                            textBorderWidth: 2
                                        },
                                        element: {
                                            color: '#000',
                                            textBorderColor: '#fff',
                                            textBorderWidth: 0,
                                            // width: 80,
                                            padding: [3, 10],
                                            align: 'left',
                                        },
                                        value: {
                                            // color: '#ff8811',
                                            color: '#ff1322',
                                            textBorderColor: '#fff',
                                            textBorderWidth: 0,
                                            align: 'right',
                                        }
                                    }
                                }
                            },
                            lineStyle: {
                                normal: {
                                    color: 'source',
                                    curveness: 0
                                },
                                emphasis: { // 高亮的图形样式。
                                    width: 2,
                                    color: '#000'
                                }
                            },

                        }
                    ]
                };
                myChart.setOption(option);
            }
        }
    }

    function add0(m) {
        return m < 10 ? '0' + m : m
    }

    function dateFormat(timeStamp) {
        let time = new Date(timeStamp);
        let y = time.getFullYear();
        let m = time.getMonth() + 1;
        let d = time.getDate();
        let h = time.getHours();
        let mm = time.getMinutes();
        let s = time.getSeconds();
        return y + '-' + add0(m) + '-' + add0(d) + ' ' + add0(h) + ':' + add0(mm) + ':' + add0(s);
    }
</script>


