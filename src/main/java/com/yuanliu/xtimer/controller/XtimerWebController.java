package com.yuanliu.xtimer.controller;

import com.yuanliu.xtimer.dto.TimerDTO;
import com.yuanliu.xtimer.service.XTimerService;
import lombok.extern.slf4j.Slf4j;
import com.yuanliu.xtimer.common.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.controller
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:50
 * @Version 1.0
 */

@RestController
@RequestMapping("/xtimer")
@Slf4j
public class XtimerWebController {

    @Resource
    private XTimerService xTimerService;

    @PostMapping(value = "/createTimer")
    public ResponseEntity<Long> createTimer(@RequestBody TimerDTO timerDTo){
        Long timerId = xTimerService.CreateTimer(timerDTo);
        return ResponseEntity.ok(timerId);
    }

    @GetMapping(value="/enableTimer")
    public ResponseEntity<String> enableTimer(@RequestParam(value="app") String app, @RequestParam(value="timerId") Long timerId){
        xTimerService.EnableTimer(app,timerId);
        return ResponseEntity.ok("ok");
    }
}
