package com.ran.common.core.enums;

import lombok.Getter;

@Getter
public enum ProgramType {

    JAVA(0, "java语言"),

    CPP(1, "C++语言");

    private Integer value;

    private String desc;

    ProgramType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}