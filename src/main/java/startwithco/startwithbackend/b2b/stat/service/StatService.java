package startwithco.startwithbackend.b2b.stat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.stat.controller.response.StatResponse;
import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.stat.repository.StatEntityRepository;
import startwithco.startwithbackend.b2b.stat.util.STAT_TYPE;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import java.util.ArrayList;
import java.util.List;

import static startwithco.startwithbackend.b2b.stat.controller.response.StatResponse.*;
import static startwithco.startwithbackend.b2b.stat.controller.response.StatResponse.GetVendorStatResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Service
@RequiredArgsConstructor
public class StatService {
    private final StatEntityRepository statEntityRepository;
    private final VendorEntityRepository vendorEntityRepository;

    @Transactional(readOnly = true)
    public GetVendorStatResponse getStatEntity(Long vendorSeq) {
        VendorEntity vendor = vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));
        List<StatEntity> statEntities = statEntityRepository.findAllByVendor(vendor);

        List<GetVendorStatResponse.StatData> salesSize = statEntities.stream()
                .filter(stat -> stat.getStatType() == STAT_TYPE.SALES_SIZE)
                .map(stat -> new GetVendorStatResponse.StatData(stat.getLabel(), stat.getPercentage()))
                .toList();

        List<GetVendorStatResponse.StatData> employeesSize = statEntities.stream()
                .filter(stat -> stat.getStatType() == STAT_TYPE.EMPLOYEES_SIZE)
                .map(stat -> new GetVendorStatResponse.StatData(stat.getLabel(), stat.getPercentage()))
                .toList();

        return new GetVendorStatResponse(salesSize, employeesSize);
    }
}
