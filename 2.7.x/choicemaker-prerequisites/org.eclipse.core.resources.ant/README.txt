2013-08-03 rphall

There's an interesting, almost circular dependency between this fragment
and the plugin that it augments (org.eclipse.core.resources).

 (*) The plugin doesn't depend on this fragment during a build, but it
     requires the fragment at runtime.

 (*) The fragment requires the plugin at buildtime and runtime.

Maven is used to build the plugin and fragment, but Eclipse is used to
run them. The Maven POM of the fragment can reference the plugin POM
without creating a circular dependence at buildtime. At runtime,
the fragment is injected into the plugin classloader space by Eclipse,
That means the fragment doesn't need to reference the plugin AS AN
ECLIPSE IMPORT, which avoids a circular dependence at runtime.

In an Eclipse build, the circular dependence can't be broken by POM
specifications. As a result, the Eclipse build has two ANT build scripts.
The first ANT build script is the usual implicit one, generated from
the plugin specification and the build properties. The second ANT
build script is an auxiliary one, recorded in the scripts/buildExtraJAR.xml
file. That file and directory, along with the lib directory, can be
(and has been) eliminated from this Maven project.

