.data

#User-created variables
myArray_int: .word 0:100
x_int: .word 0
y_int: .word 7
z_int: .word 20
loopCounter_int: .word 0
flute_float: .float 3.5

#Compiler-created variables.
__doubleMe_arg0: .word 0
_doubleMe_input_int: .word 0
_i4: .word 0
__quadrupleMe_arg0: .word 0
_quadrupleMe_input_int: .word 0
_i5: .word 0
_i6: .word 0
_i7: .word 0
_i8: .word 0
_i9: .word 0
_i10: .word 0
_i11: .word 0
_i12: .word 0
_i13: .word 0
_i14: .word 0
_i15: .word 0
_f16: .word 0
_f17: .word 0
_i18: .word 0
_f19: .word 0
_f20: .word 0
_i21: .word 0
_i22: .word 0
_f23: .word 0
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

doubleMe:
lw $t0, __doubleMe_arg0
move $t1, $t0
sw $t1, _doubleMe_input_int
lw $t0, _doubleMe_input_int
lw $t1, _doubleMe_input_int
add $t2, $t0, $t1
sw $t2, _i4
lw $v0, _i4
jr $ra

quadrupleMe:
lw $t0, __quadrupleMe_arg0
move $t1, $t0
sw $t1, _quadrupleMe_input_int
lw $t0, _quadrupleMe_input_int
move $t1, $t0
sw $t1, __doubleMe_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal doubleMe
sw $v0, _i5
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $t0, _i5
move $t1, $t0
sw $t1, _quadrupleMe_input_int
lw $t0, _quadrupleMe_input_int
move $t1, $t0
sw $t1, __doubleMe_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal doubleMe
sw $v0, _i6
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $t0, _i6
move $t1, $t0
sw $t1, _quadrupleMe_input_int
lw $v0, _quadrupleMe_input_int
jr $ra

main:
li $s0, 1
move $s1, $s0
sw $s1, _i7
li $s0, 3
move $s1, $s0
sw $s1, _i8
lw $s0, _i7
lw $s1, _i8
add $s2, $s0, $s1
sw $s2, _i9
li $s0, 6
move $s1, $s0
sw $s1, _i10
li $s0, 7
move $s1, $s0
sw $s1, _i11
lw $s0, _i10
lw $s1, _i11
add $s2, $s0, $s1
sw $s2, _i12
lw $s0, _i9
move $s1, $s0
sw $s1, loopCounter_int

_FOR_start0:
lw $s0, loopCounter_int
lw $s1, _i12
bgt $s0, $s1, _FOR_end0
li $s0, 1
move $s1, $s0
sw $s1, _i13
lw $s0, loopCounter_int
lw $s1, _i13
and $s2, $s0, $s1
sw $s2, _i14
lw $s0, _i14
li $s1, 0
beq $s0, $s1, _ELSE_start0
lw $s0, x_int
lw $s1, y_int
add $s2, $s0, $s1
sw $s2, _i15
lw $s0, _i15
move $s1, $s0
sw $s1, x_int
li.s $f0, 1.5
mfc1 $s0 $f0
move $s1, $s0
sw $s1, _f16
lw $s0, x_int
lw $s1, _f16
mtc1 $s0, $f0
mtc1 $s1, $f1
cvt.s.w $f0, $f0
div.s $f2, $f0, $f1
mfc1 $s2, $f2
sw $s2, _f17
lw $s0, _f17
move $s1, $s0
sw $s1, flute_float
j _IF_end0

_ELSE_start0:
lw $s0, x_int
lw $s1, z_int
add $s2, $s0, $s1
sw $s2, _i18
lw $s0, _i18
move $s1, $s0
sw $s1, x_int
li.s $f0, 1.5
mfc1 $s0 $f0
move $s1, $s0
sw $s1, _f19
lw $s0, x_int
lw $s1, _f19
mtc1 $s0, $f0
mtc1 $s1, $f1
cvt.s.w $f0, $f0
div.s $f2, $f0, $f1
mfc1 $s2, $f2
sw $s2, _f20
lw $s0, _f20
move $s1, $s0
sw $s1, flute_float

_IF_end0:
lw $s0, loopCounter_int
li $s1, 1
add $s2, $s0, $s1
sw $s2, loopCounter_int
j _FOR_start0

_FOR_end0:
lw $s0, x_int
move $s1, $s0
sw $s1, __quadrupleMe_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal quadrupleMe
sw $v0, _i21
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, _i21
move $s1, $s0
sw $s1, x_int
li $s0, 2
move $s1, $s0
sw $s1, _i22
lw $s0, flute_float
lw $s1, _i22
mtc1 $s0, $f0
mtc1 $s1, $f1
cvt.s.w $f1, $f1
mul.s $f2, $f0, $f1
mfc1 $s2, $f2
sw $s2, _f23
lw $s0, _f23
move $s1, $s0
sw $s1, flute_float
lw $s0, x_int
move $s1, $s0
sw $s1, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, flute_float
move $s1, $s0
sw $s1, __printf_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
