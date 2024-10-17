package com.akichou.concurrent.controller;

import com.akichou.concurrent.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/school")
public class SchoolController {

    private final SchoolService schoolService ;

    @PostMapping("/apply")
    public String applyToSchool(@RequestParam("schoolName") String schoolName) {

        return schoolService.applyToSchool(schoolName) ;
    }

    @PostMapping("/submitExam")
    public String submitExam(@RequestParam("schoolName") String schoolName) {

        return schoolService.submitExam(schoolName) ;
    }
}
