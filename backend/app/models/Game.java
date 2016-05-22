package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Unindexed;

import enums.GameStates;
import enums.Messages;
import exceptions.GameException;
import play.modules.objectify.ObjectifyModel;

@Entity
@Cached
public class Game extends ObjectifyModel {

  @Id
  public Long id;

  @Unindexed
  public String options;

  @Unindexed
  public int positions;

  @Unindexed
  public String answer;

  @Embedded
  @Unindexed
  public List<Guess> guessesA = new ArrayList<Guess>(0);

  @Embedded
  @Unindexed
  public List<Guess> guessesB = new ArrayList<Guess>(0);

  @Unindexed
  public String userA;

  @Unindexed
  public String userB;

  @Unindexed
  public boolean isMultiplayer;

  @Unindexed
  public GameStates state;

  @Unindexed
  public String winner;

  public Date createdAt = new Date();

  @Unindexed
  public Date startedAt;

  @Unindexed
  public Date finishedAt;

  private Game() {
    // required to load from the database
  }

  public Game(String user, boolean isMultiplayer, int positions, String options) {
    this.userA = user;
    generateAnswer(positions, options);
    setMode(isMultiplayer);
  }

  private void generateAnswer(int positions, String options) {
    this.positions = positions;
    this.options = options;
    answer = "";
    for (int i = 0; i < positions; i++) {
      int letter = (int) (Math.random() * positions);
      answer += options.substring(letter, letter + 1);
    }
  }

  private void setMode(boolean isMultiplayer) {
    this.isMultiplayer = isMultiplayer;
    if (isMultiplayer == false) {
      start();
    } else {
      state = GameStates.CREATED;
    }
  }

  private void start() {
    startedAt = new Date();
    state = GameStates.STARTED;
  }

  public void addOpponent(String user) throws GameException {
    if (isMultiplayer == false) {
      throw new GameException(Messages.NOT_MULTIPLAYER);
    }

    userB = user;
    start();
  }

  public List<Guess> getGuesses(String user) throws GameException {
    if (userA.equals(user)) {
      return guessesA;
    } else if (userB.equals(user)) {
      return guessesB;
    } else {
      throw new GameException(Messages.NOT_A_PLAYER);
    }
  }

  public List<Guess> getOpponentGuesses(String user) throws GameException {
    if (userA.equals(user)) {
      return guessesB;
    } else if (userB.equals(user)) {
      return guessesA;
    } else {
      throw new GameException(Messages.NOT_A_PLAYER);
    }
  }

  public void addGuess(Guess guess, String user) throws GameException {

    if (state == GameStates.CREATED) {
      throw new GameException(Messages.GAME_NOT_STARTED);
    }

    if (state == GameStates.STARTED) {

      // Test guess
      char[] a = answer.toCharArray();

      for (int i = 0; i < positions; i++) {
        if (guess.guess.charAt(i) == a[i]) {
          guess.exact++;
          a[i] = '$';
        }
      }

      for (int i = 0; i < positions; i++) {
        if (a[i] != '$') {
          for (int j = 0; j < positions; j++) {
            if (guess.guess.charAt(i) == a[j]) {
              guess.near++;
              a[j] = '#';
              break;
            }
          }
        }
      }

      getGuesses(user).add(guess);

      // Is the game finished?
      if (guess.exact == positions) {
        finishedAt = new Date();
        state = GameStates.FINISHED;
        winner = user;
      }
    }
  }
}
