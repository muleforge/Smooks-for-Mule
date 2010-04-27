+---------------------+
| Smooks for Mule 1.x |
+---------------------+

 Smooks for Mule enables Message Transformation and
 Routing using the Smooks Engine.

 Smooks is a Java Framework/Engine for processing
 XML and non XML data (CSV, EDI etc).

 Smooks can be used to:

  * Perform a wide range of Data Transforms -
    XML to XML, CSV to XML, EDI to XML, XML to EDI,
    XML to CSV, Java to XML, Java to EDI, Java to CSV,
    Java to Java, XML to Java, EDI to Java etc.
  * Populate a Java Object Model from a data source
    (CSV, EDI, XML, Java etc). Populated object models
    can be used as a transformation result itself, or
    can be used by (e.g.) Templating resources for
    generating XML or other character based results.
    Also supports Virtual Object Models
    (Maps and Lists of typed data), which can be
    used by EL and Templating functionality.
  * Process huge messages (GBs) - Split, Transform
    and Route message fragments to JMS, File, Database
    etc destinations.
  * Enrich a message with data from a Database, or other
    Datasources.
  * Perform Extract Transform Load (ETL) operations by
    leveraging Smooks' Transformation, Routing and Persistence
    functionality.

 Smooks supports both DOM and SAX processing models, but adds a
 more "code friendly" layer on top of them. It allows you to plug in
 your own "ContentHandler" implementations (written in Java or Groovy),
 or reuse the many existing handlers.

 Smooks is an ideal fit as part of an overall Integration Solution

+------------------------+
| Installation           |
+------------------------+

 For both Mule 1.x and Mule 2.x there are installation packages.
 Take a look at the parent Maven module for instructions.

+------------------------+
| Building               |
+------------------------+

 Use "mvn clean install" to build and install the module into
 your local maven repository.

+------------------------+
| Module Documentation   |
+------------------------+

 Go to the Smooks for Mule website for all relevant module
 documentation:

    http://www.mulesource.org/display/SMOOKS/Home


+------------------------+
| Smooks                 |
+------------------------+

 For information on Smooks go to their website:

    http://www.smooks.org

+------------------------+
| Questions?             |
+------------------------+

 Got a question about the Module (not Smooks itself)?
 Post a message on the  forum:

   http://forums.muleforge.org/forum.jspa?forumID=56

 or send a mail to the Mailing list:

   http://admin.muleforge.org/projects/smooks/lists

 Question on Smooks itself can best be asked by sending
 a mail to the Smooks mailing list:

   http://www.smooks.org/mediawiki/index.php?title=Mailing_Lists
