# Architecture for a Distributed Client

## Overview

Each client will have a TCP server socket that is constantly listening for messages from the server or other clients.
The messages that can be sent between clients are detailed in the Messages section. The core of this framework is the
EVENT message. This message is how clients share their current state with other clients. States are specially defined
as a map of property names to values, called a StateMap. These values must be primitives or otherwise serializable
values that can be reconstructed on the other end of the transmission.

## Messages

The following documents the messages that clients using our framework understand, and their meanings.

- REQUESTLOBBY(port): Sent by a client to the game server to request to join a game. The port is the publicly available
  port that the client promises to listen on for messages for the duration of the game.
- JOINLOBBY(lobbySize, index): Sent by the game server to all clients in a lobby. This message indicates that a
  client has joined a lobby, and primes the client to receive the addresses and IDs of all other clients in the lobby.
  The lobbySize is the size of the lobby the client is in, and the index is the host index of the client receiving the
  message.
- CLIENTINFO(index, hostname, port): Sent by the game server to all clients in a lobby. This message contains the
  index, address, and port of any client in the lobby.
- GAMESTART: Sent by the game server to one or more clients in a lobby. This message instructs a client to begin
  simulating the game.
- EVENT(clock, state): Event message broadcast by a client when a change in game state occurs. This message contains
  the vector timestamp of the event, and any changes in state the client wishes to communicate.
- GAMEEND: Broadcast by a client when a client determines their simulation is over.

## Methods

The following documents the methods that clients in our framework have.

```
void raiseEvent(StateMap state)

Parameters:
    state: The updated local game state to share with the lobby.

Issues an event to the lobby. Clients should call this function when they detect a "significant" change in their
current game state. This function broadcasts an EVENT message to all clients in the lobby, containing the updated
state.
```

```
StateMap getNextState()

Returns:
    The most current game state known by this client.

Retrieves the most current game state that this client has. This could be the client's current local state, or it could
the most recent state received from another client via an EVENT message. Clients should use this to update thier own
game state, for example, on every frame.
```