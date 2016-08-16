# Driver Coding Challenge!

Here I will outline some of my thought process and possible problems we may encounter throughout the exercise. Some of them are trivial assumptions while others are made to greatly simply the code at the cost of failure in some extreme circumstances.

994


### Assumptions
> A number of assumptions are made when going through this exercise. I'll address them in these boxes.

# Code flow
### Overview
The idea is simple, we parse the dataset, figure out the order of the segments by comparing their overlaps (using a variation of [KMP algorithm] (https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm), then combining all the fragments together in a single string. 

Normally, a string comparison like our case (compare -> shift -> compare -> etc) would take quadratic time, but KMP algo allows us to do each segment compare in linear time by some clever use of the information gathered from previous (failed) comparisons.

I utilize a **Segment** class throughout the code for better/cleaner keeping track of the the segment's name, its sequence, and the segment that follows/precedes it, as well as the length of the overlap with the following/preceding segment. Some of the fields are not used but I've included them anyway for consistency. It may also be useful if we want to do anything else with the **Segment** class in the future.

I used a *Map* to keep track of the segment because we will often be looking for a specific segment either for calculating overlaps or merging segments, and this will make that operation as fast as Michael Phelps.

Any exceptions thrown throughout the code gets caught in main(), halts execution, prints error, and exits gracefully.

### parseDataSet(String filename)
This is pretty simple. Given the location of the file, we loop through each block consisting of a name and some lines of segments.We loop through the multi-lined segments and join the fragments together, and save it in a map<String, Segment> called *dna* that maps segment name -> segment object.

StringBuilder was used instead of String to leverage its efficiency when the string is repeatedly modified (when we loop through and append all the pieces of the sequence).

> The content of the file is valid and consistent with the format given
It might be necessary to validate or sanitize the file that's passed in, but here I make the assumption that we will get a file containing the segment name, followed by the segments, and repeat. Also part of the assumption is the fact that segment names are unique within the file, and the segments only contain valid nucleotide representations.

### calculateOverlaps(Map<String, segment> dna)
We compare every pair of segments using nested loops in a quadratic execution. using KMP we compare the tail of segment1 to the head of segment2 and calculate the length of the greatest overlap. If the overlap is greater than the **greater of the half of the length of the two segments** (because we're looking for >50% match), the result is stored by saving segment1's forwardMatch as segment2, and segment2's backwardMatch as segment1. 

The FowardMatchOverlap/BackwardMatchOverlap is the length of the overlap. This is used later for joining the segments.

> There only exists one match meeting the >50% criteria between two sequences in the data set
This might be an egregious assumption, but if inputs are similar to the given test cases, having ~500+ of nucleotides matching multiple segments should be rare...if the segments are randomly generated. If instead they were generated by, say, a DNA splicer, this might be much more likely and the problem would be a more complicated and time consuming one. It becomes s a "sudoku" like problem in that we may need to explore different sequence match cases and see which ultimately produces the full sequence.

> A follow up assumption that there does not exist a redundant segment that is overlapped by the surrounding segments
For example:
CCCGG
CGGAA
CGGAATT
Segment 2 in our exmaple is reduandant, and if such case existed, we may end up in a weird execution state. The way to handle this would be to tweak the KMP algo to return the smallest >50% match length, so segment 1 -> segment 2, segment 2 -> segment 3.

### mergeSequences(Map<String, segment> dna)
Now that our dna map is fully populated, we can parse it together to formulate the full sequence. 

getHeader scans all the segments and returns the one that does not have a backwardMatch, which can only mean it is the first fragment of our final sequence.

The loop works as follows:
Starting from the first fragment,
- get the forwardMatch
- remove the "matching part" of the forwardMatch sequence (we know how many nucleotides are matching from forward/backwardMatchOverlap)
- append it to the full sequence
We again use StringBuilder for its nifty advantage over String.

### testing
