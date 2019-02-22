/*******************************************************************************
 * Copyright (c) 2011, Chair of Distributed Information Systems, University of Passau. 
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *     this list of conditions and the following disclaimer. 
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *     notice, this list of conditions and the following disclaimer in the 
 *     documentation and/or other materials provided with the distribution. 
 * 
 * 3. Neither the name of the University of Passau nor the names of its 
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
 * DAMAGE.
 ******************************************************************************/
package pdgf.util.ClassLoading;

/*
 * http://code.google.com/p/treegraft/source/browse/treegraft/java/src/info/jonclark/lang/ClassFinder.java?spec=svn50&r=47
 * ClassFinder.java
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Orginal code from:<a href=
 * "http://code.google.com/p/treegraft/source/browse/treegraft/java/src/info/jonclark/lang/ClassFinder.java?spec=svn50&r=47"
 * >http://code.google.com/p/treegraft/source/browse/treegraft/java/src/info/
 * jonclark/lang/ClassFinder.java?spec=svn50&r=47</a> ClassFinder.java
 * 
 * This utility class was based originally on Daniel Le Berre</a>'s
 * <code>RTSI</code> class. This class can be called in different modes, but the
 * principal use is to determine what subclasses/implementations of a given
 * class/interface exist in the current runtime environment. Orginal authors:
 * Daniel Le Berre, Elliott Wade<br/>
 * <br/>
 * 
 * Modified and enhanced by me. Relies not entirely on
 * System.getProperty("java.class.path"), but uses also ((URLClassLoader)
 * ClassLoader.getSystemClassLoader()).getURLs(); as Sources for Paths to
 * search.
 * 
 * @author Michael Frank
 * @version 1.2
 * @since 11.05.2011
 */
public class ClassFinder {
	private Class<?> searchClass = null;
	private Map<URL, String> classpathLocations = new HashMap<URL, String>();
	private Map<Class<?>, URL> results = new HashMap<Class<?>, URL>();
	private List<Throwable> errors = new ArrayList<Throwable>();
	private boolean working = false;
	private URLClassLoader cl = (URLClassLoader) ClassLoader
			.getSystemClassLoader();

	public ClassFinder(URLClassLoader cl) {
		this.cl = cl;
		refreshLocations();
	}

	public ClassFinder() {
		refreshLocations();
	}

	public void setClassLoader(URLClassLoader cl) {
		if (cl != null)
			this.cl = cl;
	}

	/**
	 * Rescan the classpath, cacheing all possible file locations.
	 */
	public final void refreshLocations() {

		synchronized (classpathLocations) {
			classpathLocations = buildClasspathLocationsMap();
		}

	}

	/**
	 * Rescan the classpath, cacheing all possible file locations.
	 */
	public final Map<URL, String> getClassPathLoactions() {
		Map<URL, String> local;
		synchronized (classpathLocations) {
			local = classpathLocations;
		}
		return local;
	}

	/**
	 * @param fqcn
	 *            Name of superclass/interface on which to search
	 */
	public final List<Class<?>> findSubclasses(String fqcn) {
		synchronized (classpathLocations) {
			// refreshLocations();
			synchronized (results) {
				try {
					working = true;
					searchClass = null;
					errors = new ArrayList<Throwable>();
					results = new TreeMap<Class<?>, URL>(CLASS_COMPARATOR);

					//
					// filter malformed FQCN
					//
					if (fqcn.startsWith(".") || fqcn.endsWith(".")) {
						return new ArrayList<Class<?>>();
					}

					//
					// Determine search class from fqcn
					//
					try {
						searchClass = Class.forName(fqcn, false, cl);
					} catch (ClassNotFoundException ex) {
						// if class not found, let empty vector return...
						errors.add(ex);
						return new ArrayList<Class<?>>();
					}

					// return findSubclasses(searchClass, classpathLocations);

					findSubclasses(searchClass, classpathLocations);

					ArrayList<Class<?>> v = new ArrayList<Class<?>>(
							results.size());
					Iterator<Class<?>> it = results.keySet().iterator();
					while (it.hasNext()) {
						v.add(it.next());
					}
					return v;

				} finally {
					working = false;
				}
			}
		}
	}

	public final boolean hasErrors() {
		return errors.size() != 0;
	}

	public final List<Throwable> getErrors() {
		if (errors.size() == 0) {
			return null;
		}
		return new ArrayList<Throwable>(errors);
	}

	/**
	 * The result of the last search is cached in this object, along with the
	 * URL that corresponds to each class returned. This method may be called to
	 * query the cache for the location at which the given class was found.
	 * <code>null</code> will be returned if the given class was not found
	 * during the last search, or if the result cache has been cleared.
	 */
	public final URL getLocationOf(Class<?> cls) {
		if (results != null)
			return results.get(cls);
		else
			return null;
	}

	/**
	 * Determine every URL location defined by the current classpath, and it's
	 * associated package name.
	 */
	private final Map<URL, String> buildClasspathLocationsMap() {
		Map<URL, String> map = new TreeMap<URL, String>(URL_COMPARATOR);
		File file = null;

		String pathSep = System.getProperty("path.separator");
		String classpath = System.getProperty("java.class.path");
		// System.out.println("classpath=" + classpath);

		HashMap<File, File> files = new HashMap<File, File>();

		StringTokenizer st = new StringTokenizer(classpath, pathSep);

		while (st.hasMoreTokens()) {
			String path = st.nextToken();
			file = new File(path);
			files.put(file, file);
		}

		// load urls from UrlClassLoader
		URL[] urls = cl.getURLs();

		for (int i = 0; i < urls.length; i++) {
			file = urlToFile(urls[i]);
			files.put(file, file);
		}

		for (File f : files.values()) {
			include(null, f, map);
		}

		// Debug print
		// System.out.println(ClassFinder.class.getName()
		// + " classpath locations:");
		// Iterator<URL> it = map.keySet().iterator();
		// while (it.hasNext()) {
		// URL url = it.next();
		// System.out.println(url + "-->" + map.get(url));
		// }
		// System.out.println(map.size());

		// for (URL u : map.keySet()) {
		// System.out.println(u);
		// }
		// System.out.println(map.size());
		return map;
	}

	private final static FileFilter DIRECTORIES_ONLY = new FileFilter() {
		public boolean accept(File f) {
			return f.exists() && f.isDirectory();

		}
	};

	private final static Comparator<URL> URL_COMPARATOR = new Comparator<URL>() {
		public int compare(URL u1, URL u2) {
			return String.valueOf(u1).compareTo(String.valueOf(u2));
		}
	};

	private final static Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {
		public int compare(Class<?> c1, Class<?> c2) {
			return String.valueOf(c1).compareTo(String.valueOf(c2));
		}
	};

	private final void include(String name, File file, Map<URL, String> map) {
		if (file == null || !file.exists())
			return;
		if (!file.isDirectory()) {
			// could be a JAR file
			includeJar(file, map);
			return;
		}

		if (name == null)
			name = "";
		else
			name += ".";

		// add subpackages
		File[] dirs = file.listFiles(DIRECTORIES_ONLY);
		for (int i = 0; i < dirs.length; i++) {
			try {
				// add the present package
				map.put(new URL("file://" + dirs[i].getCanonicalPath()), name
						+ dirs[i].getName());
			} catch (IOException ioe) {
				return;
			}

			include(name + dirs[i].getName(), dirs[i], map);
		}
	}

	private void includeJar(File file, Map<URL, String> map) {
		if (!file.getPath().endsWith(".jar"))
			return;

		URL jarURL = null;
		JarFile jar = null;
		try {

			// !BUGFIX 10.05.2011 : Error with double slashes in url in linux
			// jar:file://home/<user>/...
			String canoncialPath = file.getCanonicalPath();
			if (canoncialPath.startsWith("/")) {
				canoncialPath = canoncialPath.substring(1);
			}

			jarURL = new URL("file:/" + canoncialPath);
			jarURL = new URL("jar:" + jarURL.toExternalForm() + "!/");

			JarURLConnection conn = (JarURLConnection) jarURL.openConnection();
			jar = conn.getJarFile();
		} catch (Exception e) {
			// not a JAR or disk I/O error
			// either way, just skip
			return;
		}

		if (jar == null || jarURL == null)
			return;

		// include the jar's "default" package (i.e. jar's root)
		map.put(jarURL, "");

		// Enumeration<JarEntry> e = jar.entries();
		// while (e.hasMoreElements()) {
		// JarEntry entry = e.nextElement();
		//
		// if (entry.isDirectory()) {
		// if (entry.getName().toUpperCase().equals("META-INF/"))
		// continue;
		//
		// try {
		// map.put(new URL(jarURL.toExternalForm() + entry.getName()),
		// packageNameFor(entry));
		// } catch (MalformedURLException murl) {
		// // whacky entry?
		// continue;
		// }
		// }
		// }
	}

	private static String packageNameFor(JarEntry entry) {
		if (entry == null)
			return "";
		String s = entry.getName();
		if (s == null)
			return "";
		if (s.length() == 0)
			return s;
		if (s.startsWith("/"))
			s = s.substring(1, s.length());
		if (s.endsWith("/"))
			s = s.substring(0, s.length() - 1);
		return s.replace('/', '.');
	}

	private final void includeResourceLocations(String packageName,
			Map<URL, String> map) {
		try {
			Enumeration<URL> resourceLocations = ClassFinder.class
					.getClassLoader().getResources(getPackagePath(packageName));

			while (resourceLocations.hasMoreElements()) {
				map.put(resourceLocations.nextElement(), packageName);
			}
		} catch (Exception e) {
			// well, we tried
			errors.add(e);
			return;
		}
	}

	private final void findSubclasses(Class<?> superClass,
			Map<URL, String> locations) {

		// Package [] packages = Package.getPackages ();
		// for (int i=0;i<packages.length;i++)
		// {
		// System.out.println ("package: " + packages[i]);
		// }

		for (Entry<URL, String> entry : locations.entrySet()) {
			findSubclasses(entry.getKey(), entry.getValue(), superClass);
		}

	}

	private final void findSubclasses(URL location, String packageName,
			Class<?> superClass) {
		// System.out.println ("looking in package:" + packageName);
		// System.out.println ("looking for  class:" + superClass);

		synchronized (results) {
			Class tmpClass;
			// hash guarantees unique names...
			Map<Class<?>, URL> thisResult = new TreeMap<Class<?>, URL>(
					CLASS_COMPARATOR);

			// TODO: double-check for null search class
			String fqcn = searchClass.getName();

			// Get a File object for the package
			File directory = new File(location.getFile());

			// System.out.println("\tlooking in " + directory);

			if (directory.exists()) {
				// Get the list of the files contained in the package
				String[] files = directory.list();
				for (int i = 0; i < files.length; i++) {
					// we are only interested in .class files
					if (files[i].endsWith(".class")) {
						// removes the .class extension
						String classname = files[i].substring(0,
								files[i].length() - 6);

						// System.out.println ("\t\tchecking file " +
						// classname);

						try {
							tmpClass = Class.forName(packageName + "."
									+ classname, false, cl);
							if (superClass.isAssignableFrom(tmpClass)
									&& !fqcn.equals(packageName + "."
											+ classname)) {
								thisResult.put(tmpClass, location);
							}
						} catch (ClassNotFoundException cnfex) {
							errors.add(cnfex);
							// System.err.println(cnfex);
						} catch (Exception ex) {
							errors.add(ex);
							// System.err.println (ex);
						}
					}
				}
			} else {
				try {
					// It does not work with the filesystem: we must
					// be in the case of a package contained in a jar file.
					JarURLConnection conn = (JarURLConnection) location
							.openConnection();
					// String starts = conn.getEntryName();
					JarFile jarFile = conn.getJarFile();

					// System.out.println ("starts=" + starts);

					Enumeration<JarEntry> e = jarFile.entries();
					while (e.hasMoreElements()) {
						JarEntry entry = e.nextElement();
						String entryname = entry.getName();

						// System.out.println("\tconsidering entry: " +
						// entryname);

						if (!entry.isDirectory()
								&& entryname.endsWith(".class")) {
							String classname = entryname.substring(0,
									entryname.length() - 6);

							if (classname.startsWith("/"))
								classname = classname.substring(1);

							classname = classname.replace('/', '.');

							// System.out.println ("\t\ttesting classname: "
							// + classname);

							try {
								// TODO: verify this block
								tmpClass = Class.forName(classname, false, cl);

								if (superClass.isAssignableFrom(tmpClass)
										&& !fqcn.equals(classname)) {
									thisResult.put(tmpClass, location);
								}
							} catch (ClassNotFoundException cnfex) {
								// that's strange since we're scanning
								// the same classpath the classloader's
								// using... oh, well
								errors.add(cnfex);
							} catch (NoClassDefFoundError ncdfe) {
								// dependency problem... class is
								// unusable anyway, so just ignore it
								errors.add(ncdfe);
							} catch (UnsatisfiedLinkError ule) {
								// another dependency problem... class is
								// unusable anyway, so just ignore it
								errors.add(ule);
							} catch (Exception exception) {
								// unexpected problem
								// System.err.println (ex);
								errors.add(exception);
							} catch (Error error) {
								// lots of things could go wrong
								// that we'll just ignore since
								// they're so rare...
								errors.add(error);
							}
						}
					}
				} catch (IOException ioex) {
					// System.err.println(ioex);
					errors.add(ioex);
				}

			} // while

			// System.out.println ("results = " + thisResult);

			results.putAll(thisResult);

		} // synch results
	}

	private final static String getPackagePath(String packageName) {
		// Translate the package name into an "absolute" path
		String path = new String(packageName);
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		path = path.replace('.', '/');

		// ending with "/" indicates a directory to the classloader
		if (!path.endsWith("/"))
			path += "/";

		// for actual classloader interface (NOT Class.getResource() which
		// hacks up the request string!) a resource beginning with a "/"
		// will never be found!!! (unless it's at the root, maybe?)
		if (path.startsWith("/"))
			path = path.substring(1, path.length());

		// System.out.println ("package path=" + path);

		return path;
	}

	public static void main(String[] args) {

		ClassFinder finder = null;
		List<Class<?>> v = null;
		List<Throwable> errors = null;

		if (args.length == 1) {
			finder = new ClassFinder();
			v = finder.findSubclasses(args[0]);
			errors = finder.getErrors();
		} else {
			System.out
					.println("Usage: java ClassFinder <fully.qualified.superclass.name>");
			return;
		}

		System.out.println("RESULTS:");
		if (v != null && v.size() > 0) {
			for (Class<?> cls : v) {
				System.out.println(cls
						+ " in "
						+ ((finder != null) ? String.valueOf(finder
								.getLocationOf(cls)) : "?"));
			}
		} else {
			System.out.println("No subclasses of " + args[0] + " found.");
		}

		// TODO: verbose mode
		// if (errors != null && errors.size () > 0)
		// {
		// System.out.println ("ERRORS:");
		// for (Throwable t : errors) System.out.println (t);
		// }
	}

	/**
	 * code from:
	 * http://blogs.sphinx.at/java/erzeugen-von-javaiofile-aus-javaneturl/
	 * 
	 * @param url
	 * @return
	 */
	private static File urlToFile(URL url) {
		URI uri;
		try {

			uri = url.toURI();
		} catch (URISyntaxException e) {
			// obviously the URL did
			// not comply with RFC 2396. This can only
			// happen if we have illegal unescaped characters.

			try {
				uri = new URI(url.getProtocol(), url.getUserInfo(),
						url.getHost(), url.getPort(), url.getPath(),
						url.getQuery(), url.getRef());
			} catch (URISyntaxException e1) {
				// The URL is broken beyond automatic repair

				// File tmp = new File(url.getPath());
				// if(tmp!=null && tmp.exists()){
				// return tmp;
				// }
				throw new IllegalArgumentException("broken URL: " + url);
			}
		}
		return new File(uri);
	}

}