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

macOS Install

Windows Install

Linux Install

#### Initial Setup
Once Sethlans has been started, navigate with your browser to https://localhost:7443

This will start the Sethlans Setup Wizard.