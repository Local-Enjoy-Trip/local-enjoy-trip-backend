package com.ssafy.enjoytrip;

import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@Tag("suite")
@SelectPackages({
        "com.ssafy.enjoytrip.domain",
        "com.ssafy.enjoytrip.repository"
})
class CoreModuleTestSuite {
}
