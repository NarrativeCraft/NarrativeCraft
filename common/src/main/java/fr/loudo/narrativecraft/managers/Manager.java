package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class Manager<T extends NarrativeEntry> {

    protected List<T> list = new ArrayList<>();

    public T getByName(String name)
    {
        for (T item : list) {
            if(item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public void add(T item)
    {
        if (!list.contains(item))
        {
            list.add(item);
        }
    }

    public void remove(T item)
    {
        list.remove(item);
    }

    public void clear()
    {
        list.clear();
    }

    public List<T> getList()
    {
        return list;
    }

    public T get(int index)
    {
        return list.get(index);
    }

    public int size()
    {
        return list.size();
    }

}
