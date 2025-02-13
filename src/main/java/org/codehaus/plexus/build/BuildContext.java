/*
Copyright (c) 2008 Sonatype, Inc. All rights reserved.

This program is licensed to you under the Apache License Version 2.0, 
and you may not use this file except in compliance with the Apache License Version 2.0. 
You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, 
software distributed under the Apache License Version 2.0 is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
*/
package org.codehaus.plexus.build;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.plexus.util.Scanner;


// TODO should it be BuildWorkspace or something like that?
/**
 * <p>BuildContext interface.</p>
 */
public interface BuildContext {
  /** Constant <code>SEVERITY_WARNING=1</code> */
  int SEVERITY_WARNING = 1;

  /** Constant <code>SEVERITY_ERROR=2</code> */
  int SEVERITY_ERROR = 2;

  // TODO should we add File getBasedir()?
  
  /**
   * Returns <code>true</code> if file or folder identified by <code>relpath</code> has
   * changed since last build.
   *
   * @param relpath is path relative to build context basedir
   * @return a boolean.
   */
  boolean hasDelta(String relpath);

  /**
   * Returns <code>true</code> if the file has changed since last build or is not
   * under basedir.
   *
   * @since 0.0.5
   * @param file a {@link java.io.File} object.
   * @return a boolean.
   */
  boolean hasDelta(File file);

  /**
   * Returns <code>true</code> if any file or folder identified by <code>relpaths</code> has
   * changed since last build.
   *
   * @param relpaths paths relative to build context basedir
   * @return a boolean.
   */
  boolean hasDelta(List<String> relpaths);

  /**
   * Indicates that the file or folder content has been modified during the build.
   *
   * @see #newFileOutputStream(File)
   * @param file a {@link java.io.File} object.
   */
  void refresh(File file);

  /**
   * Returns new OutputStream that writes to the <code>file</code>.
   *
   * Files changed using OutputStream returned by this method do not need to be
   * explicitly refreshed using {@link #refresh(File)}.
   *
   * As an optional optimisation, OutputStreams created by incremental build
   * context will attempt to avoid writing to the file if file content
   * has not changed.
   *
   * @param file a {@link java.io.File} object.
   * @return a {@link java.io.OutputStream} object.
   * @throws java.io.IOException if any.
   */
  OutputStream newFileOutputStream(File file) throws IOException;

  /**
   * Convenience method, fully equal to newScanner(basedir, false)
   *
   * @param basedir a {@link java.io.File} object.
   * @return a {@link org.codehaus.plexus.util.Scanner} object.
   */
  Scanner newScanner(File basedir);

  /**
   * Returned Scanner scans <code>basedir</code> for files and directories
   * deleted since last build. Returns empty Scanner if <code>basedir</code>
   * is not under this build context basedir.
   *
   * @param basedir a {@link java.io.File} object.
   * @return a {@link org.codehaus.plexus.util.Scanner} object.
   */
  Scanner newDeleteScanner(File basedir);

  /**
   * Returned Scanner scans files and folders under <code>basedir</code>.
   *
   * If this is an incremental build context and  <code>ignoreDelta</code>
   * is <code>false</code>, the scanner will only "see" files and folders with
   * content changes since last build.
   *
   * If <code>ignoreDelta</code> is <code>true</code>, the scanner will "see" all
   * files and folders.
   *
   * Please beware that ignoreDelta=false does NOT work reliably for operations
   * that copy resources from source to target locations. Returned Scanner
   * only scans changed source resources and it does not consider changed or deleted
   * target resources. This results in missing or stale target resources.
   * Starting with 0.5.0, recommended way to process resources is to use
   * #newScanner(basedir,true) to locate all source resources and {@link #isUptodate(File, File)}
   * to optimized processing of uptodate target resources.
   *
   * Returns empty Scanner if <code>basedir</code> is not under this build context basedir.
   *
   * https://issues.apache.org/jira/browse/MSHARED-125
   *
   * @param basedir a {@link java.io.File} object.
   * @param ignoreDelta a boolean.
   * @return a {@link org.codehaus.plexus.util.Scanner} object.
   */
  Scanner newScanner(File basedir, boolean ignoreDelta);

  /**
   * Returns <code>true</code> if this build context is incremental.
   *
   * Scanners created by {@link #newScanner(File)} of an incremental build context
   * will ignore files and folders that were not changed since last build.
   * Additionally, {@link #newDeleteScanner(File)} will scan files and directories
   * deleted since last build.
   *
   * @return a boolean.
   */
  boolean isIncremental();

  /**
   * Associate specified <code>key</code> with specified <code>value</code>
   * in the build context.
   *
   * Primary (and the only) purpose of this method is to allow preservation of
   * state needed for proper incremental behaviour between consecutive executions
   * of the same mojo needed to.
   *
   * For example, maven-plugin-plugin:descriptor mojo
   * can store collection of extracted MojoDescritpor during first invocation. Then
   * on each consecutive execution maven-plugin-plugin:descriptor will only need
   * to extract MojoDescriptors for changed files.
   *
   * @see #getValue(String)
   * @param key a {@link java.lang.String} object.
   * @param value a {@link java.lang.Object} object.
   */
  void setValue(String key, Object value);

  /**
   * Returns value associated with <code>key</code> during previous mojo execution.
   *
   * This method always returns <code>null</code> for non-incremental builds
   * (i.e., {@link #isIncremental()} returns <code>false</code>) and mojos are
   * expected to fall back to full, non-incremental behaviour.
   *
   * @see #setValue(String, Object)
   * @see #isIncremental()
   * @param key a {@link java.lang.String} object.
   * @return a {@link java.lang.Object} object.
   */
  Object getValue(String key);

  /**
   * <p>addWarning.</p>
   *
   * @deprecated Use addMessage with severity=SEVERITY_ERROR instead
   * @since 0.0.5
   * @param file a {@link java.io.File} object.
   * @param line a int.
   * @param column a int.
   * @param message a {@link java.lang.String} object.
   * @param cause a {@link java.lang.Throwable} object.
   */
  void addWarning(File file, int line, int column, String message, Throwable cause);

  /**
   * <p>addError.</p>
   *
   * @deprecated Use addMessage with severity=SEVERITY_WARNING instead
   * @since 0.0.5
   * @param file a {@link java.io.File} object.
   * @param line a int.
   * @param column a int.
   * @param message a {@link java.lang.String} object.
   * @param cause a {@link java.lang.Throwable} object.
   */
  void addError(File file, int line, int column, String message, Throwable cause);

  /**
   * Adds a message to the build context. The message is associated with a file and a location inside that file.
   *
   * @param file The file or folder with which the message is associated. Should not be null and it is recommended to be
   *          an absolute path.
   * @param line The line number inside the file. Use 1 (not 0) for the first line. Use 0 for unknown/unspecified.
   * @param column The column number inside the file. Use 1 (not 0) for the first column. Use 0 for unknown/unspecified.
   * @param severity The severity of the message: SEVERITY_WARNING or SEVERITY_ERROR.
   * @param cause A Throwable object associated with the message. Can be null.
   * @since 0.0.7
   * @param message a {@link java.lang.String} object.
   */
  void addMessage(File file, int line, int column, String message, int severity, Throwable cause);

  /**
   * Removes all messages associated with a file or folder during a previous build. It does not affect the messages
   * added during the current build.
   *
   * @since 0.0.7
   * @param file a {@link java.io.File} object.
   */
  void removeMessages(File file);

  /**
   * Returns true, if the target file exists and is uptodate compared to the source file.
   *
   * More specifically, this method returns true when both target and source files exist,
   * do not have changes since last incremental build and the target file was last modified
   * later than the source file. Returns false in all other cases.
   *
   * @since 0.0.5
   * @param target a {@link java.io.File} object.
   * @param source a {@link java.io.File} object.
   * @return a boolean.
   */
  boolean isUptodate(File target, File source);
}
