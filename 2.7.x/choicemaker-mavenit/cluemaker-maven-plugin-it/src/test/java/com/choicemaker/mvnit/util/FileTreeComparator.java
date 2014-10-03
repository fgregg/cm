package com.choicemaker.mvnit.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import com.choicemaker.util.FileUtilities;

public class FileTreeComparator {

	private final Path root1;
	private final Path root2;
	private final Set<Path> excludedPaths = new HashSet<>();
	private final Set<FileContentListener> listeners = new HashSet<>();

	public FileTreeComparator(Path root1, Path root2) {
		if (root1 == null || root2 == null) {
			throw new IllegalArgumentException("null root");
		}
		this.root1 = root1;
		this.root2 = root2;
	}

	public FileTreeComparator(File directory1, File directory2) {
		if (directory1 == null || directory2 == null) {
			throw new IllegalArgumentException("null root");
		}
		this.root1 = Paths.get(directory1.toURI());
		this.root2 = Paths.get(directory2.toURI());
	}

	public void addExcludedPath(Path p) {
		excludedPaths.add(p);
	}

	public void addExcludedPaths(Set<Path> paths) {
		excludedPaths.addAll(paths);
	}

	public void addListener(FileContentListener l) {
		listeners.add(l);
	}

	public void addListeners(Set<FileContentListener> l) {
		listeners.addAll(l);
	}

	protected void notifyListeners(Path p1, Path p2,
			FileContentComparison0 result) {
		for (FileContentListener l : listeners) {
			l.fileComparison(p1, p2, result);
		}
	}

	public void compare() throws IOException {

		/*
		 * The current implementation is inefficient because it walks both tree
		 * in their entirety. Where the trees share common paths, this creates
		 * expensive, redundant computations of MD5 hashes and redundant
		 * notifcations to listeners. It should be possible to walk just one
		 * tree in its entirety, and to walk only the unique portions of the
		 * other tree.
		 */

		// Walk the 1st tree checking the corresponding path in the 2nd tree
		DirectoryVisitor visitor = new DirectoryVisitor(this.root1, this.root2);
		visitor.addListeners(this.listeners);
		visitor.addExcludedPaths(this.excludedPaths);
		Files.walkFileTree(this.root1, visitor);

		// Walk the 2nd tree checking the corresponding path in the 1st tree
		visitor = new DirectoryVisitor(this.root2, this.root1);
		visitor.addListeners(this.listeners);
		visitor.addExcludedPaths(this.excludedPaths);
		Files.walkFileTree(this.root2, visitor);

	}

}

class DirectoryVisitor implements FileVisitor<Path> {

//	private static final String DELIM = "|";

	// private final Set<String> notifications = new HashSet<>();

//	public static String createKey(Path p1, Path p2,
//			FileContentComparison0 result) {
//		String s1 = p1 == null ? "null" : p1.toString();
//		String s2 = p2 == null ? "null" : p2.toString();
//		String r = result == null ? "null" : result.toString();
//
//		int c = s1.compareTo(s2);
//		StringBuilder sb = new StringBuilder();
//		if (c <= 0) {
//			sb.append(s1).append(DELIM).append(s2).append(DELIM).append(r);
//		} else {
//			sb.append(s2).append(DELIM).append(s1).append(DELIM).append(r);
//		}
//
//		return sb.toString();
//	}

	private final Path thisRoot;
	private final Path thatRoot;
	private final Set<Path> excludedPaths = new HashSet<>();
	private final Set<FileContentListener> listeners = new HashSet<>();

	/**
	 * Compares two file trees, rooted at <code>thisRoot</code> and
	 * <code>thatRoot</code>, respectively. The tree rooted at
	 * <code>thisRoot</code> is the reference tree. This class assumes that the
	 * reference tree is being {@link Files#walkFileTree(Path, FileVisitor)
	 * walked} and that paths in the reference tree are compared to the
	 * corresponding relative paths in the other tree. As each file in the
	 * reference tree is encountered, one of four results is reported to any
	 * registered listeners:
	 * <ul>
	 * <li>The file appears only in the first tree; see
	 * {@link FileContentComparison0#ONLY_IN_PATH1}</li>
	 * <li>The file appears only in the second tree; see
	 * {@link FileContentComparison0#ONLY_IN_PATH2}</li>
	 * <li>The file has the same content in both trees; see
	 * {@link FileContentComparison0#SAME_CONTENT}</li>
	 * <li>The file has the different content in the two trees; see
	 * {@link FileContentComparison0#DIFFERENT_CONTENT}</li>
	 * <li>An error occurred while trying to read the a file in the first tree;
	 * see {@link FileContentComparison0#UNREACHABLE_PATH1}</li>
	 * <li>An error occurred while trying to read the a file in the second tree;
	 * see {@link FileContentComparison0#UNREACHABLE_PATH2}</li>
	 * </ul>
	 * 
	 * @param thisRoot
	 * @param thatRoot
	 */
	DirectoryVisitor(Path thisRoot, Path thatRoot) {
		if (thisRoot == null) {
			throw new IllegalArgumentException();
		}
		if (thatRoot == null) {
			throw new IllegalArgumentException();
		}
		this.thisRoot = thisRoot;
		this.thatRoot = thatRoot;
	}

	public void addExcludedPath(Path p) {
		excludedPaths.add(p);
	}

	public void addExcludedPaths(Set<Path> paths) {
		excludedPaths.addAll(paths);
	}

	public void addListener(FileContentListener l) {
		listeners.add(l);
	}

	public void addListeners(Set<FileContentListener> l) {
		listeners.addAll(l);
	}

	protected void notifyListeners(Path p1, Path p2,
			FileContentComparison0 result) {
		for (FileContentListener l : listeners) {
			l.fileComparison(p1, p2, result);
		}
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path p1, BasicFileAttributes attrs)
			throws IOException {
		Path relative = this.thisRoot.relativize(p1);
		Path p2 = thatRoot.resolve(relative);
		if (!this.excludedPaths.contains(relative)
				/* && !this.excludedPaths.contains(p2) */ ) {

			File fOther = p2.toFile();
			if (fOther.exists()) {
				File fRef = p1.toFile();
				assert fRef.exists();
				String md5Ref =
					FileUtilities.computeHash(FileUtilities.MD5_HASH_ALGORITHM,
							fRef);
				String md5Other =
					FileUtilities.computeHash(FileUtilities.MD5_HASH_ALGORITHM,
							fOther);
				if (md5Ref.equals(md5Other)) {
					notifyListeners(p1, p2,
							FileContentComparison0.SAME_CONTENT);
				} else {
					notifyListeners(p1, p2,
							FileContentComparison0.DIFFERENT_CONTENT);
				}
			} else {
				File fRef = p1.toFile();
				assert fRef.exists();
				notifyListeners(p1, p2,
						FileContentComparison0.ONLY_IN_PATH1);
			}
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path reference, IOException exc)
			throws IOException {
		Path relative = this.thisRoot.relativize(reference);
		Path other = thatRoot.resolve(relative);
		notifyListeners(relative, other,
				FileContentComparison0.UNREACHABLE_PATH1);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

}
