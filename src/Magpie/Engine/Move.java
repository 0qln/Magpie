package Engine;

public final class Move {

    public static final int FROM_SHIFT = 0;
    public static final int TO_SHIFT = 6;
    public static final int FLAG_SHIFT = 12;

    public static final int QUIET_MOVE_FLAG = 0;
    public static final int DOUBLE_PAWN_PUSH_FLAG = 1;
    public static final int PROMOTION_KNIGHT_FLAG = 2;
    public static final int PROMOTION_BISHOP_FLAG = 3;
    public static final int PROMOTION_ROOK_FLAG = 4;
    public static final int PROMOTION_QUEEN_FLAG = 5;
    public static final int CAPTURE_PROMOTION_KNIGHT_FLAG = 6;
    public static final int CAPTURE_PROMOTION_BISHOP_FLAG = 7;
    public static final int CAPTURE_PROMOTION_ROOK_FLAG = 8;
    public static final int CAPTURE_PROMOTION_QUEEN_FLAG = 9;
    public static final int KING_CASTLE_FLAG = 10;
    public static final int QUEEN_CASTLE_FLAG = 11;
    public static final int CAPTURE_FLAG = 12;
    public static final int EN_PASSANT_FLAG = 13;

    public static final int QUIET_MOVE_MASK					= QUIET_MOVE_FLAG				<< FLAG_SHIFT;
    public static final int DOUBLE_PAWN_PUSH_MASK			= DOUBLE_PAWN_PUSH_FLAG			<< FLAG_SHIFT;
    public static final int KING_CASTLE_MASK				= KING_CASTLE_FLAG				<< FLAG_SHIFT;
    public static final int QUEEN_CASTLE_MASK				= QUEEN_CASTLE_FLAG				<< FLAG_SHIFT;
    public static final int CAPTURE_MASK					= CAPTURE_FLAG					<< FLAG_SHIFT;
    public static final int EN_PASSANT_MASK					= EN_PASSANT_FLAG				<< FLAG_SHIFT;
    public static final int PROMOTION_KNIGHT_MASK			= PROMOTION_KNIGHT_FLAG			<< FLAG_SHIFT;
    public static final int PROMOTION_BISHOP_MASK			= PROMOTION_BISHOP_FLAG			<< FLAG_SHIFT;
    public static final int PROMOTION_ROOK_MASK				= PROMOTION_ROOK_FLAG			<< FLAG_SHIFT;
    public static final int PROMOTION_QUEEN_MASK			= PROMOTION_QUEEN_FLAG			<< FLAG_SHIFT;
    public static final int CAPTURE_PROMOTION_KNIGHT_MASK	= CAPTURE_PROMOTION_KNIGHT_FLAG	<< FLAG_SHIFT;
    public static final int CAPTURE_PROMOTION_BISHOP_MASK	= CAPTURE_PROMOTION_BISHOP_FLAG	<< FLAG_SHIFT;
    public static final int CAPTURE_PROMOTION_ROOK_MASK		= CAPTURE_PROMOTION_ROOK_FLAG	<< FLAG_SHIFT;
    public static final int CAPTURE_PROMOTION_QUEEN_MASK	= CAPTURE_PROMOTION_QUEEN_FLAG	<< FLAG_SHIFT;


    public static short create(int from, int to, int flag) {
        return (short)((from << FROM_SHIFT) | 
                       (to << TO_SHIFT) | 
                       (flag << FLAG_SHIFT)); 
    }
    
}
