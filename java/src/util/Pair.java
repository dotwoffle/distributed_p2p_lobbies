package util;


/**Simple class to hold a pair of objects. Java stl continues to disappoint.*/
public class Pair<T1, T2> {

    /*Variables*/
    
    /**The first object in the pair.*/
    public T1 first;
    /**The second object in the pair.*/
    public T2 second;


    /*Constructor*/

    /**Creates a pair with the two given objects.*/
    public Pair(T1 first, T2 second) {

        //init

        this.first = first;
        this.second = second;

    }

}
