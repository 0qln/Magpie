package Misc;

public class Ptr <T> {
    private T _instance = null;

    public Ptr(T instance) {
        this._instance = instance;
    }

    public static <T> Ptr<T> to(T instance) {
        return new Ptr<T>(instance);
    }
    
    public static <T> Ptr<T> size(Class<T> clazz) {
        return new Ptr<T>(null);
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
    
    public boolean isNull() {
        return _instance == null;
    }
}
