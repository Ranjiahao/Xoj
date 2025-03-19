package com.ran.common.core.controller;

import com.ran.common.core.domain.R;

public class BaseController {
    public R<Void> toR(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }

    public R<Void> toR(boolean result) {
        return result ? R.ok() : R.fail();
    }
}