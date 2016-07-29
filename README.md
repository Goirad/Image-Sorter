# Image-Sorter
Used to sort images one dimensionally, for use in linear file browsers.
I collect wallpapers, and for the last 5 or so years I've been on and off developing this tool.
In the fewest words possible it takes a directory with images, reads them all to get their color information,
and then applies a greedy TSP algorithm to sort them by color. It then renames all the images according to their position
along this path, so that when viewed in a file browser and sorted by name, the images appear to be smoothly ordered. It can
also rename them according to brightness or resolution, depending on your needs.

The source files are available, though if you just want to try it, download the jar and place it in a folder.
Run it once, and it will generate the options file and an images directory. Simply place the images you would like to sort into 
the images directory, and run the jar again to have them sorted. Note that it can take a while if there are either very many 
images or very large images. On my system I can sort about 1000 images in no more than 30 seconds.
