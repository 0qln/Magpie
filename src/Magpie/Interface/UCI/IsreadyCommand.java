package Interface.UCI;


public class IsreadyCommand extends Command
{
    public IsreadyCommand(Engine.IBoard board) {
        super(board);
    }

    public void run() {
        new ReadyokResponse().send();
    }
}
