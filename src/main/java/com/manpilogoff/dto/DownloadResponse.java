package com.manpilogoff.dto;

public class DownloadResponse extends AbstractResponse {
    private final String trackName;

    public DownloadResponse(String status, String trackName) {
        super(status);
        this.trackName = trackName;
    }

    public String getTrackName() {
        return trackName;
    }
}
