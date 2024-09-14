package com.otcbridge.service;

import com.otcbridge.domain.ask.Ask;
import com.otcbridge.domain.ask.MaturityCondition;
import com.otcbridge.domain.bond.Bond;
import com.otcbridge.domain.bond.BondType;
import com.otcbridge.domain.chat.Chat;
import com.otcbridge.domain.transaction.Transaction;
import com.otcbridge.repository.ChatRepository;
import com.otcbridge.repository.TransactionRepository;
import com.otcbridge.service.dto.ChatDto;
import com.otcbridge.service.dto.TransactionDetailDto;
import com.otcbridge.service.exception.IllegalInquiryParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AskService {

    private final ChatRepository chatRepository;
    private final TransactionRepository transactionRepository;

    private final BondClassifier bondClassifier;

    public List<Ask> inquiry(String date, BondType bondType, MaturityCondition condition, List<String> grades) {
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

        return convertToAsk(chats, transactions);
    }

    public Map<BondType, List<Ask>> findAllAsk(String date) {
        Map<BondType, List<Ask>> result = new HashMap<>();

        List<Chat> allChats = chatRepository.findFetchBondAndBondIssuerByChatDate(date);
        Map<BondType, List<Chat>> chatGroupByBondType = allChats.stream()
                .collect(Collectors.groupingBy(chat -> chat.getBond().getBondIssuer().getType()));

        List<Transaction> allTransactions = transactionRepository.findFetchBondAndBondIssuerByTransactionDate(date);
        Map<BondType, List<Transaction>> txGroupByBondType = allTransactions.stream()
                .collect(Collectors.groupingBy(transaction -> transaction.getBond().getBondIssuer().getType()));

        List<BondType> bondTypes = Arrays.asList(BondType.PUBLIC, BondType.BANK, BondType.SPECIALIZED_CREDIT, BondType.COMPANY);
        for (BondType bondType : bondTypes) {
            List<Chat> chats = chatGroupByBondType.getOrDefault(bondType, List.of());
            List<Transaction> transactions = txGroupByBondType.getOrDefault(bondType, List.of());

            result.put(bondType, convertToAsk(chats, transactions));
        }

        return result;
    }

    private List<Ask> convertToAsk(List<Chat> chats, List<Transaction> transactions) {
        List<String> exclusionKeywords = bondClassifier.getExclusionKeywords();
        Map<Bond, Ask> bondMap = new HashMap<>();

        for (Chat chat : chats) {
            bondMap.computeIfAbsent(chat.getBond(), k -> Ask.from(chat.getBond()))
                    .getChats()
                    .add(ChatDto.builder()
                            .chatId(chat.getId())
                            .senderName(chat.getSenderName())
                            .sendTime(chat.getSendDateTime())
                            .content(chat.getContent())
                            .senderAddress(chat.getSenderAddress())
                            .containExclusionKeyword(exclusionKeywords.stream().anyMatch(keyword -> chat.getContent().contains(keyword)))
                            .build());
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
        for (Ask ask : bondMap.values()) {
            ask.sortChats();
        }

        // 결과를 만기일 기준으로 정렬하여 반환
        return bondMap.values().stream()
                .sorted(Comparator.comparing(Ask::getDueDate))
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
