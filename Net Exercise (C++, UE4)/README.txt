This project is part of an work for my Network class of my master. Basically we use enet library to make a simple racing game in UE4 where communication is based on a server-client architecture.
The exercise consists in, taking the core of the car racing game, include an ability that spawn a trap on the map. Any player who collides with this trap will not have their inputs available in a short period of time.

Spawn a trap flow:
 - Player presses Space bar/X(PS)/A(XBOX), the input is captured, the skill's cooldown is checked and the client sends a message of type REQUEST_SPAWN_TRAP to the server.
 - Server verifies that the player could use the skill, calculates trap's position and warns every client with a message of type SPAWN_TRAP.
 - Also the server spawns a trap on its map.
 - When clients receive the message, they will spawn a trap on their maps. This trap is added into a map structure so they can get it by trap's ID.

Collide a trap flow:
 - If the server's trap catch an overlap event, will notify about it.
 - Server will broadcast a message of type DESPAWN_TRAP passing the ID of the trap and the ID of the client whose car triggered the previous event.
 - Every client that recives the message will despawn the trap, and only the car's owner will suffer the disable input period.

When designing this exercise, I took these points into account:
 1. The cooldown of trap skill cannot be tricked by the client because of server will check the cooldown too.
 2. Every client and the server will have traps on the same position (Consistency)
 3. For this reason, I tried to minimize the message sends when someone hits a trap. For this reasons is why only the server in its version of the game receives the overlap events and notifies the rest.
 4. All message sending are mark as reliable to ensure the second point.

Disclaimer
 - This code is an extension of my teachers code that made all the core, like the pass of messages and the communication with enet library.
