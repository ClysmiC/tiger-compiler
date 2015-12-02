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
lw $s1, loopCounter_int
lw $s0, _i2
li $t0, 1
move $s0, $t0
move $s1, $s0
sw $s1, loopCounter_int
sw $s0, _i2

_FOR_start0:
lw $s0, loopCounter_int
lw $s1, number_int
bgt $s0, $s1, _FOR_end0
lw $s1, result_int
lw $s0, loopCounter_int
lw $s2, _i3
mult $s1, $s0
mflo $s2
move $s1, $s2
li $t1, 1
add $s0, $s0, $t1
sw $s1, result_int
sw $s0, loopCounter_int
sw $s2, _i3
j _FOR_start0

_FOR_end0:
lw $s1, result_int
lw $s0, __printi_arg0
move $s0, $s1
sw $s0, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
