import Vue from "vue";
import Router from "vue-router";
/* Layout */
import Layout from "@/layout";

Vue.use(Router);

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: "/login",
    component: () => import("@/views/login/index"),
    hidden: true
  },

  {
    path: "/404",
    component: () => import("@/views/404"),
    hidden: true
  },

  {
    path: "/",
    component: Layout,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "首页",
        component: () => import("@/views/dashboard/index"),
        meta: { title: "首页", icon: "dashboard" }
      }
    ]
  },
  {
    path: "/task",
    component: Layout,
    redirect: "noRedirect",
    name: "任务管理",
    meta: { title: "任务管理", icon: "el-icon-film" },
    children: [
      {
        path: "taskList",
        name: "taskList",
        component: () => import("@/views/task/index"),
        meta: { title: "任务列表", icon: "el-icon-set-up" }
      },
      {
        path: "groupList",
        name: "groupList",
        component: () => import("@/views/group/index"),
        meta: { title: "任务组", icon: "el-icon-folder-checked" }
      },
      {
        path: "stateJudge",
        name: "stateJudge",
        component: () => import("@/views/task-manage/stateJudge/index"),
        meta: { title: "状态判断", icon: "el-icon-finished" }
      },
      // other page
      {
        path: "create",
        name: "createTask",
        component: () => import("@/views/task/create"),
        meta: {
          title: "创建任务",
          activeMenu: "/task/taskList"
        },
        hidden: true
      },
      {
        path: "update",
        name: "updateTask",
        component: () => import("@/views/task/update"),
        meta: {
          title: "更新任务",
          activeMenu: "/task/taskList"
        },
        hidden: true
      },
      // other page
      {
        path: "list/file",
        name: "logFile",
        component: () => import("@/views/log/file"),
        meta: {
          title: "日志详情",
          activeMenu: "/task/taskList"
        },
        hidden: true
      },
      {
        path: "history",
        name: "history",
        component: () => import("@/views/task/history"),
        meta: {
          title: "日志详情",
          activeMenu: "/task/taskList"
        },
        hidden: true
      }
    ]
  },
  {
    path: "/log",
    component: Layout,
    redirect: "noRedirect",
    name: "日志管理",
    meta: { title: "日志管理", icon: "el-icon-s-order" },
    children: [
      {
        path: "logList",
        name: "logList",
        component: () => import("@/views/log/index"),
        meta: { title: "例行日志列表", icon: "el-icon-tickets", activeMenu: "" }
      },
      {
        path: "rerunList",
        name: "rerunList",
        component: () => import("@/views/rerun/index"),
        meta: { title: "重启日志列表", icon: "el-icon-aim", activeMenu: "" }
      },
      {
        path: "fillList",
        name: "fillList",
        component: () => import("@/views/fill/index"),
        meta: { title: "补数日志列表", icon: "el-icon-bangzhu" }
      }
    ]
  },

  {
    path: "/lineage",
    component: Layout,
    redirect: "noRedirect",
    name: "血缘分析",
    meta: { title: "血缘分析", icon: "el-icon-s-operation" },
    children: [
      {
        path: "lineage",
        name: "lineage",
        component: () => import("@/views/lineage/index"),
        meta: { title: "血缘查询", icon: "el-icon-s-operation" }
      },
      {
        path: "dagList",
        name: "dagList",
        component: () => import("@/views/dag/index"),
        meta: { title: "DAG图", icon: "el-icon-orange" }
      },
      {
        path: "ringCheck",
        name: "ringCheck",
        component: () => import("@/views/blood-analysis/ring-check/index"),
        meta: { title: "成环检测日志", icon: "el-icon-help" }
      }
    ]
  },
  {
    path: "/file",
    component: Layout,
    redirect: "/file",
    name: "文件管理",
    children: [
      {
        path: "list",
        name: "fileList",
        component: () => import("@/views/file/index"),
        meta: { title: "文件管理", icon: "el-icon-files" }
      }
    ]
  },

  {
    path: "/node",
    component: Layout,
    redirect: "noRedirect",
    name: "节点管理",
    meta: { title: "节点管理", icon: "tree" },
    children: [
      {
        path: "group",
        name: "节点组",
        component: () => import("@/views/node/group"),
        meta: { title: "节点组", icon: "el-icon-sunrise-1" }
      },
      // {
      //     path: 'list',
      //     name: '节点列表',
      //     component: () => import('@/views/node/index'),
      //     meta: {title: '节点列表', icon: 'el-icon-s-grid'}
      // },
      {
        path: "worker",
        name: "worker",
        component: () => import("@/views/node/worker"),
        meta: { title: "worker", icon: "el-icon-s-marketing" }
      },
      {
        path: "api",
        name: "api",
        component: () => import("@/views/node/api"),
        meta: { title: "api", icon: "el-icon-picture-outline-round" }
      },
      {
        path: "master",
        name: "master",
        component: () => import("@/views/node/master"),
        meta: { title: "master", icon: "el-icon-guide" }
      }
    ]
  },
  {
    path: "/user",
    component: Layout,
    redirect: "noRedirect",
    name: "用户管理",
    meta: { title: "用户管理", icon: "el-icon-user" },
    children: [
      {
        path: "/role",
        name: "角色管理",
        component: () => import("@/views/role/index"),
        meta: { title: "角色管理", icon: "el-icon-s-check" }
      },
      {
        path: "/userList",
        name: "用户列表",
        component: () => import("@/views/user/index"),
        meta: { title: "用户列表", icon: "el-icon-s-unfold" }
      }
    ]
  },
  // {
  //     path: '/test',
  //     component: Layout,
  //     redirect: '/echarts',
  //     name: '测试',
  //     children: [
  //         {
  //             path: 'list',
  //             name: 'fileList',
  //             component: () => import('@/views/test/echarts'),
  //             meta: {title: '测试',icon: 'el-icon-files'}
  //         }
  //     ]
  // },

  { path: "*", redirect: "/404", hidden: true }
];

const createRouter = () =>
  new Router({
    // mode: 'history', // require service support
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes
  });

const router = createRouter();

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter();
  router.matcher = newRouter.matcher; // reset router
}

export default router;
