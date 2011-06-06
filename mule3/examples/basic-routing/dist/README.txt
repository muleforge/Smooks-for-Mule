+-------------------------------------+
| Smooks Basic Routing Example        |
+-------------------------------------+
This example demonstrates how to route message fragments in Mule 3, using Smooks.

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

Simply start Mule from this folder to run the example.

NOTE: It is important to start Mule from this folder, otherwise will mule not be able to find this examples data - folder, see below!


    Linux / Unix
    ------------
    mule 

    Windows
    -------
    mule.bat 

Wait for Mule to start and deploy this examples application. 
Mule should display something like the following in the console:

**********************************************************************
*            - - + APPLICATION + - -            * - - + STATUS + - - *
**********************************************************************
* smooks-4-mule-3-examples-basic-routing-1.3    * DEPLOYED           *
* default                                       * DEPLOYED           *
**********************************************************************

Copy the XML sample message from "data/out" to "data/in" and see the
transformed message xml printed to the console.  At this point, the processed message file
will be back in the "data/out" folder and the process can be repeated.

Take a look at the "target/smooks-report/report.html" for the Smooks execution report.

+--------------------------+
| Uninstalling the example |
+--------------------------+

While having Mule running remove the file:

${MULE_HOME}/apps/smooks-4-mule-3-examples-basic-routing-1.3-anchor.txt

After a few seconds Mule will shutdown and uninstall this examples application automatically.

Stop Mule with CTRL/C (twice might be needed) in the command windows where you started Mule.