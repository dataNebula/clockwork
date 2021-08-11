<template>
    <div class="app-container" style="padding-bottom:80px"
    v-loading="loadingData"
    element-loading-text="拼命加载中"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.8)"
    >
        <strong class="tiptext">请向下滚动屏幕, 加载更多日志</strong>
        
                <ul infinite-scroll-disabled="loading" v-infinite-scroll="loadMore">
                    <li style="font-size:13px;" v-for="(item,index) in list" :key="index">{{ item }}</li>
                </ul>
                <p v-show="loadingTip" class="loadingBtn">加载中...</p>
                <p v-show="loadingDoneTip" class="loadingBtn">加载完成!!!</p>
            <el-button
                type="primary"
                round
                @click="refreshMore"
                class="refreshBtn"
                icon="el-icon-refresh"
                >刷新</el-button>
    </div>
</template>

<script>
    export default {
        data: function () {
            return {
                list: [],               // 一行一行的日志信息
                loading: false,         // 加载
                loadingTip: false,      // 加载显示
                loadingDoneTip: false,  // 加载显示
                offset: 0,              // 文件起始位置
                size: 100,               // 每次请求行数
                retryNum: 1,            // 重试次数，请求一次加1
                loadingData: false,
                isRequest:true //用来判断时候想后台请求
            }
        },
        created() {
        },
        methods: {

            loadMore() {
                this.loading = true;
                this.loadingTip = true;

                this.isRequest=true
                this.loadLog();
            },
            refreshMore(){
                this.isRequest=true;
                this.loadLog();
            },
            loadLog() {
                this.loading = false;
                this.loadingTip = false;
                this.loadingData=true;

                let _self = this;
                if (this.$route.params.logName === '' || this.$route.params.logName===null) {
                    this.loadingData=false;
                    return;
                }

                if(this.isRequest){
                    // 构建请求参数
                    let logFileParam = {};
                    logFileParam.offset = this.offset;
                    logFileParam.size = this.size;
                    logFileParam.nodeIp = this.$route.params.nodeIp;
                    logFileParam.nodePort = this.$route.params.nodePort;
                    logFileParam.logName = this.$route.params.logName;
                    logFileParam.createTime = this.$route.params.createTime;
                    // console.log("文件位置："+this.offset,"每次请求的行数："+this.size)
                    try {
                        _self.axios.post('/clockwork/web/task/log/file/catLogFileContent', JSON.stringify(logFileParam), {
                            headers: {'Content-Type': 'application/json'}
                        })
                            .then(response => {
                                if (response.code === 'OK') {
                                    if (response.data.length > 0) {
                                        this.list.push(...response.data);
                                        this.offset = this.list.length;
                                        //还有数据，但是数据少于this.size,说明后面没数据了
                                        if(response.data.length<this.size){
                                            this.isRequest=false;
                                        }
                                    } else {//没日志了
                                        this.loading=true;//停止加载
                                        this.loadingTip = false;
                                        this.loadingDoneTip = true;
                                        this.isRequest=false;
                                    }
                                } else {
                                    _self.$message({
                                        message: '日志未生成，请稍后查看',
                                        type: 'warning',
                                        duration: 5000
                                    });
                                }
                                this.loadingData=false;
                            })
                            .catch(err => {
                                    console.log(err);
                                }
                            );
                    } catch (err) {
                        _self.$message({
                            message: '系统错误',
                            type: 'warning',
                            duration: 5000
                        });
                        console.log(err)
                    }
                }else{
                    this.loadingData=false;
                }
            },
        },
    }
</script>
<style scoped>
.tiptext{
    color: #909399;
    display: block;
    margin-bottom: 15px;
}
.refreshBtn {
    position: absolute;
    bottom: 10px;
    width: 95%;
    margin:0 20px;
}
.loadingBtn {
    position: absolute;
    left: 50%;
    bottom: 60px;
    text-align: center;
    font-size: 12px;
    color: #E6A23C;
}
</style>