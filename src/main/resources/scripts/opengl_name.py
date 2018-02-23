import sys
from OpenGL.GL import *
from OpenGL.GLUT import *

glutInit(sys.argv)
glutInitDisplayMode(GLUT_RGBA)

# Create a window, setting its title
glutCreateWindow(b'')

# Print Renderer Name
print(glGetString(GL_RENDERER))