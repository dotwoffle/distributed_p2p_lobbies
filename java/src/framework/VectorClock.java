package framework;

/**A class to represent a vector clock.*/
public class VectorClock {
    
    /*Variables*/

    /**The index of this specific clock in the vector of timestamps.*/
    private int index;
    /**The timestamp vector for every process in the system.*/
    private int[] vector;


    /*Constructors*/

    /**Creates and initializes a new vector clock to track n elements.
     * @param n The total number of processes in the system.
     * @param index The index where of this clock in the timestamp vector.
    */
    public VectorClock(int n, int index) {

        //init

        this.index = index;
        this.vector = new int[n];

    }

    /**Initializes a vector clock with an initial value given by the string representing another vector timestamp.*/
    public VectorClock(int n, int index, String timestamp) {

        //init

        this.index = index;
        this.vector = new int[n];

        //fill vector

        int idx = 0;
        for(String time : timestamp.split(",")) {
            vector[idx] = Integer.parseInt(time);
            idx++;
        }

    }


    /**Methods*/

    /**Updates this vector clock when its owner process sends an event.*/
    public synchronized void updateFromSendEvent() {
        vector[index]++; //just increment this process's clock
    }

    /**Updates this vector clock when its owner process receives an event with a timestamp.
     * @param eventTimestamp The timestamp of the event.
    */
    public synchronized void updateFromReceiveEvent(VectorClock eventTimestamp) {

        vector[index]++; //increment this clock

        //compare all timestamps and take largest from each
        for(int idx = 0; idx < vector.length; idx++) {
            vector[idx] = Integer.max(vector[idx], eventTimestamp.vector[idx]);
        }

    }

    /**Compares this timestamp to another timestamp.
     * @param v The other vector timestamp to compare against.
     * @return -1 if this timestamp is less than the other, 1 if this timestamp is greater than the other, or 0 if they
     * are concurrent (not comparable).
    */
    public int compareTo(VectorClock v) {

        //vectors must be same size
        if(v.vector.length != vector.length) {
            return 0;
        }

        if(isLessThan(v)) {
            return -1;
        }
        else if(v.isLessThan(this)) {
            return 1;
        }
        else {
            return 0;
        }

    }

    /**Determines if this timestamp happened before another timestamp.*/
    private boolean isLessThan(VectorClock v) {

        boolean atLeastOneLess = false; //one element here is less than its corresponding element in other

        //check all entries
        for(int idx = 0; idx < vector.length; idx++) {

            if(vector[idx] < v.vector[idx]) {
                atLeastOneLess = true;
            }

            if(vector[idx] > v.vector[idx]) {
                return false;
            }

        }

        return atLeastOneLess;

    }

    @Override
    public String toString() {

        String clockString = "";

        for(int component : vector) {
            clockString += Integer.toString(component) + ",";
        }

        return clockString.substring(0, clockString.length()-1);

    }

}