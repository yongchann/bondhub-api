package com.bbchat.service;

import com.bbchat.domain.Chat;
import com.bbchat.domain.ChatValidityType;
import com.bbchat.domain.Transaction;
import com.bbchat.domain.entity.*;
import com.bbchat.repository.BondRepository;
import com.bbchat.repository.DailyAskRepository;
import com.bbchat.repository.DailyTransactionRepository;
import com.bbchat.repository.MultiDueDateChatRepository;
import com.bbchat.support.FileInfo;
import com.bbchat.support.S3FileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatProcessor {

    private final S3FileRepository s3FileRepository;
    private final BondRepository bondRepository;
    private final DailyAskRepository dailyAskRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final MultiDueDateChatRepository multiDueDateChatRepository;

    private final ObjectMapper objectMapper;
    private final ChatParser chatParser;
    private final ChatProcessingRules rules;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd");

    public void processChat(String date) {
        log.info("Processing chats for date: {}", date);

        FileInfo chatFile = s3FileRepository.getChatFileByDate(date);
        log.info("Retrieved chat file for date: {}", date);

        List<Chat> allChats = parseAndPreprocessChats(chatFile.getContent());
        log.info("Parsed and preprocessed {} chats", allChats.size());

        List<Chat> filteredChats = filterChats(allChats);
        log.info("Filtered chats, remaining count: {}", filteredChats.size());

        // 만기일 포함 갯수에 따른 분류
        Map<ChatValidityType, List<Chat>> chatsByDueDateCount = classifyByDueDateCount(filteredChats);
        log.info("Classified chats by due date count");

        saveClassifiedChats(chatsByDueDateCount, date);
        log.info("Saved classified chats of {} to S3", date);

        List<Chat> validChats = chatsByDueDateCount.get(ChatValidityType.VALID_SINGLE_DUE_DATE);
        log.info("Processing valid chats with single due date, count: {}", validChats.size());
        
        List<Chat> validMultiDueDateChats = chatsByDueDateCount.get(ChatValidityType.VALID_MULTI_DUE_DATE);
        saveMultiDueDateChats(date, validMultiDueDateChats);
        log.info("Saved valid chats with multi due date, count: {}", validMultiDueDateChats.size());

        int dailyAsksSize = categorize(date, validChats);
        log.info("Processed {} DailyAsk entities", dailyAsksSize);
    }

    @Transactional
    private int categorize(String date, List<Chat> validChats) {
        List<DailyAsk> dailyAsks = processValidChats(validChats, date);
        dailyAskRepository.saveAll(dailyAsks);
        return dailyAsks.size();
    }

    private List<Chat> parseAndPreprocessChats(String rawContent) {
        String preprocessedContent = preprocess(rawContent);
        return chatParser.parseChatsFromRawText(preprocessedContent);
    }

    private void saveClassifiedChats(Map<ChatValidityType, List<Chat>> chatsByValidity, String date) {
        chatsByValidity.forEach((validityType, chats) -> {
            String fileName = String.format("%s/%s.json", date, validityType);
            saveChatAsJson(fileName, chats);
        });
    }

    private List<DailyAsk> processValidChats(List<Chat> validChats, String todayStr) {
        Set<DailyAsk> result = new HashSet<>();
        List<Chat> uncategorizedChats = new ArrayList<>();

        Map<BondIssuer, List<String>> bondAliasesMap = rules.getBondAliasesMap();
        validChats.forEach(chat -> {
            AtomicBoolean matched = new AtomicBoolean(false);

            for (Map.Entry<BondIssuer, List<String>> entry : bondAliasesMap.entrySet()) {
                BondIssuer bondIssuer = entry.getKey();
                List<String> aliases = entry.getValue();
                for (String alias : aliases) {
                    if (chat.getContent().contains(alias)) {
                        Bond bond = findOrCreateBond(bondIssuer, chat.getDueDate());
                        int consecutiveDays = findOrCreateDailyAsk(bond, getYesterdayStr(todayStr));

                        DailyAsk dailyAsk = createDailyAsk(bond, todayStr, consecutiveDays);
                        result.add(dailyAsk);

                        matched.set(true);
                        break;
                    }
                }
                if (matched.get()) break;
            }
            if (!matched.get()) {
                uncategorizedChats.add(chat);
            }
        });

        saveChatAsJson(String.format("%s/%s.json", todayStr, "UNCATEGORIZED"), uncategorizedChats);
        return new ArrayList<>(result);
    }

    @Transactional
    public void processTransaction(String date) {
        dailyTransactionRepository.deleteAllByCreatedDate(date);

        FileInfo transactionFile = s3FileRepository.getTransactionFileByDate(date);

        List<Transaction> transactions = parseTransactions(transactionFile.getInputStream());
        transactions = transactions.stream()
                .filter(transaction -> !transaction.getBondName().contains("조건부"))
                .filter(transaction -> !transaction.getMaturityDate().startsWith("99"))
                .toList();

        Map<BondIssuer, List<String>> bondAliasesMap = rules.getBondAliasesMap();

        List<DailyTransaction> result = new ArrayList<>();
        List<Transaction> uncategorizedTransactions = new ArrayList<>();

        transactions.forEach(transaction -> {
            AtomicBoolean matched = new AtomicBoolean(false);

            for (Map.Entry<BondIssuer, List<String>> entry : bondAliasesMap.entrySet()) {
                BondIssuer bondIssuer = entry.getKey();
                List<String> aliases = entry.getValue();
                for (String alias : aliases) {
                    if (transaction.getBondName().contains(alias)) {
                        Bond bond = findOrCreateBond(bondIssuer, transaction.getMaturityDate());
                        int consecutiveDays = findOrCreateDailyTransaction(bond, getYesterdayStr(date));

                        DailyTransaction dailyTransaction = DailyTransaction.builder()
                                .bond(bond)
                                .createdDate(date)
                                .yield(transaction.getYield())
                                .tradingYield(transaction.getTradingYield())
                                .spreadBp(transaction.getSpreadBp())
                                .consecutiveDays(consecutiveDays)
                                .build();
                        result.add(dailyTransaction);

                        matched.set(true);
                        break;
                    }
                }
                if (matched.get()) break;
            }
            if (!matched.get()) {
                uncategorizedTransactions.add(transaction);
            }
        });

        dailyTransactionRepository.saveAll(result);
        saveTransactionAsJson(String.format("%s/%s.json", date, "UNCATEGORIZED"), uncategorizedTransactions);

    }

    private String getYesterdayStr(String todayStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
        LocalDate today = LocalDate.parse(todayStr, formatter);
        LocalDate yesterday = today.minusDays(1);
        return yesterday.format(formatter);
    }

    private Bond findOrCreateBond(BondIssuer bondIssuer, String dueDate) {
        return bondRepository.findByBondIssuerAndDueDate(bondIssuer, dueDate)
                .orElseGet(() -> bondRepository.save(new Bond(bondIssuer, dueDate)));
    }

    private int findOrCreateDailyAsk(Bond bond, String date) {
        Optional<DailyAsk> optionalDailyAsk = dailyAskRepository.findByBondAndDate(bond, date);
        return optionalDailyAsk.map(DailyAsk::getConsecutiveDays).orElse(0) + 1;
    }


    private int findOrCreateDailyTransaction(Bond bond, String date) {
        Optional<DailyTransaction> optionalDailyTransaction = dailyTransactionRepository.findByBondAndDate(bond, date);
        return optionalDailyTransaction.map(DailyTransaction::getConsecutiveDays).orElse(0) + 1;
    }

    private DailyAsk createDailyAsk(Bond bond, String createDate, int consecutiveDays) {
        return DailyAsk.builder()
                .bond(bond)
                .createdDate(createDate)
                .consecutiveDays(consecutiveDays)
                .build();
    }

    private List<Chat> filterChats(List<Chat> chats) {
        return chats.stream()
                .filter(chat -> isSellingMessage(chat.getContent()))
                .filter(chat -> isAllowedMessage(chat.getContent()))
                .collect(Collectors.toList());
    }

    private boolean isSellingMessage(String content) {
        return rules.getAskKeywords().stream().anyMatch(content::contains);
    }

    private boolean isAllowedMessage(String content) {
        return rules.getExclusionKeywords().stream().noneMatch(content::contains);
    }

    private Map<ChatValidityType, List<Chat>> classifyByDueDateCount(List<Chat> chats) {
        return chats.stream().collect(Collectors.groupingBy(chat -> {
            List<String> extractedDates = chatParser.extractDueDates(chat.getContent());

            if (extractedDates.isEmpty()) {
                return ChatValidityType.INVALID;
            } else if (extractedDates.size() > 1) {
                return ChatValidityType.VALID_MULTI_DUE_DATE;
            }

            chat.setDueDate(extractedDates.get(0));
            return ChatValidityType.VALID_SINGLE_DUE_DATE;
        }));
    }

    private void saveChatAsJson(String filename, Object object) {
        try {
            s3FileRepository.saveChatFile(filename, convertToInputStream(object), MediaType.APPLICATION_JSON_VALUE);
        } catch (Exception e) {
            throw new RuntimeException("Error saving chats for filename: " + filename, e);
        }
    }

    private void saveTransactionAsJson(String filename, Object object) {
        try {
            s3FileRepository.saveExcelFile(filename, convertToInputStream(object), MediaType.APPLICATION_JSON_VALUE);
        } catch (Exception e) {
            throw new RuntimeException("Error saving transactions for filename: " + filename, e);
        }
    }



    public InputStream convertToInputStream(Object o) {
        try {
            String jsonString = objectMapper.writeValueAsString(o);
            return new ByteArrayInputStream(jsonString.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String preprocess(String rawText) {
        int index = rawText.indexOf("\r\n");
        rawText = rawText.substring(index + "\r\n".length());
        Map<String, String> replacementRules = rules.getReplacementRules();
        for (Map.Entry<String, String> entry : replacementRules.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }

        return rawText;
    }

    private void saveMultiDueDateChats(String date, List<Chat> validMultiDueDateChats) {
        List<MultiDueDateChat> multiDueDateChatEntity = validMultiDueDateChats.stream()
                .map(chat -> MultiDueDateChat.builder()
                            .content(chat.getContent())
                            .chatCreatedDate(date)
                            .status(ChatStatus.CREATED)
                            .sendDateTime(chat.getSendDateTime())
                            .senderAddress(chat.getSenderAddress())
                            .senderName(chat.getSenderName())
                            .build()
                ).toList();
        multiDueDateChatRepository.saveAll(multiDueDateChatEntity);
    }

    public List<Transaction> parseTransactions(InputStream inputStream) {
        List<Transaction> transactions = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);  // 첫 번째 시트를 선택
            Iterator<Row> rowIterator = sheet.iterator();

            int rowIndex = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // 첫 3개 행은 스킵 (필드 이름은 4번째 행부터 존재)
                if (rowIndex++ < 3) {
                    continue;
                }

                transactions.add(parseRowToTransaction(row));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    private Transaction parseRowToTransaction(Row row) {
        return Transaction.builder()
                .time(getStringCellValue(row, 0))
//                .marketType(getStringCellValue(row, 1))
                .bondName(getStringCellValue(row, 2))
                .maturityDate(getStringCellValue(row, 3))
                .transactionVolume(getStringCellValue(row, 4))
                .transactionAmount(getStringCellValue(row, 5))
                .tradingYield(getStringCellValue(row, 6))
                .tradingPrice(getStringCellValue(row, 7))
                .spreadBp(getStringCellValue(row, 8))
                .spreadPrice(getStringCellValue(row, 9))
                .yield(getStringCellValue(row, 10))
                .price(getStringCellValue(row, 11))
//                .settlement(getStringCellValue(row, 12))
//                .date(getStringCellValue(row, 13))
                .standardCode(getStringCellValue(row, 14))
                .maturityType(getStringCellValue(row, 15))
                .remainingMaturity(getStringCellValue(row, 16))
//                .interestType(getStringCellValue(row, 17))
//                .stockType(getStringCellValue(row, 18))
                .creditRating(getStringCellValue(row, 19))
//                .spread4Bp(getStringCellValue(row, 20))
//                .spread4Price(getStringCellValue(row, 21))
//                .yield4(getStringCellValue(row, 22))
//                .price4(getStringCellValue(row, 23))
//                .issuerCode(getStringCellValue(row, 24))
                .issuerName(getStringCellValue(row, 25))
//                .pre3Diff(getStringCellValue(row, 26))
//                .pre3Price(getStringCellValue(row, 27))
//                .pre3TDiff(getStringCellValue(row, 28))
//                .pre3TPrice(getStringCellValue(row, 29))
//                .pre4Diff(getStringCellValue(row, 30))
//                .pre4Price(getStringCellValue(row, 31))
//                .pre4TDiff(getStringCellValue(row, 32))
//                .pre4TPrice(getStringCellValue(row, 33))
//                .tradingNature(getStringCellValue(row, 34))
//                .tradingType(getStringCellValue(row, 35))
//                .publicOrPrivate(getStringCellValue(row, 36))
                .build();
    }

    private String getStringCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                String cellValue = cell.getStringCellValue().trim();
                return cellValue.isEmpty() ? "" : cellValue;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return DATE_FORMAT.format(date);
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
