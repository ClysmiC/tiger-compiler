.data

#User-created variables
buffer_int: .word 0:2
y_int: .word 7
z_int: .word 20
flute_float: .float 3.5

#Compiler-created variables.
__doubleMe_arg0: .word 0
_doubleMe_input_int: .word 0
_i3: .word 0
__quadrupleMe_arg0: .word 0
_quadrupleMe_input_int: .word 0
_i4: .word 0
_i5: .word 0
__fillArray_arg0: .word 0
_fillArray_firstInt_int: .word 0
__fillArray_arg1: .word 0
_fillArray_secondInt_int: .word 0
_i6: .word 0
_i7: .word 0
_i8: .word 0
_i9: .word 0
__printi_arg0: .word 0
_i10: .word 0
_i11: .word 0
_i12: .word 0
_i13: .word 0
_i14: .word 0
_i15: .word 0
_i16: .word 0
_i17: .word 0
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
sw $t2, _i3
lw $v0, _i3
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
sw $v0, _i4
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $t0, _i4
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
lw $v0, _quadrupleMe_input_int
jr $ra

fillArray:
lw $t0, __fillArray_arg0
move $t1, $t0
sw $t1, _fillArray_firstInt_int
lw $t0, __fillArray_arg1
move $t1, $t0
sw $t1, _fillArray_secondInt_int
li $t0, 0
move $t1, $t0
sw $t1, _i6
lw $t0, _i6
lw $t1, _fillArray_firstInt_int
li $a3, 4
mult $a3, $t0
mflo $a3
sw $t1, buffer_int($a3)
li $t0, 1
move $t1, $t0
sw $t1, _i7
lw $t0, _i7
lw $t1, _fillArray_secondInt_int
li $a3, 4
mult $a3, $t0
mflo $a3
sw $t1, buffer_int($a3)
jr $ra

main:
lw $t0, y_int
move $t1, $t0
sw $t1, __fillArray_arg0
lw $t0, z_int
move $t1, $t0
sw $t1, __fillArray_arg1
addi $sp, $sp, -4
sw $ra, 0($sp)
jal fillArray
lw $ra, 0($sp)
addi $sp, $sp, 4
li $t0, 0
move $t1, $t0
sw $t1, _i8
lw $t0,, _i8
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i9
lw $t0, _i9
move $t1, $t0
sw $t1, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4
li $t0, 1
move $t1, $t0
sw $t1, _i10
li $t0, 0
move $t1, $t0
sw $t1, _i11
lw $t0,, _i11
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i12
li $t0, 1
move $t1, $t0
sw $t1, _i13
lw $t0,, _i13
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i14
lw $t0, _i12
lw $t1, _i14
mult $t0, $t1
mflo $t2
sw $t2, _i15
lw $t0, _i10
lw $t1, _i15
li $a3, 4
mult $a3, $t0
mflo $a3
sw $t1, buffer_int($a3)
li $t0, 1
move $t1, $t0
sw $t1, _i16
lw $t0,, _i16
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i17
lw $t0, _i17
move $t1, $t0
sw $t1, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
