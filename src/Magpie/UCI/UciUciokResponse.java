package UCI;

/* UCI::
uciok
    Must be sent after the id and optional options to tell the GUI that the engine
    has sent all infos and is ready in uci mode.
 */
public class UciUciokResponse extends UciResponse
{
    public UciUciokResponse()
    {
    }
    
    @Override
    protected void executeSend() {
        System.out.println("uciok");
    }
}
