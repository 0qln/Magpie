package Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Event<T> {

    private List<Consumer<T>> _callbacks = new ArrayList<>();

    public static class Dispatcher<T> {
    private List<Consumer<T>> _callbacks = new ArrayList<>();
        public void dispatch(IGenerator<T> dataGenerator) {
            for (Consumer<T> callback : _callbacks) {
                callback.accept(dataGenerator.generate());
            }
        }
    }

    public void register(Consumer<T> callback) {
        _callbacks.add(callback);
    }

    // The dispatcher of the event should decide who get's to dispatch the event.
    // That's why an event requires an external dispatcher in order to be created.
    // The creator of the event thus get's forced to decide who will use the dispatcher
    // that he created.
    public Event(Dispatcher<T> dispatcher) {
        dispatcher._callbacks = _callbacks;
    }

    @FunctionalInterface
    public static interface IGenerator<T> {
        T generate();
    }

}
