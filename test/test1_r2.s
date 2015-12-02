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
_doubleMe_input_int: .word 0
__doubleMe_arg0: .word 0
_i4: .word 0
_quadrupleMe_input_int: .word 0
__quadrupleMe_arg0: .word 0
_i5: .word 0
_i6: .word 0
_i10: .word 0
_i11: .word 0
_i12: .word 0
_i7: .word 0
_i9: .word 0
_i8: .word 0
_i13: .word 0
_i14: .word 0
_f16: .word 0
_f17: .word 0
_i15: .word 0
_i24: .word 0
_i25: .word 0
_f19: .word 0
_i21: .word 0
_i22: .word 0
_i23: .word 0
_f20: .word 0
_i18: .word 0
_i26: .word 0
_f29: .word 0
_i27: .word 0
__printi_arg0: .word 0
_i28: .word 0
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
lw $s0, _doubleMe_input_int
lw $s1, __doubleMe_arg0
lw $s2, _i4
move $s0, $s1
add $s2, $s0, $s0
move $v0, $s2
sw $s0, _doubleMe_input_int
sw $s2, _i4
jr $ra

quadrupleMe:
lw $s0, _quadrupleMe_input_int
lw $s1, __doubleMe_arg0
lw $s2, __quadrupleMe_arg0
move $s0, $s2
move $s1, $s0
sw $s0, _quadrupleMe_input_int
sw $s1, __doubleMe_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal doubleMe
sw $v0, _i5
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, _quadrupleMe_input_int
lw $s1, __doubleMe_arg0
lw $s2, _i5
move $s0, $s2
move $s1, $s0
sw $s0, _quadrupleMe_input_int
sw $s1, __doubleMe_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal doubleMe
sw $v0, _i6
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, _quadrupleMe_input_int
lw $s1, _i6
move $s0, $s1
move $v0, $s0
sw $s0, _quadrupleMe_input_int
jr $ra

main:
lw $s0, _i10
lw $s5, loopCounter_int
lw $s1, _i11
lw $s6, _i12
lw $s2, _i7
lw $s3, _i9
lw $s4, _i8
li $t0, 1
move $s2, $t0
li $t0, 3
move $s4, $t0
add $s3, $s2, $s4
li $t0, 6
move $s0, $t0
li $t0, 7
move $s1, $t0
add $s6, $s0, $s1
move $s5, $s3
sw $s0, _i10
sw $s5, loopCounter_int
sw $s1, _i11
sw $s6, _i12
sw $s2, _i7
sw $s3, _i9
sw $s4, _i8

_FOR_start0:
lw $s0, loopCounter_int
lw $s1, _i12
bgt $s0, $s1, _FOR_end0
lw $s0, _i13
lw $s1, _i14
lw $s2, loopCounter_int
li $t0, 1
move $s0, $t0
and $s1, $s2, $s0
li $t1, 0
beq $s1, $t1, _ELSE_start0
sw $s0, _i13
sw $s1, _i14
lw $s1, _f16
lw $s2, _f17
lw $s3, _i15
lw $s4, y_int
lw $s5, myFloat_float
lw $s0, x_int
add $s3, $s0, $s4
move $s0, $s3
li.s $f0, 1.5
mfc1 $t0 $f0
move $s1, $t0
mtc1 $s0, $f0
mtc1 $s1, $f1
cvt.s.w $f0, $f0
div.s $f2, $f0, $f1
mfc1 $s2, $f2
move $s5, $s2
sw $s1, _f16
sw $s2, _f17
sw $s3, _i15
sw $s5, myFloat_float
sw $s0, x_int
j _IF_end0

_ELSE_start0:
lw $s2, _i24
lw $t4, _i25
lw $s3, _f19
lw $s4, _i21
lw $s5, _i22
lw $s6, _i23
lw $s7, _f20
lw $t5, z_int
lw $t6, myFloat_float
lw $s0, x_int
lw $s1, loopCounter_int
lw $t7, loopCounter2_int
lw $t3, _i18
add $t3, $s0, $t5
move $s0, $t3
li.s $f0, 1.5
mfc1 $t0 $f0
move $s3, $t0
mtc1 $s0, $f0
mtc1 $s3, $f1
cvt.s.w $f0, $f0
div.s $f2, $f0, $f1
mfc1 $s7, $f2
move $t6, $s7
sub $s4, $s0, $s1
move $s0, $s4
add $s5, $s0, $s1
move $s0, $s5
add $s6, $s0, $s1
move $s0, $s6
li $t0, 1
move $s2, $t0
li $t0, 4
move $t4, $t0
move $t7, $s2
sw $s2, _i24
sw $t4, _i25
sw $s3, _f19
sw $s4, _i21
sw $s5, _i22
sw $s6, _i23
sw $s7, _f20
sw $t6, myFloat_float
sw $s0, x_int
sw $t7, loopCounter2_int
sw $t3, _i18

_FOR_start1:
lw $s0, _i25
lw $s1, loopCounter2_int
bgt $s1, $s0, _FOR_end1
lw $s0, _i26
lw $s3, loopCounter_int
lw $s1, loopCounter2_int
lw $s2, x_int
add $s0, $s2, $s3
move $s2, $s0
li $t1, 1
add $s1, $s1, $t1
sw $s0, _i26
sw $s1, loopCounter2_int
sw $s2, x_int
j _FOR_start1

_FOR_end1:

_IF_end0:
lw $s0, loopCounter_int
li $t1, 1
add $s0, $s0, $t1
sw $s0, loopCounter_int
j _FOR_start0

_FOR_end0:
lw $s0, __quadrupleMe_arg0
lw $s1, x_int
move $s0, $s1
sw $s0, __quadrupleMe_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal quadrupleMe
sw $v0, _i27
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, _f29
lw $s5, _i27
lw $s1, __printi_arg0
lw $s2, _i28
lw $s3, myFloat_float
lw $s4, x_int
move $s4, $s5
li $t0, 2
move $s2, $t0
mtc1 $s3, $f0
mtc1 $s2, $f1
cvt.s.w $f1, $f1
mul.s $f2, $f0, $f1
mfc1 $s0, $f2
move $s3, $s0
move $s1, $s4
sw $s0, _f29
sw $s1, __printi_arg0
sw $s2, _i28
sw $s3, myFloat_float
sw $s4, x_int
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, __printf_arg0
lw $s1, myFloat_float
move $s0, $s1
sw $s0, __printf_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
