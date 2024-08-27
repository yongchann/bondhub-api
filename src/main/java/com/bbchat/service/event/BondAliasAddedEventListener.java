package com.bbchat.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BondAliasAddedEventListener {

    @EventListener
    public void handleBondAliasAddedEvent(BondAliasAddedEvent event) {

    }
}
