# Untitled Maze Game
<!--[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)-->
[![Build Status](https://travis-ci.org/DakshChan/UntitledMazeGame.svg?branch=master)](https://travis-ci.org/DakshChan/UntitledMazeGame)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-no-red.svg)](https://bitbucket.org/lbesson/ansi-colors)
[![GitHub contributors](https://img.shields.io/github/contributors/DakshChan/UntitledMazeGame.svg)](https://github.com/DakshChan/UntitledMazeGame/graphs/contributors/)

## Table of Contents

* [Introduction](#introduction)
* [How to Install](#how-to-install)
    * [Server](#server)
    * [Client](#client)
* [How to Play](#how-to-play)
* [Credits](#credits)

## Introduction

Untitled Maze Game is an online multiplayer maze game. At the beginning of each round
a maze is randomly generated. The person with the most amount of scores at the end is
the winner. The scores are based on on the items a player has picked up
and how long it took for them to exit.

## How to Install

Since the game is multiplayer, you will need to configure both the server and the client.

### Server

To run the server, simply run Server.java on the desired computer. If you are running on a
computer, make sure to configure the ports and do port-forwarding.

To configure the game settings, open config.xml, and change the variables. The variables are:

* players_per_game: The number of players that are allowed in each game.
* height: The height of the maze that will be generated.
* widthL the width of the maze that will be generated.

### Client

To run the client, you must run Client.java on your computer. Make sure
to change HOST to the ip/dns of the server you are running on.

## How to Play

Click on Play to join a lobby. You can see the current people in the lobby on your screen.
Once enough players have joined, the game automatically starts.

Press WASD to move around on the tiles. You can only move on tiles that are not walls. You can only see
a limited number of tiles around you. Players can see each other in the maze, but cannot interact with each other.

Pick up items to add to your score. Items are randomly scattered throughout the maze.

The sooner you exit the maze, the higher your score will be.

Once everyone has finished, a leaderboard will be displayed to all the players, with the score they achieved. From
this screen, you can either exit, or play again.

## Credits

Untitled Maze Game is created by:

* Aryan Abed-Esfahani
* Eric Liang
* Jesse Liu
* Daksh Malhotra
* Cindy Wang


