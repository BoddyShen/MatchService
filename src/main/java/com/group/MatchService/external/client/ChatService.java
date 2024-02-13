package com.group.MatchService.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;

@FeignClient(name = "chat", url = "http://chat-service-svc")
public interface ChatService {

    @PostMapping("/api/v1/conversation/create-conversation-service")
    Void createConversation(@RequestBody List<String> userIds);
}
