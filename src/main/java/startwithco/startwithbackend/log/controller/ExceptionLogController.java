package startwithco.startwithbackend.log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import startwithco.startwithbackend.log.dto.ExceptionLogDto;
import startwithco.startwithbackend.log.service.ExceptionLogService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/logs")
public class ExceptionLogController {
    private final ExceptionLogService exceptionLogService;

    @GetMapping()
    public String settlement(@RequestParam(defaultValue = "0") int page, @ModelAttribute String errorMessage, Model model) {
        int pageSize = 20;
        int start = page * pageSize;
        int end = start + pageSize;

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        try {
            List<ExceptionLogDto> exceptionLogDtos = exceptionLogService.getAllExceptionLogEntity(start, end)
                    .stream()
                    .map(dto -> {
                        String raw = dto.getRequestBody();
                        String pretty = raw;

                        try {
                            if (raw != null && raw.trim().startsWith("{")) {
                                var root = objectMapper.readTree(raw);

                                // case 1: "request" 키가 있고, 그 값이 JSON 문자열이라면 → 이중 파싱
                                if (root.has("request") && root.get("request").isTextual()) {
                                    String nestedStr = root.get("request").asText();

                                    try {
                                        var nestedJson = objectMapper.readTree(nestedStr);
                                        pretty = objectMapper.writerWithDefaultPrettyPrinter()
                                                .writeValueAsString(nestedJson);
                                    } catch (Exception e) {
                                        pretty = nestedStr;
                                    }

                                } else {
                                    // case 2: 일반 JSON 객체 → 바로 예쁘게 출력
                                    pretty = objectMapper.writerWithDefaultPrettyPrinter()
                                            .writeValueAsString(root);
                                }
                            }
                        } catch (Exception e) {
                            // JSON 파싱 실패 → 원본 그대로
                        }

                        return new ExceptionLogDto(
                                dto.getCreatedAt(),
                                dto.getHttpStatus(),
                                dto.getErrorCode(),
                                dto.getMessage(),
                                dto.getRequestUri(),
                                pretty,
                                dto.getMethodName()
                        );
                    })
                    .toList();

            model.addAttribute("exceptionLogs", exceptionLogDtos);
            model.addAttribute("hasNext", exceptionLogDtos.size() == pageSize);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("exceptionLogs", List.of());
            model.addAttribute("hasNext", false);
        }

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        return "log";
    }
}
