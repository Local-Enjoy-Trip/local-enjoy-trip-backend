package com.ssafy.enjoytrip;

import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@Tag("suite")
@SelectPackages({
        "com.ssafy.enjoytrip.config",
        "com.ssafy.enjoytrip.service",
        "com.ssafy.enjoytrip.support",
        "com.ssafy.enjoytrip.web"
})
class WebModuleTestSuite {
}
