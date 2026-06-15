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
                description = "All terminals, colored by connectivity status (online, expected offline, unexpected offline).",
                url = "/demo/terminal-status",
            ),
            DemoMap(
                title = "Terminals by SIM Provider & Cell Towers",
                description = "Terminals colored by SIM provider, with Telstra/Optus/other cell towers and their coverage ranges across Australia.",
                url = "/demo/terminal-network",
            ),
        ))
        return "home"
    }

    data class DemoMap(val title: String, val description: String, val url: String)
}
