package com.group.MatchService.documentation;

import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.group.MatchService.model.Match;

import java.util.List;
import java.util.Map;

@Tag(name = "Match API")
public interface MatchApi {
    @Operation(summary = "Get all matches")
    public ResponseEntity<List<Match>> getAllMatches();

    @Operation(summary = "Get all successful matches")
    public ResponseEntity<List<Match>> getAllSuccessMatches();

    @Operation(summary = "get a match by user ids")
    public ResponseEntity<?> getMatchByUserIds(String userId1, String userId2);

    @Operation(summary = "add match history")
    public ResponseEntity<?> updateMatchHistory(@RequestBody(description = "add Match history payload", required = true, 
        content = @Content(schema = @Schema(implementation = AddMatchHistoryPayload.class))
        )Map<String, Object> payload);

    class AddMatchHistoryPayload {
        @Schema(description = "people who initiate the action", example = "655d3d90fee7474f900da830")
        public String senderId;
        @Schema(description = "people receive the action", example = "655d3d90fee7474f900da830")
        public String receiverId;
        @Schema(description = "0 means reject, 1 means accept", example = "0")
        public Integer behavior;
    }

    class GetMatchByUserIdsPayload {
        @Schema(description = "user ids", example = "[\"user_id1\", \"user_id2\"]")
        public List<String> userIds;
    }
}
