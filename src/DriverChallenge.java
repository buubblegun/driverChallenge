import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DriverChallenge {
    private static final String FILENAME = "src/coding_challenge_data_set.txt";

    public static void main(String[] args) {
        try {
            Map<String, Segment> dna = parseDataSet(FILENAME);
            calculateOverlaps(dna);
            String fullSequence =  mergeSequences(dna);
            System.out.println(fullSequence);
            System.out.println("length: " + fullSequence.length());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Header is the segment that doesn't have a backwardMatch
    private static Segment getHeader(Map<String, Segment> dna) throws Exception {
        for (String name : dna.keySet()) {
            Segment seg = dna.get(name);
            if (seg.getBackwardMatch() == null) {
                return seg;
            }
        }
        throw new Exception("No header found");
    }

    // Variation of Knuth Morris Pratt algorithm for calculating string overlap in linear time
    private static int overlappedStringLength(String s1, String s2) {
        if (s1.length() > s2.length()) {
            s1 = s1.substring(s1.length() - s2.length());
        }
        int[] T = computeBackTrackTable(s2); //O(n)
        int m = 0;
        int i = 0;
        while (m + i < s1.length()) {
            if (s2.charAt(i) == s1.charAt(m + i)) {
                i += 1;
            } else {
                m += i - T[i];
                if (i > 0) {
                    i = T[i];
                }
            }
        }
        return i;
    }

    // Match table for KMP algo
    private static int[] computeBackTrackTable(String s) {
        int[] T = new int[s.length()];
        int cnd = 0;
        T[0] = -1;
        T[1] = 0;
        int pos = 2;
        while (pos < s.length()) {
            if (s.charAt(pos - 1) == s.charAt(cnd)) {
                T[pos] = cnd + 1;
                pos += 1;
                cnd += 1;
            } else if (cnd > 0) {
                cnd = T[cnd];
            } else {
                T[pos] = 0;
                pos += 1;
            }
        }
        return T;
    }

    private static void calculateOverlaps(Map<String, Segment> dna) {
        // Compare every pair of segment, looking for overlaps
        dna.forEach((name1, seg1) -> {
            dna.forEach((name2, seg2) -> {
                if (!name1.equals(name2)) {
                    int overlapLength = overlappedStringLength(seg1.getSequence(), seg2.getSequence());
                    // If the overlap is more than half of the larger sequence, we assume assume it's a match
                    // See README for special cases
                    double overlapThreshold = Math.max(Math.ceil(seg1.getSequence().length() / 2), Math.ceil(seg2.getSequence().length() / 2));
                    if (overlapLength > overlapThreshold) {
//                        System.out.println(seg1.getName() + "->" + seg2.getName());
                        // We don't actually need to set all of these but I did anyway becomes finesse
                        seg1.setForwardMatch(seg2.getName());
                        seg2.setBackwardMatch(seg1.getName());
                        seg1.setForwardMatchOverlap(overlapLength);
                        seg2.setBackwardMatchOverlap(overlapLength);
                    }
                }
            });
        });
    }

    // Merge all the sequences by appending segments based on the calculated forwardMatch
    private static String mergeSequences(Map<String, Segment> dna) throws Exception {
        Segment curSegment = getHeader(dna);
        StringBuilder out = new StringBuilder(curSegment.getSequence());

        while (curSegment.getForwardMatch() != null) {
            curSegment = dna.get(curSegment.getForwardMatch());
            String appendingSegment = curSegment.getSequence().substring(curSegment.backwardMatchOverlap);
            out.append(appendingSegment);
        }

        // Check to see full sequence contains every segment
//        int i = 0;
//        for (String name : dna.keySet()) {
//            Segment seg = dna.get(name);
//            if (out.toString().contains(seg.getSequence())) {
//                System.out.println(name + "[" + ++i + "] detected");
//            }
//        }
        return out.toString();
    }

    // Parse segment name from dataset, characterized by the '>' character followed by String
    private static Map<String, Segment> parseDataSet(String filename) throws FileNotFoundException {
        Map<String, Segment> dna = new HashMap<>();
        Scanner scan = new Scanner(new File(filename));
        while (scan.hasNext(">.*")) {
            String name = scan.nextLine();
            StringBuilder sequence = new StringBuilder();
            // Build sequence string until we arrive to next segment or eof
            while (scan.hasNext() && !scan.hasNext(">.*")) {
                sequence.append(scan.nextLine());
            }
            // Storing data as a map of segment name -> segment obj
            dna.put(name, new Segment(name, sequence.toString()));
        }
        return dna;
    }

    private static class Segment {
        private String name;
        private String sequence;
        private String forwardMatch;
        private String backwardMatch;
        private int forwardMatchOverlap;
        private int backwardMatchOverlap;

        Segment(String name, String sequence) {
            this.name = name;
            this.sequence = sequence;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getForwardMatch() {
            return forwardMatch;
        }

        public void setForwardMatch(String forwardMatch) {
            this.forwardMatch = forwardMatch;
        }

        public String getBackwardMatch() {
            return backwardMatch;
        }

        public void setBackwardMatch(String backwardMatch) {
            this.backwardMatch = backwardMatch;
        }

        public int getForwardMatchOverlap() {
            return forwardMatchOverlap;
        }

        public void setForwardMatchOverlap(int forwardMatchOverlap) {
            this.forwardMatchOverlap = forwardMatchOverlap;
        }

        public int getBackwardMatchOverlap() {
            return backwardMatchOverlap;
        }

        public void setBackwardMatchOverlap(int backwardMatchOverlap) {
            this.backwardMatchOverlap = backwardMatchOverlap;
        }
    }
}