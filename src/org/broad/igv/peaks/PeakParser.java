/*
 * Copyright (c) 2007-2011 by The Broad Institute, Inc. and the Massachusetts Institute of
 * Technology.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.peaks;

import org.broad.igv.feature.AbstractFeatureParser;
import org.broad.igv.feature.IGVFeature;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.util.ParsingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jrobinso
 * @date Apr 22, 2011
 */
public class PeakParser {

    private int nTimePoints;
    private String [] timePointHeadings;
    private TrackProperties trackProperties;

    public List<Peak> loadPeaks(String path) throws IOException {

        List<Peak> peaks = new ArrayList(25000);
        BufferedReader reader = null;

        try {
            reader = ParsingUtils.openBufferedReader(path);
            String nextLine;

            // Skip comments and read header line.  Time points start after line
            while ((nextLine = reader.readLine()) != null) {
                if (nextLine.startsWith("#")) {

                    if(nextLine.startsWith("#track")) {
                        trackProperties = new TrackProperties();
                        ParsingUtils.parseTrackLine(nextLine, trackProperties);
                    }

                    continue;
                }
                String [] tokens = nextLine.split("\t");
                if(tokens.length < 7) {
                    throw new RuntimeException("Not enough columns for peak file. At least 2 time points are required");
                }
                nTimePoints = tokens.length - 5;
                timePointHeadings = new String[nTimePoints];
                break;
            }

            // Now parse the data
            while ((nextLine = reader.readLine()) != null) {

                String [] tokens = nextLine.split("\t");
                String chr = tokens[0];
                int start = Integer.parseInt(tokens[1]);
                int end = Integer.parseInt(tokens[2]);
                String name = tokens[3];
                float combinedScore = Float.parseFloat(tokens[4]);
                float [] timePointScores = new float[nTimePoints];
                for(int i=0; i<nTimePoints; i++) {
                    timePointScores[i] = Float.parseFloat(tokens[5 + i]);
                }
                peaks.add(new Peak(chr, start, end, name, combinedScore, timePointScores));
            }

            return peaks;

        }
        finally {
            if (reader != null) reader.close();
        }

    }

    public int getnTimePoints() {
        return nTimePoints;
    }

    public String[] getTimePointHeadings() {
        return timePointHeadings;
    }

    public TrackProperties getTrackProperties() {
        return trackProperties;
    }
}
