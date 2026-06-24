package com.ssafy.enjoytrip.core.api.web.view.controller;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.service.AdminCourseService;
import com.ssafy.enjoytrip.core.domain.service.AdminUserService;
import com.ssafy.enjoytrip.core.domain.service.AttractionAdminService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminPageController {
    private static final int DASHBOARD_COURSE_LIMIT = 3;
    private static final int DASHBOARD_USER_LIMIT = 4;

    private final AdminCourseService adminCourseService;
    private final AttractionAdminService attractionAdminService;
    private final AdminUserService adminUserService;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<Course> courses = adminCourseService.findAdminCourses();
        AttractionAdminService.AdminPlaceSummary placeSummary = attractionAdminService.summarizePlaces(true);
        List<AdminUserService.AdminUserSummary> users = adminUserService.findUsers();

        model.addAttribute("courses", topCourses(courses));
        model.addAttribute("users", topUsers(users));
        model.addAttribute("totalCourseCount", courses.size());
        model.addAttribute("totalUserCount", users.size());
        model.addAttribute("totalPlaceCount", placeSummary.totalCount());
        model.addAttribute("hiddenPlaceCount", placeSummary.hiddenCount());
        model.addAttribute("publicCourseCount", courses.size());
        model.addAttribute("readyCourseCount", 0);
        model.addAttribute("adminUserCount", adminUserService.countAdmins(users));
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        List<AdminUserService.AdminUserSummary> users = adminUserService.findUsers();

        model.addAttribute("users", users);
        model.addAttribute("totalUserCount", users.size());
        model.addAttribute("adminUserCount", adminUserService.countAdmins(users));
        return "admin/users";
    }

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/admin/forbidden")
    public String forbidden() {
        return "admin/forbidden";
    }

    private static List<Course> topCourses(List<Course> courses) {
        return courses.stream()
                .sorted(Comparator.comparingInt(Course::saveCount).reversed())
                .limit(DASHBOARD_COURSE_LIMIT)
                .toList();
    }

    private static List<AdminUserService.AdminUserSummary> topUsers(
            List<AdminUserService.AdminUserSummary> users
    ) {
        return users.stream()
                .limit(DASHBOARD_USER_LIMIT)
                .toList();
    }


}
