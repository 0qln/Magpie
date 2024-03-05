package Interface;

public class ReadyokResponse extends Response
{
    @Override
    protected void executeSend() {
        System.out.println("readyok");
    }
}
