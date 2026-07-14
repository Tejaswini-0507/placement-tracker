package com.example.placement_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollResponse {

    private UUID threadId;
    private Integer newMessageCount;
    private List<DiscussionMessageResponse> newMessages;
    private Long pollTimeStamp;
    private Boolean hasUpdates;

}
