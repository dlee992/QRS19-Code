# From CC1.2 to CC2.1

## Name reference
1. CC1s : CC1 stable, that is, CC1.1
2. CC1.2: CC1s with concurrency support
3. CC2.1: CC1s with optimization

## Optimization principles
1. Can easily enable and un-enable the optimization options.
2. Try to separate the extra code with previous code. (such as, using a single class)

### Optimal 0. Add concurrent function
1. I add the simple multi-thread function into CC1s, by using java.util.ExecutorService class and its thread pool framework. 
1. Synchronized method is addIfNotNull in statistical class, and concurrent granularity is *workbook* instead of sheet.
2. When running the multi-thread program (I use 8 sub-threads in the thread pool), the whole process will consume 26 minutes. 68 of 70 spreadsheets were finished in 9 minutes and the CPU seizure rate was nearly 100% (about 98%).
3. **Bug No.1** But the rest 2 spreadsheets will consumed about 25 minutes from the very beginning to the ending (after 10 mins, the CPU seizure rate dropped to 50% and lower). I checked the consumed time in single-thread situation, these two spreadsheets consumed 3.7 and 3.9 minutes, respectively. 

### Optimal 1. Skip string-type formula cells during pre-processing
   1. Add Class *GlobalParameters*, which is used to declare a variety of global paras.
   2. Change the code of Class *BasicUtility* started from *Line 36*.
   
### Optimal 2. Use strong coverage between the first-stage clustering and weak feature extraction
   1. (*narrowly*) If the cluster contains only one cell and satisfies strong coverage condition, extend it.
   2. (*generally*) If the cluster satisfies strong coverage condition, extend it.
   
### Optimal 3. Use weak coverage before the harvest method
   1. such that narrow down the *irrelevant numeric* cells.

### Optimal 4. tackle with special(*common*) header, which contains 'total'/'gross' substring
   *O4.1*: create a new feature of the special header, the question how many attributes should be contained in needs to be discussed.
   *O4.2*: update the matrix weight of the special header.
   

### Optimal 5. two-layer headers