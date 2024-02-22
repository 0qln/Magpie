import java.util.*;

public class UciOptionResponse extends UciResponse
{
    public UciOptionResponse()
    {
    }

    @Override
    protected void executeSend() {
        for (UciOption<?> option : Config.Options) {
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
