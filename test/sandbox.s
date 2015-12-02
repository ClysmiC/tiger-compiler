.data

#User-created variables
buffer_int: .word 0:2
y_int: .word 7
z_int: .word 20
flute_float: .float 3.5

#Compiler-created variables.
_doubleMe_input_int: .word 0
_i3: .word 0
__doubleMe_arg0: .word 0
_quadrupleMe_input_int: .word 0
__quadrupleMe_arg0: .word 0
_i4: .word 0
_i5: .word 0
__fillArray_arg0: .word 0
_fillArray_secondInt_int: .word 0
_i7: .word 0
_fillArray_firstInt_int: .word 0
_i6: .word 0
__fillArray_arg1: .word 0
__printi_arg0: .word 0
_i9: .word 0
_i8: .word 0
_i13: .word 0
_i14: .word 0
_i15: .word 0
_i16: .word 0
_i10: .word 0
_i11: .word 0
_i12: .word 0
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
lw $s0, _doubleMe_input_int
lw $s1, _i3
lw $s2, __doubleMe_arg0
move $s0, $s2
add $s1, $s0, $s0
move $v0, $s1
sw $s0, _doubleMe_input_int
sw $s1, _i3
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
sw $v0, _i4
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, _quadrupleMe_input_int
lw $s1, __doubleMe_arg0
lw $s2, _i4
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
lw $s1, _i5
move $s0, $s1
move $v0, $s0
sw $s0, _quadrupleMe_input_int
jr $ra

fillArray:
lw $s0, __fillArray_arg0
lw $s1, _fillArray_secondInt_int
lw $s2, _i7
lw $s3, _fillArray_firstInt_int
lw $s4, _i6
lw $s5, __fillArray_arg1
move $s3, $s0
move $s1, $s5
li $t0, 0
move $s4, $t0
li $a3, 4
mult $a3, $s4
mflo $a3
sw $s3, buffer_int($a3)
li $t0, 1
move $s2, $t0
li $a3, 4
mult $a3, $s2
mflo $a3
sw $s1, buffer_int($a3)
sw $s1, _fillArray_secondInt_int
sw $s2, _i7
sw $s3, _fillArray_firstInt_int
sw $s4, _i6
jr $ra

main:
lw $s0, __fillArray_arg0
lw $s2, z_int
lw $s3, y_int
lw $s1, __fillArray_arg1
move $s0, $s3
move $s1, $s2
sw $s0, __fillArray_arg0
sw $s1, __fillArray_arg1
addi $sp, $sp, -4
sw $ra, 0($sp)
jal fillArray
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, __printi_arg0
lw $s1, _i9
lw $s2, _i8
li $t0, 0
move $s2, $t0
li $a3, 4
mult $a3, $s2
mflo $a3
lw $s1, buffer_int($a3)
move $s0, $s1
sw $s0, __printi_arg0
sw $s2, _i8
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s1, _i13
lw $s2, _i14
lw $s3, _i15
lw $s4, _i16
lw $s0, __printi_arg0
lw $s5, _i10
lw $s6, _i11
lw $s7, _i12
lw $t3, _i17
li $t0, 1
move $s5, $t0
li $t0, 0
move $s6, $t0
li $a3, 4
mult $a3, $s6
mflo $a3
lw $s7, buffer_int($a3)
li $t0, 1
move $s1, $t0
li $a3, 4
mult $a3, $s1
mflo $a3
lw $s2, buffer_int($a3)
mult $s7, $s2
mflo $s3
li $a3, 4
mult $a3, $s5
mflo $a3
sw $s3, buffer_int($a3)
li $t0, 1
move $s4, $t0
li $a3, 4
mult $a3, $s4
mflo $a3
lw $t3, buffer_int($a3)
move $s0, $t3
sw $s1, _i13
sw $s3, _i15
sw $s4, _i16
sw $s0, __printi_arg0
sw $s5, _i10
sw $s6, _i11
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
