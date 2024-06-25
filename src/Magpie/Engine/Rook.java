package Engine;

import static Engine.Utils.*;

public class Rook extends SlidingPiece {

    public static final MoveGenerator generator = new Rook.MoveGenerator();

    public static final int ID_Type = 4;
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
            index = generateCaptures(list, index, board, color);
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
                        board.getBitboard(Rook.ID_Type, color),
                        Move.QUIET_MOVE_FLAG);
            // captures
            return generate(
                    list,
                    index,
                    checkerBB,
                    board.getOccupancy(),
                    board.getBitboard(Rook.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        public int quiets(short[] list, int index, Board board, int color) {

            // [Ignored case] in case of a double check, this function should not get called
            // assert (board.isInDoubleCheck() == false);

            return generate(
                    list,
                    index,
                    // only non-occupied squares
                    ~board.getOccupancy()
                    // [General restriction] when in check, allow only check blocking moves
                    // & (board.isInSingleCheck() ? board.getCheckBlockSqs() : ~0x0L)
                    ,
                    board.getOccupancy(),
                    board.getBitboard(Rook.ID_Type, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int generateCaptures(short[] list, int index, Board board, int color) {

            // [Ignored case] in case of a double check, this function should not get called
            // assert (board.isInDoubleCheck() == false);

            return generate(
                    list,
                    index,
                    // only squares with enemy pieces
                    board.getCBitboard(Color.NOT(color))
                    // [General restriction] when in check, allow only check resolving captures
                    // & (board.isInSingleCheck() ? board.getCheckers() : ~0x0L)
                    ,
                    board.getOccupancy(),
                    board.getBitboard(Rook.ID_Type, color),
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
            return (Masks.Files[file(square)] | Masks.Ranks[rank(square)]) ^ target(square);
        }

        @Override
        public long attacks(int square, long occupied) {
            var lookup = lookupAttacks(square, occupied);
            return lookup;
        }
        

        // https://www.chessprogramming.org/Magic_Bitboards
        
        private static final int[] _magicBits = {
          12, 11, 11, 11, 11, 11, 11, 12,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          12, 11, 11, 11, 11, 11, 11, 12        
        };

        private static final long[] _magics = new long[] { 5944751645857415298L, 1459170952729333760L, 3638943685435981832L, 72066531865919492L, 4935947407809603585L, -9151313342096998362L, 288252368582616592L, 2449958747616329988L, -9114863398348126076L, -9223090422220320512L, -8066791082402250496L, 4644405843593218L, -9186780178212577268L, 281578089742592L, -8501388433828865536L, -8932326767792717309L, 36029071898970448L, 580984418554888192L, 306386062173970432L, 1243276071911038986L, 76702480976578562L, 2904963047468569600L, 100350229525639202L, 1351082087511246084L, 4672625490471698432L, 288863731358630016L, 5228679309648201730L, 144203151154086017L, 45317630165321728L, 5066558171776008L, 2499207110854658L, 5764608648315687076L, 2392812188339232L, 1017919087168196615L, 1513361276128993280L, 598415939033108488L, 292742774027977728L, 844442126780936L, 583271132224851976L, 1131398572277892L, 1738459827058802692L, 35185714806786L, 4036641574543753232L, 52949464252448L, 365918023908884500L, -1151795054910603252L, 72075403170414600L, 5769112362842456068L, 689087301757698176L, 1459308119490373888L, -8646770340902469504L, 4612952724542587008L, 74766925004928L, 4398097171200L, -9204213587486698496L, 18656792750066176L, 72340238998962241L, 4900057406946476137L, -6898740534289752063L, 162147183150237729L, 281767168706065L, 4785229223436833L, 36591781684715650L, 829015457926L, };

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
            SlidingPiece.MoveGenerator.Initialize(Rook.generator, Rook.generator);
        }
        
        public long lookupAttacks(int square, long occupied) {
            // Get the magic key.
            int key = getKey(occupied & relevantOccupancy(square), _magics[square], _magicBits[square]);

            // Return the lookup.
            return _attacks[square][key];
        }
        
        public long relevantOccupancy(int square) {
            long result = Masks.RelevantFiles[file(square)] | Masks.RelevantRanks[rank(square)];
            result &= ~target(square);
            return result;
        }


        @Override
        public long computeAttacks(int square, long occupied) {
            final long fileBB = Masks.Files[square % 8];
            final long rankBB = Masks.Ranks[square / 8];
            final long nortBB = Utils.splitBBNorth(square);
            final long soutBB = Utils.splitBBSouth(square);

            long occupands, moves, result = 0, ray;
            int nearest;

            // South
            ray = fileBB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.Sout) & ray;
            result |= moves;

            // North
            ray = fileBB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.Nort) & ray;
            result |= moves;

            // West
            ray = rankBB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.West) & ray;
            result |= moves;

            // East
            ray = rankBB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.East) & ray;
            result |= moves;

            return result;
        }

    }

}
