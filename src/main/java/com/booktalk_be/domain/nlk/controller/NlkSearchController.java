package com.booktalk_be.domain.nlk.controller;

import com.booktalk_be.domain.nlk.responseDto.NlkSearchResponse;
import com.booktalk_be.domain.nlk.service.NlkSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nlk")
@RequiredArgsConstructor
public class NlkSearchController {

    private final NlkSearchService nlkSearchService;

    @GetMapping("/search")
    public NlkSearchResponse search(
            @RequestParam("kwd") String kwd,
            @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize
    ) {
        return nlkSearchService.search(kwd, pageNum, pageSize);
    }
}
