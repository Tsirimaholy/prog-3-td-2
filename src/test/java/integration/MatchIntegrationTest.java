package integration;

import app.foot.FootApi;
import app.foot.controller.rest.Match;
import app.foot.controller.rest.Player;
import app.foot.controller.rest.PlayerScorer;
import app.foot.controller.rest.Team;
import app.foot.controller.rest.TeamMatch;
import app.foot.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
@Slf4j
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();  //Allow 'java.time.Instant' mapping

    @Test
    void read_match_by_id_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(expectedMatch2(), actual);
    }

    @Test
    void add_goals_ok() throws Exception {
        final int CURRENT_MATCH_ID = 3;
        MockHttpServletResponse response = mockMvc.perform(
                        post("/matches/3/goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of(playerScorerToCreate()))))
                .andReturn()
                .getResponse();

        Match actual = convertFormHttpResponse(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(CURRENT_MATCH_ID, actual.getId());
    }

    @Test
    void add_goals_ko() {
        MockHttpServletResponse response = null;
        try {
           response = mockMvc.perform(
                            post("/matches/3/goals")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(List.of(
                                            playerScorerWithInvalidScoringTime()
                                    ))))
                    .andReturn()
                    .getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            assertSame(e.getCause().getClass(), BadRequestException.class);
        }

        assertNull(response);
    }

    private Match convertFormHttpResponse(MockHttpServletResponse response) throws JsonProcessingException, UnsupportedEncodingException {
        JavaType constructType = objectMapper.getTypeFactory()
                .constructType(Match.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                constructType
        );
    }

    public static PlayerScorer playerScorerWithInvalidScoringTime() {
        return PlayerScorer.builder()
                .player(player6())
                .scoreTime(100)
                .isOG(false)
                .build();
    }

    public static PlayerScorer playerScorerToCreate() {
        return PlayerScorer.builder()
                .player(player6())
                .scoreTime(71)
                .isOG(false)
                .build();
    }

    private static Match matchToCreate() {
        return Match.builder()
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S1")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static Match expectedMatch2() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static TeamMatch teamMatchB() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchA() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                                .player(player3())
                                .scoreTime(70)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player6())
                                .scoreTime(80)
                                .isOG(true)
                                .build()))
                .build();
    }

    private static Team team3() {
        return Team.builder()
                .id(3)
                .name("E3")
                .build();
    }

    private static Player player6() {
        return Player.builder()
                .id(6)
                .name("J6")
                .isGuardian(false)
                .build();
    }

    private static Player player3() {
        return Player.builder()
                .id(3)
                .name("J3")
                .isGuardian(false)
                .build();
    }

    private static Team team2() {
        return Team.builder()
                .id(2)
                .name("E2")
                .build();
    }
}
