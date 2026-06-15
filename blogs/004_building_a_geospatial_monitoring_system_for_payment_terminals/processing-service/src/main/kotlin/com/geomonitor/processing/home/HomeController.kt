package com.geomonitor.processing.home

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * Landing page linking out to the support pages and the demo map pages.
 */
@Controller
class HomeController {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("demoMaps", listOf(
            DemoMap(
                title = "Terminals by Connectivity Status",
                description = "All terminals across Australia, colored by online, expected offline, or unexpected offline status.",
                url = "/demo/terminal-status",
                tag = "Fleet status",
                accent = "fleet",
            ),
            DemoMap(
                title = "Terminals by SIM Provider & Cell Towers",
                description = "Terminals by SIM provider with Telstra, Optus, and other cell towers and coverage ranges.",
                url = "/demo/terminal-network",
                tag = "Network & coverage",
                accent = "network",
            ),
            DemoMap(
                title = "Terminal Sales by Day",
                description = "Daily sales totals per terminal, colored in bands from light to dark green. Pick a day from the dropdown to update the map.",
                url = "/demo/sales-by-day",
                tag = "Sales",
                accent = "sales",
            ),
        ))
        return "home"
    }

    data class DemoMap(
        val title: String,
        val description: String,
        val url: String,
        val tag: String,
        val accent: String,
    )
}
