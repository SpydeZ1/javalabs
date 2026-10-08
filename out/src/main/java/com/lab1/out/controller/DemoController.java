package com.lab1.out.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/hello")
    public RedirectView hello() {
        return new RedirectView("https://rutube.ru/video/ac4ac2f35c35fe2dc78e9a66c48097cb/");
    }

    @GetMapping("/numbers")
    public Map<String, Object> numbers() {
        List<Integer> values = List.of(10, 20, 30, 40, 50, 23, 31, 23, 2, 13);
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        return Map.of(
                "values", values,
                "sum", sum,
                "average", (double) sum / values.size()
        );
    }
}