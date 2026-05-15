package com.yuanliu.xtimer.controller;

import com.yuanliu.xtimer.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.controller
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 8:39
 * @Version 1.0
 */
@RestController
@RequestMapping("/xtimer")
@Slf4j
public class TestController {

    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody String callbackInfo){
        log.info("CALLBACK:"+callbackInfo);
        return ResponseEntity.ok("ok");
    }
}
