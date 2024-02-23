package Interface.UCI;

/*
uci
    tell engine to use the uci (universal chess interface),
    this will be sent once as a first command after program boot
    to tell the engine to switch to uci mode.
    After receiving the uci command the engine must identify itself with the "id" command
    and send the "option" commands to tell the GUI which engine settings the engine supports if any.
    After that the engine should send "uciok" to acknowledge the uci mode.
    If no uciok is sent within a certain time period, the engine task will be killed by the GUI.
*/
public class UciCommand extends Command
{
    public UciCommand(Engine.IBoard board)
    {
        super(board);
    }
    
    public void run() {
        new IdResponse("Magpie", "Lucia Ocean Bartschick, Linus Nagel").send();
        new OptionResponse().send();
        new UciokResponse().send();
    }    
}
