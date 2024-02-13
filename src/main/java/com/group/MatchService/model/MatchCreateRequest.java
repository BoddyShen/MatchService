package com.group.MatchService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCreateRequest {

    private String userId;
    private Match match;

}
