package com.manpilogoff.service;

import com.manpilogoff.dto.TrackData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AudioService {
    private final Logger log = LoggerFactory.getLogger(AudioService.class);
    private final TmuxService tmuxService;
    private final ParseService parseService;

    public AudioService(TmuxService tmuxService, ParseService parseService) {
        this.tmuxService = tmuxService;
        this.parseService = parseService;
    }

    public TrackData download(String id) {
       tmuxService.tmuxDownload("",  id);
        return null;
    }

    public List <TrackData> search(String query) {
        tmuxService.tmuxSearch(query,"track");
        List<TrackData> tracks = new ArrayList<>();

        for(int i = 0; i < 29; i++) {
            String output = tmuxService.captureTmuxOutput();
            TrackData track = parseService.parseActiveTrackAndId(output);

            tracks.add(track);
        }
        return tracks;
    }



}
