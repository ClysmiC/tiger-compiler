.data

#User-created variables
myArray_int: .word 0:100
x_int: .word 0
y_int: .word 7
z_int: .word 20

#Compiler-created variables.
_i3: .word 0
_i4: .word 0


.text

program_start:
mult $t2, $t0, $t1
add $t2, $t0, $t1
move $t1, $t0
