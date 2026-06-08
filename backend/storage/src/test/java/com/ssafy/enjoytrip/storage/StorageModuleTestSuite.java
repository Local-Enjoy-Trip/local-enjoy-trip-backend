package com.ssafy.enjoytrip.storage;

import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@Tag("suite")
@SelectPackages({
        "com.ssafy.enjoytrip.storage.entity",
        "com.ssafy.enjoytrip.storage.repository"
})
class StorageModuleTestSuite {
}
