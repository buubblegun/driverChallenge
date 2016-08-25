
#Introduction:

To sequence the DNA for a given individual we typically fragment each chromosome to many small pieces that can be sequenced in parallel and then re-assemble the sequenced fragments into one long DNA sequence. In this task we ask that you take on a specific subtask of this process.

#Challenge:

The input to the problem is at most 50 DNA sequences (i.e, the character set is limited to T/C/G/A) whose length does not exceed 1000 characters. The sequences are given in FASTA format (https://en.wikipedia.org/wiki/FASTA_format). These sequences are all different fragments of one chromosome.

The specific set of sequences you will get satisfy a very unique property:  there exists a unique way to reconstruct the entire chromosome from these reads by gluing together pairs of reads that overlap by more than half their length. An example set of input strings is attached.

The output of your program should be this unique sequence that contains each of the given input strings as a substring.

In addition to the code you wrote, we also ask for a README describing your general approach as well as any additional code you wrote to evaluate your solution. We would prefer your code to be written in Python, Go, Scala, Javascript, or Java.

 

#Example input:

>Frag_56
ATTAGACCTG

>Frag_57
CCTGCCGGAA

>Frag_58
AGACCTGCCG

>Frag_59
GCCGGAATAC

Example output:

ATTAGACCTGCCGGAATAC
























# Driver Coding Challenge!

Here I will outline some of my thought process and possible problems we may encounter throughout the exercise. Some of them are trivial assumptions while others are made to greatly simply the code at the cost of failure in some extreme circumstances.

### Assumptions
> A number of assumptions are made when going through this exercise. I'll address them in these boxes.

# Code flow
### Overview
```
public static void main(String[] args) {
  try {
  Map<String, Segment> dna = parseDataSet(FILENAME);
  calculateOverlaps(dna);
    String fullSequence = mergeSequences(dna);
    System.out.println(fullSequence);
    System.out.println("length: " + fullSequence.length());
    // Check to see full sequence contains every segment
} catch (Exception e) {
    e.printStackTrace();
    System.exit(-1);
}
```
The idea is simple, we parse the dataset, figure out the order of the segments by comparing their overlaps (using a variation of [KMP algorithm] (https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm), then combining all the fragments together in a single string. 

```
// KMP algo
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
```
Normally, a string comparison like our case (compare -> shift -> compare -> etc) would take quadratic time, but KMP algo allows us to do each segment compare in linear time by some clever use of the information gathered from previous (failed) comparisons.

```
private static class Segment {
        private String name;
        private String sequence;
        private String forwardMatch;
        private String backwardMatch;
        private int forwardMatchOverlap;
        private int backwardMatchOverlap;
}
```
I utilize a **Segment** class throughout the code for better/cleaner keeping track of the the segment's name, its sequence, and the segment that follows/precedes it, as well as the length of the overlap with the following/preceding segment. Some of the fields are not used but I've included them anyway for consistency. It may also be useful if we want to do anything else with the **Segment** class in the future.

I used a *Map* to keep track of the segment because we will often be looking for a specific segment either for calculating overlaps or merging segments, and this will make that operation as fast as Michael Phelps.

Any exceptions thrown throughout the code gets caught in main(), halts execution, prints error, and exits gracefully.

### parseDataSet(String filename)
```
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
```
This is pretty simple. Given the location of the file, we loop through each block consisting of a name and some lines of segments.We loop through the multi-lined segments and join the fragments together, and save it in a map<String, Segment> called *dna* that maps segment name -> segment object.

StringBuilder was used instead of String to leverage its efficiency when the string is repeatedly modified (when we loop through and append all the pieces of the sequence).

> The content of the file is valid and consistent with the format given
It might be necessary to validate or sanitize the file that's passed in, but here I make the assumption that we will get a file containing the segment name, followed by the segments, and repeat. Also part of the assumption is the fact that segment names are unique within the file, and the segments only contain valid nucleotide representations.

### calculateOverlaps(Map<String, segment> dna)
```
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
```
We compare every pair of segments using nested loops in a quadratic execution. using KMP we compare the tail of segment1 to the head of segment2 and calculate the length of the greatest overlap. If the overlap is greater than the **greater of the half of the length of the two segments** (because we're looking for >50% match), the result is stored by saving segment1's forwardMatch as segment2, and segment2's backwardMatch as segment1. 

The FowardMatchOverlap/BackwardMatchOverlap is the length of the overlap. This is used later for joining the segments.

> There only exists one match meeting the >50% criteria between two sequences in the data set
This might be an egregious assumption, but if inputs are similar to the given test cases, having ~500+ of nucleotides matching multiple segments should be rare...if the segments are randomly generated. If instead they were generated by, say, a DNA splicer, this might be much more likely and the problem would be a more complicated and time consuming one. It becomes s a "sudoku" like problem in that we may need to explore different sequence match cases and see which ultimately produces the full sequence.

> A follow up assumption that there does not exist a redundant segment that is overlapped by the surrounding segments
For example:
CCCGG
CGGAA
CGGAATT
Segment 2 in our example is redundant, and if such case existed, we may end up in a weird execution state. The way to handle this would be to tweak the KMP algo to return the smallest >50% match length, so segment 1 -> segment 2, segment 2 -> segment 3.

### mergeSequences(Map<String, segment> dna)
Now that our dna map is fully populated, we can parse it together to formulate the full sequence. 

```
private static Segment getHeader(Map<String, Segment> dna) throws Exception {
    for (String name : dna.keySet()) {
        Segment seg = dna.get(name);
        if (seg.getBackwardMatch() == null) {
            return seg;
        }
    }
    throw new Exception("No header found");
}
```
getHeader scans all the segments and returns the one that does not have a backwardMatch, which can only mean it is the first fragment of our final sequence.

```
private static String mergeSequences(Map<String, Segment> dna) throws Exception {
    Segment curSegment = getHeader(dna);
    StringBuilder out = new StringBuilder(curSegment.getSequence());

    while (curSegment.getForwardMatch() != null) {
        curSegment = dna.get(curSegment.getForwardMatch());
        String appendingSegment = curSegment.getSequence().substring(curSegment.backwardMatchOverlap);
        out.append(appendingSegment);
    }
    return out.toString();
}
```
The loop works as follows:
Starting from the first fragment,
- get the forwardMatch
- remove the "matching part" of the forwardMatch sequence (we know how many nucleotides are matching from forward/backwardMatchOverlap)
- append it to the full sequence
We again use StringBuilder for its nifty advantage over String.

### testing
```
int i = 0;
Set<Integer> nucleotides = new HashSet<>();
for (String name : dna.keySet()) {
    Segment seg = dna.get(name);
    int match = fullSequence.indexOf(seg.getSequence());
    if (match != -1) {
        for (int j = 0; j < seg.getSequence().length(); j++) {
            nucleotides.add(match + j);
        }
        System.out.println(name + "[" + ++i + "] detected");
    }
}
System.out.println("length: " + nucleotides.size())
```
Our full sequence is correct **if and only if** it comprises of the individual segments
- Full sequence contains segments
This is an easy check, we loop through every segment and see that it's contained in the full sequence
- Full sequence only contains segments
A little trickier. We take note of the index of every nucleotide of each segment in the full sequence and store it in a set. By the time we get to the end, the set should only contain indexes 0 through (length of full sequence)-1, meaning it's size is equal to the length of the full sequence.
