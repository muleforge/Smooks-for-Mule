package org.milyn.smooks.mule.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FilteringMap<K,V> implements Map<K,V> {

	private Map<K, V> delegate;

	private Set<K> filteredKeys = new HashSet<K>();

	public FilteringMap(Map<K, V> delegate) {
		this.delegate = delegate;
	}

	public int size() {
		return delegate.size();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	public V get(Object key) {
		return delegate.get(key);
	}

	public V put(K key, V value) {
		if(filteredKeys.contains(key)) {
			return null;
		}

		return delegate.put(key, value);
	}

	public V remove(Object key) {
		return delegate.remove(key);
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		for(java.util.Map.Entry<? extends K, ? extends V> entry : t.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	public void clear() {
		delegate.clear();
	}

	public Set<K> keySet() {
		return delegate.keySet();
	}

	public Collection<V> values() {
		return delegate.values();
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}

	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public void addFilteredKeys(K ... keys) {
		for(K key : keys) {
			filteredKeys.add(key);
		}
	}

}
