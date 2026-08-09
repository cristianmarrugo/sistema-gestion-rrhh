package com.example.demo.controller.api;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard-powerbi")
@CrossOrigin(origins = "*")
public class DashboardPowerBiRestController {

    @GetMapping
    public Map<String, String> obtenerUrlDashboard() {
        // Le mandas la URL del reporte de PowerBI para que Flutter lo pinte en un WebView
        return Map.of("url", "https://app.powerbi.com/view?r=TU_LINK_DE_POWER_BI");
    }
}
