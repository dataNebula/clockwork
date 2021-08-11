<script>

    const isEndOptions = [
        {
            value: null,
            label: '是否结束'
        }, {
            value: true,
            label: '是'
        }, {
            value: false,
            label: '否'
        }
    ];

    const onlineOptions = [
        {
            value: null,
            label: '全部'
        }, {
            value: true,
            label: '上线'
        }, {
            value: false,
            label: '下线'
        }
    ];

    const executeTypeOptions = [
        {
            value: 2,
            label: '时间触发和依赖触发'
        }, {
            value: 0,
            label: '手动执行'
        }, {
            value: 1,
            label: '自动执行'
        }, {
            value: 3,
            label: '补数任务'
        }
    ];

    const triggerModeOptions = [
        {
            value: 1,
            label: '时间触发'
        }, {
            value: 0,
            label: '依赖触发'
        }, {
            value: 2,
            label: '时间触发和依赖触发'
        }, {
            value: 3,
            label: '信号触发'
        }
    ];

    const taskStatusOptions = [
        {
            value: 'enable',
            label: '等待调度'
        },{
            value: 'rerun_schedule_prep',
            label: '重启调度准备'
        }, {
            value: 'submit',
            label: '等待运行'
        },{
            value: 'master_has_received',
            label: '入队待调度'
        },{
            value: 'worker_has_received',
            label: '入队待执行'
        },{
            value: 'running',
            label: '开始'
        },{
            value: 'success',
            label: '成功'
        },{
            value: 'failed',
            label: '失败'
        },{
            value: 'killing',
            label: '停止中'
        },{
            value: 'killed',
            label: '已杀死'
        },{
            value: 'exception',
            label: '异常结束'
        },{
            value: 'run_timeout_killing',
            label: '运行超时停止中'
        },{
            value: 'run_timeout_killed',
            label: '运行超时已杀死'
        },{
            value: 'father_not_success',
            label: '父任务不成功'
        },{
            value: 'life_cycle_reset',
            label: '周期重置等待调度'
        }
    ];

    const sourceFilterOptions = [
        {
            value: 2,
            label: 'DATA_HUB'
        }, {
            value: 3,
            label: 'DATA_WORKS'
        }, {
            value: 4,
            label: 'DDS'
        }, {
            value: 0,
            label: '调度系统'
        }
    ];

    export default {
        isEndOptions,
        onlineOptions,
        executeTypeOptions,
        triggerModeOptions,
        taskStatusOptions,
        sourceFilterOptions,
    }
</script>
