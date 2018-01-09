

# Sethlans
A local distributed render farm for Blender. https://www.blender.org

Sethlans contains the following technologies:
 - Spring Boot 1.5.9
 - Python 3.5.2
 - Java 8
 - ImageMagick 6.9/7.0
 - Blender 2.78+
 
 
 ## Getting Started
### Dependencies
Sethlans **requires** the **Java 8** JRE, either Oracle Java or OpenJDK is supported.
 
Aside from Java, ImageMagick must also be installed.  For Windows and macOS ImageMagick 7.0 has been tested extensively and is the preferred version to use.  For Linux most distributions install 6.9 and Sethlans has been written to work with 6.9 in Linux environments.

#### Installing ImageMagick dependencies
##### MacOS
For macOS I recommend using [Homebrew](https://brew.sh/) to install ImageMagick.

    $ brew install imagemagick

##### Windows
ImageMagick has [several binaries for Windows](http://www.imagemagick.org/script/download.php#windows).  At the time of this writing the tested binary is the Win64 dynamic at 16 bits-per-pixel component.

https://www.imagemagick.org/download/binaries/ImageMagick-7.0.7-19-Q16-x64-dll.exe
 
##### Linux
Both Ubuntu 17.10 and Fedora Core 27 were used for testing.  These systems are still using ImageMagick 6.9 and Sethlans takes this into account.  Obtain the latest version of ImageMagick using your package manager. 

Ubuntu:
 
    $ sudo apt-get install imagemagick

Fedora Core:

    $ sudo dnf install ImageMagick

### Installation and Initial Setup
Obtain the latest release from [here](https://github.com/dryad-naiad-software/sethlans/releases).

##### macOS

- Download the Sethlans DMG and double click it.

![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/sethlans_dmg.png)

- Copy the Sethlans package to the Applications directory.
   
![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/sethlans_applications.png)
 
- Attempting to execute Sethlans for the first time on newer Mac systems will give the following error.
 
  ![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/gatekeeper.png)
 
- Navigate to Settings >> Security and Privacy.  You'll be presented with an option to open Sethlans.
 
 ![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/security_and_privacy.png)
 
 - Sethlans will load an icon in the menu bar, this serves as a shortcut to the program while it's running.

![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/sethlans_link.png)

##### Windows
- Download the Sethlans EXE file and run it.
- Recent versions of Windows might prompt the following error. Expand the window and select Run anyway.

![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/sethlans_windows_defender.png)
- In the Windows System tray, the Sethlans icon will appear.

![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/sethlans_windows.png)

- Right click, this serves as an interface to the program while it is running.

![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/sethlans_windows_open.png)

##### Linux
- Linux installation is simple.  Download the Sethlans JAR file and run the following from a terminal.

		java -jar Sethlans-X.X.X.jar

![](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/linux_terminal.png)

The console will start Sethlans.  Once the following lines are displayed you can initiate setup.

		Tomcat started on port(s): 7443 (https)
		Started Sethlans in 11.994 seconds (JVM running for 12.77)

#### Initial Setup
Once Sethlans has been started, navigate with your browser to https://localhost:7443

This will start the Sethlans Setup Wizard.