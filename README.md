<div style="text-align:center"><img src="https://github.com/baileysostek/Apiary/blob/main/res/textures/apiary.png" alt="Apiary" width="512" height="512"/></div>

# Apiary
Apiary is a tool for GPU accelerated Agent based Modeling

Apiary uses a visual programing language to translate a node graph into an intermediate representaton which is then transformed into GLSL. Apiary determunes how much data is required to represent the agents in your simulation and implicitly sets up a double-buffered SSBO which your simlations cna read from and write to. This GLSL produced by the intermediate representation is executed in Compute Shaders across as many work groups as is allowable by your GPU. 

Checkout some cool screenshots of simulations we made throughout the development process.
https://drive.google.com/drive/u/0/folders/129YUOL9ydMppT9cgqowcPm3gxeIBz0On
