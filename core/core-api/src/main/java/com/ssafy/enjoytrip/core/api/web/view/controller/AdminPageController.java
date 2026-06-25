package com.ssafy.enjoytrip.core.api.web.view.controller;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.service.AdminUserService;
import com.ssafy.enjoytrip.core.domain.service.AttractionAdminService;
import com.ssafy.enjoytrip.core.domain.service.CourseService;
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

    private final CourseService courseService;
    private final AttractionAdminService attractionAdminService;
    private final AdminUserService adminUserService;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<Course> courses = courseService.findAllBySaveCount(DASHBOARD_COURSE_LIMIT);
        AttractionAdminService.AdminPlaceSummary placeSummary = attractionAdminService.summarizePlaces(true);
        List<AdminUserService.AdminUserSummary> users = adminUserService.findUsers();

        model.addAttribute("courses", courses);
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

    @GetMapping("/admin/courses")
    public String courses(Model model) {
        List<Course> courses = courseService.findAllBySaveCount(100);
        model.addAttribute("courses", courses);
        model.addAttribute("totalCourseCount", courses.size());
        return "admin/courses";
    }

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/admin/forbidden")
    public String forbidden() {
        return "admin/forbidden";
    }

    private static List<AdminUserService.AdminUserSummary> topUsers(
            List<AdminUserService.AdminUserSummary> users
    ) {
        return users.stream()
                .limit(DASHBOARD_USER_LIMIT)
                .toList();
    }


}
