package Engine;

public class None extends PieceType {

    public static final int ID_Type = 0;
    public static final int ID_White = Piece.create(ID_Type, Color.White);
    public static final int ID_Black = Piece.create(ID_Type, Color.Black);


    @Override
    public MoveGenerator getGenerator() {
        return null;
    }

}
