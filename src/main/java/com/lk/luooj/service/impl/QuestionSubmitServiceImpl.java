package com.lk.luooj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lk.luooj.common.ErrorCode;
import com.lk.luooj.constant.CommonConstant;
import com.lk.luooj.exception.BusinessException;
import com.lk.luooj.judge.JudgeService;
import com.lk.luooj.model.dto.question.QuestionQueryRequest;
import com.lk.luooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.lk.luooj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.lk.luooj.model.entity.Post;
import com.lk.luooj.model.entity.Question;
import com.lk.luooj.model.entity.QuestionSubmit;
import com.lk.luooj.model.entity.User;
import com.lk.luooj.model.enums.QuestionSubmitLanguageEnum;
import com.lk.luooj.model.enums.QuestionSubmitStatusEnum;
import com.lk.luooj.model.vo.QuestionSubmitVO;
import com.lk.luooj.model.vo.QuestionVO;
import com.lk.luooj.model.vo.UserVO;
import com.lk.luooj.service.QuestionService;
import com.lk.luooj.service.QuestionSubmitService;
import com.lk.luooj.mapper.QuestionSubmitMapper;
import com.lk.luooj.service.UserService;
import com.lk.luooj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @Resource
    private UserService userService;


    @Resource
    @Lazy
    private JudgeService judgeService;
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
        // 执行判题服务
        Long questionSubmitId = questionSubmit.getId();
        CompletableFuture.runAsync(() -> {
           judgeService.doJudge(questionSubmitId);
        });
        return questionSubmitId;
    }

    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到mybatis框架支持的查询 QueryMapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();


        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "questionId", questionId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 获得题目的包装类（单个）
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏: 仅本人和管理员能看见自己（提交userId 和 登录用户id 不同）提交代码的答案和提交代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 获取题目分页的包装类（分页）
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        // 获取分页数据
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        // 创建分页对象
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        // 判断分页数据是否为空
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

}




