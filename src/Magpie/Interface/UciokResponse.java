package Interface;

/* UCI::
uciok
    Must be sent after the id and optional options to tell the GUI that the engine
    has sent all infos and is ready in uci mode.
 */
public class UciokResponse extends Response
{
    public UciokResponse()
    {
    }
    
    @Override
    protected void executeSend() {
        System.out.println("uciok");
    }
}
