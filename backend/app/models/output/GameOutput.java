package models.output;

import java.util.ArrayList;
import java.util.List;

import enums.GameStates;
import models.Guess;

public class GameOutput {

  public final int code_length = 8;
  public final String[] colors = {"R", "B", "G", "Y", "P", "O", "C", "M"};
  public String game_key;
  public String guess;
  public int num_guesses;
  public List<Guess> past_results = new ArrayList<Guess>(0);
  public List<Guess> past_results_opponent = new ArrayList<Guess>(0);
  public Object result;
  public String solved = "false";
  public Double time_taken;
  public String user;
  public String players;
  public GameStates game_state = GameStates.CREATED;
  public boolean is_multiplayer;
}
