<template>
    <div class="navbar">
        <hamburger
            :is-active="sidebar.opened"
            class="hamburger-container"
            @toggleClick="toggleSideBar"
        />

        <breadcrumb class="breadcrumb-container" />

        <div class="user">
            <el-popover placement="bottom" width="100" trigger="hover">
                <div @click="logout()" class="out" style="cursor: pointer;color:#409EFF;">
                    <img src="@/assets/images/out.png" style="vertical-align:middle;" /><span style="vertical-align:middle;">退出</span>
                </div>
                <p slot="reference">
                    <img
                        src="@/assets/images/default.png"
                        class="user-avatar"
                    /><span class="userName">{{ userName }}</span>
                </p>
            </el-popover>
        </div>

        <!-- <div class="right-menu">
            <el-dropdown class="avatar-container" trigger="click">
                <div class="avatar-wrapper">
                    <img src="@/assets/images/default.png" class="user-avatar">
                    <i class="el-icon-caret-bottom"/>
                </div>
                <el-dropdown-menu slot="dropdown" class="user-dropdown">
                    <router-link to="/">
                        <el-dropdown-item>
                            Home
                        </el-dropdown-item>
                    </router-link>
                    <el-dropdown-item divided @click.native="logout">
                        <span style="display:block;">退出</span>
                    </el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
        </div> -->
    </div>
</template>

<script>
import { mapGetters } from "vuex";
import Breadcrumb from "@/components/Breadcrumb";
import Hamburger from "@/components/Hamburger";

export default {
    data() {
        return {
            userName: this.$store.state.user.name,
        };
    },
    components: {
        Breadcrumb,
        Hamburger,
    },
    computed: {
        ...mapGetters(["sidebar", "avatar"]),
    },
    methods: {
        toggleSideBar() {
            this.$store.dispatch("app/toggleSideBar");
        },
        async logout() {
            await this.$store.dispatch("user/logout");
            this.$router.push(`/login?redirect=${this.$route.fullPath}`);
        }
    },
};
</script>

<style lang="scss" scoped>
.navbar {
    height: 50px;
    overflow: hidden;
    position: relative;
    background: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

    .hamburger-container {
        line-height: 46px;
        height: 100%;
        float: left;
        cursor: pointer;
        transition: background 0.3s;
        -webkit-tap-highlight-color: transparent;

        &:hover {
            background: rgba(0, 0, 0, 0.025);
        }
    }

    .breadcrumb-container {
        float: left;
    }

    .user {
        float: right;
        line-height: 50px;
        cursor: pointer;
        .user-avatar {
            width: 30px;
            height: 30px;
            border-radius: 10px;
            vertical-align: middle;
        }
        .userName {
            display: inline-block;
            vertical-align: middle;
            padding-right: 15px;
            padding-left: 5px;
            color: #606266;
            font-size: 14px;
        }
    }
    .el-popover{
        background-color: #304156 !important;
    }

    // .right-menu {
    //     float: right;
    //     height: 100%;
    //     line-height: 50px;

    //     &:focus {
    //         outline: none;
    //     }

    //     .right-menu-item {
    //         display: inline-block;
    //         padding: 0 8px;
    //         height: 100%;
    //         font-size: 18px;
    //         color: #5a5e66;
    //         vertical-align: text-bottom;

    //         &.hover-effect {
    //             cursor: pointer;
    //             transition: background .3s;

    //             &:hover {
    //                 background: rgba(0, 0, 0, .025)
    //             }
    //         }
    //     }

    //     .avatar-container {
    //         margin-right: 30px;

    //         .avatar-wrapper {
    //             margin-top: 5px;
    //             position: relative;

    //             .user-avatar {
    //                 cursor: pointer;
    //                 width: 30px;
    //                 height: 30px;
    //                 border-radius: 10px;
    //             }

    //             .el-icon-caret-bottom {
    //                 cursor: pointer;
    //                 position: absolute;
    //                 right: -20px;
    //                 top: 25px;
    //                 font-size: 12px;
    //             }
    //         }
    //     }
    // }
}
</style>
