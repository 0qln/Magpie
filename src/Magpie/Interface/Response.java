package Interface;

import java.util.concurrent.locks.*;

public abstract class Response implements IResponse
{
    private static final Lock _lock = new ReentrantLock();
    
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
