README: persistence.xml

The Java Persistence API (JPA) used by the Entity beans of the Automated
Offline Batch Algorithm (OABA) server requires a persistence configuration file
(persistence.xml) to be defined in the META-INF directory of the bean JAR.

However, a persistence configuration is often tightly coupled to a particular
choice of a target application server and target database. For example, when
EclipseLink is used as a JPA provider, the persistence.xml file usually includes
two properties, eclipselink.target-server and eclipselink.target-database, that
specify the target application server and database, respectively.

Since the ChoiceMaker Transitivity server is supported on a variety of application
servers and databases, a persistence configuration file is NOT configured here,
but rather in the projects that assemble EAR files targeted at particular
combinations of application servers and databases.
