package com.lk.luooj.judge.codesandbox.impl;

import com.lk.luooj.judge.codesandbox.CodeSandbox;
import com.lk.luooj.judge.codesandbox.model.ExecuteCodeRequest;
import com.lk.luooj.judge.codesandbox.model.ExecuteCodeResponse;
import com.lk.luooj.model.dto.questionsubmit.JudgeInfo;
import com.lk.luooj.model.enums.JudgeInfoMessageEnum;
import com.lk.luooj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 */
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputLimit(inputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);

        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }
}
