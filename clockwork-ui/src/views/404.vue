<template>
    <div class="wscn-http404-container">
        <img src="@/assets/404_images/timg.jpg"/>
        <div class="back">
            <el-link type="primary" @click="backHome">返回首页</el-link>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'Page404',
        computed: {
            message() {
                return 'The webmaster said that you can not enter this page...'
            }
        },
        methods:{
            backHome(){
                this.$router.push({path:'/dashboard'})
            }
        }
    }
</script>

<style lang="scss" scoped>
    .wscn-http404-container {
        transform: translate(-50%, -50%);
        position: absolute;
        top: 40%;
        left: 50%;
    }
    .back{
        margin-top: -80px;
        text-align: center;
    }
    
</style>
