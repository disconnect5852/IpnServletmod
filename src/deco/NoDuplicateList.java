package deco;
import java.util.ArrayList;
import java.util.Collection;


public class NoDuplicateList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 4373053271757212201L;
    @Override
    public boolean add(E e) {
        if (this.contains(e)) {
            return false;
        }
        else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        Collection<E> copy = new ArrayList<E>(collection);
        copy.removeAll(this);
        return super.addAll(copy);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        Collection<E> copy = new ArrayList<E>(collection);
        copy.removeAll(this);
        return super.addAll(index, copy);
    }

    @Override
    public void add(int index, E element) {
        if (this.contains(element)) {
            return;
        }
        else {
            super.add(index, element);
        }
    }
    
}
