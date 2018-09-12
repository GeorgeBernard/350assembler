# 350assembler

**@author: pf51**
**@date: Nov 5 2017**

**@author: mdd36**
**@date: Sep 2018**

### About

1. **What is:** this is a simple parser that converts MIPS code into machine code using the ISA provided in the Duke ECE350 handout.
2. You need Java 10 to run this project. Find it here: https://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html
3. **How to:**   
(1) clone or download the repo,  
(2) write your mips code in any file,   
(3) launch the GUI from the provided jar  
(4) Select an input folder/file and an output folder  
(5) Click launch
4. Things you **CAN** do:  
(1) write comments using "#" to start a line,  
(2) use noop's by typing noop  
(3) use $rstatus as syntax,  
(4) use $ra as syntax  
(5) have optional trailing semicolons on lines  
(6) Use a directory as input  
(7) Have the assembler pad each instruction with noops to prevent hazards  
(8) use all other instructions in the correct syntax, found in our handout and also included below 

### Instructions

    nop
    add   $rd,   $rs,   $rt
    addi  $rd,   $rs,   N
    sub   $rd,   $rs,   $rt
    or   $rd,   $rs,   $rt
    sll   $rd,   $rs,   shamt
    sra   $rd,   $rs,   shamt
    mul   $rd,   $rd,   $rt
    div   $rd,   $rs,   $rt
    sw   $rd,   N($rs)
    lw   $rd,   N($rs)
    j   T
    bne   $rd,   $rs,   N
    jal   T
    jr   $rd
    blt   $rd,   $rs,   N
    bex   T
    setx   T
