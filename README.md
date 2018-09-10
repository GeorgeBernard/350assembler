# 350assembler

**@author: pf51**
**@date: Nov 5 2017**

**@author: mdd36**
**@date: Sep 2018**

### About

1. **What is:** this is a simple parser that converts MIPS code into machine code using the ISA provided in the Duke ECE350 handout. The code isn't super elegant and only does basic error checking, so ideally make sure there aren't any mistakes when you write it.
2. **How to:** (1) clone or download the repo, (2) write your mips code in any file, but give it a .s extention, (3) launch the GUI from the provided jar
3. Things you **CAN** do: (1) write comments using "#" to start a line, (2) use nop's by typing nop (3) use $rstatus as syntax, (4) use $ra as syntax (5) use all other instructions in the correct syntax, found in our handout and also included below

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
