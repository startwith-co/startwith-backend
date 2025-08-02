package startwithco.startwithbackend.b2b.stat.controller.response;

import java.util.List;

public class StatResponse {
    public record GetVendorStatResponse(
            List<StatData> salesSize,
            List<StatData> employeesSize
    ) {
        public record StatData(
                String label,
                Long percentage
        ) {

        }
    }
}
