package UCI;


public class UciIsreadyCommand extends UciCommand
{
    public UciIsreadyCommand(Engine.IBoard board) {
        super(board);
    }

    public void run() {
        new UciReadyokResponse().send();
    }
}
