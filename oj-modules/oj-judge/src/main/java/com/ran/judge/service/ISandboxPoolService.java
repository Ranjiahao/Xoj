package com.ran.judge.service;

import com.ran.judge.domain.SandBoxExecuteResult;

import java.util.List;

public interface ISandboxPoolService {
    SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList);
}