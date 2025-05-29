package com.manpilogoff.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResponse extends AbstractResponse {
    private final String requestedParam;
    private final List<TrackInfo> tracks;
    private final int count;

    public SearchResponse(String status, String requestedParam, List<TrackInfo> tracks, int count) {
        super(status);
        this.requestedParam = requestedParam;
        this.tracks = tracks;
        this.count = count;
    }
}
