package com.bbchat.domain.entity;

import com.bbchat.domain.ask.DailyAsk;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    private String chatDate;

    private String senderName;

    private String sendDateTime;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_ask_id")
    private DailyAsk dailyAsk;

    private String dueDate;

    private String content;

    private String senderAddress;

    public void modifyStatusByDueDate(List<String> dueDateInContent) {
        if (dueDateInContent.isEmpty()) {
            status = ChatStatus.NOT_USED;
            dueDate = "";
        } else if (dueDateInContent.size() == 1) {
            status = ChatStatus.SINGLE_DD;
            dueDate = dueDateInContent.get(0);
        } else {
            status = ChatStatus.MULTI_DD;
            dueDate = "";
        }
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public void setDailyAsk(DailyAsk dailyAsk) {
        this.dailyAsk = dailyAsk;
    }
}
