package com.jd.workflow.soap.common.util;

import java.util.*;

public class CollectionHelper {
    public static <T> List<T> toList(Iterable<T> iter) {
        if (iter == null) {
            return null;
        } else if (iter instanceof List) {
            return (List)iter;
        } else if (iter instanceof Collection) {
            return new ArrayList((Collection)iter);
        } else {
            ArrayList list = new ArrayList();
            Iterator listIter = iter.iterator();

            while(listIter.hasNext()) {
                Object var3 = listIter.next();
                list.add(var3);
            }

            return list;
        }
    }

    public static <T> Set<T> toSet(Collection<? extends T> collection) {
        if (collection == null) {
            return null;
        } else if (collection instanceof Set) {
            return (Set)collection;
        } else {
            LinkedHashSet set = new LinkedHashSet(collection);
            return set;
        }
    }
    public static List repeatNull(int size){
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(null);
        }
        return list;
    }
    public static int size(Collection c){
        return c==null?0:c.size();
    }


}
