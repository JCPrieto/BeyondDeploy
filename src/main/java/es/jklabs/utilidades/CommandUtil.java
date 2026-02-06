package es.jklabs.utilidades;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

final class CommandUtil {

    private CommandUtil() {
    }

    static String findCommand() {
        String path = System.getenv("PATH");
        if (path != null && !path.isEmpty()) {
            String[] parts = path.split(File.pathSeparator);
            for (String p : parts) {
                File f = new File(p, "secret-tool");
                if (f.exists() && f.canExecute()) {
                    return f.getAbsolutePath();
                }
            }
        }
        String os = osName();
        if (os.contains("win")) {
            return findCommandWindows();
        }
        return findCommandUnix();
    }

    static CommandResult runCommand(List<String> args, byte[] stdin) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        Process p = pb.start();
        if (stdin != null) {
            p.getOutputStream().write(stdin);
            p.getOutputStream().flush();
            p.getOutputStream().close();
        } else {
            p.getOutputStream().close();
        }
        String stdout = readAll(p.getInputStream());
        String stderr = readAll(p.getErrorStream());
        int code = waitFor(p);
        return new CommandResult(code, stdout, stderr);
    }

    static String osName() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    private static String findCommandUnix() {
        String[] candidates = new String[]{"/usr/bin/" + "secret-tool", "/usr/local/bin/" + "secret-tool", "/bin/" + "secret-tool"};
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists() && f.canExecute()) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    private static String findCommandWindows() {
        try {
            CommandResult result = runCommand(java.util.List.of("where", "secret-tool"), null);
            if (result.exitCode() != 0 || result.stdout() == null || result.stdout().isEmpty()) {
                return null;
            }
            String[] lines = result.stdout().split("\\R");
            return lines.length > 0 ? lines[0].trim() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static int waitFor(Process p) throws IOException {
        try {
            return p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Comando interrumpido", e);
        }
    }

    private static String readAll(java.io.InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    record CommandResult(int exitCode, String stdout, String stderr) {
    }
}
