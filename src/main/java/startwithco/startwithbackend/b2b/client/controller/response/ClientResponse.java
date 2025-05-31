package startwithco.startwithbackend.b2b.client.controller.response;

public class ClientResponse {
    public record SaveClientResponse(
            Long clientSeq
    ) {

    }

    public record GetAllClientResponse(
            Long clientSeq,
            String clientName,
            String logoImageUrl
    ) {
    }
}
