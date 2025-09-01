import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Recursively compares two directories using java.nio.file.
 * Classifies files as ADDED (only in newDir), REMOVED (only in oldDir), or MODIFIED (in both but different content).
 * Ignores directories and non-regular files. Does not follow symlinks.
 *
 * Usage:
 *   DirectoryDiff.Result r = DirectoryDiff.compare(Paths.get("/old"), Paths.get("/new"));
 *   r.added.forEach(System.out::println);
 *   r.removed.forEach(System.out::println);
 *   r.modified.forEach(System.out::println);
 */
public final class DirectoryDiff {

    private DirectoryDiff() {}


    public static final class Result {
        public final Set<Path> added;    // relative paths
        public final Set<Path> removed;  // relative paths
        public final Set<Path> modified; // relative paths

        public Result(Set<Path> added, Set<Path> removed, Set<Path> modified) {
            this.added = Collections.unmodifiableSet(added);
            this.removed = Collections.unmodifiableSet(removed);
            this.modified = Collections.unmodifiableSet(modified);
            
        }
    }


    private static final class FileInfo {
        final long size;
        byte[] sha256; // computed lazily

        FileInfo(long size) {
            this.size = size;
        }
    }

    public static Result compare(Path oldDir, Path newDir) throws IOException {
        Objects.requireNonNull(oldDir, "oldDir");
        Objects.requireNonNull(newDir, "newDir");
        if (!Files.isDirectory(oldDir)) throw new IllegalArgumentException("oldDir is not a directory: " + oldDir);
        if (!Files.isDirectory(newDir)) throw new IllegalArgumentException("newDir is not a directory: " + newDir);

        Map<Path, FileInfo> oldFiles = indexDirectory(oldDir);
        Map<Path, FileInfo> newFiles = indexDirectory(newDir);

        Set<Path> added = new HashSet<>();
        Set<Path> removed = new HashSet<>();
        Set<Path> modified = new HashSet<>();

        // Union of keys
        Set<Path> all = new HashSet<>(oldFiles.keySet());
        all.addAll(newFiles.keySet());

        for (Path rel : all) {
            FileInfo o = oldFiles.get(rel);
            FileInfo n = newFiles.get(rel);

            if (o == null) {
                added.add(rel);
            } else if (n == null) {
                removed.add(rel);
            } else {
                if (o.size != n.size) {
                    modified.add(rel);
                } else {
                    // Same size: confirm by content hash
                    if (!Arrays.equals(hash(oldDir.resolve(rel), o), hash(newDir.resolve(rel), n))) {
                        modified.add(rel);
                    }
                }
            }
        }

        return new Result(added, removed, modified);
    }

    private static Map<Path, FileInfo> indexDirectory(Path root) throws IOException {
        try (Stream<Path> s = Files.walk(root, FileVisitOption.FOLLOW_LINKS)) {
            return s.filter(p -> {
                        try {
                            // Only count regular files; skip dirs and special files
                            return Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toMap(
                            p -> root.relativize(p),
                            p -> {
                                try {
                                    return new FileInfo(Files.size(p));
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            }));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static byte[] hash(Path path, FileInfo info) throws IOException {
        if (info.sha256 != null) return info.sha256;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(path);
                 DigestInputStream din = new DigestInputStream(in, md)) {
                byte[] buf = new byte[8192];
                while (din.read(buf) != -1) {
                    // reading updates digest
                }
            }
            info.sha256 = md.digest();
            return info.sha256;
            this.
            
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available on the standard JRE
            // so this will never happen
            // this could happen because 
            throw new AssertionError(e);
        }
    }

}

            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}