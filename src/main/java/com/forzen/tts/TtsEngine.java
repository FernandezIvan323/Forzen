package com.forzen.tts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TtsEngine {

    private final ExecutorService executor;
    private boolean available;
    private boolean speaking;

    public TtsEngine() {
        this.executor = Executors.newSingleThreadExecutor();
        try {
            Process p = Runtime.getRuntime().exec(new String[]{
                "powershell", "-Command",
                "Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer)"
            });
            p.waitFor();
            available = p.exitValue() == 0;
            if (available) {
                System.out.println("TTS: Windows Speech API available");
            } else {
                System.out.println("TTS: Windows Speech API not available");
            }
        } catch (Exception e) {
            System.out.println("TTS not available: " + e.getMessage());
            available = false;
        }
    }

    public void speak(String text) {
        if (!available || text == null || text.isEmpty() || speaking) return;
        speaking = true;
        executor.submit(() -> {
            try {
                String escaped = text.replace("'", "''").replace("\"", "\"\"");
                Process p = Runtime.getRuntime().exec(new String[]{
                    "powershell", "-Command",
                    "Add-Type -AssemblyName System.Speech; " +
                    "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$s.Speak('" + escaped + "'); $s.Dispose()"
                });
                p.waitFor();
            } catch (Exception e) {
                System.err.println("TTS error: " + e.getMessage());
            } finally {
                speaking = false;
            }
        });
    }

    public void speakAsync(String text) {
        speak(text);
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
