'''
This module contains all the tests.
'''

import json
import os
import subprocess as spc
import time


##  Methods  ##

def testGame(game, minClients, maxClients):

    print(f"Running tests for {game}")

    os.chdir("../java/bin")

    totalMessages = []

    for x in range(minClients, maxClients+1):

        print(f"Testing lobby with {x} clients")
        print("--------------------------------")

        time.sleep(1)

        if not os.path.exists("../../tests/temp"):
            os.mkdir("../../tests/temp")

        print("Starting server...")

        spc.Popen(["java", "-cp", "../lib/json-simple-1.1.1.jar:.", "server.ServerDriver", "9999", str(x), "single-lobby"])

        print("Spawning clients...")

        for client in range(x):
            if client == x-1:
                print("Lobby starting")
                spc.run(["java", "-cp", "../lib/json-simple-1.1.1.jar:.", "tests.uno.UnoClientDriver", str(13000+client), "localhost", "9999"])
            else:
                spc.Popen(["java", "-cp", "../lib/json-simple-1.1.1.jar:.", "tests.uno.UnoClientDriver", str(13000+client), "localhost", "9999"])

        print("Simulation finished")
        print("Comparing final states...")

        totalMessages.append(_compareStates(game, x))

    print("All tests finished")
    print("Total messages: " + str(totalMessages))

def _compareStates(game, numClients):

    messages = 0
    finalStates = []

    for x in range(numClients):
        with open(f"../../tests/temp/{game}_state_player_{x}.json", 'r') as stateFile:
            finalStates.append(json.load(stateFile))

    allStatesEqual = True

    for x in range(1, len(finalStates)):
        if _compareDicts(finalStates[0], finalStates[x]) == False:
            print(f"Player {x} state was not equal to player 0 state")
            allStatesEqual = False
            break
    
    if allStatesEqual:
        print("All game states were equal!")

    for state in finalStates:
        messages += int(state["totalMessages"])

    return messages

def _compareDicts(dict1, dict2):

    if set(dict1.keys()) != set(dict2.keys()):
        return False

    allEntriesEqual = True

    for key in dict1.keys():
        if key == "totalMessages":
            continue
        if isinstance(dict1[key], dict):
            if isinstance(dict2[key], dict):
                if _compareDicts(dict1[key], dict2[key]) == False:
                    allEntriesEqual = False
                    break
        elif dict1[key] != dict2[key]:
                allEntriesEqual = False
                break

    return allEntriesEqual