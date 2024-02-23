package Interface.UCI;

import Interface.Response;
import Interface.Config;

public class OptionResponse extends Response
{
    public OptionResponse()
    {
    }

    @Override
    protected void executeSend() {
        for (Option<?> option : Config.UCI_Options) {
            String varStr = "";
            if (option.getVars().isPresent()) { 
                for (int i = 0; i <= option.getVars().get().length; i++) {
                    varStr += " var " + option.getVars().get()[i];
                }
            }
            System.out.println("option " + 
                " name " + option.getName() + 
                " type " + option.getType() + 
                (option.getDefault().isPresent() ? " default " + option.getDefault().get() : "") + 
                (option.getMin().isPresent() ? " min " + option.getMin().get() : "") + 
                (option.getMax().isPresent() ? " max " + option.getMax().get() : "") + 
                varStr);
        }
    }
}
