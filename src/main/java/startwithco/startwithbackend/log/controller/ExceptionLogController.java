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
    public String getExceptionLogs(@RequestParam(defaultValue = "0") int page, @ModelAttribute String errorMessage, Model model) {
        final int pageSize = 20;
        int start = page * pageSize;
        int end = start + pageSize;
        
        if (start < 0 || end < start) {
            model.addAttribute("errorMessage", "잘못된 페이지 번호입니다.");
            model.addAttribute("exceptionLogs", List.of());
            model.addAttribute("hasNext", false);
            model.addAttribute("currentPage", 0);
            model.addAttribute("pageSize", pageSize);
            return "log";
        }

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        try {
            List<ExceptionLogDto> exceptionLogDtos = exceptionLogService.getAllExceptionLogEntity(start, end)
                    .stream()
                    .map(dto -> {
                        // [수정] 변수명 개선 (raw, pretty -> requestBody, formattedRequestBody)
                        String requestBody = dto.getRequestBody();
                        String formattedRequestBody = requestBody;

                        try {
                            if (requestBody != null && !requestBody.trim().isEmpty() && requestBody.trim().startsWith("{")) {
                                var root = objectMapper.readTree(requestBody);

                                // case 1: "request" 키가 있고, 그 값이 JSON 문자열이라면 → 이중 파싱
                                if (root.has("request") && root.get("request").isTextual()) {
                                    String nestedStr = root.get("request").asText();

                                    try {
                                        var nestedJson = objectMapper.readTree(nestedStr);
                                        formattedRequestBody = objectMapper.writerWithDefaultPrettyPrinter()
                                                .writeValueAsString(nestedJson);
                                    } catch (Exception e) {
                                        formattedRequestBody = nestedStr;
                                    }

                                } else {
                                    formattedRequestBody = objectMapper.writerWithDefaultPrettyPrinter()
                                            .writeValueAsString(root);
                                }
                            }
                        } catch (Exception e) {
                            formattedRequestBody = requestBody;
                        }

                        return new ExceptionLogDto(
                                dto.getCreatedAt(),
                                dto.getHttpStatus(),
                                dto.getErrorCode(),
                                dto.getMessage(),
                                dto.getRequestUri(),
                                formattedRequestBody,
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
