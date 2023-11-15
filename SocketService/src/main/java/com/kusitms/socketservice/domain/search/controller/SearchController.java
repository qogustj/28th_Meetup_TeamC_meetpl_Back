package com.kusitms.socketservice.domain.search.controller;

import com.kusitms.socketservice.domain.search.dto.request.SearchRequestDto;
import com.kusitms.socketservice.domain.search.dto.response.SearchResultResponseDto;
import com.kusitms.socketservice.domain.search.service.SearchService;
import com.kusitms.socketservice.global.config.auth.SessionId;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SearchController {
    private final SimpMessagingTemplate template;
    private final SearchService searchService;

    @MessageMapping("/search")
    public void getSearchResult(@SessionId final Long sessionId,
                                final SearchRequestDto searchRequestDto) {
        final SearchResultResponseDto responseDto = searchService.getSearchResult(sessionId, searchRequestDto);
        template.convertAndSend("/sub/search/" + sessionId, responseDto);
    }

//    @MessageExceptionHandler
//    public String handleException(BusinessException exception, MessageRequestDto messageRequestDto) {
//        return exception.getErrorCode().getMessage();
////        template.convertAndSend("/sub/meeting/" + messageRequestDto.getMeetingId(), exception.getErrorCode().getMessage());
//    }
}