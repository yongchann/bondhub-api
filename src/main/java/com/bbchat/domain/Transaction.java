package com.bbchat.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transaction {

    private String time;                    // 시간
//    private String marketType;              // 시장구분
    private String bondName;                // 종목명
    private String maturityDate;            // 만기일
    private String transactionVolume;       // 거래량
    private String transactionAmount;       // 거래대금
    private String tradingYield;            // 매매수익율
    private String tradingPrice;            // 매매가격
    private String spreadBp;                // 민평대비(bp)
    private String spreadPrice;             // 민평대비(P)
    private String yield;                   // 민평수익율
    private String price;                   // 민평가격
//    private String settlement;              // 결제
//    private String date;                    // 일자
    private String standardCode;            // 표준코드
    private String maturityType;            // 만기구분
    private String remainingMaturity;       // 잔존만기
//    private String interestType;            // 이자유형
//    private String stockType;               // 주식구분
    private String creditRating;            // 신용등급
//    private String spread4Bp;               // 민평4사대비(bp)
//    private String spread4Price;            // 민평4사대비(P)
//    private String yield4;                  // 민평4사수익율
//    private String price4;                  // 민평4사가격
//    private String issuerCode;              // 발행사코드
    private String issuerName;              // 발행사명
//    private String pre3Diff;                // 3사세전대비
//    private String pre3Price;               // 3사세전단가
//    private String pre3TDiff;               // 3사세전(T)대비
//    private String pre3TPrice;              // 3사세전(T)단가
//    private String pre4Diff;                // 4사세전대비
//    private String pre4Price;               // 4사세전단가
//    private String pre4TDiff;               // 4사세전(T)대비
//    private String pre4TPrice;              // 4사세전(T)단가
//    private String tradingNature;           // 매매성격
//    private String tradingType;             // 매매유형
//    private String publicOrPrivate;         // 공모/사모
}

