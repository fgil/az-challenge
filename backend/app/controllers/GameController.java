package controllers;

import com.google.gson.Gson;

import enums.GameStates;
import enums.Messages;
import exceptions.GameException;
import models.Game;
import models.Guess;
import models.input.GameStateInput;
import models.input.GuessInput;
import models.input.JoinInput;
import models.input.NewGameInput;
import models.output.GameOutput;
import models.output.ResultOutput;
import play.Logger;
import play.modules.objectify.Datastore;
import play.mvc.Controller;

public class GameController extends Controller {

  public static final int positions = 8;
  public static final String options = "RBGYPOCM";

  public static void newGame(String body){

    // Get input
    NewGameInput input = new Gson().fromJson(body, NewGameInput.class);

    // Check user
    if(input.user == null){
      renderJSON(new ResultOutput(Messages.IDENTIFY_YOURSELF));
    }

    Game game = new Game(input.user, input.is_multiplayer, positions, options);
    game.id = Datastore.put(game).getId();
    Logger.debug("New game created. key = %d, answer = %s", game.id, game.answer);

    // Prepare output
    GameOutput output = new GameOutput();
    output.game_key = game.id.toString();
    output.game_state = game.state;

    // Return
    renderJSON(new Gson().toJson(output));
  }

  public static void join(String body){

    // Get input
    JoinInput input = new Gson().fromJson(body, JoinInput.class);

    // Load game
    Game game = Datastore.find(Game.class, Long.parseLong(input.game_key));
    if(game == null){
      renderJSON(new ResultOutput(Messages.GAME_NOT_FOUND));
    }

    try{
      game.addOpponent(input.user);
    } catch(GameException e){
      renderJSON(new ResultOutput(e.getMessage()));
    }

    Datastore.put(game);

    // Prepare output
    GameOutput output = new GameOutput();
    output.game_key = game.id.toString();
    output.game_state = game.state;
    output.is_multiplayer = game.isMultiplayer;

    // Return
    renderJSON(new Gson().toJson(output));
  }

  public static void gameState(String body){

    // Get input
    GameStateInput input = new Gson().fromJson(body, GameStateInput.class);

    // Load game
    Game game = Datastore.find(Game.class, Long.parseLong(input.game_key));
    if(game == null){
      renderJSON(new ResultOutput(Messages.GAME_NOT_FOUND));
    }

    // Prepare output
    GameOutput output = new GameOutput();
    output.game_key = game.id.toString();
    output.game_state = game.state;
    output.is_multiplayer = game.isMultiplayer;
    if(game.state != GameStates.CREATED){
      output.players = String.format(Messages.VS, game.userA, game.userB);
    }
    if(game.state == GameStates.FINISHED){
      output.solved = String.valueOf(true);
      output.time_taken = (double)(game.finishedAt.getTime() - game.startedAt.getTime())/1000;
      output.result = String.format(Messages.WINNER, game.winner);
    }

    // Return
    renderJSON(new Gson().toJson(output));
  }

  public static void guess(String body){

    // Get input
    GuessInput input = new Gson().fromJson(body, GuessInput.class);

    // Start database transaction
    Datastore.beginTxn();

    // Load game
    Game game = Datastore.find(Game.class, Long.parseLong(input.game_key));
    if(game == null){
      renderJSON(new ResultOutput(Messages.GAME_NOT_FOUND));
    }

    Guess guess = new Guess(input.code);
    try {
      game.addGuess(guess, input.user);

      Datastore.put(game);

      // Finish database transaction
      Datastore.commitAll();

      // Prepare output
      GameOutput output = new GameOutput();
      output.game_key = game.id.toString();
      output.game_state = game.state;
      output.past_results = game.getGuesses(input.user);
      output.num_guesses = game.getGuesses(input.user).size();
      output.user = input.user;
      output.players = String.format(Messages.VS, game.userA, game.userB);
      output.guess = input.code;
      Guess result = new Guess(null);
      result.exact = guess.exact;
      result.near = guess.near;
      output.result = result;

      if(game.state == GameStates.FINISHED){
        output.solved = String.valueOf(true);
        output.time_taken = (double)(game.finishedAt.getTime() - game.startedAt.getTime())/1000;
        output.past_results_opponent = game.getOpponentGuesses(input.user);
        if(game.winner.equals(input.user)){
          output.result = Messages.YOU_WIN;
        } else {
          output.result = Messages.YOU_LOSE;
        }
      }

      // Return
      renderJSON(new Gson().toJson(output));

    } catch(GameException e){
      Datastore.rollbackAll();
      renderJSON(new ResultOutput(e.getMessage()));
    }
  }

}
