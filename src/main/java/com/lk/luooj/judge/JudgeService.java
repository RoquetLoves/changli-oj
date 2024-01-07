package com.lk.luooj.judge;

import com.lk.luooj.model.entity.QuestionSubmit;
import com.lk.luooj.model.vo.QuestionSubmitVO;
import com.lk.luooj.model.vo.QuestionVO;

/**
 * 判题服务
 */
public interface JudgeService {
    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId);
}
