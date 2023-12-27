package com.lk.luooj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lk.luooj.common.ErrorCode;
import com.lk.luooj.exception.BusinessException;
import com.lk.luooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.lk.luooj.model.entity.Post;
import com.lk.luooj.model.entity.Question;
import com.lk.luooj.model.entity.QuestionSubmit;
import com.lk.luooj.model.entity.User;
import com.lk.luooj.model.enums.QuestionSubmitLanguageEnum;
import com.lk.luooj.model.enums.QuestionSubmitStatusEnum;
import com.lk.luooj.service.QuestionService;
import com.lk.luooj.service.QuestionSubmitService;
import com.lk.luooj.mapper.QuestionSubmitMapper;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author 罗凯的电脑
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2023-12-26 13:35:16
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{
    
    @Resource
    private QuestionService questionService;
    
    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // todo 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        Long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        // 锁必须要包裹住事务方法
       QuestionSubmit questionSubmit = new QuestionSubmit();
       questionSubmit.setUserId(userId);
       questionSubmit.setQuestionId(questionId);
       questionSubmit.setCode(questionSubmitAddRequest.getCode());
       questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());
       // todo 设置初始状态
       questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
       questionSubmit.setJudgeInfo("{}");
        boolean result = this.save(questionSubmit);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        return questionSubmit.getId();
    }


}




