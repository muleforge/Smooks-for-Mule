TODO: NEEDS TO BE UPDATED TO REFLECT MULE 3!!!

+----------------------------+
| Smooks EDI to Java Example |
+----------------------------+
This example demonstrates how perform an EDI to Java transform in Mule 2, using Smooks.

+----------------------+
| Building the example |
+----------------------+
Just run "mvn clean package" from this folder.

+------------------------+
| Installing the example |
+------------------------+
Make sure you have set the MULE_HOME environment variable
and that you have installed the Smooks for Mule module itself.

Run "mvn -P install-in-mule install" from this folder.

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
    mule -config ./conf/mule-edi-to-java-config.xml

    Windows
    -------
    mule.bat -config .\conf\mule-edi-to-java-config.xml

Once Mule is running, copy the EDI sample message from "data/out" to "data/in" and see the
transformed message objects printed to the console.  At this point, the processed message file
will be back in the "data/out" folder and the process can be repeated.
