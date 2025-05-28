package com.manpilogoff.dto;

import java.util.List;

public class SearchResponse extends AbstractResponse {
    private final String requestedParam;
    private final List<String> tracks;
    private final int count;

    public SearchResponse(String status, String requestedParam, List<String> tracks, int count) {
        super(status);
        this.requestedParam = requestedParam;
        this.tracks = tracks;
        this.count = count;
    }

    public String getRequestedParam() {
        return requestedParam;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public int getCount() {
        return count;
    }
}
