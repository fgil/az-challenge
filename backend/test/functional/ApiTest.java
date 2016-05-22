package functional;

import java.util.Date;

import org.junit.Test;

import models.Game;
import play.modules.objectify.ObjectifyService;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class ApiTest extends FunctionalTest {

  static String POST(String url, String input) {
    Response response = POST(url, "application/json", input);
    assertEquals(200, (int) response.status);
    return response.out.toString();
  }

  @Test
  public void cleanTest() throws Exception {
    // Clear database
    ObjectifyService.delete(ObjectifyService.query(Game.class));

    // Create a new game
    POST("/new_game", "{ \"user\": \"Fernando\" }");

    // Change game date
    Game game = ObjectifyService.query(Game.class).get();
    game.createdAt = new Date(1);
    ObjectifyService.put(game);

    // Clean
    String reply1 = POST("/clean", "{}");
    assertEquals("{\"result\":\"ok\"}", reply1);

    // Verifiy
    assertEquals(0, ObjectifyService.query(Game.class).countAll());
  }

  @Test
  public void singlePlayerTest() throws Exception {

    // Clear database
    ObjectifyService.delete(ObjectifyService.query(Game.class));

    // Create a new game
    String reply1 = POST("/new_game", "{ \"user\": \"Fernando\" }");

    // Change game correct answer to test
    Game game = ObjectifyService.query(Game.class).get();
    game.answer = "RBGGGGGG";
    ObjectifyService.put(game);

    // Check game creation output
    assertEquals(String.format("{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
        + "\"game_key\":\"%d\",\"num_guesses\":0,\"past_results\":[],\"past_results_opponent\":[],"
        + "\"solved\":\"false\",\"game_state\":\"STARTED\",\"is_multiplayer\":false}", game.id), reply1);

    // Try an incorrect answer
    String reply2 = POST("/guess",
        String.format("{ \"code\":\"RGOOOOOB\", \"game_key\": \"%d\", \"user\":\"Fernando\" }", game.id));
    assertEquals(
        String.format("{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
            + "\"game_key\":\"%d\",\"guess\":\"RGOOOOOB\",\"num_guesses\":1,"
            + "\"past_results\":[{\"exact\":1,\"guess\":\"RGOOOOOB\",\"near\":2}],\"past_results_opponent\":[],"
            + "\"result\":{\"exact\":1,\"near\":2},\"solved\":\"false\",\"user\":\"Fernando\","
            + "\"players\":\"Fernando vs null\",\"game_state\":\"STARTED\",\"is_multiplayer\":false}", game.id),
        reply2);

    // Try a correct answer
    String reply3 = POST("/guess",
        String.format("{ \"code\":\"RBGGGGGG\", \"game_key\": \"%d\", \"user\":\"Fernando\" }", game.id));
    game = ObjectifyService.query(Game.class).get();
    String time = String.format("%.03f", (double) (game.finishedAt.getTime() - game.startedAt.getTime()) / 1000);
    time = time.replace(",", ".");
    assertEquals(String
        .format(
            "{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
                + "\"game_key\":\"%d\",\"guess\":\"RBGGGGGG\",\"num_guesses\":2,"
                + "\"past_results\":[{\"exact\":1,\"guess\":\"RGOOOOOB\",\"near\":2},{\"exact\":8,\"guess\":\"RBGGGGGG\",\"near\":0}],"
                + "\"past_results_opponent\":[],\"result\":\"You win!\",\"solved\":\"true\",\"time_taken\":%s,\"user\":\"Fernando\","
                + "\"players\":\"Fernando vs null\",\"game_state\":\"FINISHED\",\"is_multiplayer\":false}",
            game.id, time),
        reply3);
  }

  @Test
  public void multiPlayerTest() throws Exception {

    // Clear database
    ObjectifyService.delete(ObjectifyService.query(Game.class));

    // Create a new game
    String reply1 = POST("/new_game", "{ \"user\": \"Fernando\", \"is_multiplayer\":true }");

    // Change game correct answer to test
    Game game = ObjectifyService.query(Game.class).get();
    game.answer = "RBGGGGGG";
    ObjectifyService.put(game);

    // Check game creation output
    assertEquals(String.format("{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
        + "\"game_key\":\"%d\",\"num_guesses\":0,\"past_results\":[],\"past_results_opponent\":[],"
        + "\"solved\":\"false\",\"game_state\":\"CREATED\",\"is_multiplayer\":false}", game.id), reply1);

    // Check game state
    String reply2 = POST("/game_state", String.format("{ \"game_key\": \"%d\", \"user\":\"Fernando\" }", game.id));
    assertEquals(String.format("{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
        + "\"game_key\":\"%d\",\"num_guesses\":0,\"past_results\":[],\"past_results_opponent\":[],"
        + "\"solved\":\"false\",\"game_state\":\"CREATED\",\"is_multiplayer\":true}", game.id), reply2);

    // Try to guess before the other player joins
    String reply3 = POST("/guess",
        String.format("{ \"code\":\"RGOOOOOB\", \"game_key\": \"%d\", \"user\":\"Fernando\" }", game.id));
    assertEquals("{\"result\":\"Ops, this game did not started yet.\"}", reply3);

    // Second player joins
    String reply4 = POST("/join", String.format("{ \"game_key\": \"%d\", \"user\":\"Luciana\" }", game.id));
    assertEquals(String.format("{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
        + "\"game_key\":\"%d\",\"num_guesses\":0,\"past_results\":[],\"past_results_opponent\":[],"
        + "\"solved\":\"false\",\"game_state\":\"STARTED\",\"is_multiplayer\":true}", game.id), reply4);

    // Check game state again
    String reply5 = POST("/game_state", String.format("{ \"game_key\": \"%d\", \"user\":\"Fernando\" }", game.id));
    assertEquals(String.format(
        "{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
            + "\"game_key\":\"%d\",\"num_guesses\":0,\"past_results\":[],\"past_results_opponent\":[],"
            + "\"solved\":\"false\",\"players\":\"Fernando vs Luciana\",\"game_state\":\"STARTED\",\"is_multiplayer\":true}",
        game.id), reply5);

    // First player try an incorrect guess
    String reply6 = POST("/guess",
        String.format("{ \"code\":\"RGOOOOOB\", \"game_key\": \"%d\", \"user\":\"Fernando\" }", game.id));
    assertEquals(String.format("{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
        + "\"game_key\":\"%d\",\"guess\":\"RGOOOOOB\",\"num_guesses\":1,"
        + "\"past_results\":[{\"exact\":1,\"guess\":\"RGOOOOOB\",\"near\":2}],\"past_results_opponent\":[],"
        + "\"result\":{\"exact\":1,\"near\":2},\"solved\":\"false\",\"user\":\"Fernando\","
        + "\"players\":\"Fernando vs Luciana\",\"game_state\":\"STARTED\",\"is_multiplayer\":false}", game.id), reply6);

    // Second player try an incorrect guess
    String reply7 = POST("/guess",
        String.format("{ \"code\":\"BROOOOOG\", \"game_key\": \"%d\", \"user\":\"Luciana\" }", game.id));
    assertEquals(String.format("{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
        + "\"game_key\":\"%d\",\"guess\":\"BROOOOOG\",\"num_guesses\":1,"
        + "\"past_results\":[{\"exact\":1,\"guess\":\"BROOOOOG\",\"near\":2}],\"past_results_opponent\":[],"
        + "\"result\":{\"exact\":1,\"near\":2},\"solved\":\"false\",\"user\":\"Luciana\","
        + "\"players\":\"Fernando vs Luciana\",\"game_state\":\"STARTED\",\"is_multiplayer\":false}", game.id), reply7);

    // First player solves and wins
    String reply8 = POST("/guess",
        String.format("{ \"code\":\"RBGGGGGG\", \"game_key\": \"%d\", \"user\":\"Fernando\" }", game.id));
    game = ObjectifyService.query(Game.class).get();
    String time = String.format("%.03f", (double) (game.finishedAt.getTime() - game.startedAt.getTime()) / 1000);
    time = time.replace(",", ".");
    assertEquals(String.format(
        "{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
            + "\"game_key\":\"%d\",\"guess\":\"RBGGGGGG\",\"num_guesses\":2,"
            + "\"past_results\":[{\"exact\":1,\"guess\":\"RGOOOOOB\",\"near\":2},{\"exact\":8,\"guess\":\"RBGGGGGG\",\"near\":0}],"
            + "\"past_results_opponent\":[{\"exact\":1,\"guess\":\"BROOOOOG\",\"near\":2}],"
            + "\"result\":\"You win!\",\"solved\":\"true\",\"time_taken\":%s,\"user\":\"Fernando\","
            + "\"players\":\"Fernando vs Luciana\",\"game_state\":\"FINISHED\",\"is_multiplayer\":false}",
        game.id, time), reply8);

    // Second player guesses and receive a bad news
    String reply9 = POST("/guess",
        String.format("{ \"code\":\"BROOOOOG\", \"game_key\": \"%d\", \"user\":\"Luciana\" }", game.id));
    assertEquals(String.format(
        "{\"code_length\":8,\"colors\":[\"R\",\"B\",\"G\",\"Y\",\"P\",\"O\",\"C\",\"M\"],"
            + "\"game_key\":\"%d\",\"guess\":\"BROOOOOG\",\"num_guesses\":1,\"past_results\":[{\"exact\":1,\"guess\":\"BROOOOOG\",\"near\":2}],"
            + "\"past_results_opponent\":[{\"exact\":1,\"guess\":\"RGOOOOOB\",\"near\":2},{\"exact\":8,\"guess\":\"RBGGGGGG\",\"near\":0}],"
            + "\"result\":\"Your opponent wins!\",\"solved\":\"true\",\"time_taken\":%s,\"user\":\"Luciana\","
            + "\"players\":\"Fernando vs Luciana\",\"game_state\":\"FINISHED\",\"is_multiplayer\":false}",
        game.id, time), reply9);
  }

}
