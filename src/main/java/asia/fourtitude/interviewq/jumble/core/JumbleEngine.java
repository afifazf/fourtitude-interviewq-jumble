package asia.fourtitude.interviewq.jumble.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class JumbleEngine {

    private static final String FILE_NAME = "words.txt";
    
    Logger logger = LoggerFactory.getLogger(JumbleEngine.class);

    /**
     * From the input `word`, produces/generates a copy which has the same
     * letters, but in different ordering.
     *
     * Example: from "elephant" to "aeehlnpt".
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#scramble()
     * b) scrambled letters/output must not be the same as input
     *
     * @param word  The input word to scramble the letters.
     * @return  The scrambled output/letters.
     */
    public String scramble(String word) {
        List<String> wordCharacter = Arrays.asList(word.split(StringUtils.EMPTY));

        Collections.shuffle(wordCharacter);

        StringBuilder scrambleWord = new StringBuilder();

        wordCharacter.forEach(scrambleWord::append);

        return scrambleWord.toString();
    }

    /**
     * Retrieves the palindrome words from the internal
     * word list/dictionary ("src/main/resources/words.txt").
     *
     * Word of single letter is not considered as valid palindrome word.
     *
     * Examples: "eye", "deed", "level".
     *
     * Evaluation/Grading:
     * a) able to access/use resource from classpath
     * b) using inbuilt Collections
     * c) using "try-with-resources" functionality/statement
     * d) pass unit test: JumbleEngineTest#palindrome()
     *
     * @return  The list of palindrome words found in system/engine. 
     * @see https://www.google.com/search?q=palindrome+meaning
     */
    public Collection<String> retrievePalindromeWords() {
        List<String> palindromeWords = new ArrayList<>();

        try(InputStream inputStream = new ClassPathResource(FILE_NAME).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {

            Stream<String> wordReader = reader.lines();  

            palindromeWords = wordReader.filter(word -> {

                if(word.length() < 2) 
                    return false;

                int midlePoint = word.length() / 2;

                StringBuilder sb = new StringBuilder(word.substring(midlePoint));

                String leftWord = word.length() % 2 != 0 ? word.substring(0, midlePoint + 1)
                        : word.substring(0, midlePoint);

                return leftWord.equals(sb.reverse().toString());

            }).collect(Collectors.toList());


        } catch (IOException e) {
            logger.error("Error read words.txt");
        }

        return palindromeWords;
    }

    /**
     * Picks one word randomly from internal word list.
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#randomWord()
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param length  The word picked, must of length.
     * @return  One of the word (randomly) from word list.
     *          Or null if none matching.
     */
    public String pickOneRandomWord(Integer length) {
        String randomWords = null;

        if(length == null)
            return StringUtils.EMPTY;

        try(InputStream inputStream = new ClassPathResource(FILE_NAME).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {

            Stream<String> wordReader = reader.lines();  

            List<String> wordMatchLength = wordReader.filter(word -> word.length() == length)
                    .collect(Collectors.toList());

            if(!wordMatchLength.isEmpty()) {
                int randomIndex = (int) Math.round(Math.random() * (wordMatchLength.size() - 1));

                randomWords = wordMatchLength.get(randomIndex);
            }
        } catch (IOException e) {
            logger.error("Error read words.txt");
        }

        return randomWords;
    }

    /**
     * Checks if the `word` exists in internal word list.
     * Matching is case insensitive.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word  The input word to check.
     * @return  true if `word` exists in internal word list.
     */
    public boolean exists(String word) {
        boolean found = false;

        try(InputStream inputStream = new ClassPathResource(FILE_NAME).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {

            Stream<String> wordReader = reader.lines();  

            found = StringUtils.isNotBlank(wordReader.filter(searchWord -> searchWord.equalsIgnoreCase(word))
                    .findFirst()
                    .orElse(null));

        } catch (IOException e) {
            logger.error("Error read words.txt");
        }

        return found;
    }

    /**
     * Finds all the words from internal word list which begins with the
     * input `prefix`.
     * Matching is case insensitive.
     *
     * Invalid `prefix` (null, empty string, blank string, non letter) will
     * return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param prefix  The prefix to match.
     * @return  The list of words matching the prefix.
     */
    public Collection<String> wordsMatchingPrefix(String prefix) {
        List<String> wordsMatchingPrefix = new ArrayList<>();

        if(StringUtils.isBlank(prefix) || !prefix.matches("[a-zA-Z]*")) 
            return wordsMatchingPrefix; 

        try(InputStream inputStream = new ClassPathResource(FILE_NAME).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {

            Stream<String> wordReader = reader.lines();  

            wordsMatchingPrefix = wordReader.filter(word -> word.toLowerCase().startsWith(prefix.toLowerCase()          
            )).collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Error read words.txt");
        }

        return wordsMatchingPrefix;
    }

    /**
     * Finds all the words from internal word list that is matching
     * the searching criteria.
     *
     * `startChar` and `endChar` must be 'a' to 'z' only. And case insensitive.
     * `length`, if have value, must be positive integer (>= 1).
     *
     * Words are filtered using `startChar` and `endChar` first.
     * Then apply `length` on the result, to produce the final output.
     *
     * Must have at least one valid value out of 3 inputs
     * (`startChar`, `endChar`, `length`) to proceed with searching.
     * Otherwise, return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param startChar  The first character of the word to search for.
     * @param endChar    The last character of the word to match with.
     * @param length     The length of the word to match.
     * @return  The list of words matching the searching criteria.
     */
    public Collection<String> searchWords(Character startChar, Character endChar, Integer length) {
        List<String> wordsList = new ArrayList<>();

        if(startChar == null && endChar == null && length == null)
            return wordsList;

        if((startChar != null && !Character.isLetter(startChar)) ||
                (endChar != null && !Character.isLetter(endChar)) ||
                (length != null && length < 1))
            return wordsList; 

        try(InputStream inputStream = new ClassPathResource(FILE_NAME).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {

            Stream<String> wordReader = reader.lines();  

            if(startChar != null)
                wordReader = wordReader.filter(word -> word.toLowerCase().startsWith(startChar.toString().toLowerCase()));

            if(endChar != null)
                wordReader = wordReader.filter(word -> word.toLowerCase().endsWith(endChar.toString().toLowerCase()));

            if(length != null)
                wordReader = wordReader.filter(word -> word.length() == length);

            wordsList = wordReader.collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Error read words.txt");
        }

        return wordsList;
    }

    /**
     * Generates all possible combinations of smaller/sub words using the
     * letters from input word.
     *
     * The `minLength` set the minimum length of sub word that is considered
     * as acceptable word.
     *
     * If length of input `word` is less than `minLength`, then return empty list.
     *
     * Example: From "yellow" and `minLength` = 3, the output sub words:
     *     low, lowly, lye, ole, owe, owl, well, welly, woe, yell, yeow, yew, yowl
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word       The input word to use as base/seed.
     * @param minLength  The minimum length (inclusive) of sub words.
     *                   Expects positive integer.
     *                   Default is 3.
     * @return  The list of sub words constructed from input `word`.
     */
    public Collection<String> generateSubWords(String word, Integer minLength) {
        List<String> wordsList = new ArrayList<>();
        
        final int length = minLength != null ? minLength : 3;

        if(StringUtils.isBlank(word) || word.length() < length || length < 1)
            return wordsList;

        try(InputStream inputStream = new ClassPathResource(FILE_NAME).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {

            Stream<String> wordReader = reader.lines();  

            wordsList = wordReader.filter(searchWord -> {

                if(searchWord.length() < length || searchWord.equalsIgnoreCase(word))
                    return false;

                StringBuilder wordCopy = new StringBuilder(word);

                List<String> searchCharacterList = Arrays.asList(searchWord.split(StringUtils.EMPTY));

                boolean isMatch = true;
                for(String searchCharacter: searchCharacterList) {
                    int findIndex = wordCopy.indexOf(searchCharacter);
                    
                    if(findIndex == -1) {
                        isMatch = false;
                        break;
                    }

                    wordCopy.deleteCharAt(findIndex);
                }
                
                return isMatch;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error read words.txt");
        }

        return wordsList;
    }

    /**
     * Creates a game state with word to guess, scrambled letters, and
     * possible combinations of words.
     *
     * Word is of length 6 characters.
     * The minimum length of sub words is of length 3 characters.
     *
     * @param length     The length of selected word.
     *                   Expects >= 3.
     * @param minLength  The minimum length (inclusive) of sub words.
     *                   Expects positive integer.
     *                   Default is 3.
     * @return  The game state.
     */
    public GameState createGameState(Integer length, Integer minLength) {
        Objects.requireNonNull(length, "length must not be null");
        if (minLength == null) {
            minLength = 3;
        } else if (minLength <= 0) {
            throw new IllegalArgumentException("Invalid minLength=[" + minLength + "], expect positive integer");
        }
        if (length < 3) {
            throw new IllegalArgumentException("Invalid length=[" + length + "], expect greater than or equals 3");
        }
        if (minLength > length) {
            throw new IllegalArgumentException("Expect minLength=[" + minLength + "] greater than length=[" + length + "]");
        }
        String original = this.pickOneRandomWord(length);
        if (original == null) {
            throw new IllegalArgumentException("Cannot find valid word to create game state");
        }
        String scramble = this.scramble(original);
        Map<String, Boolean> subWords = new TreeMap<>();
        for (String subWord : this.generateSubWords(original, minLength)) {
            subWords.put(subWord, Boolean.FALSE);
        }
        return new GameState(original, scramble, subWords);
    }

}
