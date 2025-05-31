package startwithco.startwithbackend.b2b.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.client.controller.response.ClientResponse;
import startwithco.startwithbackend.b2b.client.domain.ClientEntity;
import startwithco.startwithbackend.b2b.client.repository.ClientEntityRepository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import java.util.List;

import static startwithco.startwithbackend.b2b.client.controller.request.ClientRequest.*;
import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientEntityRepository clientEntityRepository;
    private final VendorEntityRepository vendorEntityRepository;
    private final CommonService commonService;

    public SaveClientResponse saveClientEntity(SaveClientRequest request, MultipartFile logoImageUrl) {
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionCodeMapper.ExceptionType.NOT_FOUND)
                ));

        String s3LogoImageUrl = commonService.uploadJPGFile(logoImageUrl);
        ClientEntity clientEntity = ClientEntity.builder()
                .clientName(request.clientName())
                .logoImageUrl(s3LogoImageUrl)
                .vendorEntity(vendorEntity)
                .build();

        ClientEntity savedClientEntity = clientEntityRepository.saveClientEntity(clientEntity);

        return new SaveClientResponse(savedClientEntity.getClientSeq());
    }

    public List<GetAllClientResponse> getAllClientEntity(Long vendorSeq) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionCodeMapper.ExceptionType.NOT_FOUND)
                ));

        return clientEntityRepository.findAllByVendorSeqCustom(vendorSeq);
    }
}
