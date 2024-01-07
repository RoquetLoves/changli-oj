package com.lk.luooj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lk.luooj.model.dto.question.QuestionQueryRequest;
import com.lk.luooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.lk.luooj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.lk.luooj.model.entity.Question;
import com.lk.luooj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lk.luooj.model.entity.User;
import com.lk.luooj.model.vo.QuestionSubmitVO;
import com.lk.luooj.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 罗凯的电脑
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2023-12-26 13:35:16
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionQueryRequest);


    /**
     * 获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmit, User loginUser);
}
