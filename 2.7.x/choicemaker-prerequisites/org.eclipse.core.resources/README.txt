2013-08-03 rphall

There's an interesting, almost circular dependency between this plugin
and the fragment that augments it (org.eclipse.core.resources.ant).

 (*) The plugin doesn't depend on this fragment during a build, but it
     requires the fragment at runtime.

 (*) The fragment requires the plugin at buildtime and runtime.

Maven is used to build the plugin and fragment, but Eclipse is used to
run them. The Maven POM of the fragment can reference the plugin POM
without creating a circular dependence at buildtime. At runtime,
the fragment is injected into the plugin classloader space by Eclipse,
That means the fragment does NOT need to reference the plugin AS AN
ECLIPSE IMPORT, which avoids a circular dependence at runtime.

In an Eclipse-only build, the circular dependence can't be broken by Maven.
As a result, the original Eclipse build had two ANT build scripts.
The first ANT build script was the usual implicit one, generated from
the plugin specification and the build properties. The second ANT
build script was an auxiliary one, recorded in an scripts/buildExtraJAR.xml
file. That file and directory, along with the lib directory, have been
eliminated from this Maven project.

