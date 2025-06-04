package com.manpilogoff.dto;

public class DownloadResponse extends AbstractResponse {
    private final TrackData trackData;

    public DownloadResponse(String status, TrackData trackData) {
        super(status);
        this.trackData = trackData;
    }


}
