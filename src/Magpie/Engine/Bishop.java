package Engine;

import static Engine.Utils.*;

public class Bishop extends SlidingPiece {

    public static final MoveGenerator generator = new Bishop.MoveGenerator();
    public static final int ID_Type = 3;
    public static final int ID_White = Piece.create(ID_Type, Color.White);
    public static final int ID_Black = Piece.create(ID_Type, Color.Black);

    @Override
    public MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends SlidingPiece.MoveGenerator implements IMoveLookup {
        
        /*
         * Generate in chunks
         * https://www.chessprogramming.org/Move_Generation#Chunk_Move_Generation
         */

        @Override
        int generate(short[] list, int index, Board board, int color, boolean capturesOnly) {
            index = captures(list, index, board, color);
            if (!capturesOnly)
                index = quiets(list, index, board, color);
            return index;
        }

        @Override
        int resolves(short[] list, int index, Board board, int color, boolean capturesOnly) {
            final long checkerBB = board.getCheckers();
            final int checker = lsb(checkerBB);
            final int king = lsb(board.getBitboard(King.ID_Type, color));
            final long mask = Masks.squaresBetween(king, checker);
            // quiet
            if (!capturesOnly)
                index = generate(
                        list,
                        index,
                        ~board.getOccupancy() & mask,
                        board.getOccupancy(),
                        board.getBitboard(Bishop.ID_Type, color),
                        Move.QUIET_MOVE_FLAG);
            // captures
            return generate(
                    list,
                    index,
                    checkerBB,
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        public int quiets(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    ~board.getOccupancy(),
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int captures(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    board.getCBitboard(Color.NOT(color)),
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        @Override
        public long attacks(int square, int color) {
            return attacks(square);
        }

        @Override
        public long attacks(int square, long occupied, int color) {
            return attacks(square, occupied);
        }

        @Override
        public long attacks(int square) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            return (a1h8BB | a8h1BB) ^ target(square);
        }

        @Override
        public long attacks(int square, long occupied) {
            var lookup = lookupAttacks(square, occupied);
            return lookup;
        }
        

        // https://www.chessprogramming.org/Magic_Bitboards
        
        private static final int[] _magicBits = {
          6, 5, 5, 5, 5, 5, 5, 6,
          5, 5, 5, 5, 5, 5, 5, 5,
          5, 5, 7, 7, 7, 7, 5, 5,
          5, 5, 7, 9, 9, 7, 5, 5,
          5, 5, 7, 9, 9, 7, 5, 5,
          5, 5, 7, 7, 7, 7, 5, 5,
          5, 5, 5, 5, 5, 5, 5, 5,
          6, 5, 5, 5, 5, 5, 5, 6
        };


        private static final long[] _magics = new long[] { 
18015532390318216L,
585470167966843568L,
4506625582956552L,
1130300405989504L,
582809891639312L,
4612968189109208096L,
37159670638452754L,
-7493971629276388319L,
-8646255934549196288L,
1441160745847914752L,
2314854619467550720L,
234346627165332544L,
1171503339702059012L,
45037113137176833L,
-4611683746322431680L,
9079775689312320L,
487536927276796416L,
2380386617161941056L,
3607392119142613008L,
4621256580499783680L,
422504533328960L,
6192453833492992L,
2310698495529453568L,
-9222738576417353727L,
292804585645941256L,
1442696837726466L,
6989661388604440592L,
70643689259520L,
283674067174416L,
220678035308287000L,
3459050386877448448L,
168225287456768L,
-7781639336862934896L,
2306423603296477446L,
211382184710656L,
563225905333504L,
-8065944731447685100L,
1154056236577194113L,
3097616313287680L,
-4303182535413710336L,
1153225048199340032L,
-4035077827949227420L,
5373218801334669572L,
1224979236436314122L,
5044314179188560896L,
-9214363729362124288L,
38289770888102272L,
571789267700864L,
2574025475818120L,
1153484875601545227L,
9018196585873536L,
325387001593857040L,
90074589400530944L,
8867044000786L,
603493362498045097L,
164671664515206208L,
36592039039109152L,
288441775918942212L,
145241226025764870L,
18348675835237892L,
4613938917889213441L,
288881564262335520L,
1161964030172103044L,
567382393491552L,
};

        private static final long[][] _attacks = new long[64][];


        @Override
        public long[] getMagics() {
            return _magics;
        }

        @Override
        public int[] getMagicBits() {
            return _magicBits;
        }

        @Override
        public long[][] getAttacks() {
            return _attacks;
        }


        public static void Initialize() {
            SlidingPiece.MoveGenerator.Initialize(Bishop.generator, Bishop.generator);
        }
        
        public long lookupAttacks(int square, long occupied) {
            // Get the magic key.
            int key = getKey(occupied & relevantOccupancy(square), _magics[square], _magicBits[square]);

            // Return the lookup.
            return _attacks[square][key];
        }

        public long computeAttacks(int square, long occupied) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            final long nortBB = Utils.splitBBNorth(square), soutBB = Utils.splitBBSouth(square);

            long occupands, moves, result = 0, ray;
            int nearest;

            // South East
            ray = a8h1BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoEa) & ray;
            result |= moves;

            // South West
            ray = a1h8BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoWe) & ray;
            result |= moves;

            // North West
            ray = a8h1BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoWe) & ray;
            result |= moves;

            // North East
            ray = a1h8BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoEa) & ray;
            result |= moves;

            return result;
        }

    }

}
