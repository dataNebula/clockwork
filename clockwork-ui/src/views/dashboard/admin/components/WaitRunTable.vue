<template>
    <el-table :data="list" style="width: 100%;padding-top: 15px;font-size: 12px;padding-right: 5px">
        <el-table-column label="任务ID" width="70">
            <template slot-scope="scope">
                {{ scope.row.id}}
            </template>
        </el-table-column>
        <el-table-column align="待运行任务" label="待运行任务">
            <template slot-scope="scope">
                {{ scope.row.name}}
            </template>
        </el-table-column>
        <el-table-column align="下次运行时间" label="下次运行时间" width="140">
            <template slot-scope="{row}">
                {{ row.nextTriggerTime }}
            </template>
        </el-table-column>
        <el-table-column align="上次运行时间" label="上次运行时间" width="140">
            <template slot-scope="{row}">
                {{ row.lastStartTime }}
            </template>
        </el-table-column>
        <el-table-column align="状态" label="状态" width="125">
            <template slot-scope="{row}">
                <el-tag :type="row.status | statusStyleFilter">
                    {{ row.status | statusFilter }}
                </el-tag>
            </template>
        </el-table-column>
    </el-table>
</template>

<script>

    export default {
        data() {
            return {
                list: null
            }
        },
        created() {
            this.fetchData()
        },
        methods: {
            fetchData() {
                let _self = this;
                _self.loginName = this.$store.state.user.name;
                _self.isAdmin = this.$store.state.user.isAdmin;

                _self.axios.get('/clockwork/web/dashboard/getWaitForRunTask', {
                    params: {
                        size: 15,
                        userName: _self.loginName
                    }
                })
                    .then(response => {
                        if (response.code === 'OK') {
                            if (response.data) {
                                _self.list = response.data;
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
        }
    }
</script>
