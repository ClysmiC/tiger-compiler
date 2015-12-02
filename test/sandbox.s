.data

#User-created variables
myArray_int: .word 0:100
x_int: .word 0
y_int: .word 7
z_int: .word 20
loopCounter_int: .word 0
flute_float: .float 3.5

#Compiler-created variables.
_doubleMe_input_int: .word 0
__doubleMe_arg0: .word 0
_i4: .word 0
_i5: .word 0
_i7: .word 0
_i6: .word 0
_i9: .word 0
_i8: .word 0
_i10: .word 0
_i11: .word 0
_i12: .word 0
_i13: .word 0
_i14: .word 0
_f17: .word 0
_i16: .word 0
__printi_arg0: .word 0
_i15: .word 0
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
lw $t3, _doubleMe_input_int
lw $t4, __doubleMe_arg0
lw $t5, _i4
move $t3, $t4
add $t5, $t3, $t3
move $v0, $t5
sw $t3, _doubleMe_input_int
sw $t4, __doubleMe_arg0
sw $t5, _i4
jr $ra

main:
lw $s3, _i5
lw $s4, _i7
lw $s5, _i6
lw $s6, _i9
lw $s7, _i8
li $s0, 1
move $s3, $s0
li $s0, 3
move $s5, $s0
add $s4, $s3, $s5
li $s0, 6
move $s7, $s0
li $s0, 7
move $s6, $s0
add $s2, $s7, $s6
sw $s2, _i10
move $s1, $s4
sw $s1, loopCounter_int
sw $s3, _i5
sw $s4, _i7
sw $s5, _i6
sw $s6, _i9
sw $s7, _i8

_FOR_start0:
lw $s3, loopCounter_int
lw $s4, _i10
bgt $s3, $s4, _FOR_end0
sw $s3, loopCounter_int
sw $s4, _i10
lw $s5, loopCounter_int
lw $s3, _i11
lw $s4, _i12
li $s0, 1
move $s3, $s0
and $s4, $s5, $s3
li $s1, 0
beq $s4, $s1, _ELSE_start0
sw $s5, loopCounter_int
sw $s3, _i11
sw $s4, _i12
lw $s3, _i13
lw $s5, y_int
lw $s4, x_int
add $s3, $s4, $s5
move $s4, $s3
sw $s3, _i13
sw $s5, y_int
sw $s4, x_int
j _IF_end0

_ELSE_start0:
lw $s3, _i14
lw $s5, z_int
lw $s4, x_int
add $s3, $s4, $s5
move $s4, $s3
sw $s3, _i14
sw $s5, z_int
sw $s4, x_int

_IF_end0:
lw $s3, loopCounter_int
li $s1, 1
add $s3, $s3, $s1
sw $s3, loopCounter_int
j _FOR_start0

_FOR_end0:
lw $s3, __doubleMe_arg0
lw $s4, x_int
move $s3, $s4
sw $s3, __doubleMe_arg0
sw $s4, x_int
jal doubleMe
sw $v0, _i15
lw $s3, _f17
lw $s4, _i16
lw $s5, __printi_arg0
lw $s6, flute_float
lw $s7, x_int
lw $s0, _i15
move $s7, $s0
li $s0, 2
move $s4, $s0
mtc1 $s6, $f0
mtc1 $s4, $f1
cvt.s.w $f1, $f1
mul.s $f2, $f0, $f1
mfc1 $s3, $f2
move $s6, $s3
move $s5, $s7
sw $s3, _f17
sw $s4, _i16
sw $s5, __printi_arg0
sw $s6, flute_float
sw $s7, x_int
jal printi
lw $s4, flute_float
lw $s3, __printf_arg0
move $s3, $s4
sw $s4, flute_float
sw $s3, __printf_arg0
jal printf

li $v0, 10
syscall
