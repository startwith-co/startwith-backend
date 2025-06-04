package startwithco.startwithbackend.b2b.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.chat.controller.response.ChatResponse;
import startwithco.startwithbackend.b2b.chat.domain.ChatEntity;
import startwithco.startwithbackend.b2b.chat.repository.ChatEntityRepository;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

import static startwithco.startwithbackend.b2b.chat.controller.request.ChatRequest.*;
import static startwithco.startwithbackend.b2b.chat.controller.response.ChatResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatEntityRepository chatEntityRepository;
    private final VendorEntityRepository vendorEntityRepository;
    private final ConsumerRepository consumerRepository;
    private final CommonService commonService;

    @Transactional
    public void saveChatEntity(SaveChatRequest request, MultipartFile file) {
        if (!(vendorEntityRepository.existsByVendorSeq(request.senderSeq())
                || consumerRepository.existsByConsumerSeq(request.senderSeq()))) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "전송자가 수요/벤더 기업에 존재하지 않습니다.",
                    getCode("전송자가 수요/벤더 기업에 존재하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        if (!(vendorEntityRepository.existsByVendorSeq(request.receiverSeq())
                || consumerRepository.existsByConsumerSeq(request.receiverSeq()))) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "수신자가 수요/벤더 기업에 존재하지 않습니다.",
                    getCode("수신자가 수요/벤더 기업에 존재하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        String s3FileUrl = commonService.uploadJPGFile(file);
        ChatEntity chatEntity = ChatEntity.builder()
                .senderSeq(request.senderSeq())
                .receiverSeq(request.receiverSeq())
                .fileUrl(s3FileUrl)
                .build();

        chatEntityRepository.saveChatEntity(chatEntity);
    }

    @Transactional(readOnly = true)
    public List<GetChatEntityFile> getChatEntityFile(Long senderSeq, Long receiverSeq) {
        if (!(vendorEntityRepository.existsByVendorSeq(senderSeq)
                || consumerRepository.existsByConsumerSeq(senderSeq))) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "전송자가 수요/벤더 기업에 존재하지 않습니다.",
                    getCode("전송자가 수요/벤더 기업에 존재하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        if (!(vendorEntityRepository.existsByVendorSeq(receiverSeq)
                || consumerRepository.existsByConsumerSeq(receiverSeq))) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "수신자가 수요/벤더 기업에 존재하지 않습니다.",
                    getCode("수신자가 수요/벤더 기업에 존재하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        List<ChatEntity> chatEntities = chatEntityRepository.findAllBySenderSeqAndReceiverSeq(senderSeq, receiverSeq);
        List<GetChatEntityFile> response = new ArrayList<>();
        for (ChatEntity chatEntity : chatEntities) {
            GetChatEntityFile chatEntityFile = new GetChatEntityFile(
                    chatEntity.getSenderSeq(),
                    chatEntity.getReceiverSeq(),
                    chatEntity.getFileUrl()
            );

            response.add(chatEntityFile);
        }

        return response;
    }
}
