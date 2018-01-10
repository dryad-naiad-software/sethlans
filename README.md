
# Sethlans
A local distributed render farm for Blender. https://www.blender.org

Sethlans contains the following technologies:
 - Spring Boot 1.5.9
 - Python 3.5.2
 - Java 8
 - ImageMagick 6.9/7.0
 - Blender 2.78+
 
### Dependencies
Sethlans **requires**  **Java 8**, either Oracle Java or OpenJDK is supported.
 
Aside from Java, **ImageMagick** must also be installed for **Sethlans Server/Dual** Modes.  For Windows and macOS ImageMagick 7.0 has been tested extensively and is the preferred version to use.  For Linux most distributions install 6.9 and Sethlans has been written to work with 6.9 in Linux environments.

#### Installing ImageMagick dependencies
##### MacOS
For macOS we recommend using [Homebrew](https://brew.sh/) to install ImageMagick.

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

### Terminology

Sethlans uses the following terminology to describe it's functions.

#### Mode
- **Server:** Creation of render projects, management of render queues.
- **Node:** Receives rendering tasks from the server, returns finished products 
back to the server.
- **Dual:** Combination of a server and a node.  Allows both the creation and management of render queues and the rendering of tasks.

#### Compute Type (Node/Dual Modes only)

- **CPU:** Render only using CPU, number of cores can be selected
- **GPU:** Render only using GPU, multiple graphics cards can be selected and would be used all at once to render the image.
- **CPU & GPU:** Render with either the CPU or GPU.  This sets the node to accept both CPU or GPU tasks/projects.  If a project is set to CPU and GPU then it will use the compute type with the lowest benchmark.

#### Blender Versions (Server/Dual Modes only)
Sethlans doesn't require that Blender be preinstalled on the system.  The server downloads the version of Blender selected and stores it.  As tasks are needed the nodes obtain the necessary files from the server.  Multiple versions can be saved on a server and projects can be configured to use any version of Blender stored on the server.

#### Projects
Projects can be either **Still Images**, or **Animations**.  

 - Still Images allow you to render only one frame that is specified.  
 -  Animations allow you to render multiple frames from a range
   specified.

Projects also consist of **Parts**.  

By default an image is broken into 4 parts and distributed to available nodes once a project has been started.  This ensures that large images are handled efficiently.  

However certain situations require that each frame be rendered on one node. This is beneficial for numerous high powered rendering stations or for images that have special lighting effects that are dependent on a section of the image not being split during render(reflection of light off of a headlight for example).

A more detailed description of Sethlans features plus a Getting Started Guide are available on the [wiki](https://github.com/dryad-naiad-software/sethlans/wiki).


