package com.bondhub.service.analysis;

import com.bondhub.domain.transaction.Transaction;
import com.bondhub.domain.transaction.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionParser {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public List<Transaction> processTransactionFileInputStream(String date, InputStream inputStream) {
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

                transactions.add(rowToTransaction(date, row));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    private Transaction rowToTransaction(String date, Row row) {
        return Transaction.builder()
                .status(TransactionStatus.CREATED)
                .time(getStringCellValue(row, 0))
//                        .marketType(getStringCellValue(row, 1))
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
//                        .settlement(getStringCellValue(row, 12))
                        .transactionDate(date)
                .standardCode(getStringCellValue(row, 14))
                .maturityType(getStringCellValue(row, 15))
                .remainingMaturity(getStringCellValue(row, 16))
//                        .interestType(getStringCellValue(row, 17))
//                        .stockType(getStringCellValue(row, 18))
                .creditRating(getStringCellValue(row, 19))
//                        .spread4Bp(getStringCellValue(row, 20))
//                        .spread4Price(getStringCellValue(row, 21))
//                        .yield4(getStringCellValue(row, 22))
//                        .price4(getStringCellValue(row, 23))
                .issuerCode(getStringCellValue(row, 24))
                .issuerName(getStringCellValue(row, 25))
//                        .pre3Diff(getStringCellValue(row, 26))
//                        .pre3Price(getStringCellValue(row, 27))
//                        .pre3TDiff(getStringCellValue(row, 28))
//                        .pre3TPrice(getStringCellValue(row, 29))
//                        .pre4Diff(getStringCellValue(row, 30))
//                        .pre4Price(getStringCellValue(row, 31))
//                        .pre4TDiff(getStringCellValue(row, 32))
//                        .pre4TPrice(getStringCellValue(row, 33))
//                        .tradingNature(getStringCellValue(row, 34))
//                        .tradingType(getStringCellValue(row, 35))
//                        .publicOrPrivate(getStringCellValue(row, 36))
                .build();
    }
//
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
                    return isTimeOnly(date) ? TIME_FORMAT.format(date) : DATE_FORMAT.format(date);
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private boolean isTimeOnly(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) == 1899;
    }

}
