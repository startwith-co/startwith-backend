package startwithco.startwithbackend.b2b.client.controller.response;

public class ClientResponse {
    public record GetAllClientResponse(
            Long clientSeq,
            String logoImageUrl
    ) {
    }
}
