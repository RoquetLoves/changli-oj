package com.lk.luooj.service;

import com.lk.luooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.lk.luooj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lk.luooj.model.entity.User;

/**
* @author 罗凯的电脑
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2023-12-26 13:35:16
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 点赞
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

}
