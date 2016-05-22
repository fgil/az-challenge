

import java.util.List;

import org.junit.Test;

import enums.GameStates;
import exceptions.GameException;
import models.Game;
import models.Guess;
import play.test.UnitTest;

public class MultiplayerTest extends UnitTest{

  int positions = 5;
  String options = "ABCDE";
  String userA = "Fernando";
  String userB = "Luciana";

  @Test
  public void newMultiPlayerGameTest() throws Exception {
    Game game = new Game(userA, true, positions, options);
    assertEquals(game.userA,userA);
    assertEquals(game.isMultiplayer, true);
    assertEquals(GameStates.CREATED, game.state);
    assertEquals(positions, game.positions);
    assertEquals(positions, game.answer.length());
    assertNotNull(game.createdAt);
    assertNull(game.startedAt);
  }

  @Test
  public void gameplayTest() throws Exception {
    Game game = new Game(userA, true, positions, options);
    game.answer = "CBAAE";
    game.addOpponent(userB);

    //Incorrect A
    Guess guess1 = new Guess("ABCCE");
    game.addGuess(guess1, userA);
    List<Guess> guessesA = game.getGuesses(userA);
    assertEquals(2, guess1.exact);
    assertEquals(2, guess1.near);
    assertEquals(guess1, guessesA.get(guessesA.size()-1));

    //Incorrect B
    Guess guess2 = new Guess("AEACB");
    game.addGuess(guess2, userB);
    List<Guess> guessesB = game.getGuesses(userB);
    assertEquals(1, guess2.exact);
    assertEquals(4, guess2.near);
    assertEquals(guess2, guessesB.get(guessesB.size()-1));

    //Correct A
    Guess guess3 = new Guess(game.answer);
    game.addGuess(guess3, userA);
    guessesA = game.getGuesses(userA);
    assertEquals(positions, guess3.exact);
    assertEquals(0, guess3.near);
    assertEquals(guess3, guessesA.get(guessesA.size()-1));
    assertEquals(GameStates.FINISHED, game.state);
    assertEquals(userA, game.winner);
    assertEquals(2, guessesA.size());
    assertEquals(1, guessesB.size());
    assertNotNull(game.finishedAt);

    //After match B
    Guess guess4 = new Guess("AAAAA");
    game.addGuess(guess4, userB);
    guessesB = game.getGuesses(userB);
    assertEquals(0, guess4.exact);
    assertEquals(0, guess4.near);
    assertFalse(guess4.equals(guessesB.get(guessesB.size()-1)));
  }

  @Test
  public void withoutOpponentTest() throws Exception {

    Game game = new Game(userA, true, positions, options);

    boolean exception = false;
    try {
      game.addGuess(new Guess("ABCCE"), userA);
    } catch(GameException e){
      exception = true;
    }
    assertTrue(exception);

  }

  @Test
  public void invalidUserTest() throws Exception {

    Game game = new Game(userA, true, positions, options);
    game.addOpponent(userB);

    boolean exception = false;
    try {
      game.getGuesses("X");
    } catch(GameException e){
      exception = true;
    }
    assertTrue(exception);
  }

  @Test
  public void invalidOpponentTest() throws Exception {

    Game game = new Game(userA, true, positions, options);
    game.addOpponent(userB);

    boolean exception = false;
    try {
      game.getOpponentGuesses("X");
    } catch(GameException e){
      exception = true;
    }
    assertTrue(exception);
  }

}
