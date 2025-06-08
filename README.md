
# 20 Questions

A multiplayer 20 Questions game with concurrent player management.

I built this project to learn socket programming and multithreading - two areas where I had limited experience but wanted to develop my skills.

There are a handful of enhancements I still want to make to the current version, but my long-term plan is to learn Spring Boot to rebuild this with a web interface, which will let me strengthen my HTML and CSS skills while learning JavaScript and backend web development.

## Features

* **Multiplayer 20 Questions** - Two players take turns being Game Master and Guesser.
* **Real-time gameplay** - Players connect over the network and play in real-time.
* **Player matching** - Automatic pairing of players waiting for games.
* **Play again option** - Both players can choose to play multiple rounds together.
* **Username customization** - Players can change their display names.
* **Clean disconnections** - Players can exit gracefully through the menu.

## How It Works

Server starts and listens on port 5000  

Players connect and see:

    | 20 Questions |
  
    Enter your username: 

After entering a username, the main menu appears:

    1. Play Game
    2. Change Username  
    3. Exit

**Option 1:**

Matches them up with another waiting player and creates a game session for them to play.

Once the game ends, they will be prompted to decide on if they want to play against the same player again.

If yes, the game session continues.

If no, the player is directed back to the main menu.

**Option 2:**

The player is prompted to enter a new username. After collection, they are directed back to the main menu.

**Option 3:**

The player is gracefully disconnected from the server.

