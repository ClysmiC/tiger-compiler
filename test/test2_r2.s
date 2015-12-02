.data

#User-created variables
buffer_int: .word 0:2
y_int: .word 7
z_int: .word 20

#Compiler-created variables.
__fillArray_arg0: .word 0
_i3: .word 0
_i2: .word 0
_fillArray_secondInt_int: .word 0
_fillArray_firstInt_int: .word 0
__fillArray_arg1: .word 0
_i13: .word 0
_i14: .word 0
_i15: .word 0
_i16: .word 0
_i10: .word 0
_i11: .word 0
_i12: .word 0
_i5: .word 0
_i4: .word 0
_i7: .word 0
_i6: .word 0
_i9: .word 0
_i8: .word 0
__printi_arg0: .word 0
_i17: .word 0
_i18: .word 0
_i19: .word 0
_i20: .word 0
_i21: .word 0
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

fillArray:
lw $s0, __fillArray_arg0
lw $s1, _i3
lw $s2, _i2
lw $s3, _fillArray_secondInt_int
lw $s4, _fillArray_firstInt_int
lw $s5, __fillArray_arg1
move $s4, $s0
move $s3, $s5
li $t0, 0
move $s2, $t0
li $a3, 4
mult $a3, $s2
mflo $a3
sw $s4, buffer_int($a3)
li $t0, 1
move $s1, $t0
li $a3, 4
mult $a3, $s1
mflo $a3
sw $s3, buffer_int($a3)
sw $s1, _i3
sw $s2, _i2
sw $s3, _fillArray_secondInt_int
sw $s4, _fillArray_firstInt_int
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
lw $t4, _i13
lw $t5, _i14
lw $t6, _i15
lw $t7, _i16
lw $s0, _i10
lw $s1, _i11
lw $s2, _i12
lw $s3, _i5
lw $t8, _i4
lw $s4, _i7
lw $s5, _i6
lw $s6, _i9
lw $s7, _i8
lw $t3, __printi_arg0
lw $t9, _i17
li $t0, 1
move $t8, $t0
li $t0, 0
move $s3, $t0
li $t0, 1
move $s5, $t0
add $s4, $s3, $s5
li $t0, 9
move $s7, $t0
li $t0, 2
move $s6, $t0
mult $s7, $s6
mflo $s0
add $s1, $s4, $s0
li $t0, 19
move $s2, $t0
sub $t4, $s1, $s2
li $a3, 4
mult $a3, $t4
mflo $a3
lw $t5, buffer_int($a3)
li $t0, 1
move $t6, $t0
li $a3, 4
mult $a3, $t6
mflo $a3
lw $t7, buffer_int($a3)
mult $t5, $t7
mflo $t9
li $a3, 4
mult $a3, $t8
mflo $a3
sw $t9, buffer_int($a3)
li $t0, 0
move $t1, $t0
sw $t1, _i18
lw $t1,, _i18
li $a3, 4
mult $a3, $t1
mflo $a3
lw $t0, buffer_int($a3)
sw $t0, _i19
lw $t0, _i19
move $t3, $t0
sw $t4, _i13
sw $t6, _i15
sw $s0, _i10
sw $s1, _i11
sw $s2, _i12
sw $s3, _i5
sw $t8, _i4
sw $s4, _i7
sw $s5, _i6
sw $s6, _i9
sw $s7, _i8
sw $t3, __printi_arg0
sw $t9, _i17
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, __printi_arg0
lw $s1, _i20
lw $s2, _i21
li $t0, 1
move $s1, $t0
li $a3, 4
mult $a3, $s1
mflo $a3
lw $s2, buffer_int($a3)
move $s0, $s2
sw $s0, __printi_arg0
sw $s1, _i20
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
