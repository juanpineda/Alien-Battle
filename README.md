# Alien Battle


## walkthrough App

![](alienBattle.gif)

## Technical details

### Android Studio Dolphin | 2021.3.1 Patch 1

Min SDK version: 21

Relevant Tech implemented:

- Shared Preferences

App Description

This game goes by the following rules:
- A match is played by at least 3 players. No user intervention take place during the match. A fixed amount of bots are spawned.
- Each player has a number of available Shoots (S) and Health state (H) at the beginning of the match.
- Each player can perform one of two actions at any time as long as it remains alive (that is H > 0):
    - Attack another player, decreasing the opponents' health H and shoots S.
    - Heal, increasing their H in 10 points.
- The amount of shoots depends on a random distribution between (10..300 depending on the player)
- The health of player depends on a random distribution between (10..20 depending on the player)

Each bot will follow the same strategy: heal randomly and if it attacks it'll go against the player with the lowest H at the moment or nearest one if there is no lowest H player.
A match ends when only one player still stands or a given amount of time has elapsed, in which case the healthiest player wins.
Shoots can be mute or un mute in each game.

## Author
Juan David Pineda Hernández