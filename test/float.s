.data

#User-created variables
A_float: .float 0.0
B_float: .float 0.0
C_float: .float 0.0
D_float: .float 0.0
E_float: .float 0.0

#Compiler-created variables.
_f0: .word 0
_f2: .word 0
__printf_arg0: .word 0
_f1: .word 0
_f4: .word 0
_f3: .word 0
__printi_arg0: .word 0


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

main:
lw $s7, D_float
lw $s0, _f0
lw $t3, B_float
lw $t4, E_float
lw $s1, _f2
lw $s2, __printf_arg0
lw $s3, _f1
lw $s4, _f4
lw $s5, _f3
lw $t5, C_float
lw $s6, A_float
li.s $f0, 1.5
mfc1 $t0 $f0
move $s0, $t0
move $s6, $s0
li.s $f0, 2.5
mfc1 $t0 $f0
move $s3, $t0
move $t3, $s3
li.s $f0, 3.5
mfc1 $t0 $f0
move $s1, $t0
move $t5, $s1
li.s $f0, 4.5
mfc1 $t0 $f0
move $s5, $t0
move $s7, $s5
li.s $f0, 5.5
mfc1 $t0 $f0
move $s4, $t0
move $t4, $s4
move $s2, $s6
sw $s7, D_float
sw $s0, _f0
sw $t3, B_float
sw $t4, E_float
sw $s1, _f2
sw $s2, __printf_arg0
sw $s3, _f1
sw $s4, _f4
sw $s5, _f3
sw $t5, C_float
sw $s6, A_float
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s1, B_float
lw $s0, __printf_arg0
move $s0, $s1
sw $s0, __printf_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s0, __printf_arg0
lw $s1, C_float
move $s0, $s1
sw $s0, __printf_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s1, D_float
lw $s0, __printf_arg0
move $s0, $s1
sw $s0, __printf_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4
lw $s1, E_float
lw $s0, __printf_arg0
move $s0, $s1
sw $s0, __printf_arg0
addi $sp, $sp, -4
sw $ra, 0($sp)
jal printf
lw $ra, 0($sp)
addi $sp, $sp, 4

li $v0, 10
syscall
