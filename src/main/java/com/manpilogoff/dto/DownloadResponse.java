package com.manpilogoff.dto;

public class DownloadResp extends AbstractResponse {
    private final String trackName;

    public DownloadResp(String status, String trackName) {
        super(status);
        this.trackName = trackName;
    }

    public String getTrackName() {
        return trackName;
    }
}
