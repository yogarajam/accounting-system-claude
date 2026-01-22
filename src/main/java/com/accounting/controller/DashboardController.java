package com.accounting.controller;

import com.accounting.dto.DashboardDTO;
import com.accounting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ReportService reportService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardDTO dashboard = reportService.generateDashboard();
        model.addAttribute("dashboard", dashboard);
        return "dashboard";
    }
}