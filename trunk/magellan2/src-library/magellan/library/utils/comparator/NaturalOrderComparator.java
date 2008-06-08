package magellan.library.utils.comparator;

import java.util.Comparator;

/**
 * Default Comparator for  Comparable objects
 *
 * @deprecated unnecessary?
 */
public class NaturalOrderComparator<T extends Comparable<T>> implements Comparator<T> {

    private NaturalOrderComparator(){}
  
    public int compare(T o1, T o2) {
        return o1.compareTo(o2);
    }

}
