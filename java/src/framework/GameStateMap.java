package framework;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**A class that holds information about a single object's game state.*/
public class GameStateMap implements JSONAware {
    
    /*Variables*/

    /**The map of property names to values.*/
    private HashMap<String, Object> stateMap;


    /*Constructors*/

    /**Creates a new empty GameStateMap object.*/
    public GameStateMap() {

        //init

        this.stateMap = new HashMap<>();

    }


    /*Methods*/

    //TODO might have to add some convenience methods to fetch different data types to avoid app developer having to
    //manually do weird casting from JSON types

    /**Creates a new GameStateMap and fills it in with the given JSON data.
     * @param json The JSONObject representing a game state map.
     * @return A GameStateMap containing the JSON data.
    */
    public static GameStateMap constructFromJson(JSONObject json) {

        GameStateMap map = new GameStateMap(); //create empty map

        //fill map with states
        for(Object key : json.keySet()) {
            
            if(json.get(key) instanceof JSONObject) {
                map.stateMap.put((String) key, constructFromJson((JSONObject) json.get(key)));
            }
            else if(json.get(key) instanceof JSONArray) {
                ArrayList<Object> newArray = deserializeArray((JSONArray) json.get(key));
                map.stateMap.put((String) key, newArray);
            }
            else {
                map.stateMap.put((String) key, json.get(key));
            }

        }

        return map;

    }

    /**Recursively deserializes a JSON array.
     * @param jarray The JSON array to deserialize.
     * @return A new arraylist of generic objects.
    */
    private static ArrayList<Object> deserializeArray(JSONArray jarray) {

        ArrayList<Object> newArray = new ArrayList<>(); //deserialized array

        //deserialize array
        for(int idx = 0; idx < jarray.size(); idx++) {

            Object o = jarray.get(idx);

            if(o instanceof JSONObject) {
                newArray.add(constructFromJson((JSONObject) o));
            }
            else if(o instanceof JSONArray) {
                newArray.add(deserializeArray((JSONArray) o));
            }
            else {
                newArray.add(o);
            }

        }

        return newArray;

    }

    /**Adds a property to the map with the given initial value. If the property is already in the map, nothing happens.
     * @param propertyName The name of the property to track.
     * @param initialValue The desired initial value for the entry.
     * @return True if the property was inserted successfully. If the map already contained an entry for the given
     * property name, false is returned.
    */
    public boolean addProperty(String propertyName, Object initialValue) {

        if(stateMap.get(propertyName) != null) {
            return false;
        }

        stateMap.put(propertyName, initialValue);
        return true;

    }

    /**Gets the value of the given property.
     * @param propertyName The name of the property to retrieve.
     * @return An object representing the stored value for the property. If the property is not in the map, null is
     * returned.
    */
    public Object getPropertyValue(String propertyName) {
        return stateMap.get(propertyName);
    }

    /**Sets the value of a given property.
     * @param propertyName The name of the property to set.
     * @param newValue The new value to set for the property.
     * @return True if the value was set successfully. If the given property is not in the map, false is returned.
    */
    public boolean setPropertyValue(String propertyName, Object newValue) {

        if(stateMap.get(propertyName) == null) {
            return false;
        }

        stateMap.put(propertyName, newValue);
        return true;

    }

    /**Serializes this state map into a JSON object for transfer across the network.
     * @return The JSON object representing this game state.
    */
    JSONObject serializeToJson() {
        return new JSONObject(stateMap);
    }

    /**Returns an integer value associated with the given property, if any.*/
    public int getInt(String propertyName) {
        return ((Number) getPropertyValue(propertyName)).intValue();
    }

    @Override
    public String toJSONString() {
        return new JSONObject(stateMap).toJSONString();
    }

}
