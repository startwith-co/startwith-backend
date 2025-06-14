package startwithco.startwithbackend.b2b.home.controller.response;

import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;

public class HomeResponse {
    public record HomeCategoryResponse(
            List<CATEGORY> used,
            List<CATEGORY> unused
    ) {

    }
}
