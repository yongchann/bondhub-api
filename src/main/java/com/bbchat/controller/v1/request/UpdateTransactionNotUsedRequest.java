package com.bbchat.controller.v1.request;


import lombok.Getter;

import java.util.List;

@Getter
public class UpdateTransactionNotUsedRequest {

    List<Long> transactionIds;
}
