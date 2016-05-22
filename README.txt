        Mastermind Backend by Fernando Gil
       =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

Who is Fernando Gil?
--------------------

https://www.linkedin.com/in/fernandodeoliveiragil



Working version
---------------

A working version of this project is hosted at https://mastermind-fgil.appspot.com/



About this project
------------------

The Mastermind backend project was created using a MVC architecture with the help of the Play Framework in Java. The backend can be run locally or deployed to Google App Engine, which can scale automatically.

The endpoints specification was based in http://careers.axiomzen.co/challenge, although some parameters and properties were added to support multiplayer games.

The Game model class contains all the game logic and rules. It also serves as a structure for persisting the game state between requests.

The GameController class handles all the work regarding to getting the user input, calling the correct Game methods and then generating a Json output.

The multiplayer feature works by sharing a game key with another user. Their requests can hit the server at the same time. A database transaction is used inside the guess endpoint to ensure the first user that sends a correct guess wins.



Contents
--------

This bundle contains the following:

  /                           - Mastermind backend project

  /app/                         - Application classes
  /app/controllers/               - Application controllers
  /app/enums/                     - Some constants
  /app/exceptions/                - Some exceptions
  /app/models/                    - Application data models

  /conf/                        - Project configuration files
  /conf/routes                    - Endpoints routes

  /dependencies/                - Play dependencies
  /dependencies/gae-1.9.6/        - App Engine library for Play
  /dependencies/objectify-1.1/    - App Engine Datastore library for Play

  /test/                        - Unit tests
  /test/functional/               - API tests

  /war/WEB-INF/                 - Configuration files for deploy



Dependencies
------------
- Java 1.7
- Python 2.5
- Google App Engine SDK
- Play Framework version 1.2.5.x
- GAE Module for Play



Installation
------------

1) Download and install the Google App Engine SDK for Java at:
     https://cloud.google.com/appengine/downloads#Google_App_Engine_SDK_for_Java

2) Download the Play Framework from:
     https://downloads.typesafe.com/play/1.2.5.3/play-1.2.5.3.zip

3) Follow the instructions to install the Play Framework at:
     https://www.playframework.com/documentation/1.2.x/install

4) Download the GAE module from:
     http://www.fgil.com.br/gae-1.9.6.zip

8) Unzip the module and place it inside the 'dependencies' folder

6) Set the $GAE_PATH environmental variable to 'appengine-java-sdk-1.x.x' folder

7) Make sure the file 'appengine-java-sdk-1.x.x/bin/appcfg.sh' (Linux/Mac) or 'appengine-java-sdk-1.x.x/bin/appcfg.cmd' (Windows) is executable.

8) Add the 'play-1.2.5.3/play' command to your system path and make sure it is executable.

9) Inside the project folder execute 'play deps' to setup the dependencies.



Testing the application
-----------------------

To run the application locally:

1) Inside the project folder execute 'play test'

2) Go to http://localhost:9000/@tests

3) Select all tests

4) Click 'Start !'



Running the application locally
-------------------------------

To run the application locally:

1) Inside the project folder execute 'play run'

2) Hit any endpoint using the http://localhost:9000/ url.



Endpoints
---------

POST /new_game

  This endpoint creates a new game. The user parameter is required. If is_multiplayer is not specified, it will create a single player.

  - Params:
    {
      "user: "Fernando",
      "is_multiplayer": "false"
    }

  - Response:
    {
      "code_length": 8,
      "colors": [
          "R",
          "B",
          "G",
          "Y",
          "P",
          "O",
          "C",
          "M"
      ],
      "game_key": "5629499534213120",
      "game_state": "STARTED",
      "is_multiplayer": false,
      "num_guesses": 0,
      "past_results": [],
      "past_results_opponent": [],
      "solved": "false"
    }


POST /guess

  Use this endpoint to send guesses. All parameters are required. Use the game key obtained from a /new_game call.

  - Params:
    {
      "user: "Fernando",
      "game_key": "5629499534213120",
      "code": "YPCMBBGR"
    }

  - Response:
    {
      "code_length": 8,
      "colors": [
          "R",
          "B",
          "G",
          "Y",
          "P",
          "O",
          "C",
          "M"
      ],
      "game_key": "5629499534213120",
      "game_state": "FINISHED",
      "guess": "RPYGOGOP",
      "is_multiplayer": false,
      "num_guesses": 2,
      "past_results": [
          {
              "exact": 1,
              "guess": "RPRMYPMR",
              "near": 1
          },
          {
              "exact": 8,
              "guess": "OYCBYOYM",
              "near": 0
          }
      ],
      "past_results_opponent": [],
      "players": "Fernando vs Luciana",
      "result": "You win!",
      "solved": "true",
      "time_taken": 141.097,
      "user": "Fernando"
    }


POST /join

  If you create a multiplayer game. Use this endpoint to add an opponent. All parameters are required. The game state will be STARTED.

  - Params:
    {
      "user: "Luciana",
      "game_key": "5629499534213120"
    }

  - Response:
    {
      "code_length": 8,
      "colors": [
          "R",
          "B",
          "G",
          "Y",
          "P",
          "O",
          "C",
          "M"
      ],
      "game_key": "5629499534213120",
      "game_state": "STARTED",
      "is_multiplayer": true,
      "num_guesses": 0,
      "past_results": [],
      "past_results_opponent": [],
      "solved": "false"
    }


POST /game_state

  Use this endpoint to wait for an opponent in a multiplayer game. The game key parameter is required. Once a second player joins, the game state will change from CREATED to STARTED.

  - Params:
    {
      "game_key": "5629499534213120"
    }

  - Response:
    {
      "code_length": 8,
      "colors": [
          "R",
          "B",
          "G",
          "Y",
          "P",
          "O",
          "C",
          "M"
      ],
      "game_key": "5629499534213120",
      "game_state": "STARTED",
      "is_multiplayer": true,
      "num_guesses": 0,
      "past_results": [],
      "past_results_opponent": [],
      "players": "Fernando vs Luciana",
      "solved": "false"
    }


POST /clean

  This endpoint is called automatically every hour and remove old games from the database.



Uploading to Google
-------------------

1) Go to http://appengine.google.com and create your application.

2) Make sure that the application identifier in your 'backend/war/WEB-INF/appengine-web.xml' file
   matches the one you chose in step 1.

3) Inside the project folder execute 'play gae:deploy'

4) Try your application out at:  http://<app-id>.appspot.com
