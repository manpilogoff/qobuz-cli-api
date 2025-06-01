package com.manpilogoff.service;

import com.manpilogoff.dto.TrackData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseService {
    private final Logger log = LoggerFactory.getLogger(ParseService.class);

    public TrackData parseActiveTrackAndId(String output) {
        Pattern trackPattern = Pattern.compile("^>\\s*\\[\\s*]\\s*\\d+\\.\\s*(.*?) by (.+)$", Pattern.MULTILINE);
        Pattern idPattern = Pattern.compile("ID:\\s*(\\d+)");

        Matcher m = trackPattern.matcher(output);
        Matcher idMatcher = idPattern.matcher(output);

        String title = null;
        String artist = null;
        String id = null;

        if (m.find()) {
            title = m.group(1).trim();
            artist = m.group(2).trim();
        }

        if (idMatcher.find()) id = idMatcher.group(1);

        return new TrackData(title, artist, id);
    }

    public TrackData parseDownloadTrackInfo(String output) {
        return null;
    }

}
