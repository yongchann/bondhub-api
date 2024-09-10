package com.otcbridge.service;

import com.otcbridge.domain.MaturityCondition;
import com.otcbridge.domain.bond.Bond;
import com.otcbridge.domain.bond.BondType;
import com.otcbridge.domain.chat.Chat;
import com.otcbridge.domain.transaction.Transaction;
import com.otcbridge.repository.ChatRepository;
import com.otcbridge.repository.TransactionRepository;
import com.otcbridge.service.dto.BondChatDto;
import com.otcbridge.service.dto.ChatDto;
import com.otcbridge.service.dto.TransactionDetailDto;
import com.otcbridge.service.exception.IllegalInquiryParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AskService {

    private final ChatRepository chatRepository;
    private final TransactionRepository transactionRepository;

    private final BondClassifier bondClassifier;

    public List<BondChatDto> inquiry(String date, BondType bondType, MaturityCondition condition, List<String> grades) {
        String start, end;
        if (condition.getMaturityInquiryType().equals("remain")) {
            start = calculateDate(date, Integer.parseInt(condition.getMinMonth()));
            end = calculateDate(date, Integer.parseInt(condition.getMaxMonth()));
        } else if (condition.getMaturityInquiryType().equals("specific")) {
            start = formatYearMonth(formatYear(condition.getMinYear()), condition.getMinMonth()) + "-01";
            end = formatYearMonth(formatYear(condition.getMaxYear()), condition.getMaxMonth()) + "-" +
                    getLastDayOfMonth(formatYear(condition.getMaxYear()), condition.getMaxMonth());
        } else {
            throw new IllegalInquiryParameterException("Invalid maturityInquiryType: " + condition.getMaturityInquiryType());
        }

        // 조건에 맞는 채팅 조회
        List<Chat> chats = chatRepository.findValidChatsWithinDueDateRangeAndIssuerGrades(date, bondType, start, end, grades);
        List<Transaction> transactions = transactionRepository.findByTransactionDateAndBondTypeAndGrades(date, bondType, start, end, grades);

        // 채권에 따라 채팅과 거래 내역을 grouping
        List<String> exclusionKeywords = bondClassifier.getExclusionKeywords();
        Map<Bond, BondChatDto> bondMap = new HashMap<>();
        for (Chat chat : chats) {
            bondMap.computeIfAbsent(chat.getBond(), k -> BondChatDto.from(chat.getBond()))
                    .getChats()
                    .add(new ChatDto(chat.getSendDateTime(),
                            chat.getContent(),
                            exclusionKeywords.stream().anyMatch(keyword -> chat.getContent().contains(keyword))));
        }

        // 거래 내역을 순회하며 해당하는 채권에 추가
        for (Transaction transaction : transactions) {
            Bond transactionBond = transaction.getBond();
            if (bondMap.containsKey(transactionBond)) {
                bondMap.get(transactionBond)
                        .getTransactions()
                        .add(new TransactionDetailDto(
                                transaction.getTime(),
                                transaction.getYield(),
                                transaction.getTradingYield(),
                                transaction.getSpreadBp()
                        ));
            }
            // 채팅이 없는 채권의 거래는 무시 (continue)
        }

        // 채팅과 거래 내역 정렬
        for (BondChatDto bondChatDto : bondMap.values()) {
            bondChatDto.sortChats();
        }

        // 결과를 만기일 기준으로 정렬하여 반환
        return bondMap.values().stream()
                .sorted(Comparator.comparing(BondChatDto::getDueDate))
                .toList();
    }

    private String formatYear(String year) {
        if (year.length() == 2) {
            return "20" + year;
        }
        return year;
    }

    private String formatYearMonth(String year, String month) {
        return String.format("%s-%02d", year, Integer.parseInt(month));
    }

    private String getLastDayOfMonth(String year, String month) {
        LocalDate date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);
        return String.format("%02d", date.lengthOfMonth());
    }

    private String calculateDate(String baseDate, int monthsToAdd) {
        LocalDate date = LocalDate.parse(baseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return date.plusMonths(monthsToAdd).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
