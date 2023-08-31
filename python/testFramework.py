'''
Script to test the distributed lobby framework.
'''

import tests

from sys import argv


##  Methods  ##

def main():
    
    tests.testGame(argv[1], 10, 10)


##  Main  ##

if __name__ == "__main__":
    main()