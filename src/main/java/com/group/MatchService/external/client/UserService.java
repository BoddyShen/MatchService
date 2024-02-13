package com.group.MatchService.external.client;

import com.group.MatchService.model.MatchCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account", url = "http://account-service-svc")
public interface UserService {

    @PostMapping("/api/v1/users/match")
    void createMatch(@RequestBody MatchCreateRequest request);

}
