package startwithco.startwithbackend.b2b.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.chat.domain.ChatEntity;
import startwithco.startwithbackend.b2b.chat.repository.ChatEntityRepository;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.NotFoundException;

import java.util.Objects;

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

        String s3FileUrl = null;
        if (Objects.equals(file.getContentType(), "application/pdf")) {
            s3FileUrl = commonService.uploadPDFFile(file);
        } else {
            s3FileUrl = commonService.uploadJPGFile(file);
        }

        ChatEntity chatEntity = ChatEntity.builder()
                .senderSeq(request.senderSeq())
                .receiverSeq(request.receiverSeq())
                .fileUrl(s3FileUrl)
                .chatUniqueType(request.chatUniqueType())
                .build();

        chatEntityRepository.saveChatEntity(chatEntity);
    }

    @Transactional(readOnly = true)
    public GetChatEntityFile getChatEntityFile(String chatUniqueType) {
        ChatEntity chatEntity = chatEntityRepository.findByChatUniqueType(chatUniqueType)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 채팅 Unique Type 입니다.",
                        getCode("존재하지 않는 채팅 Unique Type 입니다.", ExceptionType.NOT_FOUND)
                ));

        return new GetChatEntityFile(chatEntity.getFileUrl());
    }
}
