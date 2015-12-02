.data

#User-created variables
buffer_int: .word 0:2
y_int: .word 7
z_int: .word 20

#Compiler-created variables.
__fillArray_arg0: .word 0
_fillArray_firstInt_int: .word 0
__fillArray_arg1: .word 0
_fillArray_secondInt_int: .word 0
_i2: .word 0
_i3: .word 0
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
_i17: .word 0
_i18: .word 0
_i19: .word 0
__printi_arg0: .word 0
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
lw $t0, __fillArray_arg0
move $t1, $t0
sw $t1, _fillArray_firstInt_int
lw $t0, __fillArray_arg1
move $t1, $t0
sw $t1, _fillArray_secondInt_int
li $t0, 0
move $t1, $t0
sw $t1, _i2
lw $t0, _i2
lw $t1, _fillArray_firstInt_int
li $a3, 4
mult $a3, $t0
mflo $a3
sw $t1, buffer_int($a3)
li $t0, 1
move $t1, $t0
sw $t1, _i3
lw $t0, _i3
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
li $t0, 1
move $t1, $t0
sw $t1, _i4
li $t0, 0
move $t1, $t0
sw $t1, _i5
li $t0, 1
move $t1, $t0
sw $t1, _i6
lw $t0, _i5
lw $t1, _i6
add $t2, $t0, $t1
sw $t2, _i7
li $t0, 9
move $t1, $t0
sw $t1, _i8
li $t0, 2
move $t1, $t0
sw $t1, _i9
lw $t0, _i8
lw $t1, _i9
mult $t0, $t1
mflo $t2
sw $t2, _i10
lw $t0, _i7
lw $t1, _i10
add $t2, $t0, $t1
sw $t2, _i11
li $t0, 19
move $t1, $t0
sw $t1, _i12
lw $t0, _i11
lw $t1, _i12
sub $t2, $t0, $t1
sw $t2, _i13
lw $t0,, _i13
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i14
li $t0, 1
move $t1, $t0
sw $t1, _i15
lw $t0,, _i15
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i16
lw $t0, _i14
lw $t1, _i16
mult $t0, $t1
mflo $t2
sw $t2, _i17
lw $t0, _i4
lw $t1, _i17
li $a3, 4
mult $a3, $t0
mflo $a3
sw $t1, buffer_int($a3)
li $t0, 0
move $t1, $t0
sw $t1, _i18
lw $t0,, _i18
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i19
lw $t0, _i19
move $t1, $t0
sw $t1, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4
li $t0, 1
move $t1, $t0
sw $t1, _i20
lw $t0,, _i20
li $a3, 4
mult $a3, $t0
mflo $a3
lw $t1, buffer_int($a3)
sw $t1, _i21
lw $t0, _i21
move $t1, $t0
sw $t1, __printi_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printi
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
