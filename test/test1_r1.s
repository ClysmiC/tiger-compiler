.data

#User-created variables
myArray_int: .word 0:100
x_int: .word 0
y_int: .word 7
z_int: .word 20
loopCounter_int: .word 0
loopCounter2_int: .word 0
myFloat_float: .float 3.5

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
_i23: .word 0
_i24: .word 0
_i25: .word 0
_i26: .word 0
_i27: .word 0
_i28: .word 0
_f29: .word 0
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
li $t0, 1
move $t1, $t0
sw $t1, _i7
li $t0, 3
move $t1, $t0
sw $t1, _i8
lw $t0, _i7
lw $t1, _i8
add $t2, $t0, $t1
sw $t2, _i9
li $t0, 6
move $t1, $t0
sw $t1, _i10
li $t0, 7
move $t1, $t0
sw $t1, _i11
lw $t0, _i10
lw $t1, _i11
add $t2, $t0, $t1
sw $t2, _i12
lw $t0, _i9
move $t1, $t0
sw $t1, loopCounter_int

_FOR_start0:
lw $t0, loopCounter_int
lw $t1, _i12
bgt $t0, $t1, _FOR_end0
li $t0, 1
move $t1, $t0
sw $t1, _i13
lw $t0, loopCounter_int
lw $t1, _i13
and $t2, $t0, $t1
sw $t2, _i14
lw $t0, _i14
li $t1, 0
beq $t0, $t1, _ELSE_start0
lw $t0, x_int
lw $t1, y_int
add $t2, $t0, $t1
sw $t2, _i15
lw $t0, _i15
move $t1, $t0
sw $t1, x_int
li.s $f0, 1.5
mfc1 $t0 $f0
move $t1, $t0
sw $t1, _f16
lw $t0, x_int
lw $t1, _f16
mtc1 $t0, $f0
mtc1 $t1, $f1
cvt.s.w $f0, $f0
div.s $f2, $f0, $f1
mfc1 $t2, $f2
sw $t2, _f17
lw $t0, _f17
move $t1, $t0
sw $t1, myFloat_float
j _IF_end0

_ELSE_start0:
lw $t0, x_int
lw $t1, z_int
add $t2, $t0, $t1
sw $t2, _i18
lw $t0, _i18
move $t1, $t0
sw $t1, x_int
li.s $f0, 1.5
mfc1 $t0 $f0
move $t1, $t0
sw $t1, _f19
lw $t0, x_int
lw $t1, _f19
mtc1 $t0, $f0
mtc1 $t1, $f1
cvt.s.w $f0, $f0
div.s $f2, $f0, $f1
mfc1 $t2, $f2
sw $t2, _f20
lw $t0, _f20
move $t1, $t0
sw $t1, myFloat_float
lw $t0, x_int
lw $t1, loopCounter_int
sub $t2, $t0, $t1
sw $t2, _i21
lw $t0, _i21
move $t1, $t0
sw $t1, x_int
lw $t0, x_int
lw $t1, loopCounter_int
add $t2, $t0, $t1
sw $t2, _i22
lw $t0, _i22
move $t1, $t0
sw $t1, x_int
lw $t0, x_int
lw $t1, loopCounter_int
add $t2, $t0, $t1
sw $t2, _i23
lw $t0, _i23
move $t1, $t0
sw $t1, x_int
li $t0, 1
move $t1, $t0
sw $t1, _i24
li $t0, 4
move $t1, $t0
sw $t1, _i25
lw $t0, _i24
move $t1, $t0
sw $t1, loopCounter2_int

_FOR_start1:
lw $t0, loopCounter2_int
lw $t1, _i25
bgt $t0, $t1, _FOR_end1
lw $t0, x_int
lw $t1, loopCounter_int
add $t2, $t0, $t1
sw $t2, _i26
lw $t0, _i26
move $t1, $t0
sw $t1, x_int
lw $t0, loopCounter2_int
li $t1, 1
add $t2, $t0, $t1
sw $t2, loopCounter2_int
j _FOR_start1

_FOR_end1:

_IF_end0:
lw $t0, loopCounter_int
li $t1, 1
add $t2, $t0, $t1
sw $t2, loopCounter_int
j _FOR_start0

_FOR_end0:
lw $t0, x_int
move $t1, $t0
sw $t1, __quadrupleMe_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal quadrupleMe
sw $v0, _i27
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $t0, _i27
move $t1, $t0
sw $t1, x_int
li $t0, 2
move $t1, $t0
sw $t1, _i28
lw $t0, myFloat_float
lw $t1, _i28
mtc1 $t0, $f0
mtc1 $t1, $f1
cvt.s.w $f1, $f1
mul.s $f2, $f0, $f1
mfc1 $t2, $f2
sw $t2, _f29
lw $t0, _f29
move $t1, $t0
sw $t1, myFloat_float
lw $t0, x_int
move $t1, $t0
sw $t1, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $t0, myFloat_float
move $t1, $t0
sw $t1, __printf_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
