package com.ssafy.enjoytrip.core.api;

import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@Tag("suite")
@SelectPackages({
        "com.ssafy.enjoytrip.core.api.config",
        "com.ssafy.enjoytrip.core.domain.service",
        "com.ssafy.enjoytrip.core.support",
        "com.ssafy.enjoytrip.core.api.web"
})
class WebModuleTestSuite {
}
