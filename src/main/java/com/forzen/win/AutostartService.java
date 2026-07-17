package com.forzen.win;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Enable/disable Forzen at Windows logon via HKCU Run key.
 */
public final class AutostartService {

    private static final String RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String VALUE_NAME = "Forzen";

    private AutostartService() {}

    public static boolean isEnabled() {
        try {
            return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, RUN_KEY, VALUE_NAME);
        } catch (Exception e) {
            return false;
        }
    }

    public static void setEnabled(boolean enabled) {
        try {
            if (enabled) {
                String command = resolveLaunchCommand();
                Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, RUN_KEY, VALUE_NAME, command);
                System.out.println("Autostart enabled: " + command);
            } else {
                if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, RUN_KEY, VALUE_NAME)) {
                    Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, RUN_KEY, VALUE_NAME);
                }
                System.out.println("Autostart disabled");
            }
        } catch (Exception e) {
            System.err.println("Autostart registry error: " + e.getMessage());
        }
    }

    /**
     * Prefer packaged exe path; fall back to java -jar of current jar / class path.
     */
    public static String resolveLaunchCommand() {
        String exe = System.getProperty("jpackage.app-path");
        if (exe != null && !exe.isBlank()) {
            return "\"" + exe + "\"";
        }

        // jpackage sets APPDIR
        String appDir = System.getenv("APPDIR");
        if (appDir != null) {
            java.io.File candidate = new java.io.File(appDir, "Forzen.exe");
            if (candidate.isFile()) {
                return "\"" + candidate.getAbsolutePath() + "\"";
            }
        }

        try {
            java.security.CodeSource cs = AutostartService.class.getProtectionDomain().getCodeSource();
            if (cs != null && cs.getLocation() != null) {
                java.io.File loc = new java.io.File(cs.getLocation().toURI());
                if (loc.isFile() && loc.getName().endsWith(".jar")) {
                    String javaHome = System.getProperty("java.home");
                    String javaBin = javaHome + java.io.File.separator + "bin" + java.io.File.separator + "javaw.exe";
                    return "\"" + javaBin + "\" -jar \"" + loc.getAbsolutePath() + "\"";
                }
            }
        } catch (Exception ignored) {
        }

        // Dev: prefer shaded jar in target/ if present next to classes
        try {
            java.security.CodeSource cs = AutostartService.class.getProtectionDomain().getCodeSource();
            if (cs != null && cs.getLocation() != null) {
                java.io.File loc = new java.io.File(cs.getLocation().toURI());
                if (loc.isDirectory()) {
                    java.io.File target = loc.getName().equals("classes") ? loc.getParentFile() : loc;
                    java.io.File[] jars = target.listFiles((dir, name) ->
                            name.startsWith("forzen-") && name.endsWith(".jar") && !name.contains("original"));
                    if (jars != null && jars.length > 0) {
                        java.io.File jar = jars[0];
                        String javaHome = System.getProperty("java.home");
                        String javaBin = javaHome + java.io.File.separator + "bin"
                                + java.io.File.separator + "javaw.exe";
                        return "\"" + javaBin + "\" -jar \"" + jar.getAbsolutePath() + "\"";
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // Last resort: current jar-less classpath is too large for Run key — refuse
        System.err.println("Autostart: no packaged exe/jar found; using best-effort javaw -jar if available");
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + java.io.File.separator + "bin" + java.io.File.separator + "javaw.exe";
        return "\"" + javaBin + "\" -jar forzen.jar";
    }
}
