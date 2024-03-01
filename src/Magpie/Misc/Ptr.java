package Misc;

public class Ptr <T> {
    private T _instance;

    public Ptr(T instance) {
        this._instance = instance;
    }

    public static <T> Ptr<T> to(T instance) {
        return new Ptr<T>(instance);
    }

    public T get() {
        return _instance;
    }
    
    @SuppressWarnings("unchecked")
    public <TResult> TResult getAs() {
        return (TResult)_instance;
    }

    public void set(T instance) {
        this._instance = instance;
    }
}
