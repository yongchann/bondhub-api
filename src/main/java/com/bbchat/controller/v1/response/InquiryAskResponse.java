package com.bbchat.controller.v1.response;

import com.bbchat.service.dto.BondChatDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class InquiryAskResponse{

    private List<BondChatDto> bonds;
}
