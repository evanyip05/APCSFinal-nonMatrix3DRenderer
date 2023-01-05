Running instructions:
    make sure folder with obj files is in the same directory as the program files. 
    
    the movement controls arent very intuitive, because theres no perspective so you cant tell how far the object is away from you

    use wasd and space/shift to move the camera, arrowkeys and page up/down to tilt it, 
    w and s should move you closer and further, a and d should move you to either side, space and shift should move you up and down
    left and right should turn the camera to look left/right, up and down should let you look up and down, pg up / down should rotate the camera about its axis

Program details:

    this is meant to be a 3d renderer, that can load obj files and view them     

    the concept at play is using math to project points through a plane and 
    convert those intersections to relative 2d coordinates on the screen
    things exist in 3d space and their data reflects that, and the program is meant to cast them onto a more viewable 2d plane

    uses javas functional programming implementation through Function<> Supplier<> and Consumer<> (maybe?) and functional interfaces defined by the user. 
        learned this during the arrayList unit, for removeIf(item -> qualification) and forEach(item -> action)

    uses final classes and inner classes replacing packages so its not a pain to run

    all of the 3d classes are written from scratch.

    the math used was mainly vector and parametric math, using t values to find different componets of some function (learned for this project),
    theres also normal math, mainly trig but thats not new

        dot product: way to find simmilarity between vectors, positive number if pointing in the same direction, neagitive if pointing in the oppisite direction
            used to fill each resulting projected face to simulate light, the dot product was taken between unit vectors yeilding a range of -1 -> 1 
            so i could multiply that against 255 to get a range of colors, and also determines if faces should be drawn or not
 
        cross product: way to find a perpendicular vector between 2 numbers
            used as a way to find a faces vector normal, which is perpendicular to the surface of the face, 
            was also used as a tool to give me leverage and create geometric proofs to obtain angles and have more point info to do transformations
     
        parametrics (equations): used as a representation of lines and planes, mainly to get values of intersection and other values or points
            used for lines, so you can use a t value as a percentage to get another point along the line based on a length and calculated using the lines length
            used in rotation transform as a parametric circle, to find some point p for degrees around the circle

    the gui classes (panel frame listener), extendable thread, and the triple tuple and pair classes are classes I've developed outside of class for other projects
        
        the panel works on a "buffered image" system, as places to draw to, not really buffers. you can create sevral different buffers that get stored in an array 
        you can draw to them by calling getBufferGraphics with some index. each buffer is drawn to the main panel graphics by paint override and are drawn on the repaint call

        the frame is a basic extension of jframe that i listeners are added to 

        the listener is a extension of keylistener that stores pressed keys to an arrayList and removes them when theyre released. allowing you to detect multiple inputs

        the extendable thread is a synchronized thread extension (not runnable implementation) that uses wait notify calls to pause and play the thread, not used much in this program, 
        other than to act as a "game loop" but is used in my other programs (asteroids uses this)

        pair tuple and triple use generics to have a type parameter on creation so they can see different use, learned during the comparable unit

    parsing done from scratch using split and string methods


     
