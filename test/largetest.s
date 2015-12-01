.data

#User-created variables
y_int: .word 2
z_float: .float 0
loopCounter_int: .word 0
f1_float: .float 5
f2_float: .float 1.3
afloat_float: .float 0
buffer_int: .word 8:2

#Compiler-created variables.
__fillArray_arg0: .word 0
_fillArray_int1_int: .word 0
__fillArray_arg1: .word 0
_fillArray_int2_int: .word 0
_i5: .word 0
_i6: .word 0
__fillArrayNoRetVal_arg0: .word 0
_fillArrayNoRetVal_int3_int: .word 0
__fillArrayNoRetVal_arg1: .word 0
_fillArrayNoRetVal_int4_int: .word 0
_i7: .word 0
_i8: .word 0
__areIntsEqual_arg0: .word 0
_areIntsEqual_n_int: .word 0
__areIntsEqual_arg1: .word 0
_areIntsEqual_n2_int: .word 0
_i9: .word 0
__areFloatsEqual_arg0: .word 0
_areFloatsEqual_n_float: .word 0
__areFloatsEqual_arg1: .word 0
_areFloatsEqual_n2_float: .word 0
_i10: .word 0
_f11: .word 0
_f12: .word 0
_i13: .word 0
_i14: .word 0
_i15: .word 0
_i16: .word 0
_i17: .word 0
_f18: .word 0
_f19: .word 0
_i20: .word 0
_i21: .word 0
_i22: .word 0
_i23: .word 0
_i24: .word 0
_i25: .word 0
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

fillArray:
lw $t0, __fillArray_arg0
move $t1, $t0
sw $t1, _fillArray_int1_int
lw $t0, __fillArray_arg1
move $t1, $t0
sw $t1, _fillArray_int2_int
li $t0, 0
move $t1, $t0
sw $t1, _i5
lw $t0, _i5
lw $t1, _fillArray_int1_int
li $at 4
mul $at $t0
mflo $at
sw $t1, buffer_int($at)
li $t0, 1
move $t1, $t0
sw $t1, _i6
lw $t0, _i6
lw $t1, _fillArray_int2_int
li $at 4
mul $at $t0
mflo $at
sw $t1, buffer_int($at)
lw $t0, buffer_int
move $t1, $t0
sw $t1, buffer_int
lw $v0, buffer_int
jr $ra

fillArrayNoRetVal:
lw $t0, __fillArrayNoRetVal_arg0
move $t1, $t0
sw $t1, _fillArrayNoRetVal_int3_int
lw $t0, __fillArrayNoRetVal_arg1
move $t1, $t0
sw $t1, _fillArrayNoRetVal_int4_int
li $t0, 0
move $t1, $t0
sw $t1, _i7
lw $t0, _i7
lw $t1, _fillArrayNoRetVal_int3_int
li $at 4
mul $at $t0
mflo $at
sw $t1, buffer_int($at)
li $t0, 1
move $t1, $t0
sw $t1, _i8
lw $t0, _i8
lw $t1, _fillArrayNoRetVal_int4_int
li $at 4
mul $at $t0
mflo $at
sw $t1, buffer_int($at)
jr $ra

areIntsEqual:
lw $t0, __areIntsEqual_arg0
move $t1, $t0
sw $t1, _areIntsEqual_n_int
lw $t0, __areIntsEqual_arg1
move $t1, $t0
sw $t1, _areIntsEqual_n2_int
li $t0, 0
move $t1, $t0
sw $t1, _i9
lw $t0, _areIntsEqual_n_int
lw $t1, _areIntsEqual_n2_int
beq $t0, $t1, _EQ_true0
j _EQ_false0

_EQ_true0:
li $t0, 1
move $t1, $t0
sw $t1, _i9

_EQ_false0:
lw $v0, _i9
jr $ra

areFloatsEqual:
lw $t0, __areFloatsEqual_arg0
move $t1, $t0
sw $t1, _areFloatsEqual_n_float
lw $t0, __areFloatsEqual_arg1
move $t1, $t0
sw $t1, _areFloatsEqual_n2_float
li $t0, 0
move $t1, $t0
sw $t1, _i10
lw $t0, _areFloatsEqual_n_float
lw $t1, _areFloatsEqual_n2_float
beq $t0, $t1, _EQ_true1
j _EQ_false1

_EQ_true1:
li $t0, 1
move $t1, $t0
sw $t1, _i10

_EQ_false1:
lw $v0, _i10
jr $ra

main:
li $t0, 0.5
move $t1, $t0
sw $t1, _f11
li $t0, 0.1
move $t1, $t0
sw $t1, _f12
li $t0, 0
move $t1, $t0
sw $t1, _i13
lw $t0, _f11
lw $t1, _f12
beq $t0, $t1, _EQ_true2
j _EQ_false2

_EQ_true2:
li $t0, 1
move $t1, $t0
sw $t1, _i13

_EQ_false2:
li $t0, 10
move $t1, $t0
sw $t1, _i14
li $t0, 5
move $t1, $t0
sw $t1, _i15
lw $t0, _i14
lw $t1, _i15
add $t2, $t0, $t1
sw $t2, _i16
lw $t0, _i13
move $t1, $t0
sw $t1, loopCounter_int

_FOR_start0:
lw $t0, loopCounter_int
lw $t1, _i16
bgt $t0, $t1, _FOR_end0
li $t0, 1
move $t1, $t0
sw $t1, _i17
lw $t0, z_float
lw $t1, _i17
mtc1 $t0, $f0
mtc1 $t1, $f1
cvt.s.w $f1, $f1
add.s $f2, $f0, $f1
mfc1 $t2, $f2
sw $t2, _f18
lw $t0, _f18
move $t1, $t0
sw $t1, z_float
li $t0, 0.0
move $t1, $t0
sw $t1, _f19
li $t0, 0
move $t1, $t0
sw $t1, _i20
lw $t0, z_float
lw $t1, _f19
bgt $t0, $t1, _INEQ_true0
j _INEQ_false0

_INEQ_true0:
li $t0, 1
move $t1, $t0
sw $t1, _i20

_INEQ_false0:
lw $t0, _i20
li $t1, 0
beq $t0, $t1, _ELSE_start0
j _FOR_end0
j _IF_end0

_ELSE_start0:

_IF_end0:
lw $t0, loopCounter_int
li $t1, 1
add $t2, $t0, $t1
sw $t2, loopCounter_int
j _FOR_start0

_FOR_end0:
li $t0, 15
move $t1, $t0
sw $t1, _i21
lw $t0, _i21
move $t1, $t0
sw $t1, __fillArray_arg0
li $t0, 10
move $t1, $t0
sw $t1, _i22
lw $t0, _i22
move $t1, $t0
sw $t1, __fillArray_arg1
lw $a0, __fillArray_arg0
lw $a1, __fillArray_arg1
jal fillArray
sw $v0, _i23
lw $t0, _i23
move $t1, $t0
sw $t1, buffer_int
li $t0, 3
move $t1, $t0
sw $t1, _i24
lw $t0, _i24
move $t1, $t0
sw $t1, __fillArrayNoRetVal_arg0
li $t0, 7
move $t1, $t0
sw $t1, _i25
lw $t0, _i25
move $t1, $t0
sw $t1, __fillArrayNoRetVal_arg1
lw $a0, __fillArrayNoRetVal_arg0
lw $a1, __fillArrayNoRetVal_arg1
jal fillArrayNoRetVal

li $v0, 10
syscall
