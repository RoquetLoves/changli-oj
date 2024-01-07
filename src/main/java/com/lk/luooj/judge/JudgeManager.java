package com.lk.luooj.judge;

import com.lk.luooj.judge.strategy.DefaultJudgeStrategy;
import com.lk.luooj.judge.strategy.JavaLanguageJudgeStrategy;
import com.lk.luooj.judge.strategy.JudgeContext;
import com.lk.luooj.judge.strategy.JudgeStrategy;
import com.lk.luooj.model.dto.questionsubmit.JudgeInfo;
import com.lk.luooj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
