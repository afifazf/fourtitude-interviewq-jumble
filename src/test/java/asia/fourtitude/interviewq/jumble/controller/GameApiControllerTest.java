package asia.fourtitude.interviewq.jumble.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import asia.fourtitude.interviewq.jumble.TestConfig;
import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import asia.fourtitude.interviewq.jumble.model.GameGuessInput;
import asia.fourtitude.interviewq.jumble.model.GameGuessOutput;

@WebMvcTest(GameApiController.class)
@Import(TestConfig.class)
class GameApiControllerTest {

    static final ObjectMapper OM = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    JumbleEngine jumbleEngine;

    /*
     * NOTE: Refer to "RootControllerTest.java", "GameWebControllerTest.java"
     * as reference. Search internet for resource/tutorial/help in implementing
     * the unit tests.
     *
     * Refer to "http://localhost:8080/swagger-ui/index.html" for REST API
     * documentation and perform testing.
     *
     * Refer to Postman collection ("interviewq-jumble.postman_collection.json")
     * for REST API documentation and perform testing.
     */

    @Test
    void whenCreateNewGame_thenSuccess() throws Exception {
        /*
         * Doing HTTP GET "/api/game/new"
         *
         * Input: None
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Created new game."
         * c) `id` is not null
         * d) `originalWord` is not null
         * e) `scrambleWord` is not null
         * f) `totalWords` > 0
         * g) `remainingWords` > 0 and same as `totalWords`
         * h) `guessedWords` is empty list
         */
        MvcResult mvcResult = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();

        GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        assertEquals("Created new game.", output.getResult(), "result");
        assertNotNull(output.getId(), "id");
        assertNotNull(output.getOriginalWord(), "original-word");
        assertNotNull(output.getScrambleWord(), "scramble-word");
        assertTrue(output.getTotalWords() > 0, "total-words");
        assertTrue(output.getRemainingWords() > 0 
                && output.getRemainingWords() == output.getTotalWords(), "remaining-words");
        assertTrue(output.getGuessedWords().isEmpty(), "guessed-words");
    }

    @Test
    void givenMissingId_whenPlayGame_thenInvalidId() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Input: JSON request body
         * a) `id` is null or missing
         * b) `word` is null/anything or missing
         *
         * Expect: Assert these
         * a) HTTP status == 404
         * b) `result` equals "Invalid Game ID."
         */
        MvcResult mvcNewResult = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcNewResult.getRequest().getSession();

        GameGuessInput guessInput = new GameGuessInput();
        guessInput.setId("  ");
        guessInput.setWord("  ");

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult mvcResult = this.mvc.perform(post("/api/game/guess")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guessInput)))
                .andExpect(status().isNotFound())
                .andReturn();

        GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        assertEquals("Invalid Game ID.", output.getResult(), "result");
    }

    @Test
    void givenMissingRecord_whenPlayGame_thenRecordNotFound() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Input: JSON request body
         * a) `id` is some valid ID (but not exists in game system)
         * b) `word` is null/anything or missing
         *
         * Expect: Assert these
         * a) HTTP status == 404
         * b) `result` equals "Game board/state not found."
         */
        MvcResult mvcNewResult = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcNewResult.getRequest().getSession();

        GameGuessInput guessInput = new GameGuessInput();
        guessInput.setId("51eb70da-7e19-46eb-b45e-ab25e9b6c444");
        guessInput.setWord("  ");

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult mvcResult = this.mvc.perform(post("/api/game/guess")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guessInput)))
                .andExpect(status().isNotFound())
                .andReturn();

        GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        assertEquals("Game board/state not found.", output.getResult(), "result");
    }

    @Test
    void givenCreateNewGame_whenSubmiNullWord_thenGuessedIncorrectly() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is null or missing
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Guessed incorrectly."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` is equals to `input.word`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is equals to `remainingWords` of previous game state (no change)
         * i) `guessedWords` is empty list (because this is first attempt)
         */
        MvcResult mvcNewResult = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcNewResult.getRequest().getSession();

        ObjectMapper objectMapper = new ObjectMapper();

        GameGuessOutput newGameOutput = objectMapper.readValue(mvcNewResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        GameGuessInput guessInput = new GameGuessInput();
        guessInput.setId(newGameOutput.getId());
        guessInput.setWord("");

        MvcResult mvcResult = this.mvc.perform(post("/api/game/guess")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guessInput)))
                .andExpect(status().isOk())
                .andReturn();

        GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        assertEquals("Guessed incorrectly.", output.getResult(), "result");
        assertEquals(newGameOutput.getId(), output.getId(), "id");
        assertEquals(newGameOutput.getOriginalWord(), output.getOriginalWord(), "original-word");
        assertNotNull(output.getScrambleWord(), "scramble-word");
        assertEquals(guessInput.getWord(), output.getGuessWord(), "guess-word");
        assertEquals(newGameOutput.getTotalWords(), output.getTotalWords(), "total-words");
        assertEquals(newGameOutput.getRemainingWords(), output.getRemainingWords(), "total-words");
        assertTrue(output.getGuessedWords().isEmpty(), "guessed-words");
    }

    @Test
    void givenCreateNewGame_whenSubmitWrongWord_thenGuessedIncorrectly() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is some value (that is not correct answer)
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Guessed incorrectly."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` equals to input `guessWord`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is equals to `remainingWords` of previous game state (no change)
         * i) `guessedWords` is empty list (because this is first attempt)
         */
        MvcResult mvcNewResult = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcNewResult.getRequest().getSession();

        ObjectMapper objectMapper = new ObjectMapper();

        GameGuessOutput newGameOutput = objectMapper.readValue(mvcNewResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        GameGuessInput guessInput = new GameGuessInput();
        guessInput.setId(newGameOutput.getId());
        guessInput.setWord("helloworld");

        MvcResult mvcResult = this.mvc.perform(post("/api/game/guess")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guessInput)))
                .andExpect(status().isOk())
                .andReturn();

        GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        assertEquals("Guessed incorrectly.", output.getResult(), "result");
        assertEquals(newGameOutput.getId(), output.getId(), "id");
        assertEquals(newGameOutput.getOriginalWord(), output.getOriginalWord(), "original-word");
        assertNotNull(output.getScrambleWord(), "scramble-word");
        assertEquals(guessInput.getWord(), output.getGuessWord(), "guess-word");
        assertEquals(newGameOutput.getTotalWords(), output.getTotalWords(), "total-words");
        assertEquals(newGameOutput.getRemainingWords(), output.getRemainingWords(), "total-words");
        assertTrue(output.getGuessedWords().isEmpty(), "guessed-words");
    }

    @Test
    void givenCreateNewGame_whenSubmitFirstCorrectWord_thenGuessedCorrectly() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is of correct answer
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "Guessed correctly."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` equals to input `guessWord`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is equals to `remainingWords - 1` of previous game state (decrement by 1)
         * i) `guessedWords` is not empty list
         * j) `guessWords` contains input `guessWord`
         */
        MvcResult mvcNewResult = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcNewResult.getRequest().getSession();

        ObjectMapper objectMapper = new ObjectMapper();

        GameGuessOutput newGameOutput = objectMapper.readValue(mvcNewResult.getResponse().getContentAsString(),
                GameGuessOutput.class);
        String originalWord = newGameOutput.getOriginalWord();

        String correctWord = jumbleEngine.generateSubWords(originalWord, null).stream().findAny().orElse("");

        GameGuessInput guessInput = new GameGuessInput();
        guessInput.setId(newGameOutput.getId());
        guessInput.setWord(correctWord);

        MvcResult mvcResult = this.mvc.perform(post("/api/game/guess")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guessInput)))
                .andExpect(status().isOk())
                .andReturn();

        GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                GameGuessOutput.class);

        assertEquals("Guessed correctly.", output.getResult(), "result");
        assertEquals(newGameOutput.getId(), output.getId(), "id");
        assertEquals(newGameOutput.getOriginalWord(), output.getOriginalWord(), "original-word");
        assertNotNull(output.getScrambleWord(), "scramble-word");
        assertEquals(guessInput.getWord(), output.getGuessWord(), "guess-word");
        assertEquals(newGameOutput.getTotalWords(), output.getTotalWords(), "total-words");
        assertEquals(newGameOutput.getRemainingWords()-1, output.getRemainingWords(), "total-words");
        assertFalse(output.getGuessedWords().isEmpty(), "guessed-words");
        assertTrue(output.getGuessedWords().contains(correctWord), "correct-guessed-words");
    }

    @Test
    void givenCreateNewGame_whenSubmitAllCorrectWord_thenAllGuessed() throws Exception {
        /*
         * Doing HTTP POST "/api/game/guess"
         *
         * Given:
         * a) has valid game ID from previously created game
         * b) has submit all correct answers, except the last answer
         *
         * Input: JSON request body
         * a) `id` of previously created game
         * b) `word` is of the last correct answer
         *
         * Expect: Assert these
         * a) HTTP status == 200
         * b) `result` equals "All words guessed."
         * c) `id` equals to `id` of this game
         * d) `originalWord` is equals to `originalWord` of this game
         * e) `scrambleWord` is not null
         * f) `guessWord` equals to input `guessWord`
         * g) `totalWords` is equals to `totalWords` of this game
         * h) `remainingWords` is 0 (no more remaining, game ended)
         * i) `guessedWords` is not empty list
         * j) `guessWords` contains input `guessWord`
         */
        MvcResult mvcNewResult = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcNewResult.getRequest().getSession();

        ObjectMapper objectMapper = new ObjectMapper();

        GameGuessOutput newGameOutput = objectMapper.readValue(mvcNewResult.getResponse().getContentAsString(),
                GameGuessOutput.class);
        String originalWord = newGameOutput.getOriginalWord();

        List<String> correctWords = jumbleEngine.generateSubWords(originalWord, null).stream().collect(Collectors.toList());

        MvcResult mvcResult = mvcNewResult;
        for(String correctWord: correctWords) {
            GameGuessInput guessInput = new GameGuessInput();
            guessInput.setId(newGameOutput.getId());
            guessInput.setWord(correctWord);

            mvcResult = this.mvc.perform(post("/api/game/guess")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(guessInput)))
                    .andExpect(status().isOk())
                    .andReturn();

            GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), 
                    GameGuessOutput.class);
            assertEquals(guessInput.getWord(), output.getGuessWord(), "guess-word");
        }

        GameGuessOutput output = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), 
                GameGuessOutput.class);

        assertEquals("All words guessed.", output.getResult(), "result");
        assertEquals(newGameOutput.getId(), output.getId(), "id");
        assertEquals(newGameOutput.getOriginalWord(), output.getOriginalWord(), "original-word");
        assertNotNull(output.getScrambleWord(), "scramble-word");
        assertEquals(newGameOutput.getTotalWords(), output.getTotalWords(), "total-words");
        assertEquals(0, output.getRemainingWords(), "total-words");
        assertFalse(output.getGuessedWords().isEmpty(), "guessed-words");
        assertEquals(correctWords.size(), output.getGuessedWords().size(), "correct-guessed-words");
    }

}
