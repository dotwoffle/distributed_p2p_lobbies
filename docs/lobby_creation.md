# Lobby Creation Algorithm

Creating a game lobby is an essential step before our framework can be utilized. However, our framework will
not specifically be designed to set up lobbies. The application designer will usually wish to design and set
up their own lobbies in a specific way, for example, to consider player's skill levels. The important part is
that the application needs to be able to pass information about all clients into our framework, which must be
collected during lobby setup. Each client in the lobby must be given the address of each other client for our
framework to function. Described in this file is a simple algorithm an application may use to collect clients
and set up a lobby.

## Algorithm Outline

### Process

```
Game server is listening for connections
Client connects and sends server the REQUESTLOBBY message
When client connects:
  Server records client address & offered port, places client in queue
When enough clients in queue to start a game:
  Server removes LOBBY_SIZE clients from queue and stores them
  For each client c in the lobby:
    Send JOINLOBBY message to c
    Send CLIENTINFO with address and port of each client to c, including CLIENTINFO of c
  Send GAMESTART to one or more clients
```

### Pseudocode

```
q = empty queue of clients
lobbySize = integer defined by application
serverSocket = listener socket

while true:
    client = serverSocket.accept()
    q.push(client)
    if q.size() >= lobbySize:
        setupLobby()

function setupLobby():
    lobby = empty list of clients
    loop from 1 to lobbySize:
        lobby.add(q.pop())
    for client in lobby:
        client.send(LOBBYSETUP)
        for otherClient in lobby:
            if client != otherClient:
                client.send(otherClient.getAddress())
```

### Notes

- If the server is accepting clients on a separate thread, clients may still be added to the queue while a
  lobby is being set up. This is why setupLobby() does not pop every element off the queue and rather uses a
  loop to pop a set amount of clients.
- Clients are assumed to be implementing our framework, which means when they connect, they will be waiting
  for the LOBBYSETUP message from the server already.
- If the server is using threads, additional caution is needed to make sure that the setupLobby() doesn't run
  at inappropriate times, like when more clients enter the queue while clients are being popped off.
- After the sever sends each client all addresses, it should send the GAMESTART message to one or more
  clients as needed.