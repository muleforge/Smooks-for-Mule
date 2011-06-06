+--------------------------------------------------+
| Smooks for Mule: huge message processing example |
+--------------------------------------------------+
This example demonstrates how Smooks with Smooks for Mule 3
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

NOTE: This command will copy the examples zip file to the ${MULE_HOME}/apps - folder.

+---------------------+
| Running the example |
+---------------------+
Make sure you have installed the example as describe in the previous section have added Mule to your executable path as described in ${MULE_HOME}/README.txt.

  * Open 5 command shell windows in the root folder of this example.
  * In the first window, execute "ant run-services" to start the HSQLDB Database and JSM Server
  * In the second window, execute 'mule' shell script (Unix/Linux) or 'mule.bat' batch file (Windows) to start Mule.
  * In the third window, execute "ant run-consumer-1" to start the JMS consumer of the first party
  * In the fourth window, execute "ant run-consumer-2" to start the JMS consumer of the second party
  * In the fifth window, execute "ant generate" to start the message creation tool.

+--------------------------+
| Uninstalling the example |
+--------------------------+

While having Mule running remove the file:

${MULE_HOME}/apps/smooks-4-mule-3-examples-huge-message-processing-1.3-anchor.txt

After a few seconds Mule will shutdown and uninstall this examples application automatically.

Stop Mule with CTRL/C (twice might be needed) in the command windows where you started Mule.