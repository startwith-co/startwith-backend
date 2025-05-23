package startwithco.startwithbackend.base;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator/health")
public class HealthCheck {
    @GetMapping()
    public String health() {
        return "OK";
    }
}