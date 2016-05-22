package models;

import play.modules.objectify.ObjectifyModel;

public class Guess {
  public int exact;
  public String guess;
  public int near;

  public Guess(String code){
    this.guess = code;
  }
}
