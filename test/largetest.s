.data

#User-created variables
y_int: .word 2
z_float: .word 0
loopCounter_int: .word 0
f1_float: .word 5
f2_float: .word 1.3
afloat_float: .word 0
buffer_int: .word 8:2

#Compiler-created variables.
_fillArray_arg0: .word 0
_fillArray_int1_int: .word 0
_fillArray_arg1: .word 0
_fillArray_int2_int: .word 0
_i5: .word 0
_i6: .word 0
_fillArrayNoRetVal_arg0: .word 0
_fillArrayNoRetVal_int3_int: .word 0
_fillArrayNoRetVal_arg1: .word 0
_fillArrayNoRetVal_int4_int: .word 0
_i7: .word 0
_i8: .word 0
_areIntsEqual_arg0: .word 0
_areIntsEqual_n_int: .word 0
_areIntsEqual_arg1: .word 0
_areIntsEqual_n2_int: .word 0
_i9: .word 0
_areFloatsEqual_arg0: .word 0
_areFloatsEqual_n_float: .word 0
_areFloatsEqual_arg1: .word 0
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
__fillArray_arg0: .word 0
_i22: .word 0
__fillArray_arg1: .word 0
_i23: .word 0
_i24: .word 0
__fillArrayNoRetVal_arg0: .word 0
_i25: .word 0
__fillArrayNoRetVal_arg1: .word 0


.text

fillArray:
move $f1, $f0
move $f1, $f0
move $t1, $t0
move $t1, $t0
move $t1, $t0

fillArrayNoRetVal:
move $f1, $f0
move $f1, $f0
move $t1, $t0
move $t1, $t0

areIntsEqual:
move $t1, $t0
move $t1, $t0
move $t1, $t0

_EQ_true0:
move $t1, $t0

_EQ_false0:

areFloatsEqual:
move $t1, $t0
move $t1, $t0
move $t1, $t0

_EQ_true1:
move $t1, $t0

_EQ_false1:

program_start:
move $f1, $f0
move $f1, $f0
move $t1, $t0

_EQ_true2:
move $t1, $t0

_EQ_false2:
move $t1, $t0
move $t1, $t0
move $t1, $t0

_FOR_start0:
move $t1, $t0
move $f1, $f0
move $f1, $f0
move $t1, $t0

_INEQ_true0:
move $t1, $t0

_INEQ_false0:

_ELSE_start0:

_IF_end0:

_FOR_end0:
move $t1, $t0
move $t1, $t0
move $t1, $t0
move $t1, $t0
move $t1, $t0
move $t1, $t0
move $t1, $t0
move $t1, $t0
move $t1, $t0
