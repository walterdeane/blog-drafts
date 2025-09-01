import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DirectoryComparator {
    
    public static class ComparisonResult {
        private final Set<Path> addedFiles;
        private final Set<Path> removedFiles;
        private final Set<Path> modifiedFiles;
        
        public ComparisonResult() {
            this.addedFiles = new HashSet<>();
            this.removedFiles = new HashSet<>();
            this.modifiedFiles = new HashSet<>();
        }
        
        public Set<Path> getAddedFiles() { return addedFiles; }
        public Set<Path> getRemovedFiles() { return removedFiles; }
        public Set<Path> getModifiedFiles() { return modifiedFiles; }
        
        public boolean hasChanges() {
            return !addedFiles.isEmpty() || !removedFiles.isEmpty() || !modifiedFiles.isEmpty();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Directory Comparison Results:\n");
            
            if (addedFiles.isEmpty() && removedFiles.isEmpty() && modifiedFiles.isEmpty()) {
                sb.append("No changes detected.\n");
                return sb.toString();
            }
            
            if (!addedFiles.isEmpty()) {
                sb.append("\nAdded Files (").append(addedFiles.size()).append("):\n");
                addedFiles.stream().sorted().forEach(file -> sb.append("  + ").append(file).append("\n"));
            }
            
            if (!removedFiles.isEmpty()) {
                sb.append("\nRemoved Files (").append(removedFiles.size()).append("):\n");
                removedFiles.stream().sorted().forEach(file -> sb.append("  - ").append(file).append("\n"));
            }
            
            if (!modifiedFiles.isEmpty()) {
                sb.append("\nModified Files (").append(modifiedFiles.size()).append("):\n");
                modifiedFiles.stream().sorted().forEach(file -> sb.append("  * ").append(file).append("\n"));
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Compares two directories recursively and returns the differences
     * @param dir1 First directory path
     * @param dir2 Second directory path
     * @return ComparisonResult containing added, removed, and modified files
     * @throws IOException if an I/O error occurs
     */
    public static ComparisonResult compareDirectories(Path dir1, Path dir2) throws IOException {
        if (!Files.isDirectory(dir1) || !Files.isDirectory(dir2)) {
            throw new IllegalArgumentException("Both paths must be directories");
        }
        
        ComparisonResult result = new ComparisonResult();
        
        // Get all files from both directories
        Map<Path, FileInfo> files1 = getAllFiles(dir1);
        Map<Path, FileInfo> files2 = getAllFiles(dir2);
        
        // Find added and modified files
        for (Map.Entry<Path, FileInfo> entry : files2.entrySet()) {
            Path relativePath = entry.getKey();
            FileInfo fileInfo2 = entry.getValue();
            
            if (!files1.containsKey(relativePath)) {
                // File exists only in dir2 (added)
                result.getAddedFiles().add(relativePath);
            } else {
                // File exists in both directories, check if modified
                FileInfo fileInfo1 = files1.get(relativePath);
                if (!fileInfo1.equals(fileInfo2)) {
                    result.getModifiedFiles().add(relativePath);
                }
            }
        }
        
        // Find removed files
        for (Path relativePath : files1.keySet()) {
            if (!files2.containsKey(relativePath)) {
                // File exists only in dir1 (removed)
                result.getRemovedFiles().add(relativePath);
            }
        }
        
        return result;
    }
    
    /**
     * Compares two directories using string paths
     * @param dir1Path First directory path as string
     * @param dir2Path Second directory path as string
     * @return ComparisonResult containing added, removed, and modified files
     * @throws IOException if an I/O error occurs
     */
    public static ComparisonResult compareDirectories(String dir1Path, String dir2Path) throws IOException {
        return compareDirectories(Paths.get(dir1Path), Paths.get(dir2Path));
    }
    
    /**
     * Recursively gets all files in a directory with their metadata
     * @param rootDir The root directory to scan
     * @return Map of relative paths to FileInfo objects
     * @throws IOException if an I/O error occurs
     */
    private static Map<Path, FileInfo> getAllFiles(Path rootDir) throws IOException {
        Map<Path, FileInfo> files = new HashMap<>();
        
        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = rootDir.relativize(file);
                files.put(relativePath, new FileInfo(file, attrs));
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("Failed to visit file: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
        
        return files;
    }
    
    /**
     * Inner class to store file metadata for comparison
     */
    private static class FileInfo {
        private final long size;
        private final long lastModifiedTime;
        private final String hash;
        
        public FileInfo(Path file, BasicFileAttributes attrs) throws IOException {
            this.size = attrs.size();
            this.lastModifiedTime = attrs.lastModifiedTime().toMillis();
            this.hash = calculateFileHash(file);
        }
        
        private String calculateFileHash(Path file) throws IOException {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hashBytes = md.digest(Files.readAllBytes(file));
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                // Fallback to size + modification time if hash calculation fails
                return size + "_" + lastModifiedTime;
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            FileInfo other = (FileInfo) obj;
            return size == other.size && 
                   lastModifiedTime == other.lastModifiedTime && 
                   Objects.equals(hash, other.hash);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(size, lastModifiedTime, hash);
        }
    }
    
    /**
     * Main method for testing the directory comparison
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java DirectoryComparator <directory1> <directory2>");
            System.out.println("Example: java DirectoryComparator /path/to/dir1 /path/to/dir2");
            return;
        }
        
        try {
            String dir1Path = args[0];
            String dir2Path = args[1];
            
            System.out.println("Comparing directories:");
            System.out.println("  Dir1: " + dir1Path);
            System.out.println("  Dir2: " + dir2Path);
            System.out.println();
            
            ComparisonResult result = compareDirectories(dir1Path, dir2Path);
            System.out.println(result);
            
        } catch (IOException e) {
            System.err.println("Error comparing directories: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid arguments: " + e.getMessage());
        }
    }
}
