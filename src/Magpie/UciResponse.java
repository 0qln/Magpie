import java.util.concurrent.locks.*;

public abstract class UciResponse implements IResponse
{
    private static Lock _lock = new ReentrantLock();
    
    public final void send() {
        _lock.lock();
        try {
            executeSend();
        } finally {
            _lock.unlock();
        }
    }
    
    protected abstract void executeSend();
}
