package com.choicemaker.cmit.util;

/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sample code that finds files that match the specified glob pattern. For more
 * information on what constitutes a glob pattern, see
 * http://docs.oracle.com/javase
 * /javatutorials/tutorial/essential/io/fileOps.html#glob
 *
 * The file or directories that match the pattern are printed to standard out.
 * The number of matches is also printed.
 *
 * When executing this application, you must put the glob pattern in quotes, so
 * the shell will not expand any wild cards: java Find . -name "*.java"
 */

public class Find {

	/**
	 * A {@code FileVisitor} that finds all files that match the specified
	 * pattern.
	 */
	public static class Finder extends SimpleFileVisitor<Path> {

		private static final Logger logger = Logger.getLogger(Finder.class
				.getName());

		private final PathMatcher matcher;
		private List<Path> found;

		public Finder(String pattern) {
			matcher =
				FileSystems.getDefault().getPathMatcher("glob:" + pattern);
			found = new LinkedList<>();
		}

		// Compares the glob pattern against the file or directory name.
		void find(Path file) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				found.add(file);
				logger.info(file.toString());
			}
		}

		// Prints the total number of matches to standard out.
		public List<Path> done() {
			logger.info("Matched: " + found.size());
			return Collections.unmodifiableList(found);
		}

		// Clears the current list of paths that have been found
		public void clear() {
			logger.info("Cleared: " + found.size());
			found.clear();
		}

		// Invoke the pattern matching method on each file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return CONTINUE;
		}

		// Invoke the pattern matching method on each directory.
		@Override
		public FileVisitResult preVisitDirectory(Path dir,
				BasicFileAttributes attrs) {
			find(dir);
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			logger.severe(exc.toString());
			return CONTINUE;
		}
	}

	static void usage() {
		System.err.println("java Find <path>" + " -name \"<glob_pattern>\"");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {

		if (args.length < 3 || !args[1].equals("-name"))
			usage();

		Path startingDir = Paths.get(args[0]);
		String pattern = args[2];

		Finder finder = new Finder(pattern);
		Files.walkFileTree(startingDir, finder);
		List<Path> found = finder.done();
		for (Path p : found) {
			System.out.println(p.toString());
		}
	}
}