/**
 * Created by XuanDongTang on 2020/09/26.
 */
import Day from "dayjs";

/**
 * 格式化任务状态样式
 *
 * @param {string} status
 * @returns {string}
 */
function statusStyleFilter(status) {
  const statusMap = {
    enable: "primary",
    submit: "info",
    rerun_schedule_prep: "info",
    master_has_received: "info",
    worker_has_received: "info",
    running: "primary",
    success: "success",
    failed: "danger",
    killing: "warning",
    killed: "danger",
    exception: "danger",
    run_timeout_killing: "warning",
    run_timeout_killed: "danger",
    father_not_success: "warning",
    life_cycle_reset: "warning"
  };
  return statusMap[status];
}

function isNotFilter(status) {
  return status ? "是" : "否";
}

/**
 * 格式化任务状态
 *
 * @param {string} status
 * @returns {string}
 */
function statusFilter(status) {
  if (status == null) return "无";
  const statusMap = {
    enable: "等待调度",
    rerun_schedule_prep: "重启调度准备",
    submit: "等待运行",
    master_has_received: "入队待调度",
    worker_has_received: "入队待执行",
    running: "开始",
    success: "成功",
    failed: "失败",
    killing: "停止中",
    killed: "已杀死",
    exception: "异常结束",
    run_timeout_killing: "运行超时停止中",
    run_timeout_killed: "运行超时已杀死",
    father_not_success: "父任务不成功",
    life_cycle_reset: "周期重置等待调度"
  };
  return statusMap[status];
}

function onlineFilter(online) {
  switch (online) {
    case false:
      return "下线";
    case true:
      return "上线";
    default:
      return online;
  }
}

function nodeStatusFilter(status) {
  switch (status) {
    case "disable":
      return "禁用";
    case "enable":
      return "启用";
    default:
      return status;
  }
}

function sourceFilter(source) {
  switch (source) {
    case 2:
      return "DATA_HUB";
      break;
    case 3:
      return "DATA_WORKS";
      break;
    case 4:
      return "DDS";
      break;
    default:
      return "调度系统";
  }
}

function executeTypeFilter(executeType) {
  switch (executeType) {
    case 1:
      return "自动执行";
      break;
    case 0:
      return "手动执行";
      break;
    case 3:
      return "补数任务";
      break;
    default:
      return executeType;
  }
}

/**
 * 成环检测日志
 */
function typeStatusFilter(status) {
  return status ? "danger" : "success";
}

/**
 * 时间戳格式化
 * @para {string} day 时间戳
 * @para {string} format 格式
 * @return {string}
 */
function formatDayFilter(day, format) {
  format = format || "YYYY-MM-DD HH:mm:ss";
  if (day) {
    return Day(day).format(format);
  }
  return day;
}

export default {
  statusStyleFilter: statusStyleFilter,
  statusFilter: statusFilter,
  onlineFilter: onlineFilter,
  sourceFilter: sourceFilter,
  isNotFilter: isNotFilter,
  executeTypeFilter: executeTypeFilter,
  nodeStatusFilter: nodeStatusFilter,
  typeStatusFilter,
  formatDayFilter
};
