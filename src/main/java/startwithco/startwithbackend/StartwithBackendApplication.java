package startwithco.startwithbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class StartwithBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartwithBackendApplication.class, args);
    }

}
