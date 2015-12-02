.data

#User-created variables
number_int: .word 8
loopCounter_int: .word 0
result_int: .word 1

#Compiler-created variables.
_i2: .word 0
_i3: .word 0
__printi_arg0: .word 0
__printf_arg0: .word 0


.text

j main

printi:
li $v0, 1
lw $a0, __printi_arg0
syscall
jr $ra

printf:
li $v0, 2
lw $a0, __printf_arg0
mtc1 $a0, $f12
syscall
jr $ra

main:
li $t0, 1
move $t1, $t0
sw $t1, _i2
lw $t0, _i2
move $t1, $t0
sw $t1, loopCounter_int

_FOR_start0:
lw $t0, loopCounter_int
lw $t1, number_int
bgt $t0, $t1, _FOR_end0
lw $t0, result_int
lw $t1, loopCounter_int
mult $t0, $t1
mflo $t2
sw $t2, _i3
lw $t0, _i3
move $t1, $t0
sw $t1, result_int
lw $t0, loopCounter_int
li $t1, 1
add $t2, $t0, $t1
sw $t2, loopCounter_int
j _FOR_start0

_FOR_end0:
lw $t0, result_int
move $t1, $t0
sw $t1, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
