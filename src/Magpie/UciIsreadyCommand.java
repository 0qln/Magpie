
public class UciIsreadyCommand extends UciCommand
{
    public UciIsreadyCommand(IBoard board) {
        super(board);
    }

    public void run() {
        new UciReadyokResponse().send();
    }
}
