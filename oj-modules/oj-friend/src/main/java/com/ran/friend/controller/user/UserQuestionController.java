package com.ran.friend.controller.user;

import com.ran.api.domain.vo.UserQuestionResultVO;
import com.ran.common.core.controller.BaseController;
import com.ran.common.core.domain.R;
import com.ran.friend.domain.user.dto.UserSubmitDTO;
import com.ran.friend.service.user.IUserQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/question")
public class UserQuestionController extends BaseController {

    @Autowired
    private IUserQuestionService userQuestionService;

    @PostMapping("/submit")
    public R<Void> submit(@RequestBody UserSubmitDTO submitDTO) {
        return toR(userQuestionService.submit(submitDTO));
    }

    @GetMapping("/exe/result")
    public  R<UserQuestionResultVO> exeResult(Long examId, Long questionId, String currentTime) {
        return R.ok(userQuestionService.exeResult(examId, questionId, currentTime));
    }
}