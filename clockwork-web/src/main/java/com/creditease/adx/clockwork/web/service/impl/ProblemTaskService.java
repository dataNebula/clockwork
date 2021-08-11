package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.ProblemTaskMapper;
import com.creditease.adx.clockwork.web.service.IProblemTaskService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:58 下午 2020/12/28
 * @ Description：
 * @ Modified By：
 */
@Service
public class ProblemTaskService implements IProblemTaskService {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemTaskService.class);

    @Autowired
    private ProblemTaskMapper problemTaskMapper;

    /**
     * 查询任务中无效的dagId信息（dag信息早已经不存在）
     *
     * @return list
     */
    @Override
    public List<TbClockworkTaskPojo> getTasksByInvalidDagId() {
        List<TbClockworkTask> tbClockworkTasks = problemTaskMapper.selectTaskInvalidDagId();
        if (CollectionUtils.isEmpty(tbClockworkTasks)) {
            return null;
        }
        return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
    }




}
