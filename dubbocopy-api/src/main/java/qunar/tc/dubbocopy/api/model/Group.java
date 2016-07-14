package qunar.tc.dubbocopy.api.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author kelly.li
 * @date 2015-06-29
 */
public class Group implements Iterable<Target>, Serializable {

	private final String name;

	private int n;

	private final AtomicInteger index = new AtomicInteger(0);

	private CopyOnWriteArraySet<Target> targets = new CopyOnWriteArraySet<Target>();

	public Group(String name) {
		this.name = name;
		this.n = 1;
	}

	public Group(String name, int n) {
		this.name = name;
		this.n = n;
	}

	public void add(Target target) {
		targets.add(target);
	}

	public void addAll(Set<Target> targets) {
		this.targets.addAll(targets);
	}

	public void remove(Target target) {
		targets.remove(target);
	}

	public int size() {
		return targets.size();
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getN() {
		return n;
	}

	public void setTargets(CopyOnWriteArraySet<Target> targets) {
		this.targets = targets;
	}

	public CopyOnWriteArraySet<Target> getTargets() {
		return targets;
	}

	public Iterator<Target> iterator() {
		return targets.iterator();
	}

	public Target get(int index) {
		int i = 0;
		for (Target target : targets) {
			if (i++ == index)
				return target;
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public int nextAndGet() {
		return index.incrementAndGet();
	}

	@Override
	public String toString() {
		return "Group{" + "name='" + name + '\'' + "n=" + n + ", targets=" + targets + '}';

	}
}