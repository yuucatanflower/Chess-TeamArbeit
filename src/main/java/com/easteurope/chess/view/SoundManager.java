package com.easteurope.chess.view;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static final String SOUND_PATH = "/sound/";

    // --- Volume State ---
    // 1.0 = 100%, 0.5 = 50%
    private static double masterVolume = 1.0;
    private static double musicVolume = 0.4;
    private static double uiVolume = 0.4;

    private static final Map<String, AudioClip> soundCache = new HashMap<>();
    private static MediaPlayer musicPlayer;
    private static boolean isMusicMuted = false;

    public static void loadSounds() {
        loadClip("start", "chess_piece_select.wav");
        loadClip("move", "chess_move.mp3");
        loadClip("knight_move", "chess_knight_move.wav");
        loadClip("pause", "chess_pause_game.wav");
        loadClip("promote", "chess_promotion.wav");
        loadClip("click", "chess_select.wav");
        loadClip("capture", "chess_takes.wav");
        loadClip("victory", "chess_victory.wav");
        loadClip("castle", "chess_castle.wav");
        loadClip("check", "chess_check.wav");
        loadClip("checkmate", "chess_checkmate.wav");
        loadClip("defeat", "chess_game_over.wav");
        loadClip("illegal", "chess_illegal_move.wav");
        loadClip("switch_turn", "chess_switch_turn.wav");

        // Setup Background Music
        try {
            URL musicUrl = SoundManager.class.getResource(SOUND_PATH + "menu_bg1.mp3");
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                updateMusicVolume(); // Apply initial volume
            }
        } catch (Exception e) {
            System.err.println("Could not load background music.");
        }
    }

    private static void loadClip(String key, String filename) {
        try {
            URL url = SoundManager.class.getResource(SOUND_PATH + filename);
            if (url != null) {
                AudioClip clip = new AudioClip(url.toExternalForm());
                soundCache.put(key, clip);
            } else {
                System.err.println("Sound missing: " + filename);
            }
        } catch (Exception e) {
            System.err.println("Error loading sound: " + filename);
        }
    }

    public static void playSound(String key) {
        AudioClip clip = soundCache.get(key);
        if (clip != null) {
            // Calculate effective volume: Master * UI
            clip.setVolume(masterVolume * uiVolume);
            clip.play();
        }
    }

    // --- Volume Setters ---

    public static void setMasterVolume(double volume) {
        masterVolume = Math.max(0.0, Math.min(1.0, volume)); // Clamp 0-1
        updateMusicVolume();
    }

    public static void setMusicVolume(double volume) {
        musicVolume = Math.max(0.0, Math.min(1.0, volume));
        updateMusicVolume();
    }

    private static void updateMusicVolume() {
        if (musicPlayer != null) {
            // Effective music volume is Master * Music setting
            musicPlayer.setVolume(masterVolume * musicVolume);
        }
    }

    // --- Music Controls ---

    public static void startMusic() {
        if (musicPlayer != null && !isMusicMuted) musicPlayer.play();
    }

    public static void stopMusic() {
        if (musicPlayer != null) musicPlayer.stop();
    }

    public static void toggleMusic() {
        if (musicPlayer == null) return;
        if (musicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            musicPlayer.pause();
            isMusicMuted = true;
        } else {
            musicPlayer.play();
            isMusicMuted = false;
        }
    }

    public static boolean isMusicPlaying() {
        return musicPlayer != null && musicPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    // Getters for UI Sliders
    public static double getMasterVolume() { return masterVolume; }
    public static double getMusicVolume() { return musicVolume; }
}