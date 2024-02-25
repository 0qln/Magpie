package Interface.Custom;

import java.util.logging.Logger;

import Interface.ICommand;
import Misc.LoggerConfigurator;

public abstract class Command extends ICommand
{
    protected Engine.IBoard _board;
    protected Logger logger = LoggerConfigurator.configureLogger(Command.class); 
    
    public Command(Engine.IBoard board) {
        logger.info("Creating new Custom Command");
        _board = board;
    }
}