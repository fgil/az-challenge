
import java.util.List;

import org.junit.Test;

import enums.GameStates;
import exceptions.GameException;
import models.Game;
import models.Guess;
import play.test.UnitTest;

public class SinglePlayerTest extends UnitTest {

  int positions = 5;
  String options = "ABCDE";
  String userA = "Fernando";

  @Test
  public void newSinglePlayerGameTest() throws Exception {
    Game game = new Game(userA, false, positions, options);
    assertEquals(game.userA, userA);
    assertEquals(game.isMultiplayer, false);
    assertEquals(GameStates.STARTED, game.state);
    assertEquals(positions, game.positions);
    assertEquals(positions, game.answer.length());
    assertNotNull(game.createdAt);
    assertNotNull(game.startedAt);
  }

  @Test
  public void gameplayTest() throws Exception {
    Game game = new Game(userA, false, positions, options);
    game.answer = "CBAAE";

    // Incorrect
    Guess guess1 = new Guess("ABCCE");
    game.addGuess(guess1, userA);
    List<Guess> guessesA = game.getGuesses(userA);
    assertEquals(2, guess1.exact);
    assertEquals(2, guess1.near);
    assertEquals(guess1, guessesA.get(guessesA.size() - 1));

    // Correct
    Guess guess3 = new Guess(game.answer);
    game.addGuess(guess3, userA);
    guessesA = game.getGuesses(userA);
    assertEquals(positions, guess3.exact);
    assertEquals(0, guess3.near);
    assertEquals(guess3, guessesA.get(guessesA.size() - 1));
    assertEquals(GameStates.FINISHED, game.state);
    assertEquals(userA, game.winner);
    assertNotNull(game.finishedAt);
  }

  @Test
  public void opponentTest() throws Exception {

    Game game = new Game(userA, false, positions, options);

    boolean exception1 = false;
    try {
      game.addOpponent("X");
    } catch (GameException e) {
      exception1 = true;
    }
    assertTrue(exception1);

  }

}
