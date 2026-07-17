package com.forzen.tts;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TtsEngine {

    private final ExecutorService executor;
    private final AtomicBoolean speaking = new AtomicBoolean(false);
    private boolean available;
    private Process currentProcess;

    public TtsEngine() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "forzen-tts");
            t.setDaemon(true);
            return t;
        });
        try {
            Process p = new ProcessBuilder(
                    "powershell", "-NoProfile", "-Command",
                    "Add-Type -AssemblyName System.Speech; $null = New-Object System.Speech.Synthesis.SpeechSynthesizer"
            ).redirectErrorStream(true).start();
            boolean finished = p.waitFor(8, java.util.concurrent.TimeUnit.SECONDS);
            available = finished && p.exitValue() == 0;
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
        if (!available || text == null || text.isBlank()) return;
        if (!speaking.compareAndSet(false, true)) {
            // Busy: cancel and re-queue
            stop();
            speaking.set(true);
        }
        final String payload = text.length() > 800 ? text.substring(0, 800) : text;
        executor.submit(() -> {
            try {
                // Escape for single-quoted PowerShell string
                String escaped = payload.replace("'", "''");
                currentProcess = new ProcessBuilder(
                        "powershell", "-NoProfile", "-Command",
                        "Add-Type -AssemblyName System.Speech; "
                                + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                                + "$s.Rate = 0; $s.Speak('" + escaped + "'); $s.Dispose()"
                ).redirectErrorStream(true).start();
                currentProcess.waitFor();
            } catch (Exception e) {
                System.err.println("TTS error: " + e.getMessage());
            } finally {
                currentProcess = null;
                speaking.set(false);
            }
        });
    }

    public void speakAsync(String text) {
        speak(text);
    }

    public void stop() {
        Process p = currentProcess;
        if (p != null) {
            p.destroyForcibly();
        }
        speaking.set(false);
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isSpeaking() {
        return speaking.get();
    }

    public void shutdown() {
        stop();
        executor.shutdownNow();
    }
}
