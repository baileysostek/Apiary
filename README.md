<p align="center"><img src="https://github.com/baileysostek/Apiary/blob/main/res/textures/apiary.png" alt="Apiary" width="512" height="512"/></p>

# Apiary
Apiary is a tool for GPU accelerated Agent based Modeling

Apiary uses a visual programing language to translate a node graph into an intermediate representation which is then transformed into GLSL. Apiary determines how much data is required to represent the agents in your simulation and implicitly sets up a double-buffered SSBO which your simulations can read from and write to. This GLSL produced by the intermediate representation is executed in Compute Shaders across as many work groups as is allowable by your GPU. 

Checkout some cool screenshots of simulations we made throughout the development process.
https://drive.google.com/drive/u/0/folders/129YUOL9ydMppT9cgqowcPm3gxeIBz0On


Apiary was initially designed and developed as the thesis project I pursued to complete my Masters of Science in Compute Science degree at Worcester Polytechnic Institute in 2023 resources relating to that work can be found below:

[Thesis Proposal]( https://github.com/baileysostek/Apiary/blob/main/Programming_Abstractions_for_Agent_Based_Simulations.pdf)

# What can you do with Apiary?

Apiary is a framework to allow users to create Agent Based Simulations through a visual-programming interface. We have Implemented a nodegraph which allows users to qucikly create performant simulations which run entirly on the GPU. 


Here is an Example of Conway's Game of Life written in Apiary using our nodegraph system.
<p align="center"><img src="https://github.com/baileysostek/Apiary/blob/main/repository_images/GameOfLife.png" alt="Apiary"/></p>
