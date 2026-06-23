package com.ssafy.enjoytrip.core.api.web.view.controller;

import com.ssafy.enjoytrip.core.api.web.dto.request.AdminPlaceForm;
import com.ssafy.enjoytrip.core.api.web.dto.response.AdminPlaceActionResponse;
import com.ssafy.enjoytrip.core.domain.service.AttractionAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/places")
@RequiredArgsConstructor
public class AdminPlaceController {
    private static final int PLACE_PAGE_SIZE = 20;

    private final AttractionAdminService attractionAdminService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "true") boolean includeHidden,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        addPlacePageAttributes(model, includeHidden, page);
        model.addAttribute("placeForm", emptyForm());
        return "admin/places";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("placeForm") AdminPlaceForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addPlacePageAttributes(model, true, 1);
            return "admin/places";
        }

        attractionAdminService.createPlace(
                form.id(),
                form.normalizedTitle(),
                form.blankToNull(form.addr1()),
                form.blankToNull(form.addr2()),
                form.blankToNull(form.firstImage()),
                form.blankToNull(form.firstImage2()),
                form.sidoCode(),
                form.gugunCode(),
                form.latitude(),
                form.longitude(),
                form.blankToNull(form.contentTypeId()),
                form.blankToNull(form.overview())
        );
        redirectAttributes.addFlashAttribute("message", "장소를 생성했습니다.");
        return "redirect:/admin/places";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("placeForm") AdminPlaceForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addPlacePageAttributes(model, true, 1);
            return "admin/places";
        }

        attractionAdminService.updatePlace(
                id,
                form.normalizedTitle(),
                form.blankToNull(form.addr1()),
                form.blankToNull(form.addr2()),
                form.blankToNull(form.firstImage()),
                form.blankToNull(form.firstImage2()),
                form.sidoCode(),
                form.gugunCode(),
                form.latitude(),
                form.longitude(),
                form.blankToNull(form.contentTypeId()),
                form.blankToNull(form.overview()),
                form.normalizedStatus(),
                form.duplicateOfAttractionId(),
                form.blankToNull(form.duplicateReason())
        );
        redirectAttributes.addFlashAttribute("message", "장소를 수정했습니다.");
        return "redirect:/admin/places";
    }

    @PostMapping(value = "/{id}/hide", headers = "!X-Requested-With")
    public String hide(@PathVariable Long id,
                       @RequestParam(defaultValue = "true") boolean includeHidden,
                       @RequestParam(defaultValue = "1") int page,
                       RedirectAttributes redirectAttributes) {
        attractionAdminService.hidePlace(id);
        redirectAttributes.addAttribute("includeHidden", includeHidden);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addFlashAttribute("message", "장소를 숨김 처리했습니다.");
        return "redirect:/admin/places";
    }

    @PostMapping(value = "/{id}/hide", headers = "X-Requested-With=XMLHttpRequest")
    @ResponseBody
    public AdminPlaceActionResponse hideAjax(@PathVariable Long id,
                                             @RequestParam(defaultValue = "true") boolean includeHidden,
                                             @RequestParam(defaultValue = "1") int page) {
        attractionAdminService.hidePlace(id);
        return placeActionResponse(
                id,
                "HIDDEN",
                includeHidden,
                page,
                "장소를 숨김 처리했습니다."
        );
    }

    @PostMapping(value = "/{id}/restore", headers = "!X-Requested-With")
    public String restore(@PathVariable Long id,
                          @RequestParam(defaultValue = "true") boolean includeHidden,
                          @RequestParam(defaultValue = "1") int page,
                          RedirectAttributes redirectAttributes) {
        attractionAdminService.restorePlace(id);
        redirectAttributes.addAttribute("includeHidden", includeHidden);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addFlashAttribute("message", "장소를 복구했습니다.");
        return "redirect:/admin/places";
    }

    @PostMapping(value = "/{id}/restore", headers = "X-Requested-With=XMLHttpRequest")
    @ResponseBody
    public AdminPlaceActionResponse restoreAjax(@PathVariable Long id,
                                                @RequestParam(defaultValue = "true") boolean includeHidden,
                                                @RequestParam(defaultValue = "1") int page) {
        attractionAdminService.restorePlace(id);
        return placeActionResponse(
                id,
                "ACTIVE",
                includeHidden,
                page,
                "장소를 복구했습니다."
        );
    }

    private void addPlacePageAttributes(Model model, boolean includeHidden, int page) {
        AttractionAdminService.AdminPlacePage placePage = attractionAdminService.findPlacesPage(
                includeHidden,
                page,
                PLACE_PAGE_SIZE
        );
        model.addAttribute("placePage", placePage);
        model.addAttribute("places", placePage.places());
        model.addAttribute("includeHidden", includeHidden);
        model.addAttribute("page", placePage.currentPage());
    }

    private AdminPlaceActionResponse placeActionResponse(Long id,
                                                         String status,
                                                         boolean includeHidden,
                                                         int page,
                                                         String message) {
        AttractionAdminService.AdminPlacePage placePage = attractionAdminService.findPlacesPage(
                includeHidden,
                page,
                PLACE_PAGE_SIZE
        );
        return new AdminPlaceActionResponse(
                id,
                status,
                message,
                includeHidden || !"HIDDEN".equals(status),
                placePage.totalCount(),
                placePage.pageSize(),
                placePage.currentPage(),
                placePage.totalPages(),
                nextActionUrl(id, status),
                nextActionLabel(status),
                nextActionStyle(status)
        );
    }

    private static String nextActionUrl(Long id, String status) {
        if ("HIDDEN".equals(status)) {
            return "/admin/places/" + id + "/restore";
        }
        return "/admin/places/" + id + "/hide";
    }

    private static String nextActionLabel(String status) {
        if ("HIDDEN".equals(status)) {
            return "숨김 해제";
        }
        return "숨김";
    }

    private static String nextActionStyle(String status) {
        if ("HIDDEN".equals(status)) {
            return "secondary";
        }
        return "danger";
    }

    private static AdminPlaceForm emptyForm() {
        return new AdminPlaceForm(
                null,
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                null,
                "",
                "",
                "ACTIVE",
                null,
                ""
        );
    }
}
