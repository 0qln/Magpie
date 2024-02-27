package Interface.Custom;

import java.util.logging.Logger;

import Interface.ICommand;
import Misc.LoggerConfigurator;
import Misc.Ptr;

public abstract class Command extends ICommand
{
    protected Ptr<Engine.IBoard> _board;
    protected Logger logger = LoggerConfigurator.configureLogger(Command.class); 
    
    public Command(Ptr<Engine.IBoard> board) {
        logger.info("Creating new Custom Command");
        _board = board;
    }
}