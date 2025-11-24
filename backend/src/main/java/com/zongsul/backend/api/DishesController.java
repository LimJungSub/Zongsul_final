package com.zongsul.backend.api;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/api/dishes")
public class DishesController {

    @PostMapping("/distribute")
    public String distribute(@RequestBody List<Map<String, Object>> dishes) {
        System.out.println("배포 요청 받은 반찬 정보: " + dishes);

        // TODO: 여기서 DB에 저장하거나 DistributionSession으로 넘길 수도 있음.

        return "ok";
    }
}
