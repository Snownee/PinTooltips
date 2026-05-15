package snownee.pintooltips.util;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

public class Event<T> {
	private final InvokerFactory<T> invokerFactory;
	private final ReferenceArrayList<T> listeners = new ReferenceArrayList<>();
	private T invoker;

	public Event(InvokerFactory<T> invokerFactory) {
		this.invokerFactory = invokerFactory;
		updateInvoker();
	}

	public void register(T listener) {
		listeners.add(listener);
		updateInvoker();
	}

	public T getInvoker() {
		return invoker;
	}

	private void updateInvoker() {
		invoker = invokerFactory.create(listeners);
	}

	@FunctionalInterface
	public interface InvokerFactory<T> {
		T create(List<T> listeners);
	}
}