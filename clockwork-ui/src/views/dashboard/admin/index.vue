<template>
    <div class="dashboard-editor-container">

        <panel-group @handleSetLineChartData="handleSetLineChartData"/>

        <el-row style="background:#fff;padding:16px 16px 0;margin-bottom:32px;">
            <line-chart :chart-data="lineChartData"/>
        </el-row>

        <el-row :gutter="32">

            <el-col :lg="8" :sm="24" :xs="24">
                <div class="chart-wrapper">
                    <pie-chart/>
                </div>
            </el-col>

            <el-col :lg="16" :sm="24" :xs="24">
                <div class="chart-wrapper">
                    <bar-chart/>
                </div>
            </el-col>

        </el-row>


        <el-row :gutter="8">
            <el-col :lg="{span: 12}" :md="{span: 24}" :sm="{span: 24}" :xl="{span: 12}" :xs="{span: 24}"
                    style="padding-right:8px;margin-bottom:30px;">
                <wait-run-table/>
            </el-col>
            <el-col :lg="{span: 12}" :md="{span: 24}" :sm="{span: 24}" :xl="{span: 12}" :xs="{span: 24}"
                    style="padding-right:8px;margin-bottom:30px;">
                <failed-task-table/>
            </el-col>
        </el-row>


    </div>
</template>

<script>
    import PanelGroup from './components/PanelGroup'
    import LineChart from './components/LineChart'
    import BarChart from './components/BarChart'
    import PieChart from './components/PieChart'
    import WaitRunTable from './components/WaitRunTable'
    import FailedTaskTable from './components/FailedTaskTable'

    export default {
        name: 'DashboardAdmin',
        components: {
            PanelGroup,
            LineChart,
            BarChart,
            PieChart,
            WaitRunTable,
            FailedTaskTable,
        },
        data() {
            return {
                lineChartData: {
                    title: '今日任务运行情况',
                    xName: [],
                    data: []
                },
            }
        },
        methods: {
            handleSetLineChartData(lineChartData) {
                this.lineChartData = lineChartData;
            },
        },
    }
</script>

<style lang="scss" scoped>
    .dashboard-editor-container {
        padding: 32px;
        background-color: rgb(240, 242, 245);
        position: relative;

        .github-corner {
            position: absolute;
            top: 0px;
            border: 0;
            right: 0;
        }

        .chart-wrapper {
            background: #fff;
            padding: 16px 16px 0;
            margin-bottom: 32px;
        }
    }

    @media (max-width: 1024px) {
        .chart-wrapper {
            padding: 8px;
        }
    }
</style>
