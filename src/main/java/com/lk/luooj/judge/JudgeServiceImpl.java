package com.lk.luooj.judge;

import cn.hutool.json.JSONUtil;
import com.lk.luooj.common.ErrorCode;
import com.lk.luooj.exception.BusinessException;
import com.lk.luooj.judge.codesandbox.CodeSandbox;
import com.lk.luooj.judge.codesandbox.CodeSandboxFactory;
import com.lk.luooj.judge.codesandbox.CodeSandboxProxy;
import com.lk.luooj.judge.codesandbox.model.ExecuteCodeRequest;
import com.lk.luooj.judge.codesandbox.model.ExecuteCodeResponse;
import com.lk.luooj.judge.strategy.JudgeContext;
import com.lk.luooj.model.dto.question.JudgeCase;
import com.lk.luooj.model.dto.questionsubmit.JudgeInfo;
import com.lk.luooj.model.entity.Question;
import com.lk.luooj.model.entity.QuestionSubmit;
import com.lk.luooj.model.enums.JudgeInfoMessageEnum;
import com.lk.luooj.model.enums.QuestionSubmitStatusEnum;
import com.lk.luooj.service.QuestionService;
import com.lk.luooj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Value("${codesandbox.type}")
    private String type;

    @Resource
    private JudgeManager judgeManager;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1）传入题目提交的 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }

        // 查看题目表有没有对应的题目
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        // 2.如果不为等待状态
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"题目正在判题中");
        }
        //3.更改判题（题目提交）的状态为“判题中”，防止重复执行，也能让用户及时看到状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4.调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        // 代码沙箱
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().
                code(code)
                .language(language)
                .inputList(inputList).
        build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputLimit = executeCodeResponse.getOutputLimit();
        // 5.根据沙箱的执行结果，设置题目的判题状态和信息

        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputLimit);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);

        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 6.修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
        return questionSubmitResult;
    }
}

