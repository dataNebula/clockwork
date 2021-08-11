<template>
<div>
    <el-dialog title="查看结构图" :visible.sync="modelChild" @close="close()" width="70%">
        <div class="main" v-loading="loading">
            <div class="left_info">
                <div class="left_echarts" id="guanxitu" ref="oDiv"></div>
            </div>
            <div class="right_info">
                <p><span>id：{{id}}</span></p>
                <p><span>名称：{{name}}</span></p>
                <p><span>状态：{{status}}</span></p>
                <p><span>开始时间：{{lastStartTime}}</span></p>
                <p><span>结束时间：{{lastEndTime}}</span></p>
                <div class="listCheckbox">
                    <h2 class="title">{{dirFlag==1 ? '上游任务列表' : '下游任务列表'}}</h2>
                    <div v-if="nodeList.length>0">
                        <p v-for="item in nodeList" :key="item.id">
                            <el-checkbox v-model="item.selected" @change="checked=>checkRow(checked,item)">{{item.id}}：{{item.name}}</el-checkbox>
                        </p> 
                    </div>
                    <p v-else class="noData">暂无数据</p>
                </div>
            </div>
        </div>
    </el-dialog>
</div>
</template>

<script>
var echarts = require('echarts');
export default {
    props: {
        getIsShow: {
            type: Boolean
        },
        rowList: {
            type: Object
        },
        checkPic: {
            type: Function
        },
        dirFlag: {
            type: Number
        }
    },
    data() {
        return {
            loading:false,
            taskStatusOptions: this.GLOBAL.taskStatusOptions,
            chart:null,
            modelChild: false,
            seriesNodeArr: [],
            seriesLinkArr: [],
            selectedNodes: [],
            selectedLinks: [],
            selectedParents: [],
            selectLinks: [],
            delNodes: [],
            delLinks: [],
            myChart:{},
            rootId: null,
            dir: null,
            //右侧信息
            id: null,
            name: '',
            status: '',
            lastStartTime: '',
            lastEndTime: '',

            nodeList: [],
            radio: '1',
            //基本配置项
            option: {
                series: [{
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
                    data: [],
                    links: [],
                    lineStyle: { //关系边样式
                        normal: {
                            color: '#04aed2',
                            opacity: 0.9,
                            width: 1,
                            curveness: 0.2
                        }
                    }
                }]
            }
        }
    },
    created() {
        this.modelChild = this.getIsShow;

    },
    mounted() {
        setTimeout(()=>{this.getTaskData()},500)
        // this.getTaskData();
    },
    methods: {
        initFn() {
            this.seriesNodeArr = []; //需要展示的节点 前端echars使用
            this.seriesLinkArr = []; //链接关系 前端echars使用
            this.selectedNodes = []; //已经展示的节点 需要传给后端
            this.selectedLinks = []; //传给后端的链接关系;
            this.selectLinks = [];
            this.selectedParents = []; //用户待选xx节点上下游列表;
            //重绘画布
            this.option.series[0].data = [];
            this.option.series[0].links = [];
            this.chart.setOption({
                series: this.option.series
            })
            // console.log("data:",this.option.series[0].data,"initFn函数")
            // console.log("links:",this.option.series[0].links,"initFn函数")
        },
        //绘制关系图
        setOptionEchart(node, link) { 
            var node = node || [];
            var link = link || [];
            this.option.series[0].data = node;
            this.option.series[0].links = link;
            this.chart.setOption({
                series: this.option.series
            })
            // console.log("data:",this.option.series[0].data,"绘制关系图")
            // console.log("links:",this.option.series[0].links,"绘制关系图")
            
        },
        getTaskData() {
            const id = this.rowList.id;
            let param = {
                name: id,
                level: 0,
            }
            this.rootId = id;
            this.chart = echarts.init(this.$refs['oDiv']);

            //右侧信息列表
            this.showDetail(this.rowList.id)

            if (this.dirFlag == 1) { //上游
                this.getTheParentTask(param, [], [], 'getTheParentTaskRelPicArray');
                
            } else {
                this.getTheParentTask(param, [], [], 'getTheChildTaskRelPicArray');
            }
            this.bindEvent()
            this.initFn();

        },
        showDetail(id){
            this.loading=true;
            this.axios.get('/clockwork/web/task/getTaskByTaskId', {params: {taskId: id}}).then((res) => {
                if (res.code == 'OK') {
                    const data = res.data;
                    this.id = data.id;
                    this.name = data.name;
                    this.taskStatusOptions.forEach((item,index)=>{
                        if(data.status==item.value){
                            this.status=item.label
                        }
                    })
                    this.lastStartTime = data.lastStartTime;
                    this.lastEndTime = data.lastEndTime;
                    this.loading=false;
                }

            }).catch((err) => {

            });
        },
        getTheParentTask(param, selectedNodes, selectedLinks, url) {
            this.loading=true;
            const id = this.rowList.id;
            let level = param.level || 0;
            //右侧信息列表
            // this.showDetail(id)
            //右侧上下游列表
            let pageParam = {}
            pageParam = {
                "taskId": param.name,
                "selectedParams": {
                    selectedNodes: selectedNodes,
                    selectedLinks: selectedLinks,
                }
            }
            this.axios.post('/clockwork/web/dag/' + url,
                    JSON.stringify(pageParam), {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                .then((res) => {
                    this.nodeList = [];
                    if (res.code == 'OK') {

                        let _data = res.data;
                        let nodeListData = null;
                        
                        if (this.dirFlag == 0) { //下游
                            nodeListData = _data.node.children;
                        } else { //上游
                            nodeListData = _data.node.parents;
                        }
                        if (nodeListData.length > 0) { //更新右侧列表项
                            this.nodeList = nodeListData;
                        }
                        let taskId = _data.node.id;
                        this.seriesNodeArr.length < 1 && this.seriesNodeArr.push({
                            name: taskId,
                            'x': 0,
                            'y': 0,
                            dir: this.dirFlag,
                            level: 0,
                            itemStyle: {
                                normal: {
                                    color: "#04aed2",
                                    borderColor: "#04aed2"
                                }
                            },
                            'label': {
                                'normal': {
                                    'show': true,
                                    textStyle: {
                                        'color': "#000",
                                        'fontSize': 8
                                    }
                                }
                            }
                        });
                        
                        this.selectLinks = _data.links;
                        this.seriesLinkArr = this.repetition(this.seriesLinkArr.concat(this.repetition(_data.links))); //去重
                        
                        if (level > 0) {
                            // console.log(1111)
                            this.selectedParents = this.doGetNodeMessLev(param, nodeListData); //多级
                            // console.log(222)
                        } else {
                            this.selectedParents = this.doGetNodeMess(param, nodeListData); //一个
                        }
                        this.setOptionEchart(this.seriesNodeArr, this.seriesLinkArr);
                        this.loading=false;
                    }
                    // console.log(res)
                }).catch((err) => {

                });
        },
        checkRow(val,item){
            // console.log("每项复选框:",val,item);
            var postNodes,postLinks;
            var taskId=item.id;
            //如果被选中
            if(val){
                this.selectedParents.map((xItem, index, arr)=> {
                    if (xItem.name == taskId && this.isRepeat(this.seriesNodeArr, taskId)) {
                        this.seriesNodeArr.push(xItem);
                    }
                })
                var _this=this;
                var links = this.selectLinks.filter(function(item) {
                    var target = _this.dirFlag == 1 ? 'source' : 'target';
                    return item[target] == taskId;
                })
                if(this.delNodes){
                    this.delNodes =  this.repetition(this.delNodes.concat(this.seriesNodeArr));
                }
                if(this.delLinks){
                    this.delLinks = this.repetition(this.delLinks.concat(links));
                }
            
                this.seriesLinkArr = this.repetition(this.seriesLinkArr.concat(links));
                this.setOptionEchart(this.seriesNodeArr, this.seriesLinkArr);
            }else{ //未选中 删除
                this.loading=true;
                if(!this.delNodes){
                    postNodes = this.taskToObj(this.seriesNodeArr);
                }else{
                    postNodes = this.taskToObj(this.delNodes)
                }
                if(!this.delLinks){
                    postLinks =  this.repetition(this.seriesLinkArr);
                }else{
                    postLinks =  this.repetition(this.delLinks);
                }
                let params={}
                params={
                    "direction": this.dirFlag,//上下游
                    "inactiveId": taskId,
                    "rootId": this.rootId, 
                    "selectedParams": {
                        "selectedNodes": postNodes,
                        "selectedLinks": postLinks
                    }
                }
                this.axios.post('/clockwork/web/dag/deleteNodesFromPic', JSON.stringify(params), {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).then(response => {
                        if (response.code === 'OK') {
                            var _data = response.data;
                            var removeNodes = _data.removedNodes;
                            var removedLinks = _data.removedLinks;
                            this.delNodes = this.doRemoveNodes(this.seriesNodeArr, removeNodes);
                            this.delLinks =this.doRemoveNodesLinks(this.seriesLinkArr, removedLinks);
                            this.setOptionEchart(this.delNodes, this.delLinks);
                            this.loading=false;
                            this.$message({
                                message: '删除成功',
                                type: 'success',
                                duration:500
                            });
                        } else {
                            this.$message({
                                message: response.msg,
                                type: 'warning',
                                duration: 5000
                            });
                        }
                    })
                    .catch(err => {
                        console.log(err);
                    });
            }
            
        },
        
        //移除画布指定节点
        doRemoveNodes(nodes, removeNodes) {
            for (var i = 0; i < removeNodes.length; i++) {
                for (var j = 0; j < nodes.length; j++) {
                    if (removeNodes[i].id == nodes[j].name) {
                        var remove = nodes.remove(nodes[j]);
                        this.selectedParents = this.repetition(this.selectedParents.concat(remove));
                    }
                }
            }
            return nodes;
        },
        //移除画布指定节点关系
        doRemoveNodesLinks(seriesLinkArr, removedLinks) {
            var nodes = [];
            nodes = nodes.concat(seriesLinkArr)
            for (var i = 0; i < removedLinks.length; i++) {
                for (var j = 0; j < nodes.length; j++) {
                    if (removedLinks[i].source == nodes[j].source && removedLinks[i].target == nodes[j].target) {
                        var remove = nodes.remove(nodes[j]);
                        this.seriesLinkArr = this.repetition(nodes.concat(remove));
                    }
                }
            }
            return nodes;
        },
        isRepeat(arr, id) {
            var result = true;
            var len = arr.length;
            if (len > 0) {
                for (var i = 0; i < len; i++) {
                    if (arr[i].name == id) {
                        result = false;
                    }
                }
            }
            return result;
        },
        taskToObj(arr) {
            var objArr = arr.map(function(item) {
                return {
                    name: item.name,
                    id:item.name
                }
            })
            return objArr;
        },
        //绑定事件
        bindEvent(){
            //点击关系图
            var myChart =  echarts.init(document.getElementById('guanxitu'));
            var _this=this;
            myChart.on('click',function(param){
                if (param.dataType === "node") {
                    var _data = param.data;
                    var dir = _data.dir;
                    var url = dir == 1 ? "getTheParentTaskRelPicArray" : "getTheChildTaskRelPicArray";
                    _this.showDetail(_data.name);
                    _this.getTheParentTask(_data, _this.taskToObj(_this.seriesNodeArr), _this.seriesLinkArr, url);
                }
            })
            
        },

        ///////////////
        //多个层级
        doGetNodeMessLev(param, nodeList) {
            var parentsNodes = [];
            var level = param.level;
            var x = param.x;
            var xMargin = 40;
            var yMargin = 50;
            var xValue = param.x || 0;
            var plus = 1;
            this.dirFlag == 1 ? plus = -1 : "";
            var levelArrLeft = this.seriesNodeArr.filter(function (item, index, arr) { //下层左侧是否已有
                return item.level == (param.level) + 1 && item.x <= 0;
            })
            levelArrLeft = levelArrLeft.sort(function (val1, val2) {
                return val1.x - val2.x;
            })

            var levelArrRight = this.seriesNodeArr.filter(function (item, index, arr) { //下层左侧是否已有
                return item.level == (param.level) + 1 && item.x > 0;
            })
            levelArrRight = levelArrRight.sort(function (val1, val2) {
                return val1.x - val2.x;
            })

            if (x < 0) {
                if (levelArrLeft.length > 0) {
                    // console.log(levelArrLeft);
                    var leftLast = levelArrLeft[levelArrLeft.length - 1];
                    nodeList.map( (item, index, arr) =>{
                        parentsNodes.push({
                            name: item.id,
                            'x': leftLast.x - (index + 1) * xMargin,
                            'y': plus * (yMargin * (level + 1)),
                            level: level + 1,
                            arrow: "left",
                            dir: this.dirFlag,
                            itemStyle: {
                                normal: {
                                    color: "#fff",
                                    borderColor: "#04aed2"
                                }
                            },
                            'label': {
                                'normal': {
                                    'show': true,
                                    textStyle: {
                                        'color': "#000",
                                        'fontSize': 8
                                    }
                                }
                            }
                        })
                    })
                } else {
                    nodeList.map( (item, index, arr) => {
                        parentsNodes.push({
                            name: item.id,
                            'x': (0 - 20) - (index) * xMargin,
                            'y': plus * (yMargin * (level + 1)),
                            level: level + 1,
                            arrow: "left",
                            dir: this.dirFlag,
                            itemStyle: {
                                normal: {
                                    color: "#fff",
                                    borderColor: "#04aed2"
                                }
                            },
                            'label': {
                                'normal': {
                                    'show': true,
                                    textStyle: {
                                        'color': "#000",
                                        'fontSize': 8
                                    }
                                }
                            }
                        })
                    })
                }
            } else {
                if (levelArrRight.length > 0) {
                    var rightLast = levelArrRight[levelArrRight.length - 1];
                    nodeList.map( (item, index, arr) => {
                        parentsNodes.push({
                            name: item.id,
                            'x': rightLast.x + (index + 1) * xMargin,
                            'y': plus * (yMargin * (level + 1)),
                            level: level + 1,
                            arrow: "left",
                            dir: this.dirFlag,
                            itemStyle: {
                                normal: {
                                    color: "#fff",
                                    borderColor: "#04aed2"
                                }
                            },
                            'label': {
                                'normal': {
                                    'show': true,
                                    textStyle: {
                                        'color': "#000",
                                        'fontSize': 8
                                    }
                                }
                            }
                        })
                    })
                } else {
                    nodeList.map( (item, index, arr) => {
                        parentsNodes.push({
                            name: item.id,
                            'x': (0 + 20) + (index) * xMargin,
                            'y': plus * (yMargin * (level + 1)),
                            level: level + 1,
                            arrow: "left",
                            dir: this.dirFlag,
                            itemStyle: {
                                normal: {
                                    color: "#fff",
                                    borderColor: "#04aed2"
                                }
                            },
                            'label': {
                                'normal': {
                                    'show': true,
                                    textStyle: {
                                        'color': "#000",
                                        'fontSize': 8
                                    }
                                }
                            }
                        })
                    })
                }
            }

            return parentsNodes;
        },
        //生成坐标x,y，leval层级节点；
        doGetNodeMess(param, nodeList) {
            if (!nodeList || (nodeList && nodeList.length < 1)) {
                return [];
            }
            var parentsNodes = [];
            var level = param.level;
            var xMargin = 40;
            var yMargin = 50;
            var xValue = param.x || 0;
            var plus = 1;
            this.dirFlag == 1 ? plus = -1 : "";
            var midIndex = Math.floor(nodeList.length / 2); //中间索引
            var left = nodeList.slice(0, midIndex); //左边项
            var right = nodeList.slice(midIndex); //右边 
            left.map((item, index, arr)=>{
                parentsNodes.push({
                    name: item.id,
                    'x': (0-20)- (index) * xMargin,
                    'y': plus * (yMargin * (level + 1)),
                    level: level + 1,
                    dir: this.dirFlag,
                    itemStyle: {
                        normal: {
                            color: "#fff",
                            borderColor: "#04aed2"
                        }
                    },
                    'label': {
                        'normal': {
                            'show': true,
                            textStyle: {
                                'color': "#000",
                                'fontSize': 8
                            }
                        }
                    }
                })
            })
            right.map((item, index, arr) => {
                parentsNodes.push({
                    name: item.id,
                    x: (0 + 20) + (index) * xMargin,
                    y: plus * (yMargin * (level + 1)),
                    level: level + 1,
                    dir: this.dirFlag,
                    itemStyle: {
                        normal: {
                            color: "#fff",
                            borderColor: "#04aed2"
                        }
                    },
                    label: {
                        normal: {
                            show: true,
                            textStyle: {
                                color: "#000",
                                fontSize: 8
                            }
                        }
                    }
                })
            })

            // console.log(parentsNodes,"parentsNodes");
            return parentsNodes;
        },
        repetition(jsonArr) {
            var unique = {};
            jsonArr.forEach(function (gpa) {
                unique[JSON.stringify(gpa)] = gpa
            });
            jsonArr = Object.keys(unique).map(function (item) {
                return JSON.parse(item)
            });
            return jsonArr;
        },
        close() {
            this.$emit("getIsClose", { // 通知父组件关闭窗口发生变化了
                changeType: true
            })
        }
    },
    
    watch: {
        isShow(val) {
            this.modelChild = this.getIsShow;
        },
        option: {
            handler() {
                this.chart.setOption(this.option)
            },
            deep: true
        },
    }
}
//移除制定数组索引
Array.prototype.remove = function(val) {
    var index = this.indexOf(val);
    if (index > -1) {
        var remove = this.splice(index, 1);
    }
    return remove;
}
</script>
<style scoped>
.main{
    border: 1px solid #eee;
    border-radius: 10px;
    height:620px;
}
.title{
    font-weight: 500;
    line-height: 1.1;
    color: #303133;
    font-size: 18px;
    text-align: center;
    padding: 10px 0 10px;
}
.left_info{
    width:70%;
    height:620px;
    display:inline-block;
    border-right:1px solid #eee;
    overflow: hidden;
}
.left_echarts{
    width:100%;height:100%;
}
.right_info{
    display:inline-block;
    width:28%;
    padding:10px;
    margin-top:10px;
    height:100%;
    vertical-align: top;
    overflow: auto;
}
.right_info .listCheckbox{
    margin-top:20px;
    margin-bottom: 10px;
}
.right_info p{
    color: #303133;
    margin-bottom: 15px;
    font-size: 16px;
    white-space : nowrap
}
.right_info .noData{
    color: #C0C4CC;
    font-size: 14px;
    text-align: center;
    margin-top:20px;
}

</style>
