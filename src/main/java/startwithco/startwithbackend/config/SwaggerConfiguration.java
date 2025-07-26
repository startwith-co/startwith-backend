package startwithco.startwithbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Value("${swagger.server-url}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        Server server = new Server();
        server.setUrl(serverUrl);

        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components)
                .addServersItem(server);
    }

    private Info apiInfo() {
        return new Info()
                .title("StartWith")
                .description(
                        """
                        1. 프론트에서 분기 잡을 수 있게 CUSTOM CODE 값을 같이 반환합니다.
                        2. EXCEPTION 시 **httpStatus, message, code**의 형식이 아닌 **다른 형식**으로 날라갈 경우 *담당자*에게 말씀해주세요.
                        3. CODE 값에 **"UNAUTHORIZED_EXCEPTION_예외코드 설정하세요."**가 반환될 경우 *담당자*에게 말씀해주세요.
                        4. 필요한 API 혹은 데이터가 있을 경우 말씀해주세요.
                        5. 각각의 API Description에 빠져있는 EXCEPTION이 있을 경우 말씀해주세요.
                        6. CATEGORY: DEFECT_INSPECTION, PREDICTIVE_MAINTENANCE, PROCESS_MONITORING, MES_INVENTORY_MANAGEMENT
                        7. DIRECTION: INCREASE, DECREASE
                        8. 중복 가능한 데이터의 경우 ','로 이어서 String으로 보내주세요. EX) 소기업,중기업,중견기업
                        9. 광클 방지를 위한 disable 처리해주세요.
                        """
                )
                .version("1.0.0");
    }
}
