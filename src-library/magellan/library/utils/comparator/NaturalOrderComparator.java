package magellan.library.utils.comparator;

import java.util.Comparator;

public class NaturalOrderComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        return ((Comparable) o1).compareTo((Comparable) o2);
    }

}
