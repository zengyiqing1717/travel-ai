package com.example.traveljava.controller;

import com.example.traveljava.dto.ChatRequestDTO;
import com.example.traveljava.dto.TravelRequestDTO;
import com.example.traveljava.service.TravelService;
import com.example.traveljava.vo.Result;
import com.example.traveljava.vo.TravelRecommendVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelController {
    private final TravelService travelService;

//    public TravelController(TravelService travelService) {
//        this.travelService = travelService;
//    }

    @GetMapping("/hello")
    public Result<String> hello(){
//        Result<String> result = new Result<>();
//        result.success = true;
//        result.setSuccess(true);
//        System.out.println(result.getSuccess());
        return Result.ok("hello word");
    }

    @PostMapping("/recommend")
    public Result<TravelRecommendVO> recommend(@Valid @RequestBody TravelRequestDTO travelRequestDTO){
        System.out.println(travelRequestDTO.getCity());
        System.out.println(travelRequestDTO.getDays());
        System.out.println(travelRequestDTO.getBudget());
        TravelRecommendVO travelRecommendVO = travelService.recommend(travelRequestDTO.getCity(), travelRequestDTO.getDays(), travelRequestDTO.getBudget());
        return Result.ok(travelRecommendVO);
    }

    @PostMapping(value = "/chat", produces = "text/event-stream")
    public SseEmitter chat(@Valid @RequestBody ChatRequestDTO chatRequestDTO) {
        return travelService.chat(chatRequestDTO.getMessage());
    }

}
