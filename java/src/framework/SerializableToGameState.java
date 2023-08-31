package framework;


/**An interface that classes can implement to convert their internal states into a GameStateMap.*/
public interface SerializableToGameState {
    
    /**Implementers of this interface should use this method to return a GameStateMap containing all states from the
     * object the implementer wishes to share with other clients during a call to raiseEvent().
     * @return A GameStateMap containing states the implementer deems important.
    */
    public GameStateMap convertToStateMap();

}
