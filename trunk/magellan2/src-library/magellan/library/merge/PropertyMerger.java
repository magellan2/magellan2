package magellan.library.merge;

import static org.apache.commons.beanutils.PropertyUtils.getProperty;
import static org.apache.commons.beanutils.PropertyUtils.setProperty;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Unique;

public class PropertyMerger<T> {
  /*
   * Modifier method to avoid the boiler plate of template code with the constructor
   */
  public static <T> PropertyMerger<T> mergeBeans(T sourceBean, T targetBean) {
    return new PropertyMerger<T>(sourceBean, targetBean);
  }

  private final T sourceBean, targetBean;

  private PropertyMerger(T sourceBean, T targetBean) {
    if (sourceBean == null)
      throw new NullPointerException("source bean may not be null");
    if (targetBean == null)
      throw new NullPointerException("target bean may not be null");
    this.sourceBean = sourceBean;
    this.targetBean = targetBean;
  }

  public PropertyMerger<T> merge(String propertyName) {
    Object value = read(propertyName);
    if (value != null) {
      write(propertyName, value);
    }
    return this;
  }

  public PropertyMerger<T> mergeObjects(String... propertyNames) {
    for (String propertyName : propertyNames) {
      merge(propertyName);
    }
    return this;
  }

  public PropertyMerger<T> mergeBoolean(String propertyName) {
    boolean value = (Boolean) read(propertyName);
    if (value) {
      write(propertyName, value);
    }
    return this;
  }

  public PropertyMerger<T> mergeBooleans(String... propertyNames) {
    for (String propertyName : propertyNames) {
      mergeBoolean(propertyName);
    }
    return this;
  }

  public <E> PropertyMerger<T> mergeEntity(String propertyName, Map<? extends ID, E> entityLookup) {
    if (entityLookup != null) {
      Unique value = (Unique) read(propertyName);
      if (value != null) {
        write(propertyName, entityLookup.get(value.getID()));
      }
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public <X> PropertyMerger<T> modify(String propertyName, Modifier<X> modifier) {
    X value = (X) read(propertyName);
    if (value != null) {
      write(propertyName, modifier.modify(value));
    }
    return this;
  }

  public PropertyMerger<T> mergeResources(boolean firstPass, boolean newTurn, String... resourceNames) {
    for (String resourceName : resourceNames) {
      mergeResource(firstPass, newTurn, resourceName);
    }
    return this;
  }

  public PropertyMerger<T> mergeResource(boolean firstPass, boolean newTurn, String resourceName) {
    // first pass, copy to old property
    String oldResourceName = "old" + Character.toLowerCase(resourceName.charAt(0)) + resourceName.substring(1);
    if (newTurn) {
      if (firstPass) {
        copyInt(resourceName, oldResourceName);
      }
    } else {
      mergeInt(oldResourceName);
    }
    // second pass, overwrite current property
    mergeInt(resourceName);
    return this;
  }

  public PropertyMerger<T> copyInt(String sourcePropertyName, String targetPropertyName) {
    mergeInt(sourcePropertyName, targetPropertyName, 0);
    return this;
  }

  public PropertyMerger<T> mergeNaturalInt(String propertyName) {
    mergeInt(propertyName, propertyName, 1);
    return this;
  }

  public PropertyMerger<T> mergeInts(String... propertyNames) {
    for (String propertyName : propertyNames) {
      mergeInt(propertyName);
    }
    return this;
  }

  public PropertyMerger<T> mergeInt(String propertyName) {
    mergeInt(propertyName, propertyName, 0);
    return this;
  }

  public PropertyMerger<T> mergeInt(String sourcePropertyName, String targetPropertyName, int threshold) {
    int value = (Integer) read(sourcePropertyName);
    if (value >= threshold) {
      write(targetPropertyName, value);
    }
    return this;
  }

  public PropertyMerger<T> replaceList(String propertyName) {
    Collection<?> value = (Collection<?>) read(propertyName);
    if (value != null && value.size() > 0) {
      write(propertyName, new ArrayList<Object>(value));
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public <X> PropertyMerger<T> replaceListForEach(String propertyName, Modifier<X> modifier) {
    Collection<X> value = (Collection<X>) read(propertyName);
    if (value != null && value.size() > 0) {
      ArrayList<Object> targetList = new ArrayList<Object>(value.size());
      for (X o : value) {
        targetList.add(modifier.modify(o));
      }
      write(propertyName, targetList);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public PropertyMerger<T> addList(String propertyName) {
    Collection<T> value = (Collection<T>) read(propertyName);
    if (value != null && value.size() > 0) {
      Collection<T> target = (Collection<T>) read(propertyName);
      if (target == null) {
        target = new ArrayList<T>(value);
      }
      target.addAll(value);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public <X> PropertyMerger<T> addListForEach(String propertyName, Modifier<X> modifier) {
    Collection<X> value = (Collection<X>) read(propertyName);
    if (value != null && value.size() > 0) {
      ArrayList<Object> targetList = new ArrayList<Object>(value);
      for (X o : value) {
        o = modifier.modify(o);
        if (!targetList.contains(o)) {
          targetList.add(o);
        }
      }
      write(propertyName, targetList);
    }
    return this;
  }

  private Object read(String propertyName) {
    try {
      return getProperty(sourceBean, propertyName);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("No access to getter method possible for property " +
          fullPropertyName(sourceBean, propertyName));
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Unexpected exception getting property " +
          fullPropertyName(sourceBean, propertyName));
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("No getter method found for property " +
          fullPropertyName(sourceBean, propertyName));
    }
  }

  private void write(String propertyName, Object value) {
    try {
      setProperty(targetBean, propertyName, value);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("No access to setter method possible for property " +
          fullPropertyName(targetBean, propertyName) + " with value " + value);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Unexpected exception setting property " +
          fullPropertyName(targetBean, propertyName) + " with value " + value);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("No setter method found for property " +
          fullPropertyName(targetBean, propertyName) + " with value " + value);
    }
  }

  private static String fullPropertyName(Object bean, String propertyName) {
    return bean.getClass().getName() + '#' + propertyName;
  }

  public static interface Modifier<T> {
    T modify(T original);
  }
}
