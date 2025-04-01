package com.ran.friend.controller.exam;

import com.ran.common.core.controller.BaseController;
import com.ran.common.core.domain.R;
import com.ran.common.core.domain.TableDataInfo;
import com.ran.friend.domain.exam.dto.ExamQueryDTO;
import com.ran.friend.domain.exam.dto.ExamRankDTO;
import com.ran.friend.service.exam.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Autowired
    private IExamService examService;

    @GetMapping("/semiLogin/list")
    public TableDataInfo List(ExamQueryDTO examQueryDTO) {
        return examService.List(examQueryDTO);
    }

    @GetMapping("/rank/list")
    public TableDataInfo rankList(ExamRankDTO examRankDTO) {
        return examService.rankList(examRankDTO);
    }

    @GetMapping("/getFirstQuestion")
    public R<String> getFirstQuestion(Long examId) {
        return R.ok(examService.getFirstQuestion(examId));
    }

    @GetMapping("/preQuestion")
    public R<String> preQuestion(Long examId, Long questionId) {
        return R.ok(examService.preQuestion(examId, questionId));
    }

    @GetMapping("/nextQuestion")
    public R<String> nextQuestion(Long examId, Long questionId) {
        return R.ok(examService.nextQuestion(examId, questionId));
    }
}