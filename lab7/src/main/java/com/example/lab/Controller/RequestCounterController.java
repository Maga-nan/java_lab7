package com.example.lab.Controller;

import com.example.lab.Service.RequestCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestCounterController {

    private final RequestCounterService requestCounterService;

    @Autowired
    public RequestCounterController(RequestCounterService requestCounterService) {
        this.requestCounterService = requestCounterService;
    }

    @GetMapping("/api/requests/count")
    public long getRequestCount() {
        return requestCounterService.getCount();
    }

    @GetMapping("/api/requests/reset")
    public String resetRequestCount() {
        requestCounterService.reset();
        return "Request counter reset successfully.";
    }
}
