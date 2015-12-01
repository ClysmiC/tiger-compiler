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
_i16: .word 0
_f17: .word 0
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

main:
li $t0, 1
move $t1, $t0
sw $t1, _i5
li $t0, 3
move $t1, $t0
sw $t1, _i6
lw $t0, _i5
lw $t1, _i6
add $t2, $t0, $t1
sw $t2, _i7
li $t0, 6
move $t1, $t0
sw $t1, _i8
li $t0, 7
move $t1, $t0
sw $t1, _i9
lw $t0, _i8
lw $t1, _i9
add $t2, $t0, $t1
sw $t2, _i10
lw $t0, _i7
move $t1, $t0
sw $t1, loopCounter_int

_FOR_start0:
lw $t0, loopCounter_int
lw $t1, _i10
bgt $t0, $t1, _FOR_end0
li $t0, 1
move $t1, $t0
sw $t1, _i11
lw $t0, loopCounter_int
lw $t1, _i11
and $t2, $t0, $t1
sw $t2, _i12
lw $t0, _i12
li $t1, 0
beq $t0, $t1, _ELSE_start0
lw $t0, x_int
lw $t1, y_int
add $t2, $t0, $t1
sw $t2, _i13
lw $t0, _i13
move $t1, $t0
sw $t1, x_int
j _IF_end0

_ELSE_start0:
lw $t0, x_int
lw $t1, z_int
add $t2, $t0, $t1
sw $t2, _i14
lw $t0, _i14
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
sw $t1, __doubleMe_arg0
lw $a0, __doubleMe_arg0
jal doubleMe
sw $v0, _i15
lw $t0, _i15
move $t1, $t0
sw $t1, x_int
li $t0, 2
move $t1, $t0
sw $t1, _i16
lw $t0, flute_float
lw $t1, _i16
mtc1 $t0, $f0
mtc1 $t1, $f1
cvt.s.w $f1, $f1
mul.s $f2, $f0, $f1
mfc1 $t2, $f2
sw $t2, _f17
lw $t0, _f17
move $t1, $t0
sw $t1, flute_float
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
