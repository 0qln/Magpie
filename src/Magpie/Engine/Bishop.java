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


        private static final long[] _magics = new long[] { 578818414479549057L, -8633387287095147390L, 5264743983218432005L, -4034077339477064704L, 21396633723994368L, 285951419484161L, 566536339196288L, 70678049980928L, 1152931537673683249L, 72101578815049984L, 581685649553156389L, 8959314378752L, 4613183562122797158L, 27736289197687872L, 72138007672522496L, 1164181604277706784L, -8853865486226268032L, 794895367845707856L, 617003183084101728L, -9142272050566512624L, 3378045475889152L, 9570170700366080L, 324832021019107456L, -9223231278692169728L, -8933804516587729776L, -8644078942044286846L, 76798688445628432L, 2323861810622562336L, 288797724185297408L, 1153485013040759048L, 20310797431734592L, 19492805877466112L, 146407945100593152L, 4615081413421701760L, 149749240275796993L, -9223229098194108288L, 4579467004215361L, 873707125950398536L, 217591229427517440L, 1155182652416917824L, 2307042715185055808L, 56649072991273985L, 793198821504254977L, 283602060416L, 1153097443919791105L, 18017714241290785L, -9186164560652697344L, -9221113622653038560L, 18234326697478208L, 2310981336965858306L, 1652624458240L, -9214364836491558392L, 9969726521344L, -5710493906819248128L, 45075664730472480L, 1162510076305623040L, 81135445515182208L, 3472416892029928225L, -9223371485991140352L, -8935141381460940800L, 68988045824L, 326510990432797185L, 35742789664896L, 2963934266574241864L, };

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

        public long relevantOccupancy(int square) {
            long relevantOccupancy =  
                // Pieces that aren't in the bishops diagonals aren't relevant.
                attacks(square) 
                // Pieces that are on the outer edges aren't relevant.
                & Masks.RelevantOccupancy;

            return relevantOccupancy;
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
