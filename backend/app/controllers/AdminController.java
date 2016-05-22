package controllers;

import java.util.Date;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

import models.Game;
import models.output.ResultOutput;
import play.modules.objectify.Datastore;
import play.mvc.Controller;

public class AdminController extends Controller {

  public static final int SECONDS_TO_BE_OLD = 24 * 60 * 60;

  public static void home(){
    renderText("Hi");
  }

  public static void clean(){
    Query<Game> oldGames = Datastore.query(Game.class).filter("createdAt <", new Date(new Date().getTime() - SECONDS_TO_BE_OLD * 1000));
    Datastore.delete(oldGames.fetchKeys());
    renderJSON(new ResultOutput());
  }
}
