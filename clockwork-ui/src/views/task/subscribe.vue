<template>
    <div>
        <el-dialog title="订阅" :visible.sync="modelChild" @close="close()" width="400" center>
            <el-form ref="form" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="手机号码" prop="mobileNumber">
                    <el-input style="width:220px" v-model.number="form.mobileNumber"></el-input>
                </el-form-item>
                <el-form-item label="时间" prop="subscriptionTime">
                    <el-time-picker
                        v-model="form.subscriptionTime"
                        format="HH:mm"
                        @change="getTime"
                        placeholder="任意时间点">
                    </el-time-picker>
                </el-form-item>
            </el-form>
            <div>
                <div>
                    <h2 class="title" v-if="subscriptData && subscriptData.length>0">已订阅列表</h2>
                    <div v-if="subscriptData && subscriptData.length>0">
                        <div class="tipData" v-for="item in subscriptData" :key="item.id">
                            <p class="op"><span>用户名：</span>{{item.userName}}</p>
                            <p class="op"><span>手机号：</span>{{item.mobileNumber}}</p>
                            <p class="op"><span>订阅时间：</span>{{item.subscriptionTime}}</p>
                        </div>
                    </div>
                </div>
            </div>
        <span slot="footer" class="dialog-footer">
            <el-button @click="onSubmit('form')" type="primary">订阅</el-button>
        </span>
    </el-dialog>
    </div>
</template>
<script>
export default {
    props: {
        getIsShow: {
            type: Boolean
        },
        rowList: {
            type: Object
        }
    },
    data(){
        return{
            modelChild:false,
            subscriptData:[],
            form: {
                mobileNumber: '',
                subscriptionTime:null
            },
            rules: {
                mobileNumber:[
                    { required: true, message: '手机号不能为空'},
                    { type: 'number', message: '手机号必须为数字值'},
                    { validator:this.mobileLenth, trigger: 'blur'}
                ],
                subscriptionTime:[
                    { required: true, message: '时间不能为空'},
                ]
            }
        }
    },
    created() {
        this.modelChild = this.getIsShow;
        
    },
    mounted() {
        this.getTaskSubscription(); 
        this.getMobileNumber();
    },
    methods:{
        onSubmit(formName) {
            //提交
            this.$refs[formName].validate((valid) => {
            if (valid) {
                let params={}
                params={
                    mobileNumber:this.form.mobileNumber,
                    subscriptionTime:this.subscriptionTime,
                    taskId:this.rowList.id,
                    userEmail: this.$store.state.user.name + '@clockwork.com',
                    userId: null,
                    userName: this.$store.state.user.name
                }
                this.axios.post('/clockwork/web/task/subscription/addTaskSubscription', JSON.stringify(params), {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                }).then((res)=>{
                    if(res.code=="OK"){
                        this.$message({
                            message: '订阅成功',
                            type: 'success'
                        });
                    }
                    this.modelChild=false;
                }).catch(()=>{

                })
                
            } else {
                console.log('error submit!!');
                return false;
            }
            });
        },
        //获取已订阅的列表
        getTaskSubscription(){
            this.subscriptData=[];
            this.axios.get("/clockwork/web/task/subscription/getTaskSubscriptionByTaskId",{params: {taskId: this.rowList.id}})
            .then((response) => {
                if (response.code === "OK") {
                    this.subscriptData=response.data
                } else {
                    _self.$message({
                        message: response.msg,
                        type: "warning",
                        duration: 5000,
                    });
                }
            })
            .catch((err) => {
                console.log(err);
            });
        },
        //获取手机号
        getMobileNumber(){
            this.axios.get("/clockwork/web/user/getMobileNumberUserName",{params: {userName: this.$store.state.user.name}})
            .then((response) => {
                if (response.code === "OK") {
                    this.form.mobileNumber=Number(response.data);
                } else {
                    _self.$message({
                        message: response.msg,
                        type: "warning",
                        duration: 5000,
                    });
                }
            })
            .catch((err) => {
                console.log(err);
            });
        },
        //获取时间格式 小时:分钟
        getTime(val){
            let H=val.getHours();
            let M=val.getMinutes() < 10 ? '0'+val.getMinutes() : val.getMinutes();
            this.subscriptionTime=H+":"+M+":00"
        },
        mobileLenth(rule, value, callback){
            let s=String(value);
            if(s.length!=11){
                return callback(new Error('手机号长度不对'));
            }else {
                callback();
            }
        },
        close() {
            this.$emit("getIsClose", { // 通知父组件关闭窗口发生变化了
                changeType: true
            });
        }
    }
}
</script>
<style scoped>
.tipData{
    border-bottom: 1px solid #E4E7ED;
    padding:10px 0;
}
.tipData .op{
    display: inline-block;
    margin-right:15px;
}
.tipData .op span{
    font-weight: bold;
}
.title{
    font-size: 16px;
    color: #303133;
    text-align: center;
    padding-top: 10px;
    padding-bottom: 5px;
}
</style>