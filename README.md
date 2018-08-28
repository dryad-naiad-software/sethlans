[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3ULGBPAGGTSLA)


![logo](https://github.com/dryad-naiad-software/sethlans/raw/master/wiki/images/logo-text-dark.png)

[![Join the chat at https://gitter.im/sethlans-render/Lobby](https://badges.gitter.im/sethlans-render/Lobby.svg)](https://gitter.im/sethlans-render/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A local render farm system for Blender. https://www.blender.org
 
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


