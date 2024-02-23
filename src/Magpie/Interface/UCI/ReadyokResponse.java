package Interface.UCI;

import Interface.Response;

public class ReadyokResponse extends Response
{
    @Override
    protected void executeSend() {
        System.out.println("readyok");
    }
}
