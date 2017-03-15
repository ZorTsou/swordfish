/*
 * Copyright (C) 2017 Baifendian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baifendian.swordfish.dao;

import com.baifendian.swordfish.common.job.FlowStatus;
import com.baifendian.swordfish.dao.mysql.model.ExecutionFlow;
import com.baifendian.swordfish.dao.mysql.model.ExecutionNode;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : liujin
 * @date : 2017-03-14 9:06
 */
public class FlowDaoTest {
    FlowDao flowDao;

    @Before
    public void before(){
        flowDao = DaoFactory.getDaoInstance(FlowDao.class);
    }

    @Test
    public void testQueryExecutionNodeLastAttempt(){
        ExecutionNode executionNode = flowDao.queryExecutionNodeLastAttempt(411, 6);
        System.out.println(executionNode.getStatus());
    }

    @Test
    public void testQueryAllExecutionFlow(){
        List<ExecutionFlow> executionNodeList = flowDao.queryAllNoFinishFlow();
        System.out.println(executionNodeList.size());
    }
}
