.data

.globl main

#User-created variables
myArray_int: .word 0:100
x_int: .word 0
y_int: .word 7
z_int: .word 20
loopCounter_int: .word 0
flute_float: .float 3.5

#Compiler-created variables.
_i4: .word 0
_i5: .word 0
_i6: .word 0
_i7: .word 0
_i8: .word 0
_i9: .word 0
_i10: .word 0
_i11: .word 0
_i12: .word 0
_i13: .word 0
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

main:
li $t0, 1
move $t1, $t0
sw $t1, _i4
li $t0, 3
move $t1, $t0
sw $t1, _i5
lw $t0, _i4
lw $t1, _i5
add $t2, $t0, $t1
sw $t2, _i6
li $t0, 6
move $t1, $t0
sw $t1, _i7
li $t0, 7
move $t1, $t0
sw $t1, _i8
lw $t0, _i7
lw $t1, _i8
add $t2, $t0, $t1
sw $t2, _i9
lw $t0, _i6
move $t1, $t0
sw $t1, loopCounter_int

_FOR_start0:
lw $t0, loopCounter_int
lw $t1, _i9
bgt $t0, $t1, _FOR_end0
li $t0, 1
move $t1, $t0
sw $t1, _i10
lw $t0, loopCounter_int
lw $t1, _i10
and $t2, $t0, $t1
sw $t2, _i11
lw $t0, _i11
li $t1, 0
beq $t0, $t1, _ELSE_start0
lw $t0, x_int
lw $t1, y_int
add $t2, $t0, $t1
sw $t2, _i12
lw $t0, _i12
move $t1, $t0
sw $t1, x_int
j _IF_end0

_ELSE_start0:
lw $t0, x_int
lw $t1, z_int
add $t2, $t0, $t1
sw $t2, _i13
lw $t0, _i13
move $t1, $t0
sw $t1, x_int

_IF_end0:
lw $t0, loopCounter_int
li $t1, 1
add $t2, $t0, $t1
sw $t2, loopCounter_int
j _FOR_start0

_FOR_end0:
lw $t0, x_int
move $t1, $t0
sw $t1, __printi_arg0
lw $a0, __printi_arg0
jal printi

li $v0, 10
syscall
