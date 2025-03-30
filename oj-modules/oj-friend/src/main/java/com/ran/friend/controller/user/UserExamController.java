package com.ran.friend.controller.user;

import com.ran.common.core.controller.BaseController;
import com.ran.common.core.domain.R;
import com.ran.common.core.domain.TableDataInfo;
import com.ran.friend.aspect.CheckUserStatus;
import com.ran.friend.domain.exam.dto.ExamDTO;
import com.ran.friend.domain.exam.dto.ExamQueryDTO;
import com.ran.friend.service.user.IUserExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/exam")
public class UserExamController extends BaseController {

    @Autowired
    private IUserExamService userExamService;

    @CheckUserStatus
    @PostMapping("/enter")
    public R<Void> enter(@RequestBody ExamDTO examDTO) {
        return toR(userExamService.enter(examDTO.getExamId()));
    }

    @GetMapping("/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return userExamService.list(examQueryDTO);
    }
}