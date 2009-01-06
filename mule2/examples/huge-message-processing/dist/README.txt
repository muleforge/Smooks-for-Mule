+--------------------------------------------------+
| Smooks for Mule: huge message processing example |
+--------------------------------------------------+
This example demonstrates how Smooks with Smooks for Mule
handles huge gigabyte large data files and split, enrich, transform and
route the data to multiple endpoints.

In the 'doc' directory an install.html file is provided with
a better explanation how this example works. This readme
file only contains the bare minimum to install and run the example.

+------------------------+
| Installing the example |
+------------------------+
Make sure you have set the MULE_HOME environment variable
and that you have installed the Smooks for Mule module itself

Run "ant install" from this folder.

+---------------------+
| Running the example |
+---------------------+
Make sure you have installed the example as describe in the previous section.

  * Open 5 command shell windows in the root folder of this example.
  * In the first window, execute "ant run-services" to start the HSQLDB Database and JSM Server
  * In the second window, execute 'run' shell script (Unix/Linux) or 'run.bat' batch file (Windows) to start Mule.
    Alternatively, if you have added Mule to your executable path as recommended in INSTALL.txt, you can run the example from the command line as follows:
    Linux / Unix: mule -config ./conf/mule-config.xml
    Windows: mule.bat -config .\conf\mule-config.xml
  * In the third window, execute "ant run-consumer-1" to start the JMS consumer of the first party
  * In the fourth window, execute "ant run-consumer-2" to start the JMS consumer of the second party
  * In the fifth window, execute "ant generate" to start the message creation tool.
