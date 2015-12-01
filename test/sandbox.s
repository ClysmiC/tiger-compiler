.data

#User-created variables
myArray_int: .word 0:100
x_int: .word 0
y_int: .word 7
z_int: .word 20
flute_float: .float 3.5

#Compiler-created variables.
_i4: .word 0
_i5: .word 0
__printi_arg0: .word 0
__printf_arg0: .word 0


.text

printi:
li $v0, 1
syscall
jr $ra

printf:
li $v0, 2
mtc1 $a0, $f12
syscall
jr $ra

_program_start:
main:
lw $t0, z_int
lw $t1, z_int
mult $t0, $t1
mflo $t2
sw $t2, _i4
lw $t0, y_int
lw $t1, _i4
add $t2, $t0, $t1
sw $t2, _i5
lw $t0, _i5
move $t1, $t0
sw $t1, x_int
lw $t0, x_int
move $t1, $t0
sw $t1, __printi_arg0
lw $a0, __printi_arg0
jal printi
lw $t0, flute_float
move $t1, $t0
sw $t1, __printf_arg0
lw $a0, __printf_arg0
jal printf

li $v0, 10
syscall
