package com.ssafy.enjoytrip.core.api.web.view.controller;

import com.ssafy.enjoytrip.core.api.security.AdminAuthenticationSupport;
import com.ssafy.enjoytrip.core.api.web.dto.request.AdminCourseForm;
import com.ssafy.enjoytrip.core.domain.service.AdminCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {
    private final AdminCourseService adminCourseService;
    private final AdminAuthenticationSupport adminAuthenticationSupport;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", adminCourseService.findAdminCourses());
        model.addAttribute("courseForm", emptyForm());
        return "admin/courses";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("courseForm") AdminCourseForm form,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", adminCourseService.findAdminCourses());
            return "admin/courses";
        }

        Long adminMemberId = adminAuthenticationSupport.requireAdminMemberId(authentication);
        adminCourseService.createAdminCourse(form.toCourse(adminMemberId));
        redirectAttributes.addFlashAttribute("message", "관리자 코스를 생성했습니다.");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("courseForm") AdminCourseForm form,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", adminCourseService.findAdminCourses());
            return "admin/courses";
        }

        Long adminMemberId = adminAuthenticationSupport.requireAdminMemberId(authentication);
        adminCourseService.updateAdminCourse(adminMemberId, form.toCourse(adminMemberId, id.strip()));
        redirectAttributes.addFlashAttribute("message", "관리자 코스를 수정했습니다.");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        adminCourseService.deleteAdminCourse(
                adminAuthenticationSupport.requireAdminMemberId(authentication),
                id.strip()
        );
        redirectAttributes.addFlashAttribute("message", "관리자 코스를 삭제했습니다.");
        return "redirect:/admin/courses";
    }

    private static AdminCourseForm emptyForm() {
        return new AdminCourseForm("", "", "", "");
    }
}
