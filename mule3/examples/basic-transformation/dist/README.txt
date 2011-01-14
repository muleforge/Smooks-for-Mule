+-------------------------------------+
| Smooks Basic Transformation Example |
+-------------------------------------+
This example demonstrates how perform a basix XSLT transform in Mule 2, using Smooks.

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

Simply use the 'run' shell script (Unix/Linux) or 'run.bat' batch file (Windows) provided
in this directory to run the example.

Alternatively, if you have added Mule to your executable path as recommended in INSTALL.txt, you
can run the example from the command line as follows:

    Linux / Unix
    ------------
    mule -config ./conf/mule-config.xml

    Windows
    -------
    mule.bat -config .\conf\mule-config.xml

Once Mule is running, copy the XML sample message from "data/out" to "data/in" and see the
transformed message xml printed to the console.  At this point, the processed message file
will be back in the "data/out" folder and the process can be repeated.

Take a look at the "target/smooks-report/report.html" for the Smooks execution report.
