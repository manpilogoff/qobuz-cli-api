package com.manpilogoff.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResponse extends AbstractResponse {
    private final String requestedParam;
    private final List<TrackData> tracks;
    private final int count;

    public SearchResponse(String status, String requestedParam, List<TrackData> tracks, int count) {
        super(status);
        this.requestedParam = requestedParam;
        this.tracks = tracks;
        this.count = count;
    }
}
