package com.bondhub.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ChatJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private int batchSize = 1000;

    public void saveAll(List<Chat> chats) {
        int batchCount = 0;
        List<Chat> bulkChats = new ArrayList<>();
        for (int i = 0; i < chats.size(); i++) {
            bulkChats.add(chats.get(i));

            if ((i + 1) % batchSize == 0) {
                batchCount = insertInBatch(batchCount, bulkChats);
            }
        }
        if (!bulkChats.isEmpty()) {
            insertInBatch(batchCount, bulkChats);
        }
    }

    private int insertInBatch(int batchCount, List<Chat> bulkChats) {
        String sql = "INSERT INTO chat (" +
                "chat_date_time, " +
                "content, " +
                "yield, " +
                "trade_type, " +
                "status, " +
                "bond_type, " +
                "bond_issuer_id, " +
                "maturity_date, " +
                "trigger_keyword, " +
                "sender_name, " +
                "sender_address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Chat chat = bulkChats.get(i);
                ps.setTimestamp(1, Timestamp.valueOf(chat.getChatDateTime()));
                ps.setString(2, chat.getContent());
                ps.setString(3, chat.getYield());
                ps.setString(4, chat.getTradeType().name());
                ps.setString(5, chat.getStatus().name());

                if (chat.getBondType() != null) {
                    ps.setString(6, chat.getBondType().name());
                } else {
                    ps.setNull(6, Types.VARCHAR);
                }

                if (chat.getBondIssuer() != null && chat.getBondIssuer().getId() != null) {
                    ps.setLong(7, chat.getBondIssuer().getId());
                } else {
                    ps.setNull(7, Types.BIGINT);
                }


                ps.setString(8, chat.getMaturityDate());
                ps.setString(9, chat.getTriggerKeyword());
                ps.setString(10, chat.getSenderName());
                ps.setString(11, chat.getSenderAddress());
            }

            @Override
            public int getBatchSize() {
                return bulkChats.size();
            }
        });

        bulkChats.clear();
        batchCount++;
        return batchCount;
    }
}
