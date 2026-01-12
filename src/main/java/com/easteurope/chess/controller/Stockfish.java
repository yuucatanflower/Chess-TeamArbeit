package com.easteurope.chess.controller;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Stockfish {

    private Process engineProcess;
    private BufferedReader processReader;
    private OutputStreamWriter processWriter;

    // path to stockfish
    private static final String PATH = "engine/stockfish.exe";

    public boolean startEngine() {
        try {
            ProcessBuilder builder = new ProcessBuilder(PATH);
            engineProcess = builder.start();
            processReader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            processWriter = new OutputStreamWriter(engineProcess.getOutputStream());
            //sends standard uci command
            sendCommand("uci");
            return true;
        } catch (IOException e) {
            System.err.println("Could not start Stockfish! Check path: " + new File(PATH).getAbsolutePath());
            return false;
        }
    }

    public void sendCommand(String command) {
        try {
            processWriter.write(command + "\n");
            processWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a specific ranked move.
     *  fen Current board state
     *  waitTimeMs Thinking time
     *  rank 1 for Best Move, 2 for Second Best, etc.
     *  The move string (e.g. "e2e4")
     */
    //Multiple Principal Variations
    public String getRankedMove(String fen, int waitTimeMs, int rank) {
        // Tell Stockfish to calculate enough lines
        // We need at least 'rank' lines to find the N-th best move
        sendCommand("setoption name MultiPV value " + rank);
//
        sendCommand("position fen " + fen);
        sendCommand("go movetime " + waitTimeMs);

        // Map to store the latest calculation for each rank (1st, 2nd, etc.)
        Map<Integer, String> rankedMoves = new HashMap<>();

        String line;
        try {
            while ((line = processReader.readLine()) != null) {
                // Stop when engine is done
                if (line.startsWith("bestmove")) {
                    break;
                }

                // Parse info lines to find rankings
                // Example: "info depth 10 ... multipv 2 ... pv e2e4"
                if (line.contains("multipv") && line.contains(" pv ")) {
                    try {
                        // Extract Rank ID
                        String[] parts = line.split(" multipv ");
                        String afterMultipv = parts[1].split(" ")[0]; // Get the number
                        int currentRank = Integer.parseInt(afterMultipv);

                        // Extract The Move
                        String[] pvParts = line.split(" pv ");
                        String move = pvParts[1].split(" ")[0]; // Get the first move after "pv"

                        // Update our map
                        rankedMoves.put(currentRank, move);
                    } catch (Exception ignored) {
                        // Ignore parsing errors for weird lines
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reset MultiPV to 1 to save CPU for normal moves later
        sendCommand("setoption name MultiPV value 1");

        // Return the requested rank, or default to the best available if rank not found
        return rankedMoves.getOrDefault(rank, rankedMoves.get(1));
    }

    /**
     * Helper for standard best move (Rank 1)
     */
    public String getBestMove(String fen, int waitTimeMs) {
        return getRankedMove(fen, waitTimeMs, 1);
    }

    public void stopEngine() {
        try {
            sendCommand("quit");
            if (processReader != null) processReader.close();
            if (processWriter != null) processWriter.close();
            if (engineProcess != null) engineProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}